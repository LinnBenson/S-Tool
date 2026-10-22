package io.softwarebyandroid.tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.softwarebyandroid.tool.core.ui.*

import io.softwarebyandroid.tool.app.*
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    val model: AppViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(android.os.Build.VERSION.SDK_INT>=33) setRecentsScreenshotEnabled(false)
        enableEdgeToEdge()
        setContent {
            val theme by model.theme.collectAsStateWithLifecycle()
            SToolTheme(theme) {
                val dark=MaterialTheme.colorScheme.background.luminance()<0.5f
                SideEffect {
                    enableEdgeToEdge(statusBarStyle=SystemBarStyle.auto(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT){dark},
                        navigationBarStyle=SystemBarStyle.auto(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT){dark})
                }
                val fold by remember { androidx.window.layout.WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity).map { info ->
                        info.displayFeatures.filterIsInstance<androidx.window.layout.FoldingFeature>().firstNotNullOfOrNull { it.toPosture() }
                    } }.collectAsStateWithLifecycle(initialValue = null)
                CompositionLocalProvider(LocalFoldPosture provides fold) { ToolApp(model,theme) }
            }
        }
    }
}
