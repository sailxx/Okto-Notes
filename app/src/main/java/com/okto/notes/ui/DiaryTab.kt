package com.okto.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okto.notes.OktoViewModel
import com.okto.notes.data.Entry
import com.okto.notes.data.Moods
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

@Composable
fun DiaryTab(vm: OktoViewModel) {
    if (T.okto) OktoDiary(vm) else TonalDiary(vm)
}

// ---------------- Цветная ----------------

@Composable
private fun TonalDiary(vm: OktoViewModel) {
    val c = C
    val entries = vm.diary
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val todayMood = entries.firstOrNull { it.day == LocalDate.now().toEpochDay() }?.mood ?: -1

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top + 12.dp, bottom = 190.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            ScreenTitle("Дневник", todaySubtitle(), Modifier.padding(start = 4.dp)) {
                IconBtn(Icons.Filled.Settings, "Настройки", { vm.showSettings = true }, bg = c.primary, tint = c.onPrimary)
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(c.primary)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(c.onPrimary),
                    contentAlignment = Alignment.Center,
                ) { Text("${vm.streak}", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 26.sp, color = c.primary) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        plural(vm.streak, "день", "дня", "дней").substringAfter(' ').replaceFirstChar { it.uppercase() } + " подряд",
                        fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = c.onPrimary,
                    )
                    Text(
                        "Всего ${plural(entries.size, "запись", "записи", "записей")}",
                        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = c.onPrimary.copy(alpha = 0.8f),
                    )
                }
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(c.surface)
                    .padding(18.dp),
            ) {
                Text("Как ты сегодня?", style = T.cardTitle, color = c.onBg)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Moods.forEachIndexed { i, (emoji, label) ->
                        val active = i == todayMood
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (active) c.container else c.surfaceHi)
                                .clickable { vm.quickMood(i) }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(emoji, fontSize = 24.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                label, fontFamily = Nunito, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold, color = if (active) c.onContainer else c.muted,
                            )
                        }
                    }
                }
            }
        }
        item { TonalWeekCard(vm) }
        if (entries.isEmpty()) {
            item { EmptyState("📔", "Начни свой дневник", "Отметь настроение или нажми «Запись», чтобы описать день") }
        } else {
            item { SectionTitle("Записи", Modifier.padding(start = 4.dp, top = 8.dp)) }
            items(entries, key = { it.id }) { e -> TonalDiaryCard(e, { vm.open(e) }, Modifier.animateItem()) }
        }
    }
}

@Composable
private fun TonalDiaryCard(e: Entry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = T
    val c = t.c
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(c.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .size(width = 58.dp, height = 62.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(c.container),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("${e.date.dayOfMonth}", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 22.sp, color = c.onContainer)
            Text(e.date.monthShort(), fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = c.onContainer.copy(alpha = 0.75f))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(e.title.ifBlank { e.date.weekday() }, style = t.cardTitle, color = c.onBg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(e.body.ifBlank { "Без текста" }, style = t.bodyText, color = c.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (e.mood >= 0) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(c.surfaceHi),
                contentAlignment = Alignment.Center,
            ) { Text(Moods[e.mood].first, fontSize = 20.sp) }
        }
    }
}

// ---------------- Okto ----------------

@Composable
private fun OktoDiary(vm: OktoViewModel) {
    val t = T
    val c = t.c
    val entries = vm.diary
    val today = LocalDate.now()
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val days = entries.map { it.day }.toSet()
    val todayMood = entries.firstOrNull { it.day == today.toEpochDay() }?.mood ?: -1

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top + 6.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { OktoHeader { vm.showSettings = true } }
        item { ScreenTitle(greeting(), todaySubtitle(), Modifier.padding(top = 4.dp, bottom = 4.dp)) }

        // Серия + тепловая карта последних 4 недель
        item {
            Well(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        WellLabel("Серия")
                        Spacer(Modifier.height(6.dp))
                        BigDigits(vm.streak.toString().padStart(2, '0'), 58.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("ДНЕЙ ПОДРЯД", fontFamily = JbMono, fontSize = 10.sp, letterSpacing = 1.4.sp, color = c.wellDim)
                    }
                    val start = today.with(DayOfWeek.MONDAY).minusWeeks(3)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (w in 0..3) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (d in 0..6) {
                                    val date = start.plusDays((w * 7 + d).toLong())
                                    val on = date.toEpochDay() in days
                                    val isToday = date == today
                                    Box(
                                        Modifier
                                            .size(16.dp)
                                            .then(if (isToday) Modifier.border(1.dp, c.wellDim, RoundedCornerShape(4.dp)).padding(2.dp) else Modifier)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                when {
                                                    on -> c.wellInk
                                                    date.isAfter(today) -> Color.Transparent
                                                    else -> c.wellGhost
                                                },
                                            )
                                            .then(if (!date.isAfter(today)) Modifier.clickable { vm.openDay(date) } else Modifier),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Настроение сегодня — клавиши 1..5
        item {
            Well(Modifier.fillMaxWidth()) {
                WellLabel("Настроение · сегодня", if (todayMood >= 0) Moods[todayMood].second else "—")
                Spacer(Modifier.height(10.dp))
                MoodCells(todayMood) { vm.quickMood(it) }
            }
        }

        // Настроение за неделю — столбики
        item {
            val monday = today.with(DayOfWeek.MONDAY)
            val moods = (0..6).map { i ->
                val d = monday.plusDays(i.toLong()).toEpochDay()
                entries.firstOrNull { it.day == d && it.mood >= 0 }?.mood ?: -1
            }
            val rated = moods.filter { it >= 0 }
            val avg = if (rated.isEmpty()) "—" else String.format(Locale.US, "%.1f", rated.average() + 1)
            Well(Modifier.fillMaxWidth()) {
                WellLabel("Настроение · неделя", "среднее $avg")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(84.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                    moods.forEachIndexed { i, m ->
                        val isToday = monday.plusDays(i.toLong()) == today
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(if (m >= 0) (m + 1) / 5f else 0.04f)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(
                                    when {
                                        m < 0 -> c.wellGhost
                                        isToday -> c.wellInk
                                        else -> c.wellInk.copy(alpha = 0.28f)
                                    },
                                ),
                        )
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.wellGhost))
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (0..6).forEach { i ->
                        val d = monday.plusDays(i.toLong())
                        Text(
                            d.weekdayShort().uppercase(), fontFamily = JbMono, fontSize = 10.sp,
                            fontWeight = if (d == today) FontWeight.Bold else FontWeight.Normal,
                            color = if (d == today) c.wellInk else c.wellDim,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            item { EmptyState("", "Начни свой дневник", "Отметь настроение или нажми «Запись», чтобы описать день") }
        } else {
            item { SectionTitle("Записи", Modifier.padding(start = 2.dp, top = 8.dp)) }
            items(entries, key = { it.id }) { e -> OktoDiaryRow(e, { vm.open(e) }, Modifier.animateItem()) }
        }
    }
}

/** Пять ячеек настроения 1..5 на табло. */
@Composable
fun MoodCells(selected: Int, onSelect: (Int) -> Unit) {
    val c = C
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0..4) {
            val active = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .then(if (active) Modifier.border(1.dp, c.wellInk, RoundedCornerShape(10.dp)).padding(3.dp) else Modifier)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) c.wellInk else c.wellGhost)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${i + 1}", fontFamily = JbMono, fontSize = 16.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (active) c.well else c.wellDim,
                )
            }
        }
    }
}

@Composable
private fun OktoDiaryRow(e: Entry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = T
    val c = t.c
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
            Text(e.date.dayOfMonth.toString().padStart(2, '0'), fontFamily = JbMono, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = c.onBg)
            Text(e.date.monthShort().uppercase(), fontFamily = JbMono, fontSize = 10.sp, letterSpacing = 0.8.sp, color = c.muted)
        }
        Column(Modifier.weight(1f)) {
            Text(e.title.ifBlank { e.date.weekday() }, style = t.cardTitle, color = c.onBg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(e.body.ifBlank { "Без текста" }.replace('\n', ' '), style = t.bodyText, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (e.mood >= 0) {
            Text(
                "${e.mood + 1}/5", fontFamily = JbMono, fontSize = 12.sp, color = c.onBg,
                modifier = Modifier
                    .border(1.dp, c.outline, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
    }
}
