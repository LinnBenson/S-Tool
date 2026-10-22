package io.softwarebyandroid.tool.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.core.ui.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow

import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.app.Page
@Composable fun HomeScreen(navigate: (Page)->Unit) {
    val fold=LocalFoldPosture.current
    val density=androidx.compose.ui.platform.LocalDensity.current
    var origin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    BoxWithConstraints(Modifier.fillMaxSize().onGloballyPositioned { origin=it.positionInWindow() }) {
        val wide=maxWidth>=600.dp && density.fontScale<=1.5f
        val shift=if(fold?.vertical==true)origin.x else origin.y
        val plan=io.softwarebyandroid.tool.core.data.planPanes(maxWidth.value,maxHeight.value,fold?.vertical,
            ((fold?.start?:0)-shift)/density.density,((fold?.end?:0)-shift)/density.density,density.fontScale)
        val title: @Composable ()->Unit = {
            Text(stringResource(R.string.all_tools),style=MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.tagline),style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val first: @Composable ()->Unit = { ToolCard(R.string.svg_title,R.string.svg_description,Symbol.Vector){navigate(Page.SVG)} }
        val second: @Composable ()->Unit = { ToolCard(R.string.color_title,R.string.color_description,Symbol.Dropper){navigate(Page.COLOR)} }
        val third: @Composable ()->Unit = { ToolCard(R.string.watermark_title,R.string.watermark_description,Symbol.Watermark){navigate(Page.WATERMARK)} }
        if(plan.mode==io.softwarebyandroid.tool.core.data.PaneMode.BOOK) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.width(plan.first.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical=Space.page),verticalArrangement=Arrangement.spacedBy(Space.page)) { title(); first();third() }
                Spacer(Modifier.width(plan.gap.dp))
                Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical=Space.page)) { second() }
            }
        } else if(plan.mode==io.softwarebyandroid.tool.core.data.PaneMode.TABLETOP) {
            Column {
                Column(Modifier.fillMaxWidth().height(plan.first.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(Space.page)) { title();first() }
                Spacer(Modifier.height(plan.gap.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(Space.page)) { second();third() }
            }
        } else Column(Modifier.widthIn(max=1120.dp).fillMaxWidth().align(Alignment.TopCenter).verticalScroll(rememberScrollState()).padding(vertical=Space.section),verticalArrangement=Arrangement.spacedBy(Space.page)) {
            title()
            if(wide) Row(horizontalArrangement=Arrangement.spacedBy(Space.section)) {
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Space.page)) { first();third() }; Box(Modifier.weight(1f)) { second() }
            } else { first();second();third() }
        }
    }
}
@Composable private fun ToolCard(title: Int, detail: Int, symbol: Symbol, onClick: ()->Unit) {
    Card(onClick=onClick,colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainer),modifier=Modifier.fillMaxWidth()) {
        Row(Modifier.padding(Space.page).heightIn(min=64.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Space.page)) {
            Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=MaterialTheme.shapes.medium) {
                ToolIcon(symbol,modifier=Modifier.padding(Space.page).size(28.dp),tint=MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Space.xs)) {
                Text(stringResource(title),style=MaterialTheme.typography.titleLarge)
                Text(stringResource(detail),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ToolIcon(Symbol.Chevron)
        }
    }
}
