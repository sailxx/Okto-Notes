package com.okto.notes

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.okto.notes.data.Attachments
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
import java.time.LocalDate
import kotlin.random.Random

enum class Tab { NOTES, DIARY }

class OktoViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)
    val attachments = Attachments(app)
    private val writeLock = Mutex()
    private var saveJob: Job? = null

    var entries by mutableStateOf(store.load())
        private set
    var editing by mutableStateOf<Entry?>(null)
        private set
    var recentlyDeleted by mutableStateOf<Entry?>(null)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) { attachments.cleanup(entries) }
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
        editing = Entry(id = newId(), type = EntryType.NOTE)
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
        recentlyDeleted?.let(::commit)
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

    private fun persist() {
        saveJob?.cancel()
        val snapshot = entries
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(400)
            writeLock.withLock { store.save(snapshot) }
        }
    }

    /** Немедленная запись на диск — вызывается, когда приложение уходит в фон. */
    fun flush() {
        saveJob?.cancel()
        val snapshot = entries
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            writeLock.withLock { store.save(snapshot) }
        }
    }

    private fun newId() = System.currentTimeMillis() * 1000 + Random.nextInt(1000)
}
