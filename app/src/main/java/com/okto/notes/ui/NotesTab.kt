package com.okto.notes.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okto.notes.OktoViewModel
import com.okto.notes.data.Entry
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun NotesTab(vm: OktoViewModel) {
    val all = vm.notes
    val q = vm.query.trim()
    val shown = all
        .filter { !vm.pinnedOnly || it.pinned }
        .filter { q.isEmpty() || it.title.contains(q, true) || it.body.contains(q, true) }
    if (T.okto) OktoNotes(vm, all, shown) else TonalNotes(vm, all, shown)
}

// ---------------- Цветная ----------------

@Composable
private fun TonalNotes(vm: OktoViewModel, all: List<Entry>, shown: List<Entry>) {
    val c = C
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top + 12.dp, bottom = 190.dp),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SearchField(vm.query, { vm.query = it }, S.searchNotes, Modifier.weight(1f))
                IconBtn(Icons.Filled.Settings, S.settings, { vm.showSettings = true }, bg = c.primary, tint = c.onPrimary)
            }
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            Text(greeting(), style = T.h1, modifier = Modifier.padding(start = 4.dp, top = 6.dp))
        }
        item(span = StaggeredGridItemSpan.FullLine) { TonalWeekCard(vm) }
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                Chip(S.all, !vm.pinnedOnly) { vm.pinnedOnly = false }
                Chip(S.favorites, vm.pinnedOnly) { vm.pinnedOnly = true }
            }
        }
        if (shown.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                if (all.isEmpty()) EmptyState("📝", S.emptyNotesTitle, S.emptyNotesText)
                else EmptyState("🔍", S.nothingFoundTitle, S.nothingFoundText)
            }
        }
        items(shown, key = { it.id }) { note ->
            TonalNoteCard(note, { vm.open(note) }, { vm.togglePin(note) }, Modifier.animateItem())
        }
    }
}

/** Карточка «Дневник на этой неделе» с днями Пн–Вс. */
@Composable
fun TonalWeekCard(vm: OktoViewModel) {
    val c = C
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)
    val days = vm.diary.map { it.day }.toSet()
    val count = (0..6).count { monday.plusDays(it.toLong()).toEpochDay() in days }
    Well(Modifier.fillMaxWidth()) {
        WellLabel(S.diaryThisWeek, S.ofSeven(count))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0..6) {
                val d = monday.plusDays(i.toLong())
                val has = d.toEpochDay() in days
                val isToday = d == today
                val future = d.isAfter(today)
                val (bg, fg) = when {
                    has -> c.primary to c.onPrimary
                    isToday -> c.accent2 to c.onAccent2
                    else -> c.bg to c.muted
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .then(if (!future) Modifier.clickable { vm.openDay(d) } else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        d.weekdayShort(), fontFamily = Nunito, fontSize = 13.sp,
                        fontWeight = if (has || isToday) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (future) fg.copy(alpha = 0.5f) else fg,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TonalNoteCard(note: Entry, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = T
    val nc = t.note(note.color)
    val haptic = LocalHapticFeedback.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(nc.bg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .padding(16.dp),
    ) {
        if (note.title.isNotBlank()) {
            Text(note.title, style = t.cardTitle, color = nc.fg, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (note.body.isNotBlank()) Spacer(Modifier.height(6.dp))
        }
        if (note.body.isNotBlank()) {
            Text(
                note.body, style = if (note.title.isBlank()) t.bodyText.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp) else t.bodyText,
                color = if (note.title.isBlank()) nc.fg else nc.muted, maxLines = 8, overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatStamp(note.updatedAt), style = t.caption.copy(fontSize = 12.sp), color = nc.muted, modifier = Modifier.weight(1f))
            if (note.pinned) Icon(Icons.Filled.Star, S.favorites, tint = if (note.color == 0) t.c.primary else nc.fg, modifier = Modifier.size(16.dp))
        }
    }
}

// ---------------- Okto ----------------

@Composable
private fun OktoNotes(vm: OktoViewModel, all: List<Entry>, shown: List<Entry>) {
    val t = T
    val c = t.c
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val searching = vm.searchOpen || vm.query.isNotEmpty()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top + 6.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { OktoHeader { vm.showSettings = true } }
        item {
            ScreenTitle(S.notes, todaySubtitle(), Modifier.padding(top = 4.dp, bottom = 6.dp)) {
                Text(
                    (if (searching) S.close else S.search).uppercase(),
                    fontFamily = JbMono, fontSize = 12.sp, letterSpacing = 1.6.sp, color = c.muted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (searching) {
                                vm.query = ""
                                vm.searchOpen = false
                            } else {
                                vm.searchOpen = true
                            }
                        }
                        .padding(8.dp),
                )
            }
        }
        if (searching) {
            item { SearchField(vm.query, { vm.query = it }, S.searchNotesShort, Modifier.fillMaxWidth()) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                Well(Modifier.weight(1f)) {
                    WellLabel(S.notesCount)
                    Spacer(Modifier.height(6.dp))
                    BigDigits(all.size.toString().padStart(2, '0'))
                }
                Well(Modifier.weight(1f), onClick = { vm.tab = com.okto.notes.Tab.DIARY }) {
                    WellLabel(S.diaryStreak)
                    Spacer(Modifier.height(6.dp))
                    BigDigits(vm.streak.toString().padStart(2, '0'))
                    Spacer(Modifier.height(8.dp))
                    WeekSegments(vm)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                Chip(S.all, !vm.pinnedOnly) { vm.pinnedOnly = false }
                Chip(S.favorites, vm.pinnedOnly) { vm.pinnedOnly = true }
            }
        }
        if (shown.isEmpty()) {
            item {
                if (all.isEmpty()) EmptyState("", S.emptyNotesTitle, S.emptyNotesText)
                else EmptyState("", S.nothingFoundTitle, S.nothingFoundText)
            }
        }
        items(shown, key = { it.id }) { note ->
            OktoNoteRow(note, { vm.open(note) }, { vm.togglePin(note) }, Modifier.animateItem())
        }
    }
}

/** 7 сегментов — последние семь дней, закрашены дни с записью в дневнике. */
@Composable
private fun WeekSegments(vm: OktoViewModel) {
    val c = C
    val days = vm.diary.map { it.day }.toSet()
    val today = LocalDate.now().toEpochDay()
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (i in 6 downTo 0) {
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (today - i in days) c.wellInk else c.wellGhost),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OktoNoteRow(note: Entry, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = T
    val c = t.c
    val nc = t.note(note.color)
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(nc.bg)
            .border(1.dp, nc.border, shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            val headline = note.title.ifBlank { note.body.lineSequence().firstOrNull().orEmpty() }
            Text(headline, style = t.cardTitle, color = c.onBg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val preview = if (note.title.isBlank()) note.body.lineSequence().drop(1).joinToString(" ") else note.body.replace('\n', ' ')
            if (preview.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(preview, style = t.bodyText, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(shortStamp(note.updatedAt), fontFamily = JbMono, fontSize = 12.sp, color = c.muted)
            if (note.pinned) {
                Spacer(Modifier.height(6.dp))
                Icon(Icons.Filled.Star, S.favorites, tint = c.onBg, modifier = Modifier.size(14.dp))
            }
        }
    }
}
