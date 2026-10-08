package com.okto.notes.data

import android.content.Context
import java.util.Locale

/** Какая тема выбрана в настройках. */
enum class ThemeKind { COLOR, OKTO, OKTO_LIGHT, CUSTOM }

/** Раскладка интерфейса: «Цветная» (мягкие карточки) или «Okto» (табло и клавиши). */
enum class UiStyle { TONAL, OKTO }

/** Язык интерфейса. По умолчанию — русский, если система на русском (или близком), иначе английский. */
enum class Lang {
    RU, EN;

    companion object {
        fun system(): Lang = if (Locale.getDefault().language in setOf("ru", "uk", "be", "kk")) RU else EN
    }
}

data class ThemeSettings(
    val kind: ThemeKind = ThemeKind.COLOR,
    /** Для своей темы: раскладка, режим и акцентный цвет (оттенок + насыщенность). */
    val style: UiStyle = UiStyle.TONAL,
    val dark: Boolean = false,
    val hue: Float = 255f,
    val sat: Float = 0.7f,
    val lang: Lang = Lang.system(),
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load() = ThemeSettings(
        kind = enumOr(prefs.getString("kind", null), ThemeKind.COLOR),
        style = enumOr(prefs.getString("style", null), UiStyle.TONAL),
        dark = prefs.getBoolean("dark", false),
        hue = prefs.getFloat("hue", 255f),
        sat = prefs.getFloat("sat", 0.7f),
        lang = enumOr(prefs.getString("lang", null), Lang.system()),
    )

    fun save(s: ThemeSettings) {
        prefs.edit()
            .putString("kind", s.kind.name)
            .putString("style", s.style.name)
            .putBoolean("dark", s.dark)
            .putFloat("hue", s.hue)
            .putFloat("sat", s.sat)
            .putString("lang", s.lang.name)
            .apply()
    }

    private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default
}
