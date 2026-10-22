package io.softwarebyandroid.tool.feature.svg

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.ui.*
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable fun SvgScreen(state: SvgState, model: SvgController, copy: (String)->Unit, fullscreen: Boolean, setFullscreen: (Boolean)->Unit) {
    val doc=state.svg
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var zoom by rememberSaveable(doc?.uri?.toString()) { mutableFloatStateOf(1f) }
    var panX by rememberSaveable(doc?.uri?.toString()) { mutableFloatStateOf(0f) }
    var panY by rememberSaveable(doc?.uri?.toString()) { mutableFloatStateOf(0f) }
    val viewport=rememberUpdatedState(Triple(zoom,panX,panY))
    val sourceScroll=rememberLazyListState()
    val reset={zoom=1f;panX=0f;panY=0f}
    Column(Modifier.fillMaxSize().padding(bottom=Space.md),verticalArrangement=Arrangement.spacedBy(Space.md)) {
        WorkspaceLayout(Modifier.weight(1f),workspace={ paneModifier ->
            Column(paneModifier, verticalArrangement=Arrangement.spacedBy(Space.sm)) {
        if(!fullscreen) {
            if(doc!=null) {
                FileHeading(doc.name,stringResource(R.string.dimensions,doc.width,doc.height),Symbol.Vector)
                TabRow(selectedTabIndex=tab) {
                    Tab(selected=tab==0,onClick={tab=0},text={Text(stringResource(R.string.preview))})
                    Tab(selected=tab==1,onClick={tab=1},text={Text(stringResource(R.string.source_code))})
                }
            }
        }
            val modifier = Modifier.fillMaxWidth().weight(1f)
            when {
                doc==null -> EmptyContent(Symbol.Vector,R.string.empty_svg,R.string.empty_svg_detail,modifier)
                tab==1 && !fullscreen -> Surface(modifier,shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainerLow) {
                    val lines=remember(doc.source) { doc.source.lineSequence().flatMap { if(it.isEmpty()) sequenceOf("") else it.chunked(2048).asSequence() }.toList() }
                    SelectionContainer {
                        LazyColumn(Modifier.fillMaxSize().padding(Space.page),state=sourceScroll) {
                            items(lines) { Text(it,fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
                else -> SvgCanvas(doc,modifier,viewport,{ scale,x,y -> zoom=scale;panX=x;panY=y },fullscreen,setFullscreen)
            }
        }
        },panel={
            ErrorNotice(state.svgError)
            BusyNotice(state.svgLoading)
            if(doc!=null && (tab==0 || fullscreen)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
                    IconButton(onClick={zoom=(zoom/1.5f).coerceAtLeast(0.25f)},enabled=zoom>0.25f) { ToolIcon(Symbol.Minus,stringResource(R.string.zoom_out)) }
                    Text(stringResource(R.string.zoom_percent,(zoom*100).roundToInt()),style=MaterialTheme.typography.labelLarge)
                    IconButton(onClick={zoom=(zoom*1.5f).coerceAtMost(16f)},enabled=zoom<16f) { ToolIcon(Symbol.Plus,stringResource(R.string.zoom_in)) }
                    TextButton(onClick=reset) { Text(stringResource(R.string.fit)) }
                }
                Text(stringResource(R.string.zoom_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if(!fullscreen) {
                if(doc?.warning==true) Text(stringResource(R.string.svg_warning),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                if(doc!=null && tab==1 && doc.source.length<=128_000) TextButton(onClick={copy(doc.source)}) {ToolIcon(Symbol.Copy); Spacer(Modifier.width(Space.sm));Text(stringResource(R.string.copy_source))}
                BusyNotice(state.exporting,R.string.exporting)
                if(doc!=null) Text(stringResource(R.string.export_note),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        })
    }
}
