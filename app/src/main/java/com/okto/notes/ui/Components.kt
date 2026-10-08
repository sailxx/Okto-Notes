package com.okto.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okto.notes.Tab
import java.time.LocalDateTime

private val KeyShape = RoundedCornerShape(10.dp)

/** Объёмная клавиша Okto: тёмная кромка снизу и тонкий блик сверху. pressed — «вдавленная» (активная вкладка). */
@Composable
fun KeySurface(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    bg: Color = C.key,
    pressed: Boolean = false,
    shape: Shape = KeyShape,
    content: @Composable BoxScope.() -> Unit,
) {
    val c = C
    val base = if (pressed) {
        modifier.clip(shape).background(bg)
    } else {
        modifier
            .clip(shape)
            .background(c.keyEdge)
            .padding(bottom = 2.dp)
            .clip(shape)
            .background(bg)
            .drawBehind { drawRect(c.keyHi, size = Size(size.width, 1.dp.toPx())) }
    }
    Box(
        base.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun Btn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    icon: ImageVector? = null,
    height: Dp = 48.dp,
) {
    val t = T
    val c = t.c
    if (t.okto) {
        val fg = if (primary) c.onPrimary else c.keyInk
        KeySurface(onClick, modifier.height(height), bg = if (primary) c.primary else c.key) {
            Row(
                Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                icon?.let { Icon(it, null, tint = fg, modifier = Modifier.size(18.dp)) }
                Text(text.uppercase(), style = t.keyLabel, color = fg)
            }
        }
    } else {
        val fg = if (primary) c.onPrimary else c.onBg
        Row(
            modifier
                .height(height)
                .clip(RoundedCornerShape(if (primary) 20.dp else 16.dp))
                .background(if (primary) c.primary else c.surfaceHi)
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            icon?.let { Icon(it, null, tint = fg, modifier = Modifier.size(20.dp)) }
            Text(text, color = fg, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun IconBtn(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    bg: Color? = null,
) {
    val t = T
    val c = t.c
    if (t.okto) {
        KeySurface(onClick, modifier.size(44.dp), bg = bg ?: c.key) {
            Icon(icon, contentDescription, tint = tint ?: c.keyInk, modifier = Modifier.size(20.dp))
        }
    } else {
        Box(
            modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bg ?: c.surfaceHi)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription, tint = tint ?: c.onBg, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val t = T
    val c = t.c
    if (t.okto) {
        KeySurface(onClick, Modifier.height(36.dp), bg = if (selected) c.primary else c.key) {
            Text(
                text.uppercase(), style = t.keyLabel, color = if (selected) c.onPrimary else c.keyInk,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
        }
    } else {
        val shape = RoundedCornerShape(12.dp)
        Row(
            Modifier
                .height(36.dp)
                .clip(shape)
                .then(if (selected) Modifier.background(c.container) else Modifier.border(1.dp, c.outline, shape))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (selected) Icon(Icons.Filled.Check, null, tint = c.onContainer, modifier = Modifier.size(16.dp))
            Text(
                text, fontFamily = Nunito, fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (selected) c.onContainer else c.muted,
            )
        }
    }
}

/** Панель-«колодец»: в Okto — утопленное тёмное табло, в цветной теме — пастельная карточка. */
@Composable
fun Well(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val t = T
    val c = t.c
    val shape = RoundedCornerShape(if (t.okto) 12.dp else 28.dp)
    Column(
        modifier
            .clip(shape)
            .background(c.well)
            .then(if (t.okto) Modifier.border(1.dp, c.wellEdge, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(if (t.okto) PaddingValues(horizontal = 14.dp, vertical = 12.dp) else PaddingValues(horizontal = 20.dp, vertical = 18.dp)),
        content = content,
    )
}

@Composable
fun WellLabel(left: String, right: String? = null) {
    val t = T
    val c = t.c
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (t.okto) {
            val st = TextStyle(fontFamily = JbMono, fontSize = 10.sp, letterSpacing = 1.4.sp, color = c.wellDim)
            Text(left.uppercase(), style = st, modifier = Modifier.weight(1f))
            right?.let { Text(it.uppercase(), style = st) }
        } else {
            Text(left, fontFamily = Nunito, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = c.wellInk, modifier = Modifier.weight(1f))
            right?.let { Text(it, fontFamily = Nunito, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.primary) }
        }
    }
}

/** Крупные цифры табло; в Okto за ними просвечивают «призрачные» восьмёрки, как на LCD. */
@Composable
fun BigDigits(value: String, size: TextUnit = 44.sp) {
    val t = T
    val c = t.c
    if (t.okto) {
        val st = TextStyle(fontFamily = JbMono, fontSize = size, fontWeight = FontWeight.Bold, lineHeight = size)
        Box {
            Text(value.map { if (it.isDigit()) '8' else it }.joinToString(""), style = st, color = c.wellGhost)
            Text(value, style = st, color = c.wellInk)
        }
    } else {
        Text(value, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = size, lineHeight = size, color = c.wellInk)
    }
}

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val t = T
    val c = t.c
    val shape = if (t.okto) RoundedCornerShape(10.dp) else RoundedCornerShape(28.dp)
    Row(
        modifier
            .height(if (t.okto) 46.dp else 56.dp)
            .clip(shape)
            .background(if (t.okto) c.surface else c.surfaceHi)
            .then(if (t.okto) Modifier.border(1.dp, c.outline, shape) else Modifier)
            .padding(horizontal = if (t.okto) 14.dp else 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, null, tint = c.muted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            val st = TextStyle(fontFamily = t.font, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = c.onBg)
            if (value.isEmpty()) Text(placeholder, style = st.copy(color = c.muted))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = st,
                cursorBrush = SolidColor(c.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Filled.Close, S.clear, tint = c.muted,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable { onValueChange("") },
            )
        }
    }
}

/** Шапка Okto: точка, логотип, кнопка настроек. */
@Composable
fun OktoHeader(onSettings: () -> Unit) {
    val c = C
    Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(c.key)
                .border(1.dp, c.outline, CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Text("okto notes", fontFamily = JbMono, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = c.onBg)
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Settings, S.settings, tint = c.onBg, modifier = Modifier.size(20.dp)) }
    }
}

/** Нижняя навигация. В Okto — ряд клавиш с широкой «НОВАЯ», в цветной — панель с индикатором. */
@Composable
fun NavBar(tab: Tab, onTab: (Tab) -> Unit, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val t = T
    val c = t.c
    if (t.okto) {
        Row(
            modifier
                .fillMaxWidth()
                .background(c.bg)
                .drawBehind { drawLine(c.outline, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OktoNavKey(S.notes, Icons.AutoMirrored.Filled.List, tab == Tab.NOTES, Modifier.weight(1f)) { onTab(Tab.NOTES) }
            OktoNavKey(S.diary, Icons.Filled.DateRange, tab == Tab.DIARY, Modifier.weight(1f)) { onTab(Tab.DIARY) }
            KeySurface(onAdd, Modifier.weight(2f).height(62.dp), bg = c.primary) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Add, null, tint = c.onPrimary, modifier = Modifier.size(22.dp))
                    Text((if (tab == Tab.NOTES) S.newNote else S.newEntry).uppercase(), style = t.keyLabel.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = c.onPrimary)
                }
            }
        }
    } else {
        Row(
            modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.lerp(c.bg, c.primary, 0.05f))
                .navigationBarsPadding()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TonalNavItem(S.notes, Icons.AutoMirrored.Filled.List, tab == Tab.NOTES) { onTab(Tab.NOTES) }
            TonalNavItem(S.diary, Icons.Filled.DateRange, tab == Tab.DIARY) { onTab(Tab.DIARY) }
        }
    }
}

@Composable
private fun OktoNavKey(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val t = T
    val c = t.c
    KeySurface(onClick, modifier.height(62.dp), bg = if (selected) c.surface else c.key, pressed = selected) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (selected) c.onBg else c.muted, modifier = Modifier.size(20.dp))
            Text(
                label.uppercase(),
                style = t.keyLabel.copy(fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, letterSpacing = 0.8.sp),
                color = if (selected) c.onBg else c.muted,
            )
        }
    }
}

@Composable
private fun TonalNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val c = C
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = 64.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) c.container else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = if (selected) c.onContainer else c.muted, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.height(4.dp))
        Text(
            label, fontFamily = Nunito, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (selected) c.onBg else c.muted,
        )
    }
}

/** Большая кнопка «Новая» цветной темы. */
@Composable
fun ExtendedFab(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = C
    Row(
        modifier
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(c.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, null, tint = c.onPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = c.onPrimary)
    }
}

@Composable
fun UndoBar(onUndo: () -> Unit, modifier: Modifier = Modifier) {
    val t = T
    val c = t.c
    if (t.okto) {
        Row(
            modifier
                .clip(KeyShape)
                .background(c.well)
                .border(1.dp, c.outline, KeyShape)
                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(S.entryDeleted.uppercase(), style = t.keyLabel, color = c.wellInk)
            Spacer(Modifier.width(14.dp))
            KeySurface(onUndo, Modifier.height(36.dp), bg = c.primary) {
                Text(S.undo.uppercase(), style = t.keyLabel, color = c.onPrimary, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    } else {
        Row(
            modifier
                .clip(RoundedCornerShape(16.dp))
                .background(c.onBg)
                .padding(start = 18.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(S.entryDeleted, color = c.bg, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onUndo)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) { Text(S.undo, color = c.container, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, text: String, modifier: Modifier = Modifier) {
    val t = T
    val c = t.c
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (t.okto) {
            Text(S.emptyMark, fontFamily = JbMono, fontSize = 13.sp, letterSpacing = 2.sp, color = c.muted)
        } else {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(c.surfaceHi),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 36.sp) }
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = t.cardTitle, color = c.onBg)
        Spacer(Modifier.height(6.dp))
        Text(text, style = t.bodyText, color = c.muted, textAlign = TextAlign.Center)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    val t = T
    if (t.okto) {
        Text(text.uppercase(), fontFamily = JbMono, fontSize = 11.sp, letterSpacing = 1.6.sp, color = t.c.muted, modifier = modifier)
    } else {
        Text(text, fontFamily = Nunito, fontSize = 18.sp, fontWeight = FontWeight.Black, color = t.c.onBg, modifier = modifier)
    }
}

/** Заголовок экрана: приветствие/название + дата. */
@Composable
fun ScreenTitle(title: String, subtitle: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    val t = T
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(title, style = t.h1)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = if (t.okto) TextStyle(fontFamily = JbMono, fontSize = 13.sp) else TextStyle(fontFamily = Nunito, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = t.c.muted,
            )
        }
        trailing?.invoke()
    }
}

@Composable @ReadOnlyComposable
fun todaySubtitle(now: LocalDateTime = LocalDateTime.now()) = "${now.toLocalDate().weekday()}, ${now.toLocalDate().dayMonth()}"
