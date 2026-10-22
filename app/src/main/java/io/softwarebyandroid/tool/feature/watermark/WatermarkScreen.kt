package io.softwarebyandroid.tool.feature.watermark

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.ui.*
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable fun WatermarkScreen(state: WatermarkState,model: WatermarkController) {
    val image=state.image
    var zoom by androidx.compose.runtime.saveable.rememberSaveable(image?.uri?.toString()) {mutableFloatStateOf(1f)}
    var x by androidx.compose.runtime.saveable.rememberSaveable(image?.uri?.toString()) {mutableFloatStateOf(0f)}
    var y by androidx.compose.runtime.saveable.rememberSaveable(image?.uri?.toString()) {mutableFloatStateOf(0f)}
    val viewport=rememberUpdatedState(Triple(zoom,x,y))
    WorkspaceLayout(Modifier.fillMaxSize().padding(bottom=Space.md),stackedPanelMaxHeight=320.dp,stableStackedPanel=true,workspace={ modifier ->
        Column(modifier,verticalArrangement=Arrangement.spacedBy(Space.sm)) {
            if(image==null) EmptyContent(Symbol.Watermark,R.string.watermark_empty,R.string.watermark_empty_detail,Modifier.fillMaxSize())
            else {
                FileHeading(image.name,stringResource(R.string.dimensions,image.bitmap.width,image.bitmap.height),Symbol.Image)
                WatermarkCanvas(image,state.overlay,Modifier.fillMaxWidth().weight(1f),viewport,{s,px,py ->zoom=s;x=px;y=py})
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
                    IconButton(onClick={zoom=(zoom/1.5f).coerceAtLeast(.25f)}) {ToolIcon(Symbol.Minus,stringResource(R.string.zoom_out))}
                    Text(stringResource(R.string.zoom_percent,(zoom*100).roundToInt()),style=MaterialTheme.typography.labelLarge)
                    IconButton(onClick={zoom=(zoom*1.5f).coerceAtMost(16f)}) {ToolIcon(Symbol.Plus,stringResource(R.string.zoom_in))}
                    TextButton(onClick={zoom=1f;x=0f;y=0f}) {Text(stringResource(R.string.fit))}
                }
            }
        }
    },panel={
        ErrorNotice(state.error)
        BusyNotice(state.loading)
        Box(Modifier.fillMaxWidth().height(24.dp)) {
            if(state.rendering) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.Center))
        }
        BusyNotice(state.exporting,R.string.exporting)
        WatermarkControls(state,model::update)
    })
}
