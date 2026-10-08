@file:OptIn(ExperimentalTextApi::class)

package com.okto.notes.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.okto.notes.R
import com.okto.notes.data.ThemeKind
import com.okto.notes.data.ThemeSettings
import com.okto.notes.data.UiStyle
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------- Шрифты (вариативные TTF из Google Fonts, с кириллицей) ----------

private fun variable(res: Int, vararg weights: Int) = FontFamily(
    weights.map { Font(res, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) },
)

val Nunito = variable(R.font.nunito, 400, 500, 600, 700, 800, 900)
val Golos = variable(R.font.golos, 400, 500, 600, 700, 800)
val JbMono = variable(R.font.jbmono, 400, 500, 600, 700, 800)

// ---------- Палитра ----------

@Immutable
data class Palette(
    val dark: Boolean,
    val bg: Color,
    val onBg: Color,
    val muted: Color,
    val surface: Color,
    val surfaceHi: Color,
    val outline: Color,
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color,
    val accent2: Color,
    val onAccent2: Color,
    // «Колодец» — утопленная панель с крупными цифрами (в Okto тёмная, в цветной теме — container).
    val well: Color,
    val wellInk: Color,
    val wellDim: Color,
    val wellGhost: Color,
    val wellEdge: Color,
    // «Клавиша» — объёмная кнопка Okto с тенью снизу.
    val key: Color,
    val keyInk: Color,
    val keyEdge: Color,
    val keyHi: Color,
    val danger: Color,
)

fun hsl(h: Float, s: Float, l: Float, a: Float = 1f): Color {
    val argb = ColorUtils.HSLToColor(floatArrayOf(((h % 360f) + 360f) % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f)))
    return Color(argb).copy(alpha = a)
}

/** «Цветная» — основная тема (макет «Мягкий тональный»). */
val ColorPalette = Palette(
    dark = false,
    bg = Color(0xFFF7F2FA), onBg = Color(0xFF1D1B20), muted = Color(0xFF49454F),
    surface = Color.White, surfaceHi = Color(0xFFECE6F0), outline = Color(0xFF79747E),
    primary = Color(0xFF4A2FC0), onPrimary = Color.White,
    container = Color(0xFFE8DEFF), onContainer = Color(0xFF21005D),
    accent2 = Color(0xFFFFD8E4), onAccent2 = Color(0xFF31111D),
    well = Color(0xFFE8DEFF), wellInk = Color(0xFF21005D), wellDim = Color(0xFF4F4566),
    wellGhost = Color(0x1421005D), wellEdge = Color.Transparent,
    key = Color(0xFFECE6F0), keyInk = Color(0xFF1D1B20), keyEdge = Color(0xFFCAC4D0), keyHi = Color.Transparent,
    danger = Color(0xFFB3261E),
)

/** «Okto» — графитовая тема по мотивам sailxx.github.io/Okto. */
val OktoPalette = Palette(
    dark = true,
    bg = Color(0xFF141414), onBg = Color(0xFFEDEDED), muted = Color(0xFF8E8E8E),
    surface = Color(0xFF1D1D1D), surfaceHi = Color(0xFF262626), outline = Color(0xFF2A2A2A),
    primary = Color(0xFFEDEDED), onPrimary = Color(0xFF141414),
    container = Color(0xFF262626), onContainer = Color(0xFFEDEDED),
    accent2 = Color(0xFF262626), onAccent2 = Color(0xFFEDEDED),
    well = Color(0xFF0A0A0A), wellInk = Color(0xFFF2F2F2), wellDim = Color(0xFF8A8A8A),
    wellGhost = Color(0x0FFFFFFF), wellEdge = Color(0xFF1F1F1F),
    key = Color(0xFF262626), keyInk = Color(0xFFEDEDED), keyEdge = Color(0xFF070707), keyHi = Color(0x12FFFFFF),
    danger = Color(0xFFEF5A5F),
)

/** «Okto Light» — белая тема в раскладке Okto. */
val OktoLightPalette = Palette(
    dark = false,
    bg = Color(0xFFF6F6F6), onBg = Color(0xFF141414), muted = Color(0xFF6B6B6B),
    surface = Color.White, surfaceHi = Color(0xFFEBEBEB), outline = Color(0xFFDADADA),
    primary = Color(0xFF141414), onPrimary = Color(0xFFF6F6F6),
    container = Color(0xFFEBEBEB), onContainer = Color(0xFF141414),
    accent2 = Color(0xFFEBEBEB), onAccent2 = Color(0xFF141414),
    well = Color(0xFFEAEAEA), wellInk = Color(0xFF141414), wellDim = Color(0xFF7A7A7A),
    wellGhost = Color(0x0F000000), wellEdge = Color(0xFFD6D6D6),
    key = Color.White, keyInk = Color(0xFF141414), keyEdge = Color(0xFFC4C4C4), keyHi = Color(0xCCFFFFFF),
    danger = Color(0xFFD33A3F),
)

/** Своя тема в цветной раскладке: тональная палитра из одного оттенка. */
fun tonalPalette(h: Float, s: Float, dark: Boolean): Palette {
    val ps = s.coerceIn(0f, 1f)
    return if (!dark) Palette(
        dark = false,
        bg = hsl(h, 0.45f * ps, 0.97f), onBg = hsl(h, 0.12f * ps, 0.12f), muted = hsl(h, 0.08f * ps, 0.30f),
        surface = Color.White, surfaceHi = hsl(h, 0.25f * ps, 0.92f), outline = hsl(h, 0.06f * ps, 0.48f),
        primary = hsl(h, maxOf(0.45f, 0.8f * ps), 0.40f), onPrimary = Color.White,
        container = hsl(h, 0.85f * ps, 0.91f), onContainer = hsl(h, 0.65f * ps, 0.18f),
        accent2 = hsl(h + 60f, 0.85f * ps, 0.90f), onAccent2 = hsl(h + 60f, 0.6f * ps, 0.16f),
        well = hsl(h, 0.85f * ps, 0.91f), wellInk = hsl(h, 0.65f * ps, 0.18f), wellDim = hsl(h, 0.15f * ps, 0.36f),
        wellGhost = hsl(h, 0.65f * ps, 0.18f, 0.08f), wellEdge = Color.Transparent,
        key = hsl(h, 0.25f * ps, 0.92f), keyInk = hsl(h, 0.12f * ps, 0.12f), keyEdge = hsl(h, 0.12f * ps, 0.80f),
        keyHi = Color.Transparent, danger = Color(0xFFB3261E),
    ) else Palette(
        dark = true,
        bg = hsl(h, 0.12f * ps, 0.075f), onBg = hsl(h, 0.15f * ps, 0.90f), muted = hsl(h, 0.08f * ps, 0.68f),
        surface = hsl(h, 0.12f * ps, 0.13f), surfaceHi = hsl(h, 0.10f * ps, 0.18f), outline = hsl(h, 0.06f * ps, 0.57f),
        primary = hsl(h, maxOf(0.5f, 0.85f * ps), 0.80f), onPrimary = hsl(h, 0.65f * ps, 0.20f),
        container = hsl(h, 0.35f * ps, 0.30f), onContainer = hsl(h, 0.85f * ps, 0.92f),
        accent2 = hsl(h + 60f, 0.35f * ps, 0.30f), onAccent2 = hsl(h + 60f, 0.8f * ps, 0.90f),
        well = hsl(h, 0.35f * ps, 0.30f), wellInk = hsl(h, 0.85f * ps, 0.92f), wellDim = hsl(h, 0.3f * ps, 0.78f),
        wellGhost = hsl(h, 0.85f * ps, 0.92f, 0.10f), wellEdge = Color.Transparent,
        key = hsl(h, 0.10f * ps, 0.18f), keyInk = hsl(h, 0.15f * ps, 0.90f), keyEdge = Color.Black,
        keyHi = Color.Transparent, danger = Color(0xFFF2B8B5),
    )
}

/** Своя тема в раскладке Okto: акцент красит табло и главную клавишу (как темы Amber/Ocean у Okto). */
fun oktoPalette(h: Float, s: Float, dark: Boolean): Palette {
    val ps = s.coerceIn(0f, 1f)
    return if (dark) Palette(
        dark = true,
        bg = hsl(h, 0.18f * ps, 0.08f), onBg = hsl(h, 0.15f * ps, 0.91f), muted = hsl(h, 0.08f * ps, 0.58f),
        surface = hsl(h, 0.15f * ps, 0.11f), surfaceHi = hsl(h, 0.14f * ps, 0.15f), outline = hsl(h, 0.14f * ps, 0.17f),
        primary = if (ps < 0.05f) Color(0xFFEDEDED) else hsl(h, maxOf(0.7f, ps), 0.60f),
        onPrimary = if (ps < 0.05f) Color(0xFF141414) else hsl(h, 0.6f, 0.10f),
        container = hsl(h, 0.14f * ps, 0.15f), onContainer = hsl(h, 0.15f * ps, 0.91f),
        accent2 = hsl(h, 0.14f * ps, 0.15f), onAccent2 = hsl(h, 0.15f * ps, 0.91f),
        well = hsl(h, 0.25f * ps, 0.04f),
        wellInk = if (ps < 0.05f) Color(0xFFF2F2F2) else hsl(h, maxOf(0.75f, ps), 0.66f),
        wellDim = hsl(h, 0.18f * ps, 0.52f),
        wellGhost = if (ps < 0.05f) Color(0x0FFFFFFF) else hsl(h, 0.75f, 0.66f, 0.08f),
        wellEdge = hsl(h, 0.2f * ps, 0.10f),
        key = hsl(h, 0.14f * ps, 0.15f), keyInk = hsl(h, 0.15f * ps, 0.91f), keyEdge = Color.Black,
        keyHi = Color(0x10FFFFFF), danger = Color(0xFFEF5A5F),
    ) else Palette(
        dark = false,
        bg = hsl(h, 0.22f * ps, 0.89f), onBg = hsl(h, 0.30f * ps, 0.12f), muted = hsl(h, 0.12f * ps, 0.34f),
        surface = hsl(h, 0.20f * ps, 0.85f), surfaceHi = hsl(h, 0.25f * ps, 0.95f), outline = hsl(h, 0.18f * ps, 0.76f),
        primary = hsl(h, 0.55f * ps, 0.28f), onPrimary = Color.White,
        container = hsl(h, 0.25f * ps, 0.95f), onContainer = hsl(h, 0.30f * ps, 0.12f),
        accent2 = hsl(h, 0.25f * ps, 0.95f), onAccent2 = hsl(h, 0.30f * ps, 0.12f),
        well = hsl(h, 0.18f * ps, 0.15f), wellInk = hsl(h, 0.55f * ps, 0.86f), wellDim = hsl(h, 0.15f * ps, 0.60f),
        wellGhost = hsl(h, 0.55f * ps, 0.86f, 0.07f), wellEdge = Color(0x66000000),
        key = hsl(h, 0.30f * ps, 0.96f), keyInk = hsl(h, 0.30f * ps, 0.12f), keyEdge = hsl(h, 0.18f * ps, 0.68f),
        keyHi = Color(0xCCFFFFFF), danger = Color(0xFFD33A3F),
    )
}

// ---------- Тема приложения ----------

data class NoteColors(val bg: Color, val fg: Color, val muted: Color, val border: Color)

/** Оттенки меток заметок: зелёный, розовый, сиреневый, персиковый, жёлтый, голубой. */
val NoteHues = listOf(150f, 340f, 255f, 22f, 46f, 205f)

@Immutable
class AppTheme(val style: UiStyle, val c: Palette) {
    val okto: Boolean get() = style == UiStyle.OKTO
    val font: FontFamily get() = if (okto) Golos else Nunito

    /** Цвет метки (кружок в выборе цвета). 0 — без метки. */
    fun tag(i: Int): Color =
        if (i <= 0 || i > NoteHues.size) c.surface else hsl(NoteHues[i - 1], 0.75f, if (okto || c.dark) 0.62f else 0.80f)

    fun note(i: Int): NoteColors {
        if (i <= 0 || i > NoteHues.size) {
            return NoteColors(c.surface, c.onBg, c.muted, if (okto) c.outline else Color.Transparent)
        }
        val h = NoteHues[i - 1]
        return when {
            okto -> {
                val t = tag(i)
                NoteColors(lerp(c.surface, t, 0.16f), c.onBg, c.muted, t.copy(alpha = 0.35f))
            }
            c.dark -> NoteColors(hsl(h, 0.30f, 0.22f), hsl(h, 0.70f, 0.90f), hsl(h, 0.35f, 0.74f), Color.Transparent)
            else -> NoteColors(hsl(h, 0.75f, 0.90f), hsl(h, 0.55f, 0.16f), hsl(h, 0.30f, 0.34f), Color.Transparent)
        }
    }

    // Типографика
    val h1: TextStyle
        get() = if (okto) TextStyle(fontFamily = Golos, fontSize = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.14).sp, lineHeight = 42.sp, color = c.onBg)
        else TextStyle(fontFamily = Nunito, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp, lineHeight = 38.sp, color = c.onBg)

    val cardTitle: TextStyle
        get() = if (okto) TextStyle(fontFamily = Golos, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 21.sp)
        else TextStyle(fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 22.sp)

    val bodyText: TextStyle
        get() = if (okto) TextStyle(fontFamily = Golos, fontSize = 14.sp, lineHeight = 20.sp)
        else TextStyle(fontFamily = Nunito, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)

    /** Мелкие подписи: в Okto — моно заглавными, в цветной — жирный Nunito. */
    val caption: TextStyle
        get() = if (okto) TextStyle(fontFamily = JbMono, fontSize = 12.sp, letterSpacing = 0.6.sp)
        else TextStyle(fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.Bold)

    val keyLabel: TextStyle
        get() = TextStyle(fontFamily = JbMono, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)

    fun cap(s: String) = if (okto) s.uppercase() else s
}

val LocalAppTheme = staticCompositionLocalOf { AppTheme(UiStyle.TONAL, ColorPalette) }

val T: AppTheme
    @Composable @ReadOnlyComposable get() = LocalAppTheme.current

val C: Palette
    @Composable @ReadOnlyComposable get() = LocalAppTheme.current.c

fun buildTheme(s: ThemeSettings): AppTheme = when (s.kind) {
    ThemeKind.COLOR -> AppTheme(UiStyle.TONAL, ColorPalette)
    ThemeKind.OKTO -> AppTheme(UiStyle.OKTO, OktoPalette)
    ThemeKind.OKTO_LIGHT -> AppTheme(UiStyle.OKTO, OktoLightPalette)
    ThemeKind.CUSTOM -> when (s.style) {
        UiStyle.TONAL -> AppTheme(UiStyle.TONAL, tonalPalette(s.hue, s.sat, s.dark))
        UiStyle.OKTO -> AppTheme(UiStyle.OKTO, oktoPalette(s.hue, s.sat, s.dark))
    }
}

@Composable
fun OktoTheme(theme: AppTheme, strings: Strings, content: @Composable () -> Unit) {
    val c = theme.c
    val scheme = if (c.dark) {
        darkColorScheme(primary = c.primary, onPrimary = c.onPrimary, background = c.bg, onBackground = c.onBg, surface = c.surface, onSurface = c.onBg, surfaceVariant = c.surfaceHi, outline = c.outline)
    } else {
        lightColorScheme(primary = c.primary, onPrimary = c.onPrimary, background = c.bg, onBackground = c.onBg, surface = c.surface, onSurface = c.onBg, surfaceVariant = c.surfaceHi, outline = c.outline)
    }
    CompositionLocalProvider(LocalAppTheme provides theme, LocalStrings provides strings) {
        MaterialTheme(colorScheme = scheme) {
            ProvideTextStyle(TextStyle(fontFamily = theme.font, color = c.onBg), content)
        }
    }
}

// ---------- Даты ----------

private val formatters = HashMap<Pair<String, Locale>, DateTimeFormatter>()
private val time = DateTimeFormatter.ofPattern("HH:mm")

/** Форматтер для шаблона на языке интерфейса (кэшируется). */
private fun fmt(pattern: String, locale: Locale): DateTimeFormatter =
    formatters.getOrPut(pattern to locale) { DateTimeFormatter.ofPattern(pattern, locale) }

@Composable @ReadOnlyComposable
fun LocalDate.dayMonth(): String = format(fmt(S.dayMonthPattern, S.locale))
@Composable @ReadOnlyComposable
fun LocalDate.weekday(): String = format(fmt("EEEE", S.locale)).replaceFirstChar { it.uppercase() }
@Composable @ReadOnlyComposable
fun LocalDate.weekdayShort(): String = format(fmt("EE", S.locale)).replaceFirstChar { it.uppercase() }
@Composable @ReadOnlyComposable
fun LocalDate.monthShort(): String = format(fmt("MMM", S.locale)).trimEnd('.')
@Composable @ReadOnlyComposable
fun LocalDate.ddMM(): String = format(fmt(S.ddMMPattern, S.locale))
fun LocalDateTime.hhmm(): String = format(time)

@Composable @ReadOnlyComposable
fun formatStamp(millis: Long): String {
    val s = S
    val dt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    val d = dt.toLocalDate()
    val today = LocalDate.now()
    return when (d) {
        today -> "${s.today}, ${dt.format(time)}"
        today.minusDays(1) -> "${s.yesterday}, ${dt.format(time)}"
        else -> d.format(fmt(if (d.year == today.year) s.dayMonthShortPattern else s.fullDatePattern, s.locale))
    }
}

/** Короткая метка времени для строк Okto: «14:32», «ВЧЕРА», «05.10». */
@Composable @ReadOnlyComposable
fun shortStamp(millis: Long): String {
    val dt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    val d = dt.toLocalDate()
    val today = LocalDate.now()
    return when (d) {
        today -> dt.format(time)
        today.minusDays(1) -> S.yesterday.uppercase()
        else -> d.ddMM()
    }
}

@Composable @ReadOnlyComposable
fun greeting(now: LocalDateTime = LocalDateTime.now()) = S.greeting(now.hour)
