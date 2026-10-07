package com.okto.notes.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Хранит все записи в одном JSON-файле во внутренней памяти приложения. */
class Store(context: Context) {
    private val file = File(context.filesDir, "entries.json")

    fun load(): List<Entry> {
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            List(arr.length()) { arr.getJSONObject(it).toEntry() }
        } catch (e: Exception) {
            // Не теряем данные молча: сохраняем копию повреждённого файла.
            file.copyTo(File(file.parentFile, "entries.corrupt.json"), overwrite = true)
            emptyList()
        }
    }

    fun save(entries: List<Entry>) {
        val arr = JSONArray()
        entries.forEach { arr.put(it.toJson()) }
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(arr.toString())
        if (!tmp.renameTo(file)) {
            file.delete()
            tmp.renameTo(file)
        }
    }

    private fun Entry.toJson() = JSONObject()
        .put("id", id)
        .put("type", type.name)
        .put("title", title)
        .put("body", body)
        .put("color", color)
        .put("pinned", pinned)
        .put("mood", mood)
        .put("day", day)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)

    private fun JSONObject.toEntry() = Entry(
        id = getLong("id"),
        type = EntryType.valueOf(optString("type", "NOTE")),
        title = optString("title"),
        body = optString("body"),
        color = optInt("color"),
        pinned = optBoolean("pinned"),
        mood = optInt("mood", -1),
        day = optLong("day"),
        createdAt = optLong("createdAt"),
        updatedAt = optLong("updatedAt"),
    )
}
