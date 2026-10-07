package com.okto.notes.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okto.notes.OktoViewModel
import com.okto.notes.data.Entry
import com.okto.notes.data.EntryType
import com.okto.notes.data.Moods
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** Вставка с новой строки, если курсор не в начале строки (для списков и задач). */
private fun TextFieldValue.insertLine(prefix: String): TextFieldValue {
    val start = selection.min
    val lead = if (start == 0 || text[start - 1] == '\n') "" else "\n"
    val ins = lead + prefix
    return TextFieldValue(text.replaceRange(start, selection.max, ins), TextRange(start + ins.length))
}

private fun TextFieldValue.insert(s: String): TextFieldValue {
    val start = selection.min
    return TextFieldValue(text.replaceRange(start, selection.max, s), TextRange(start + s.length))
}

@Composable
fun EditorScreen(entry: Entry, vm: OktoViewModel) {
    val t = T
    val c = t.c
    val isDiary = entry.type == EntryType.DIARY
    val nc = if (isDiary || t.okto) NoteColors(c.bg, c.onBg, c.muted, Color.Transparent) else t.note(entry.color).let { if (entry.color == 0) it.copy(bg = c.bg) else it }
    val bg by animateColorAsState(nc.bg, label = "bg")
    val chipBg = if (!t.okto && !isDiary && entry.color != 0) nc.fg.copy(alpha = 0.08f) else null

    var body by remember(entry.id) { mutableStateOf(TextFieldValue(entry.body)) }
    fun setBody(v: TextFieldValue) {
        body = v
        if (v.text != entry.body) vm.edit { it.copy(body = v.text) }
    }

    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }
    LaunchedEffect(entry.id) {
        if (entry.title.isEmpty() && entry.body.isEmpty()) {
            runCatching { (if (isDiary) bodyFocus else titleFocus).requestFocus() }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // Верхняя панель
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconBtn(Icons.AutoMirrored.Filled.ArrowBack, "Назад", vm::closeEditor, bg = chipBg, tint = nc.fg)
            if (t.okto) {
                Text(
                    "${if (isDiary) "ДНЕВНИК" else "ЗАМЕТКА"} · ${entry.date.ddMM()}",
                    fontFamily = JbMono, fontSize = 12.sp, letterSpacing = 1.6.sp, color = c.muted,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            if (!isDiary) {
                IconBtn(
                    Icons.Filled.Star, "Избранное", { vm.edit { it.copy(pinned = !it.pinned) } },
                    bg = chipBg,
                    tint = if (entry.pinned) (if (t.okto || entry.color == 0) c.primary else nc.fg) else nc.fg.copy(alpha = 0.35f),
                )
            }
            IconBtn(Icons.Filled.Delete, "Удалить", { vm.delete(entry) }, bg = chipBg, tint = if (t.okto) c.danger else nc.fg)
            Btn("Готово", vm::closeEditor, primary = true, height = if (t.okto) 46.dp else 48.dp)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (t.okto) 16.dp else 24.dp),
        ) {
            if (isDiary) {
                if (t.okto) {
                    Well(Modifier.fillMaxWidth()) {
                        WellLabel("Настроение", if (entry.mood >= 0) Moods[entry.mood].second else "—")
                        Spacer(Modifier.height(10.dp))
                        MoodCells(entry.mood) { m -> vm.edit { it.copy(mood = if (it.mood == m) -1 else m) } }
                    }
                    Spacer(Modifier.height(18.dp))
                } else {
                    Text(entry.date.weekday(), fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = c.primary)
                    Text(entry.date.dayMonth(), style = t.h1)
                    Spacer(Modifier.height(14.dp))
                    TonalMoodPicker(entry.mood) { m -> vm.edit { it.copy(mood = if (it.mood == m) -1 else m) } }
                    Spacer(Modifier.height(18.dp))
                }
            } else {
                if (t.okto) {
                    Well(Modifier.fillMaxWidth()) {
                        WellLabel("Метка")
                        Spacer(Modifier.height(10.dp))
                        TagPicker(entry.color) { i -> vm.edit { it.copy(color = i) } }
                    }
                    Spacer(Modifier.height(18.dp))
                } else {
                    TagPicker(entry.color) { i -> vm.edit { it.copy(color = i) } }
                    Spacer(Modifier.height(16.dp))
                }
            }

            val titleStyle = if (t.okto) {
                TextStyle(fontFamily = Golos, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.9).sp, lineHeight = 34.sp, color = nc.fg)
            } else {
                TextStyle(fontFamily = Nunito, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp, lineHeight = 34.sp, color = nc.fg)
            }
            val bodyStyle = if (t.okto) {
                TextStyle(fontFamily = Golos, fontSize = 17.sp, lineHeight = 27.sp, color = nc.fg.copy(alpha = 0.88f))
            } else {
                TextStyle(fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp, color = nc.fg)
            }
            Field(
                value = entry.title,
                onValueChange = { s -> vm.edit { it.copy(title = s) } },
                placeholder = if (isDiary) "Заголовок дня" else "Заголовок",
                style = titleStyle, placeholderColor = nc.muted, cursor = c.primary,
                modifier = Modifier.focusRequester(titleFocus),
            )
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp),
            ) {
                if (body.text.isEmpty()) {
                    Text(if (isDiary) "Что сегодня произошло? О чём думаешь?" else "Начни писать…", style = bodyStyle.copy(color = nc.muted))
                }
                BasicTextField(
                    value = body,
                    onValueChange = ::setBody,
                    textStyle = bodyStyle,
                    cursorBrush = SolidColor(c.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp)
                        .focusRequester(bodyFocus),
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Статус и панель вставки
        val words = (entry.title + " " + body.text).split(Regex("\\s+")).count { it.isNotBlank() }
        val saved = Instant.ofEpochMilli(entry.updatedAt).atZone(ZoneId.systemDefault()).toLocalDateTime().hhmm()
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                val st = if (t.okto) TextStyle(fontFamily = JbMono, fontSize = 11.sp, letterSpacing = 1.sp) else TextStyle(fontFamily = Nunito, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(t.cap("Сохранено · $saved"), style = st, color = nc.muted, modifier = Modifier.weight(1f))
                Text(if (t.okto) "${words.toString().padStart(3, '0')} СЛОВ" else plural(words, "слово", "слова", "слов"), style = st, color = nc.muted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolKey("• Список", Modifier.weight(1f), chipBg) { setBody(body.insertLine("• ")) }
                ToolKey("☐ Задача", Modifier.weight(1f), chipBg) { setBody(body.insertLine("☐ ")) }
                ToolKey("Время", Modifier.weight(1f), chipBg) { setBody(body.insert(LocalDateTime.now().hhmm() + " ")) }
            }
        }
    }
}

@Composable
private fun ToolKey(label: String, modifier: Modifier, bg: Color?, onClick: () -> Unit) {
    val t = T
    val c = t.c
    if (t.okto) {
        KeySurface(onClick, modifier.height(46.dp)) { Text(label.uppercase(), style = t.keyLabel, color = c.keyInk) }
    } else {
        Box(
            modifier
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg ?: c.surfaceHi)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Text(label, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = c.onBg) }
    }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    style: TextStyle,
    placeholderColor: Color,
    cursor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()) {
        if (value.isEmpty()) Text(placeholder, style = style.copy(color = placeholderColor))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = style,
            cursorBrush = SolidColor(cursor),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Выбор цвета/метки заметки. */
@Composable
private fun TagPicker(selected: Int, onSelect: (Int) -> Unit) {
    val t = T
    val c = t.c
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (i in 0..NoteHues.size) {
            val fill = if (t.okto) (if (i == 0) c.wellGhost else t.tag(i)) else t.note(i).bg
            val active = i == selected
            val ring = if (t.okto) c.wellInk else c.onBg
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(if (active) 2.dp else 1.dp, if (active) ring else c.outline, CircleShape)
                    .padding(if (active) 5.dp else 0.dp)
                    .clip(CircleShape)
                    .background(fill)
                    .clickable { onSelect(i) },
            )
        }
    }
}

@Composable
private fun TonalMoodPicker(selected: Int, onSelect: (Int) -> Unit) {
    val c = C
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Moods.forEachIndexed { i, (emoji, label) ->
            val active = i == selected
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (active) c.container else c.surfaceHi)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(emoji, fontSize = 24.sp)
                Spacer(Modifier.height(2.dp))
                Text(label, fontFamily = Nunito, fontSize = 10.sp, maxLines = 1, fontWeight = FontWeight.Bold, color = if (active) c.onContainer else c.muted)
            }
        }
    }
}
