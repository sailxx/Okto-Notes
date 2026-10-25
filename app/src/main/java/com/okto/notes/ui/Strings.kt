package com.okto.notes.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.okto.notes.data.Lang
import java.util.Locale

/** Все тексты интерфейса на одном языке. */
abstract class Strings {
    abstract val locale: Locale

    /** Шаблоны дат: «7 октября» / «October 7». */
    abstract val dayMonthPattern: String
    abstract val dayMonthShortPattern: String
    abstract val fullDatePattern: String
    abstract val ddMMPattern: String

    // Общее
    abstract val back: String
    abstract val settings: String
    abstract val notes: String
    abstract val diary: String
    abstract val favorites: String
    abstract val all: String
    abstract val clear: String
    abstract val newNote: String
    abstract val newEntry: String
    abstract val entryDeleted: String
    abstract val undo: String
    abstract val emptyMark: String
    abstract val today: String
    abstract val yesterday: String
    abstract val moods: List<String>
    abstract fun greeting(hour: Int): String

    // Заметки
    abstract val searchNotes: String
    abstract val searchNotesShort: String
    abstract val search: String
    abstract val close: String
    abstract val emptyNotesTitle: String
    abstract val emptyNotesText: String
    abstract val nothingFoundTitle: String
    abstract val nothingFoundText: String
    abstract val diaryThisWeek: String
    abstract fun ofSeven(n: Int): String

    // Дневник
    abstract fun daysInRow(n: Int): String
    abstract fun totalEntries(n: Int): String
    abstract val howAreYou: String
    abstract val emptyDiaryTitle: String
    abstract val emptyDiaryText: String
    abstract val entries: String
    abstract val noText: String
    abstract val streak: String
    abstract val daysInRowCaps: String
    abstract val moodToday: String
    abstract val moodWeek: String
    abstract fun average(v: String): String

    // Редактор
    abstract val diaryCaps: String
    abstract val noteCaps: String
    abstract val delete: String
    abstract val done: String
    abstract val mood: String
    abstract val tag: String
    abstract val dayTitle: String
    abstract val title: String
    abstract val diaryHint: String
    abstract val noteHint: String
    abstract fun saved(time: String): String
    abstract fun words(n: Int): String
    abstract fun wordsCaps(n: Int): String
    abstract val toolList: String
    abstract val toolTask: String
    abstract val toolTime: String
    abstract val toolPhoto: String
    abstract val toolFile: String
    abstract val attachFailed: String
    abstract val noAppToOpen: String

    // Настройки
    abstract val theme: String
    abstract val themeSystem: String
    abstract val mode: String
    abstract val light: String
    abstract val dark: String
    abstract val accent: String
    abstract fun hue(deg: Int): String
    abstract fun saturation(pct: Int): String
    abstract val language: String
    abstract val about: String
    abstract val syncTitle: String
    abstract val syncSub: String
    abstract val syncSignIn: String
    abstract val syncSignOut: String
    abstract val syncNow: String
    abstract val syncing: String
    abstract fun syncedAs(email: String): String
    abstract fun syncedAt(time: String): String
    abstract val syncFailed: String
    abstract val syncOffline: String
    abstract val syncSignedOut: String
    abstract val syncNote: String
    abstract fun aboutText(version: String): String
}

object RuStrings : Strings() {
    override val locale: Locale = Locale("ru")
    override val dayMonthPattern = "d MMMM"
    override val dayMonthShortPattern = "d MMM"
    override val fullDatePattern = "d MMM yyyy"
    override val ddMMPattern = "dd.MM"

    override val back = "Назад"
    override val settings = "Настройки"
    override val notes = "Заметки"
    override val diary = "Дневник"
    override val favorites = "Избранное"
    override val all = "Все"
    override val clear = "Очистить"
    override val newNote = "Новая"
    override val newEntry = "Запись"
    override val entryDeleted = "Запись удалена"
    override val undo = "Вернуть"
    override val emptyMark = "[ ПУСТО ]"
    override val today = "Сегодня"
    override val yesterday = "Вчера"
    override val moods = listOf("Плохо", "Так себе", "Норм", "Хорошо", "Отлично")
    override fun greeting(hour: Int) = when (hour) {
        in 5..11 -> "Доброе утро"
        in 12..16 -> "Добрый день"
        in 17..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }

    override val searchNotes = "Искать в заметках"
    override val searchNotesShort = "Поиск по заметкам"
    override val search = "Поиск"
    override val close = "Закрыть"
    override val emptyNotesTitle = "Пока пусто"
    override val emptyNotesText = "Нажми «Новая», чтобы записать первую мысль"
    override val nothingFoundTitle = "Ничего не нашлось"
    override val nothingFoundText = "Попробуй другой запрос или фильтр"
    override val diaryThisWeek = "Дневник на этой неделе"
    override fun ofSeven(n: Int) = "$n из 7"

    override fun daysInRow(n: Int) = ru(n, "день", "дня", "дней").replaceFirstChar { it.uppercase() } + " подряд"
    override fun totalEntries(n: Int) = "Всего $n " + ru(n, "запись", "записи", "записей")
    override val howAreYou = "Как ты сегодня?"
    override val emptyDiaryTitle = "Начни свой дневник"
    override val emptyDiaryText = "Отметь настроение или нажми «Запись», чтобы описать день"
    override val entries = "Записи"
    override val noText = "Без текста"
    override val streak = "Серия"
    override val daysInRowCaps = "ДНЕЙ ПОДРЯД"
    override val moodToday = "Настроение · сегодня"
    override val moodWeek = "Настроение · неделя"
    override fun average(v: String) = "среднее $v"

    override val diaryCaps = "ДНЕВНИК"
    override val noteCaps = "ЗАМЕТКА"
    override val delete = "Удалить"
    override val done = "Готово"
    override val mood = "Настроение"
    override val tag = "Метка"
    override val dayTitle = "Заголовок дня"
    override val title = "Заголовок"
    override val diaryHint = "Что сегодня произошло? О чём думаешь?"
    override val noteHint = "Начни писать…"
    override fun saved(time: String) = "Сохранено · $time"
    override fun words(n: Int) = "$n " + ru(n, "слово", "слова", "слов")
    override fun wordsCaps(n: Int) = "${n.toString().padStart(3, '0')} СЛОВ"
    override val toolList = "• Список"
    override val toolTask = "☐ Задача"
    override val toolTime = "Время"
    override val toolPhoto = "Фото"
    override val toolFile = "Файл"
    override val attachFailed = "Не удалось прикрепить файл"
    override val noAppToOpen = "Нет приложения для открытия файла"

    override val theme = "Тема"
    override val themeSystem = "Системная"
    override val mode = "Режим"
    override val light = "Светлая"
    override val dark = "Тёмная"
    override val accent = "Акцентный цвет"
    override fun hue(deg: Int) = "Оттенок · $deg°"
    override fun saturation(pct: Int) = "Насыщенность · $pct%"
    override val language = "Язык"
    override val about = "О приложении"
    override val syncTitle = "Синхронизация с Okto"
    override val syncSub = "Заметки появятся в Okto и обратно через ваш Google-аккаунт. Дневник остаётся только на этом устройстве."
    override val syncSignIn = "Войти через Google"
    override val syncSignOut = "Выйти"
    override val syncNow = "Синхронизировать"
    override val syncing = "Синхронизация…"
    override fun syncedAs(email: String) = if (email.isEmpty()) "Аккаунт Google подключён" else "Аккаунт: $email"
    override fun syncedAt(time: String) = "Синхронизировано в $time"
    override val syncFailed = "Не удалось синхронизировать. Попробуем позже"
    override val syncOffline = "Нет сети — заметки синхронизируются позже"
    override val syncSignedOut = "Вход устарел — войдите снова"
    override val syncNote = "Вложения в Okto не передаются: файлы остаются на устройстве, где их добавили."
    override fun aboutText(version: String) = "Okto Notes $version · заметки и дневник хранятся на этом устройстве; заметки синхронизируются с Okto, только если вы войдёте в аккаунт."

    /** Русское окончание по числу: 1 день, 2 дня, 5 дней. */
    private fun ru(n: Int, one: String, few: String, many: String): String {
        val m10 = n % 10
        val m100 = n % 100
        return when {
            m10 == 1 && m100 != 11 -> one
            m10 in 2..4 && m100 !in 12..14 -> few
            else -> many
        }
    }
}

object EnStrings : Strings() {
    override val locale: Locale = Locale.ENGLISH
    override val dayMonthPattern = "MMMM d"
    override val dayMonthShortPattern = "MMM d"
    override val fullDatePattern = "MMM d, yyyy"
    override val ddMMPattern = "MM/dd"

    override val back = "Back"
    override val settings = "Settings"
    override val notes = "Notes"
    override val diary = "Diary"
    override val favorites = "Favorites"
    override val all = "All"
    override val clear = "Clear"
    override val newNote = "New"
    override val newEntry = "Entry"
    override val entryDeleted = "Entry deleted"
    override val undo = "Undo"
    override val emptyMark = "[ EMPTY ]"
    override val today = "Today"
    override val yesterday = "Yesterday"
    override val moods = listOf("Bad", "Meh", "Okay", "Good", "Great")
    override fun greeting(hour: Int) = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..22 -> "Good evening"
        else -> "Good night"
    }

    override val searchNotes = "Search notes"
    override val searchNotesShort = "Search notes"
    override val search = "Search"
    override val close = "Close"
    override val emptyNotesTitle = "Nothing here yet"
    override val emptyNotesText = "Tap “New” to write down your first thought"
    override val nothingFoundTitle = "Nothing found"
    override val nothingFoundText = "Try a different search or filter"
    override val diaryThisWeek = "Diary this week"
    override fun ofSeven(n: Int) = "$n of 7"

    override fun daysInRow(n: Int) = (if (n == 1) "Day" else "Days") + " in a row"
    override fun totalEntries(n: Int) = "$n " + (if (n == 1) "entry" else "entries") + " total"
    override val howAreYou = "How are you today?"
    override val emptyDiaryTitle = "Start your diary"
    override val emptyDiaryText = "Pick a mood or tap “Entry” to describe your day"
    override val entries = "Entries"
    override val noText = "No text"
    override val streak = "Streak"
    override val daysInRowCaps = "DAYS IN A ROW"
    override val moodToday = "Mood · today"
    override val moodWeek = "Mood · week"
    override fun average(v: String) = "average $v"

    override val diaryCaps = "DIARY"
    override val noteCaps = "NOTE"
    override val delete = "Delete"
    override val done = "Done"
    override val mood = "Mood"
    override val tag = "Label"
    override val dayTitle = "Title of the day"
    override val title = "Title"
    override val diaryHint = "What happened today? What's on your mind?"
    override val noteHint = "Start writing…"
    override fun saved(time: String) = "Saved · $time"
    override fun words(n: Int) = "$n " + if (n == 1) "word" else "words"
    override fun wordsCaps(n: Int) = "${n.toString().padStart(3, '0')} WORDS"
    override val toolList = "• List"
    override val toolTask = "☐ Task"
    override val toolTime = "Time"
    override val toolPhoto = "Photo"
    override val toolFile = "File"
    override val attachFailed = "Couldn't attach the file"
    override val noAppToOpen = "No app to open this file"

    override val theme = "Theme"
    override val themeSystem = "System"
    override val mode = "Mode"
    override val light = "Light"
    override val dark = "Dark"
    override val accent = "Accent color"
    override fun hue(deg: Int) = "Hue · $deg°"
    override fun saturation(pct: Int) = "Saturation · $pct%"
    override val language = "Language"
    override val about = "About"
    override val syncTitle = "Sync with Okto"
    override val syncSub = "Notes appear in Okto and back through your Google account. The diary stays on this device only."
    override val syncSignIn = "Sign in with Google"
    override val syncSignOut = "Sign out"
    override val syncNow = "Sync now"
    override val syncing = "Syncing…"
    override fun syncedAs(email: String) = if (email.isEmpty()) "Google account connected" else "Account: $email"
    override fun syncedAt(time: String) = "Synced at $time"
    override val syncFailed = "Couldn't sync. Will try again later"
    override val syncOffline = "No network — notes will sync later"
    override val syncSignedOut = "Sign-in expired — please sign in again"
    override val syncNote = "Attachments are not sent to Okto: files stay on the device where they were added."
    override fun aboutText(version: String) = "Okto Notes $version · notes and diary are stored on this device; notes sync with Okto only if you sign in."
}

fun stringsFor(lang: Lang): Strings = when (lang) {
    Lang.RU -> RuStrings
    Lang.EN -> EnStrings
}

val LocalStrings = staticCompositionLocalOf<Strings> { RuStrings }

val S: Strings
    @Composable @ReadOnlyComposable get() = LocalStrings.current
