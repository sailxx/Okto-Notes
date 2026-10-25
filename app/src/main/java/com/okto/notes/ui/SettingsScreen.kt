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
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.okto.notes.SyncStatus
import java.text.DateFormat
import java.util.Date
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okto.notes.OktoViewModel
import com.okto.notes.data.Lang

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
            IconBtn(Icons.AutoMirrored.Filled.ArrowBack, S.back, { vm.showSettings = false })
            Spacer(Modifier.width(14.dp))
            Text(t.cap(S.settings), style = if (t.okto) t.keyLabel.copy(fontSize = 14.sp, letterSpacing = 2.sp) else t.h1.copy(fontSize = 26.sp))
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (vm.cloudAvailable) SyncSection(vm)

            SectionTitle(S.language, Modifier.padding(start = 4.dp, top = 12.dp))
            Row(Modifier.padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Названия языков — всегда на самих языках, чтобы свой находился в любом интерфейсе.
                Chip("Русский", s.lang == Lang.RU) { vm.updateSettings { it.copy(lang = Lang.RU) } }
                Chip("English", s.lang == Lang.EN) { vm.updateSettings { it.copy(lang = Lang.EN) } }
            }

            SectionTitle(S.about, Modifier.padding(start = 4.dp, top = 12.dp))
            Text(
                S.aboutText("1.6"),
                style = t.bodyText, color = c.muted, modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Связь с Okto: вход через Google и состояние обмена заметками. */
@Composable
private fun SyncSection(vm: OktoViewModel) {
    val t = T
    val c = t.c
    val activity = LocalContext.current as Activity
    val shape = RoundedCornerShape(if (t.okto) 12.dp else 24.dp)
    val status = vm.syncStatus
    val signedIn = status != SyncStatus.OFF
    SectionTitle(S.syncTitle, Modifier.padding(start = 4.dp, top = 12.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(S.syncSub, style = t.bodyText, color = c.muted)
        if (signedIn) {
            Text(S.syncedAs(vm.syncAccount), style = t.cardTitle, color = c.onBg)
            val line = when (status) {
                SyncStatus.SYNCING -> S.syncing
                SyncStatus.ERROR -> S.syncFailed
                SyncStatus.OFFLINE -> S.syncOffline
                SyncStatus.EXPIRED -> S.syncSignedOut
                else -> if (vm.lastSyncAt > 0) S.syncedAt(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(vm.lastSyncAt))) else ""
            }
            if (line.isNotEmpty()) Text(line, style = t.bodyText, color = if (status == SyncStatus.IDLE || status == SyncStatus.SYNCING) c.muted else c.danger)
            Text(S.syncNote, style = t.bodyText, color = c.muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status == SyncStatus.EXPIRED) Btn(S.syncSignIn, { vm.signIn(activity) }, primary = true)
                else Btn(S.syncNow, { vm.requestSync() }, primary = true)
                Btn(S.syncSignOut, { vm.signOut() })
            }
        } else {
            Btn(S.syncSignIn, { vm.signIn(activity) }, primary = true)
        }
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
