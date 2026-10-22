package io.softwarebyandroid.tool.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.core.data.ThemeMode
import io.softwarebyandroid.tool.core.ui.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow

import io.softwarebyandroid.tool.R
@Composable fun SettingsScreen(theme: ThemeMode, select: (ThemeMode)->Unit) {
    val fold=LocalFoldPosture.current
    val density=androidx.compose.ui.platform.LocalDensity.current
    var origin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    BoxWithConstraints(Modifier.fillMaxSize().onGloballyPositioned { origin=it.positionInWindow() }) {
    val safeWidth=if(fold?.vertical==true) ((fold.start-origin.x)/density.density-12).coerceIn(80f,maxWidth.value).dp else maxWidth
    val safeHeight=if(fold?.vertical==false) ((fold.start-origin.y)/density.density-12).coerceIn(80f,maxHeight.value).dp else maxHeight
    Column(Modifier.widthIn(max=600.dp).width(safeWidth).height(safeHeight).verticalScroll(rememberScrollState()).padding(vertical=Space.page),verticalArrangement=Arrangement.spacedBy(Space.page)) {
        Text(stringResource(R.string.appearance),style=MaterialTheme.typography.titleMedium)
        ThemeMode.entries.forEach { mode ->
            Surface(onClick={select(mode)},shape=MaterialTheme.shapes.medium,color=if(theme==mode)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(Modifier.fillMaxWidth().padding(Space.sm),verticalAlignment=Alignment.CenterVertically) {
                    RadioButton(selected=theme==mode,onClick={select(mode)})
                    Text(stringResource(when(mode){ThemeMode.SYSTEM->R.string.system_theme;ThemeMode.LIGHT->R.string.light_theme;ThemeMode.DARK->R.string.dark_theme}))
                }
            }
        }
        Spacer(Modifier.height(Space.page))
        Text(stringResource(R.string.local_only),color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.version),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

}
