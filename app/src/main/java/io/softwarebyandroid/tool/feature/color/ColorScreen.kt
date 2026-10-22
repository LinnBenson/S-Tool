package io.softwarebyandroid.tool.feature.color

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import io.softwarebyandroid.tool.core.ui.*
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable fun ColorScreen(state: ColorState, model: ColorController, copy: (String)->Unit) {
    val doc=state.image
    var zoom by rememberSaveable(doc?.uri?.toString()) { mutableFloatStateOf(1f) }
    var panX by rememberSaveable(doc?.uri?.toString()) { mutableFloatStateOf(0f) }
    var panY by rememberSaveable(doc?.uri?.toString()) { mutableFloatStateOf(0f) }
    val viewport=rememberUpdatedState(Triple(zoom,panX,panY))
    Column(Modifier.fillMaxSize().padding(bottom=Space.md),verticalArrangement=Arrangement.spacedBy(Space.md)) {
        WorkspaceLayout(Modifier.weight(1f),workspace={ paneModifier ->
            Column(paneModifier,verticalArrangement=Arrangement.spacedBy(Space.sm)) {
                if(doc!=null) FileHeading(doc.name,stringResource(R.string.dimensions,doc.bitmap.width,doc.bitmap.height),Symbol.Image)
                val modifier=Modifier.fillMaxWidth().weight(1f)
                if(doc==null) EmptyContent(Symbol.Dropper,R.string.empty_image,R.string.empty_image_detail,modifier)
                else ImageCanvas(doc,state.sample,modifier,viewport,{ scale,x,y -> zoom=scale;panX=x;panY=y },model::sample)
            }
        },panel={
            ErrorNotice(state.imageError)
            BusyNotice(state.imageLoading)
            if(doc!=null) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
                    IconButton(onClick={zoom=(zoom/1.5f).coerceAtLeast(0.25f)},enabled=zoom>0.25f) {ToolIcon(Symbol.Minus,stringResource(R.string.zoom_out))}
                    Text(stringResource(R.string.zoom_percent,(zoom*100).roundToInt()),style=MaterialTheme.typography.labelLarge)
                    IconButton(onClick={zoom=(zoom*1.5f).coerceAtMost(16f)},enabled=zoom<16f) {ToolIcon(Symbol.Plus,stringResource(R.string.zoom_in))}
                    TextButton(onClick={zoom=1f;panX=0f;panY=0f}) {Text(stringResource(R.string.fit))}
                }
                ColorDetails(state.sample, copy, model::moveSample)

            }
        })
    }
}
