package io.softwarebyandroid.tool.app

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.softwarebyandroid.tool.core.data.ThemeMode
import io.softwarebyandroid.tool.core.ui.*
import io.softwarebyandroid.tool.feature.svg.SvgActions
import io.softwarebyandroid.tool.feature.color.ColorActions
import io.softwarebyandroid.tool.feature.watermark.WatermarkActions
import io.softwarebyandroid.tool.feature.svg.SvgScreen
import io.softwarebyandroid.tool.feature.color.ColorScreen
import io.softwarebyandroid.tool.feature.watermark.WatermarkScreen
import kotlinx.coroutines.launch

import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.feature.home.HomeScreen
import io.softwarebyandroid.tool.feature.settings.SettingsScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ToolApp(model: AppViewModel, theme: ThemeMode) {
    val pageName by model.page.collectAsStateWithLifecycle()
    val page=Page.valueOf(pageName)
    val svgState by model.svg.state.collectAsStateWithLifecycle()
    val colorState by model.color.state.collectAsStateWithLifecycle()
    val watermarkState by model.watermark.state.collectAsStateWithLifecycle()
    val snackbar=remember { SnackbarHostState() }
    val scope=rememberCoroutineScope()
    val context=LocalContext.current
    val holder=rememberSaveableStateHolder()
    var fullscreen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(page) { if(page!=Page.SVG) fullscreen=false }
    LaunchedEffect(model) { model.events.collect { snackbar.showSnackbar(context.getString(it)) } }
    val copy: (String)->Unit = { text ->
        val clipboard=context.getSystemService(ClipboardManager::class.java)
        val clip=ClipData.newPlainText(context.getString(R.string.app_name),text)
        clip.description.extras=android.os.PersistableBundle().apply { putBoolean("android.content.extra.IS_SENSITIVE",true) }
        clipboard.setPrimaryClip(clip)
        scope.launch { snackbar.showSnackbar(context.getString(R.string.copied)) }
    }
    BackHandler(page!=Page.HOME || fullscreen) { if(fullscreen) fullscreen=false else model.navigate(Page.HOME) }
    Scaffold(
        topBar={ if(!fullscreen) TopAppBar(title={Text(stringResource(when(page){Page.HOME->R.string.app_name;Page.SVG->R.string.svg_title;Page.COLOR->R.string.color_title;Page.WATERMARK->R.string.watermark_title;Page.SETTINGS->R.string.settings}))},
            navigationIcon={ if(page!=Page.HOME) IconButton(onClick={model.navigate(Page.HOME)}){ToolIcon(Symbol.Back,stringResource(R.string.back))} },
            actions={ when(page) {
                Page.HOME -> IconButton(onClick={model.navigate(Page.SETTINGS)}){ToolIcon(Symbol.Settings,stringResource(R.string.settings))}
                Page.SVG -> SvgActions(svgState,model.svg)
                Page.COLOR -> ColorActions(colorState,model.color,copy)
                Page.WATERMARK -> WatermarkActions(watermarkState,model.watermark)
                Page.SETTINGS -> Unit
            } }) },
        snackbarHost={if(page!=Page.COLOR) SnackbarHost(snackbar)}
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal=Space.page),contentAlignment=Alignment.TopCenter) {
            holder.SaveableStateProvider(page.name) {
                when(page) {
                    Page.HOME -> HomeScreen { model.navigate(it) }
                    Page.SETTINGS -> SettingsScreen(theme,model::setTheme)
                    Page.SVG -> SvgScreen(svgState,model.svg,copy,fullscreen,{fullscreen=it})
                    Page.COLOR -> ColorScreen(colorState,model.color,copy)
                    Page.WATERMARK -> WatermarkScreen(watermarkState,model.watermark)
                }
            }
            if(page==Page.COLOR) SnackbarHost(snackbar,Modifier.align(Alignment.TopCenter))
        }
    }
}
