package com.okto.notes.data

import android.content.Context
import java.util.Locale

/** Язык интерфейса. По умолчанию — русский, если система на русском (или близком), иначе английский. */
enum class Lang {
    RU, EN;

    companion object {
        fun system(): Lang = if (Locale.getDefault().language in setOf("ru", "uk", "be", "kk")) RU else EN
    }
}

/** Тема: следовать системной или зафиксировать светлую/тёмную. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val lang: Lang = Lang.system(),
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load() = ThemeSettings(
        mode = enumOr(prefs.getString("mode", null), ThemeMode.SYSTEM),
        lang = enumOr(prefs.getString("lang", null), Lang.system()),
    )

    fun save(s: ThemeSettings) {
        prefs.edit()
            .putString("mode", s.mode.name)
            .putString("lang", s.lang.name)
            .apply()
    }

    private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default
}
