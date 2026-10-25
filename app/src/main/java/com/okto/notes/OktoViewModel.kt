package com.okto.notes

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.okto.notes.data.Attachments
import com.okto.notes.data.CloudApi
import com.okto.notes.data.CloudConfig
import com.okto.notes.data.CloudException
import com.okto.notes.data.CloudState
import com.okto.notes.data.Change
import com.okto.notes.data.NoteSync
import com.okto.notes.data.requestGoogleIdToken
import com.okto.notes.data.Entry
import com.okto.notes.data.EntryType
import com.okto.notes.data.SettingsStore
import com.okto.notes.data.Store
import com.okto.notes.data.ThemeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.okto.notes.data.Attachment
import com.okto.notes.ui.stringsFor
import java.io.IOException
import java.time.LocalDate
import kotlin.random.Random

enum class Tab { NOTES, DIARY }

enum class SyncStatus { OFF, IDLE, SYNCING, ERROR, OFFLINE, EXPIRED }

class OktoViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)
    val attachments = Attachments(app)
    private val writeLock = Mutex()
    private var saveJob: Job? = null

    var entries by mutableStateOf(emptyList<Entry>())
        private set

    /** Пока записи не прочитаны с диска, писать нельзя: пустой список затёр бы файл. */
    var loaded by mutableStateOf(false)
        private set
    var editing by mutableStateOf<Entry?>(null)
        private set
    var recentlyDeleted by mutableStateOf<Entry?>(null)
        private set

    private val cloud = CloudState(app)
    private val api = CloudApi(cloud)
    private val syncLock = Mutex()
    private var syncJob: Job? = null

    /** Синхронизацию с Okto можно включить, только если в сборке заданы настройки облака. */
    val cloudAvailable = CloudConfig.available
    var syncStatus by mutableStateOf(if (cloud.signedIn) SyncStatus.IDLE else SyncStatus.OFF)
        private set
    var syncAccount by mutableStateOf(cloud.email)
        private set
    var lastSyncAt by mutableStateOf(cloud.lastSync)
        private set

    init {
        viewModelScope.launch {
            val disk = withContext(Dispatchers.IO) { store.load() }
            val early = entries // правки, успевшие появиться до конца чтения, важнее диска
            val ids = early.mapTo(HashSet()) { it.id }
            entries = disk.filterNot { it.id in ids } + early
            loaded = true
            if (early.isNotEmpty()) persist(sync = false)
            val snapshot = entries
            launch(Dispatchers.IO) { attachments.cleanup(snapshot) }
            if (cloud.signedIn) requestSync(500)
            while (true) {
                delay(120_000)
                if (cloud.signedIn) runSync()
            }
        }
    }

    var tab by mutableStateOf(Tab.NOTES)
    var query by mutableStateOf("")
    var pinnedOnly by mutableStateOf(false)
    var searchOpen by mutableStateOf(false)
    var showSettings by mutableStateOf(false)

    private val settingsStore = SettingsStore(app)
    var settings by mutableStateOf(settingsStore.load())
        private set

    fun updateSettings(transform: (ThemeSettings) -> ThemeSettings) {
        settings = transform(settings)
        settingsStore.save(settings)
    }

    val notes: List<Entry>
        get() = entries.filter { it.type == EntryType.NOTE }
            .sortedWith(compareByDescending<Entry> { it.pinned }.thenByDescending { it.updatedAt })

    val diary: List<Entry>
        get() = entries.filter { it.type == EntryType.DIARY }
            .sortedWith(compareByDescending<Entry> { it.day }.thenByDescending { it.createdAt })

    /** Количество дней подряд с записями (сегодня ещё можно успеть написать). */
    val streak: Int
        get() {
            val days = diary.map { it.day }.toSet()
            var d = LocalDate.now().toEpochDay()
            if (d !in days) d--
            var n = 0
            while (d in days) {
                n++
                d--
            }
            return n
        }

    fun newNote() {
        editing = Entry(id = newId(), type = EntryType.NOTE, cloudId = NoteSync.newCloudId())
    }

    fun newDiary(day: LocalDate = LocalDate.now(), mood: Int = -1) {
        val e = Entry(id = newId(), type = EntryType.DIARY, day = day.toEpochDay(), mood = mood)
        editing = e
        commit(e)
    }

    fun openDay(day: LocalDate) {
        diary.firstOrNull { it.day == day.toEpochDay() }?.let(::open) ?: newDiary(day)
    }

    fun quickMood(mood: Int) {
        val today = diary.firstOrNull { it.day == LocalDate.now().toEpochDay() }
        if (today == null) {
            newDiary(mood = mood)
        } else {
            val updated = today.copy(mood = mood, updatedAt = System.currentTimeMillis())
            editing = updated
            commit(updated)
        }
    }

    fun open(e: Entry) {
        editing = e
    }

    fun closeEditor() {
        editing = null
    }

    fun edit(transform: (Entry) -> Entry) {
        val cur = editing ?: return
        val updated = transform(cur).copy(updatedAt = System.currentTimeMillis())
        editing = updated
        commit(updated)
    }

    /** Копирует выбранные файлы в хранилище и прикрепляет их к открытой записи. */
    fun attach(uris: List<Uri>) {
        val id = editing?.id ?: return
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) { uris.mapNotNull { attachments.import(it) } }
            if (imported.size < uris.size) Toast.makeText(getApplication(), stringsFor(settings.lang).attachFailed, Toast.LENGTH_SHORT).show()
            if (imported.isNotEmpty() && editing?.id == id) edit { it.copy(attachments = it.attachments + imported) }
        }
    }

    fun removeAttachment(a: Attachment) = edit { e -> e.copy(attachments = e.attachments - a) }

    fun togglePin(e: Entry) = commit(e.copy(pinned = !e.pinned))

    fun delete(e: Entry) {
        val existed = entries.any { it.id == e.id }
        entries = entries.filterNot { it.id == e.id }
        if (editing?.id == e.id) editing = null
        if (existed && !e.isBlank) recentlyDeleted = e
        persist()
    }

    fun undoDelete() {
        recentlyDeleted?.let { commit(it.copy(updatedAt = System.currentTimeMillis())) }
        recentlyDeleted = null
    }

    fun clearUndo() {
        recentlyDeleted = null
    }

    private fun commit(e: Entry) {
        entries = when {
            e.isBlank -> entries.filterNot { it.id == e.id }
            entries.any { it.id == e.id } -> entries.map { if (it.id == e.id) e else it }
            else -> entries + e
        }
        persist()
    }

    private fun persist(sync: Boolean = true) {
        if (!loaded) return
        saveJob?.cancel()
        val snapshot = entries
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(400)
            writeLock.withLock { store.save(snapshot) }
        }
        if (sync && cloud.signedIn) requestSync(5000)
    }

    /** Немедленная запись на диск — вызывается, когда приложение уходит в фон. */
    fun flush() {
        if (!loaded) return
        saveJob?.cancel()
        val snapshot = entries
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            writeLock.withLock { store.save(snapshot) }
        }
        if (cloud.signedIn) requestSync(0)
    }

    // ---------- Синхронизация с Okto ----------

    fun requestSync(delayMs: Long = 0) {
        if (!cloud.signedIn) return
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            delay(delayMs)
            runSync()
        }
    }

    fun signIn(activity: Activity) {
        if (!cloudAvailable || syncStatus == SyncStatus.SYNCING) return
        viewModelScope.launch {
            syncStatus = SyncStatus.SYNCING
            try {
                val token = requestGoogleIdToken(activity)
                if (token == null) {
                    syncStatus = SyncStatus.OFF
                    return@launch
                }
                withContext(Dispatchers.IO) { api.signIn(token) }
                syncAccount = cloud.email
                runSync()
            } catch (e: Exception) {
                syncStatus = if (e is IOException) SyncStatus.OFFLINE else SyncStatus.ERROR
                Toast.makeText(getApplication(), stringsFor(settings.lang).syncFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signOut() {
        syncJob?.cancel()
        cloud.clear()
        syncAccount = ""
        lastSyncAt = 0
        syncStatus = SyncStatus.OFF
    }

    private suspend fun runSync() = syncLock.withLock {
        if (!cloud.signedIn) return@withLock
        syncStatus = SyncStatus.SYNCING
        try {
            val started = System.currentTimeMillis()
            issuedIds.clear()
            val remote = withContext(Dispatchers.IO) { api.listNotes() }
            val res = NoteSync.merge(entries, remote, cloud.known, cloud.lastSync, started, ::freeId)
            applyChanges(res.changes)
            withContext(Dispatchers.IO) {
                res.uploads.forEach(api::upload)
                res.remoteDeletes.forEach { api.markDeleted(it, started) }
            }
            cloud.known = entries.filter { it.type == EntryType.NOTE && it.cloudId.isNotEmpty() }.map { it.cloudId }.toSet()
            cloud.lastSync = started
            withContext(Dispatchers.IO) { cloud.save() }
            lastSyncAt = started
            syncStatus = SyncStatus.IDLE
        } catch (e: CloudException) {
            syncStatus = if (e.signedOut) SyncStatus.EXPIRED else SyncStatus.ERROR
        } catch (e: IOException) {
            syncStatus = SyncStatus.OFFLINE
        } catch (e: Exception) {
            syncStatus = SyncStatus.ERROR
        }
    }

    /** Применяет результат слияния к тому, что сейчас на экране: заметки, изменённые за время синхронизации, не трогаем. */
    private fun applyChanges(changes: List<Change>) {
        var list = entries
        for (c in changes) {
            val old = c.old
            val new = c.new
            list = when {
                old == null -> list + new!!
                list.none { it == old } -> list
                editing?.id == old.id -> {
                    // Открытую запись не подменяем, кроме присвоения облачного id — иначе правка создаст дубликат.
                    if (new != null && new.updatedAt == old.updatedAt) {
                        editing = editing?.copy(cloudId = new.cloudId)
                        list.map { if (it.id == old.id) new else it }
                    } else list
                }
                new == null -> list.filterNot { it.id == old.id }
                else -> list.map { if (it.id == old.id) new else it }
            }
        }
        if (list !== entries) {
            entries = list
            persist(sync = false)
        }
    }

    private val issuedIds = mutableSetOf<Long>()

    private fun freeId(): Long {
        var id = newId()
        while (id in issuedIds || entries.any { it.id == id }) id++
        issuedIds += id
        return id
    }

    private fun newId() = System.currentTimeMillis() * 1000 + Random.nextInt(1000)
}
