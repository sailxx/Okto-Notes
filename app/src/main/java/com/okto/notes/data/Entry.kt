package com.okto.notes.data

import java.time.LocalDate

enum class EntryType { NOTE, DIARY }

data class Entry(
    val id: Long,
    val type: EntryType,
    val title: String = "",
    val body: String = "",
    val color: Int = 0,
    val pinned: Boolean = false,
    val mood: Int = -1,
    val day: Long = LocalDate.now().toEpochDay(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val attachments: List<Attachment> = emptyList(),
) {
    val isBlank: Boolean get() = title.isBlank() && body.isBlank() && mood < 0 && attachments.isEmpty()
    val date: LocalDate get() = LocalDate.ofEpochDay(day)
}

/** Эмодзи настроений; подписи — в строках интерфейса (Strings.moods). */
val Moods = listOf("😞", "😕", "😐", "🙂", "🤩")

/** Вложение: копия файла во внутренней памяти (filesDir/attachments/[file]). */
data class Attachment(
    val file: String,
    val name: String,
    val mime: String,
    val size: Long,
) {
    val isImage: Boolean get() = mime.startsWith("image/")
}
