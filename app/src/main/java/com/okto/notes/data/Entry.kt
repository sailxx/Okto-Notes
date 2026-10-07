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
) {
    val isBlank: Boolean get() = title.isBlank() && body.isBlank() && mood < 0
    val date: LocalDate get() = LocalDate.ofEpochDay(day)
}

val Moods = listOf("😞" to "Плохо", "😕" to "Так себе", "😐" to "Норм", "🙂" to "Хорошо", "🤩" to "Отлично")
