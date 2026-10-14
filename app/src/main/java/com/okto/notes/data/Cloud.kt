package com.okto.notes.data

import android.content.Context
import com.okto.notes.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.random.Random

/**
 * Связь с Okto через облако: заметки лежат в Firestore того же Firebase-проекта, что и у Okto
 * (users/{uid}/notes), вход — тот же Google-аккаунт. Дневник в облако не уходит.
 * Работает по REST, без Firebase SDK: приложение остаётся лёгким, а сеть нужна только при включённой синхронизации.
 */
object CloudConfig {
    val apiKey: String = BuildConfig.FIREBASE_API_KEY
    val projectId: String = BuildConfig.FIREBASE_PROJECT_ID
    val webClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val available: Boolean get() = apiKey.isNotBlank() && projectId.isNotBlank() && webClientId.isNotBlank()
}

class CloudException(message: String, val signedOut: Boolean = false, val code: Int = 0) : Exception(message)

/** Запись заметки в облаке в том виде, в каком её хранит Okto. */
data class RemoteNote(
    val id: String,
    val title: String,
    val body: String,
    val color: String?,
    val pinned: Boolean,
    /** Вложения Okto как JSON-массив значений Firestore; Okto Notes их не показывает, но возвращает нетронутыми. */
    val files: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean,
)

/** Что синхронизация меняет у себя на устройстве: old == null — добавить, new == null — убрать, иначе заменить. */
data class Change(val old: Entry?, val new: Entry?)

class MergeResult(val changes: List<Change>, val uploads: List<Entry>, val remoteDeletes: List<String>)

/** Цвета меток Okto Notes (1–6) и их соответствие палитре Okto. */
object NoteColor {
    private val toHex = listOf("", "#30a46c", "#e93d82", "#8e4ec6", "#f76b15", "#ffb224", "#0090ff")
    private val fromHex = mapOf(
        "#30a46c" to 1, "#12a594" to 1, "#e93d82" to 2, "#e5484d" to 2,
        "#8e4ec6" to 3, "#f76b15" to 4, "#ffb224" to 5, "#0090ff" to 6,
    )

    fun index(hex: String?): Int = fromHex[hex?.lowercase()] ?: 0

    /** Цвет для облака: если заметка пришла из Okto с цветом, который в Okto Notes выглядит так же, отдаём его как был. */
    fun toCloud(e: Entry): String? =
        if (e.cloudColor.isNotEmpty() && index(e.cloudColor) == e.color) e.cloudColor else toHex[e.color.coerceIn(0, 6)].ifEmpty { null }
}

object NoteSync {
    /** Идентификатор в стиле Okto: короткая строка из букв и цифр. */
    fun newCloudId(): String = buildString {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        repeat(11) { append(chars[Random.nextInt(chars.length)]) }
    }

    /**
     * Сливает локальные заметки с облачными. Побеждает более свежая правка; удаление работает в обе стороны.
     * [known] — облачные id заметок, которые были здесь при прошлой синхронизации: если такой заметки больше нет, её удалили на устройстве.
     */
    fun merge(
        local: List<Entry>,
        remote: List<RemoteNote>,
        known: Set<String>,
        lastSync: Long,
        now: Long,
        newLocalId: () -> Long,
    ): MergeResult {
        val remoteById = remote.associateBy { it.id }
        val notes = local.filter { it.type == EntryType.NOTE }
        val localIds = notes.mapNotNull { it.cloudId.ifEmpty { null } }.toSet()
        val changes = mutableListOf<Change>()
        val uploads = mutableListOf<Entry>()
        val remoteDeletes = mutableListOf<String>()

        for (e in notes) {
            val cid = e.cloudId.ifEmpty { newCloudId() }
            val e1 = if (cid == e.cloudId) e else e.copy(cloudId = cid)
            val r = remoteById[cid]
            val next: Entry? = when {
                r == null -> { uploads += e1; e1 }
                r.deleted -> if (e1.updatedAt > r.updatedAt) { uploads += e1; e1 } else null
                r.updatedAt > e1.updatedAt -> fromRemote(r, e1.id)
                r.updatedAt < e1.updatedAt -> { uploads += e1; e1 }
                else -> e1
            }
            if (next != e) changes += Change(e, next)
        }

        for (r in remote) {
            if (r.deleted || r.id in localIds) continue
            if (r.id in known && r.updatedAt <= lastSync) {
                // Заметку удалили на этом устройстве, а в облаке её с тех пор не трогали.
                remoteDeletes += r.id
            } else {
                // Новая заметка из облака или правка из Okto, которая новее удаления здесь.
                changes += Change(null, fromRemote(r, newLocalId()))
            }
        }
        return MergeResult(changes, uploads, remoteDeletes)
    }

    private fun fromRemote(r: RemoteNote, localId: Long) = Entry(
        id = localId, type = EntryType.NOTE, title = r.title, body = r.body,
        color = NoteColor.index(r.color), pinned = r.pinned,
        createdAt = r.createdAt, updatedAt = r.updatedAt,
        cloudId = r.id, cloudColor = r.color.orEmpty(), cloudFiles = r.files,
    )
}

/** Вход и снимок состояния синхронизации; лежит в filesDir/cloud.json рядом с заметками. */
class CloudState(context: Context) {
    private val file = File(context.filesDir, "cloud.json")

    var uid = ""
    var email = ""
    var name = ""
    var refreshToken = ""
    var lastSync = 0L
    var known: Set<String> = emptySet()

    val signedIn: Boolean get() = refreshToken.isNotEmpty() && uid.isNotEmpty()

    init {
        runCatching {
            if (file.exists()) {
                val j = JSONObject(file.readText())
                uid = j.optString("uid"); email = j.optString("email"); name = j.optString("name")
                refreshToken = j.optString("refreshToken"); lastSync = j.optLong("lastSync")
                known = j.optJSONArray("known")?.let { a -> List(a.length()) { a.getString(it) }.toSet() } ?: emptySet()
            }
        }
    }

    fun save() {
        val j = JSONObject().put("uid", uid).put("email", email).put("name", name)
            .put("refreshToken", refreshToken).put("lastSync", lastSync).put("known", JSONArray(known.toList()))
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(j.toString())
        if (!tmp.renameTo(file)) { file.delete(); tmp.renameTo(file) }
    }

    fun clear() {
        uid = ""; email = ""; name = ""; refreshToken = ""; lastSync = 0; known = emptySet()
        file.delete()
    }
}

/** Тонкий REST-клиент Firebase Auth и Firestore. Все вызовы блокирующие — запускать вне главного потока. */
class CloudApi(private val state: CloudState) {
    private var idToken = ""

    /** Обмен Google ID-токена на вход в Firebase; тот же аккаунт даёт тот же uid, что и в Okto. */
    fun signIn(googleIdToken: String) {
        val body = JSONObject()
            .put("postBody", "id_token=${enc(googleIdToken)}&providerId=google.com")
            .put("requestUri", "http://localhost")
            .put("returnIdpCredential", true)
            .put("returnSecureToken", true)
        val r = request("POST", "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=${CloudConfig.apiKey}", body.toString(), json = true)
        idToken = r.getString("idToken")
        state.uid = r.getString("localId")
        state.refreshToken = r.getString("refreshToken")
        state.email = r.optString("email")
        state.name = r.optString("displayName")
        state.save()
    }

    private fun refresh() {
        val form = "grant_type=refresh_token&refresh_token=${enc(state.refreshToken)}"
        val r = request("POST", "https://securetoken.googleapis.com/v1/token?key=${CloudConfig.apiKey}", form, json = false)
        idToken = r.getString("id_token")
        r.optString("refresh_token").takeIf { it.isNotEmpty() && it != state.refreshToken }?.let { state.refreshToken = it; state.save() }
    }

    fun listNotes(): List<RemoteNote> {
        refresh()
        val out = mutableListOf<RemoteNote>()
        var token = ""
        do {
            val url = "$docs/users/${state.uid}/notes?pageSize=300" + if (token.isNotEmpty()) "&pageToken=${enc(token)}" else ""
            val r = request("GET", url, null, bearer = idToken)
            r.optJSONArray("documents")?.let { a -> for (i in 0 until a.length()) parse(a.getJSONObject(i))?.let(out::add) }
            token = r.optString("nextPageToken")
        } while (token.isNotEmpty())
        return out
    }

    fun upload(e: Entry) {
        val files = if (e.cloudFiles.isNotEmpty()) JSONArray(e.cloudFiles) else JSONArray()
        val fields = JSONObject()
            .put("id", str(e.cloudId))
            .put("title", str(e.title.take(200)))
            .put("body", str(e.body.take(20000)))
            .put("color", NoteColor.toCloud(e)?.let(::str) ?: JSONObject().put("nullValue", JSONObject.NULL))
            .put("pinned", JSONObject().put("booleanValue", e.pinned))
            .put("attachments", JSONObject().put("arrayValue", JSONObject().put("values", files)))
            .put("createdAt", num(e.createdAt))
            .put("updatedAt", num(e.updatedAt))
            .put("deleted", JSONObject().put("booleanValue", false))
        request("PATCH", "$docs/users/${state.uid}/notes/${enc(e.cloudId)}", JSONObject().put("fields", fields).toString(), bearer = idToken, json = true)
    }

    /** Помечает заметку удалённой, не трогая остальное: так Okto увидит удаление, а текст останется. */
    fun markDeleted(id: String, now: Long) {
        val fields = JSONObject().put("deleted", JSONObject().put("booleanValue", true)).put("updatedAt", num(now))
        val url = "$docs/users/${state.uid}/notes/${enc(id)}?updateMask.fieldPaths=deleted&updateMask.fieldPaths=updatedAt&currentDocument.exists=true"
        try {
            request("PATCH", url, JSONObject().put("fields", fields).toString(), bearer = idToken, json = true)
        } catch (e: CloudException) {
            if (e.code != 404 && e.code != 400) throw e // документ уже исчез — удалять нечего
        }
    }

    private val docs get() = "https://firestore.googleapis.com/v1/projects/${CloudConfig.projectId}/databases/(default)/documents"

    private fun parse(doc: JSONObject): RemoteNote? {
        val f = doc.optJSONObject("fields") ?: return null
        val id = doc.optString("name").substringAfterLast('/').ifEmpty { return null }
        fun s(k: String) = f.optJSONObject(k)?.optString("stringValue").orEmpty()
        fun n(k: String) = f.optJSONObject(k)?.let { it.optString("integerValue").toLongOrNull() ?: it.optDouble("doubleValue", 0.0).toLong() } ?: 0L
        fun b(k: String) = f.optJSONObject(k)?.optBoolean("booleanValue") ?: false
        val files = f.optJSONObject("attachments")?.optJSONObject("arrayValue")?.optJSONArray("values")?.takeIf { it.length() > 0 }?.toString().orEmpty()
        val color = f.optJSONObject("color")?.optString("stringValue")?.takeIf { it.isNotEmpty() }
        return RemoteNote(id, s("title"), s("body"), color, b("pinned"), files, n("createdAt"), n("updatedAt"), b("deleted"))
    }

    private fun str(v: String) = JSONObject().put("stringValue", v)
    private fun num(v: Long) = JSONObject().put("integerValue", v.toString())
    private fun enc(v: String) = URLEncoder.encode(v, "UTF-8")

    private fun request(method: String, url: String, body: String?, bearer: String? = null, json: Boolean = true): JSONObject {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = if (method == "PATCH") "POST" else method
            if (method == "PATCH") c.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            c.connectTimeout = 15000
            c.readTimeout = 30000
            if (bearer != null) c.setRequestProperty("Authorization", "Bearer $bearer")
            if (body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", if (json) "application/json" else "application/x-www-form-urlencoded")
                c.outputStream.use { it.write(body.toByteArray()) }
            }
            val code = c.responseCode
            val text = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val msg = runCatching { JSONObject(text).getJSONObject("error").getString("message") }.getOrDefault(text.take(200))
                val dead = msg.contains("INVALID_REFRESH_TOKEN") || msg.contains("TOKEN_EXPIRED") || msg.contains("USER_DISABLED") || msg.contains("USER_NOT_FOUND")
                throw CloudException(msg, dead, code)
            }
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            c.disconnect()
        }
    }

}
