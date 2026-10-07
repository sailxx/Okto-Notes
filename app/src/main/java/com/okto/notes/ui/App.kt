package com.okto.notes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.okto.notes.OktoViewModel
import com.okto.notes.Tab
import com.okto.notes.data.Entry
import kotlinx.coroutines.delay

private enum class Screen { HOME, EDITOR, SETTINGS }

@Composable
fun App(vm: OktoViewModel) {
    val editing = vm.editing
    // Держим последнюю открытую запись, чтобы редактор доиграл анимацию закрытия.
    var lastEditing by remember { mutableStateOf<Entry?>(null) }
    if (editing != null) lastEditing = editing

    val screen = when {
        editing != null -> Screen.EDITOR
        vm.showSettings -> Screen.SETTINGS
        else -> Screen.HOME
    }

    BackHandler(enabled = screen != Screen.HOME) {
        if (editing != null) vm.closeEditor() else vm.showSettings = false
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            if (targetState != Screen.HOME) {
                (fadeIn(tween(220)) + scaleIn(tween(260), initialScale = 0.94f)) togetherWith fadeOut(tween(160))
            } else {
                fadeIn(tween(220)) togetherWith (fadeOut(tween(180)) + scaleOut(tween(220), targetScale = 0.94f))
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(C.bg),
        label = "screen",
    ) { s ->
        when (s) {
            Screen.EDITOR -> (vm.editing ?: lastEditing)?.let { EditorScreen(it, vm) }
            Screen.SETTINGS -> SettingsScreen(vm)
            Screen.HOME -> HomeScreen(vm)
        }
    }
}

@Composable
private fun HomeScreen(vm: OktoViewModel) {
    val t = T
    Box(
        Modifier
            .fillMaxSize()
            .background(t.c.bg),
    ) {
        Crossfade(targetState = vm.tab, label = "tab") { tab ->
            when (tab) {
                Tab.NOTES -> NotesTab(vm)
                Tab.DIARY -> DiaryTab(vm)
            }
        }

        val onAdd = { if (vm.tab == Tab.NOTES) vm.newNote() else vm.newDiary() }
        NavBar(vm.tab, { vm.tab = it }, onAdd, Modifier.align(Alignment.BottomCenter))
        if (!t.okto) {
            ExtendedFab(
                if (vm.tab == Tab.NOTES) "Новая" else "Запись", onAdd,
                Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 96.dp),
            )
        }

        val deleted = vm.recentlyDeleted
        LaunchedEffect(deleted) {
            if (deleted != null) {
                delay(4000)
                vm.clearUndo()
            }
        }
        AnimatedVisibility(
            visible = deleted != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (t.okto) 100.dp else 172.dp),
        ) {
            UndoBar(onUndo = vm::undoDelete)
        }
    }
}
