package com.okto.notes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import com.okto.notes.ui.App
import com.okto.notes.ui.OktoTheme
import com.okto.notes.ui.buildTheme
import com.okto.notes.ui.stringsFor

class MainActivity : ComponentActivity() {
    private lateinit var vm: OktoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this)[OktoViewModel::class.java]
        setContent {
            val theme = remember(vm.settings) { buildTheme(vm.settings) }
            // Иконки статус-бара: светлые на тёмной теме, тёмные на светлой.
            LaunchedEffect(theme.c.dark) {
                val style = if (theme.c.dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            OktoTheme(theme, stringsFor(vm.settings.lang)) { App(vm) }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.flush()
    }
}
