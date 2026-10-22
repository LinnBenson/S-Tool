package io.softwarebyandroid.tool.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.testTag
import io.softwarebyandroid.tool.core.data.*

@Composable fun WorkspaceLayout(modifier: Modifier=Modifier, workspace: @Composable (Modifier)->Unit, stackedPanelMaxHeight: Dp? = null, stableStackedPanel: Boolean = false, panel: @Composable ()->Unit) {
    val density = LocalDensity.current
    val fold = LocalFoldPosture.current
    var origin by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInWindow() }) {
        val availableHeight = maxHeight
        val offset = if (fold?.vertical == true) origin.x else origin.y
        val plan = planPanes(maxWidth.value, maxHeight.value, fold?.vertical,
            ((fold?.start ?: 0) - offset) / density.density,
            ((fold?.end ?: 0) - offset) / density.density, density.fontScale)
        val controls: @Composable (Modifier) -> Unit = { paneModifier ->
            Column(paneModifier.testTag("controls-pane").verticalScroll(rememberScrollState()),
                verticalArrangement=Arrangement.spacedBy(Space.md)) { panel() }
        }
        val content: @Composable (Modifier) -> Unit = { workspace(it.testTag("workspace-pane")) }
        when (plan.mode) {
            PaneMode.BOOK, PaneMode.SIDE_BY_SIDE -> Row(Modifier.fillMaxSize()) {
                content(Modifier.width(plan.first.dp).fillMaxHeight())
                Spacer(Modifier.width(plan.gap.dp))
                controls(Modifier.weight(1f).fillMaxHeight())
            }
            PaneMode.TABLETOP -> Column(Modifier.fillMaxSize()) {
                content(Modifier.fillMaxWidth().height(plan.first.dp))
                Spacer(Modifier.height(plan.gap.dp))
                controls(Modifier.fillMaxWidth().weight(1f))
            }
            PaneMode.STACKED -> if (!stableStackedPanel && (maxHeight < 540.dp || density.fontScale > 1.3f)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(Space.md)) {
                    content(Modifier.fillMaxWidth().height(400.dp))
                    Column(Modifier.testTag("controls-pane"),verticalArrangement=Arrangement.spacedBy(Space.md)) { panel() }
                }
            } else Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(Space.md)) {
                content(Modifier.fillMaxWidth().weight(1f))
                if(stackedPanelMaxHeight!=null) controls(Modifier.fillMaxWidth().heightIn(max=if(stableStackedPanel) minOf(stackedPanelMaxHeight,availableHeight * .55f) else stackedPanelMaxHeight))
                else Column(Modifier.testTag("controls-pane"),verticalArrangement=Arrangement.spacedBy(Space.md)) { panel() }
            }
        }
    }
}
