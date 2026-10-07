package com.okto.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okto.notes.OktoViewModel
import com.okto.notes.data.ThemeKind
import com.okto.notes.data.ThemeSettings
import com.okto.notes.data.UiStyle

/** Готовые акценты для своей темы: (оттенок, насыщенность). Первый — графит без цвета. */
private val Swatches = listOf(
    0f to 0f, 255f to 0.7f, 220f to 0.75f, 195f to 0.8f, 165f to 0.65f, 140f to 0.6f,
    85f to 0.7f, 45f to 0.9f, 25f to 0.85f, 0f to 0.75f, 335f to 0.7f, 290f to 0.6f,
)

@Composable
fun SettingsScreen(vm: OktoViewModel) {
    val t = T
    val c = t.c
    val s = vm.settings

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBtn(Icons.AutoMirrored.Filled.ArrowBack, "Назад", { vm.showSettings = false })
            Spacer(Modifier.width(14.dp))
            Text(t.cap("Настройки"), style = if (t.okto) t.keyLabel.copy(fontSize = 14.sp, letterSpacing = 2.sp) else t.h1.copy(fontSize = 26.sp))
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Тема", Modifier.padding(start = 4.dp, top = 6.dp))

            ThemeOption(
                title = "Цветная", subtitle = "Основная тема: мягкие пастельные карточки",
                preview = buildTheme(ThemeSettings(kind = ThemeKind.COLOR)),
                selected = s.kind == ThemeKind.COLOR,
            ) { vm.updateSettings { it.copy(kind = ThemeKind.COLOR) } }
            ThemeOption(
                title = "Okto", subtitle = "Графит, табло с цифрами и клавиши",
                preview = buildTheme(ThemeSettings(kind = ThemeKind.OKTO)),
                selected = s.kind == ThemeKind.OKTO,
            ) { vm.updateSettings { it.copy(kind = ThemeKind.OKTO) } }
            ThemeOption(
                title = "Своя", subtitle = "Собери тему из своей гаммы",
                preview = buildTheme(s.copy(kind = ThemeKind.CUSTOM)),
                selected = s.kind == ThemeKind.CUSTOM,
            ) { vm.updateSettings { it.copy(kind = ThemeKind.CUSTOM) } }

            if (s.kind == ThemeKind.CUSTOM) CustomThemeEditor(vm, s)

            SectionTitle("О приложении", Modifier.padding(start = 4.dp, top = 12.dp))
            Text(
                "Okto Notes 1.1 · заметки и дневник хранятся только на этом устройстве.",
                style = t.bodyText, color = c.muted, modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeOption(title: String, subtitle: String, preview: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val t = T
    val c = t.c
    val shape = RoundedCornerShape(if (t.okto) 12.dp else 24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.outline.copy(alpha = if (t.okto) 1f else 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiniPreview(preview)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(t.cap(title), style = if (t.okto) t.keyLabel.copy(fontSize = 14.sp) else t.cardTitle, color = c.onBg)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = t.bodyText, color = c.muted)
        }
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (selected) c.primary else c.surfaceHi),
            contentAlignment = Alignment.Center,
        ) { if (selected) Icon(Icons.Filled.Check, null, tint = c.onPrimary, modifier = Modifier.size(16.dp)) }
    }
}

/** Миниатюра темы: фон, табло, карточка и акцент. */
@Composable
private fun MiniPreview(p: AppTheme) {
    val pc = p.c
    val r = if (p.okto) 6.dp else 12.dp
    Column(
        Modifier
            .size(width = 64.dp, height = 76.dp)
            .clip(RoundedCornerShape(r + 4.dp))
            .background(pc.bg)
            .border(1.dp, pc.outline.copy(alpha = 0.4f), RoundedCornerShape(r + 4.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(22.dp).clip(RoundedCornerShape(r)).background(pc.well))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(r)).background(p.note(1).bg))
            Box(Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(r)).background(p.note(0).bg))
        }
        Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(r)).background(pc.primary))
    }
}

@Composable
private fun CustomThemeEditor(vm: OktoViewModel, s: ThemeSettings) {
    val t = T
    val c = t.c
    val shape = RoundedCornerShape(if (t.okto) 12.dp else 24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Label("Оформление")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Цветное", s.style == UiStyle.TONAL) { vm.updateSettings { it.copy(style = UiStyle.TONAL) } }
            Chip("Okto", s.style == UiStyle.OKTO) { vm.updateSettings { it.copy(style = UiStyle.OKTO) } }
        }
        Spacer(Modifier.height(2.dp))
        Label("Режим")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Светлая", !s.dark) { vm.updateSettings { it.copy(dark = false) } }
            Chip("Тёмная", s.dark) { vm.updateSettings { it.copy(dark = true) } }
        }
        Spacer(Modifier.height(2.dp))
        Label("Акцентный цвет")
        for (row in Swatches.chunked(6)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (h, sat) ->
                    val active = kotlin.math.abs(s.hue - h) < 0.5f && kotlin.math.abs(s.sat - sat) < 0.01f
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .border(if (active) 3.dp else 0.dp, if (active) c.onBg else c.surface, CircleShape)
                            .padding(if (active) 5.dp else 0.dp)
                            .clip(CircleShape)
                            .background(if (sat == 0f) hsl(0f, 0f, 0.35f) else hsl(h, sat, 0.55f))
                            .clickable { vm.updateSettings { it.copy(hue = h, sat = sat) } },
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Label("Оттенок · ${s.hue.toInt()}°")
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Brush.horizontalGradient((0..12).map { hsl(it * 30f, 0.75f, 0.55f) })),
        )
        Slider(
            value = s.hue,
            onValueChange = { h -> vm.updateSettings { it.copy(hue = h, sat = if (it.sat < 0.05f) 0.7f else it.sat) } },
            valueRange = 0f..359f,
            colors = SliderDefaults.colors(
                thumbColor = c.primary,
                activeTrackColor = c.primary,
                inactiveTrackColor = c.surfaceHi,
            ),
        )
        Label("Насыщенность · ${(s.sat * 100).toInt()}%")
        Slider(
            value = s.sat,
            onValueChange = { v -> vm.updateSettings { it.copy(sat = v) } },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = c.primary,
                activeTrackColor = c.primary,
                inactiveTrackColor = c.surfaceHi,
            ),
        )
    }
}

@Composable
private fun Label(text: String) {
    val t = T
    if (t.okto) {
        Text(text.uppercase(), fontFamily = JbMono, fontSize = 11.sp, letterSpacing = 1.4.sp, color = t.c.muted)
    } else {
        Text(text, fontFamily = Nunito, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = t.c.onBg)
    }
}
