package com.okto.notes.data

import android.content.Context

/** Какая тема выбрана в настройках. */
enum class ThemeKind { COLOR, OKTO, CUSTOM }

/** Раскладка интерфейса: «Цветная» (мягкие карточки) или «Okto» (табло и клавиши). */
enum class UiStyle { TONAL, OKTO }

data class ThemeSettings(
    val kind: ThemeKind = ThemeKind.COLOR,
    /** Для своей темы: раскладка, режим и акцентный цвет (оттенок + насыщенность). */
    val style: UiStyle = UiStyle.TONAL,
    val dark: Boolean = false,
    val hue: Float = 255f,
    val sat: Float = 0.7f,
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load() = ThemeSettings(
        kind = enumOr(prefs.getString("kind", null), ThemeKind.COLOR),
        style = enumOr(prefs.getString("style", null), UiStyle.TONAL),
        dark = prefs.getBoolean("dark", false),
        hue = prefs.getFloat("hue", 255f),
        sat = prefs.getFloat("sat", 0.7f),
    )

    fun save(s: ThemeSettings) {
        prefs.edit()
            .putString("kind", s.kind.name)
            .putString("style", s.style.name)
            .putBoolean("dark", s.dark)
            .putFloat("hue", s.hue)
            .putFloat("sat", s.sat)
            .apply()
    }

    private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default
}
