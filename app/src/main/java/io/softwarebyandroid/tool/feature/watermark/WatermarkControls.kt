package io.softwarebyandroid.tool.feature.watermark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.ui.*
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun WatermarkControls(state: WatermarkState,update: (WatermarkOptions)->Unit) {
    val options=state.options
    val image=state.image?:return
    val edge=minOf(image.bitmap.width,image.bitmap.height).toFloat()
    val limits=remember(image) { Triple(maxOf(2f,edge*.5f,options.fontSize),maxOf(1f,edge,options.gapX),maxOf(1f,edge,options.gapY)) }
    OutlinedTextField(value=options.text,onValueChange={ raw ->
        val text=raw.replace('\n',' ').replace('\r',' ')
        update(options.copy(text=text.substring(0,text.offsetByCodePoints(0,minOf(64,text.codePointCount(0,text.length))))))
    },label={Text(stringResource(R.string.watermark_text))},singleLine=true,enabled=!state.exporting,
        isError=options.text.isBlank(),supportingText={Text(stringResource(if(options.text.isBlank())R.string.watermark_text_error else R.string.watermark_text_hint))},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(value=options.colorHex,onValueChange={update(options.copy(colorHex=it.take(7)))},
        label={Text(stringResource(R.string.watermark_color))},singleLine=true,enabled=!state.exporting,isError=options.color==null,
        supportingText={Text(if(options.color==null) stringResource(R.string.watermark_color_error) else " ")},
        keyboardOptions=KeyboardOptions(capitalization=KeyboardCapitalization.Characters),modifier=Modifier.fillMaxWidth())
    FlowRow(horizontalArrangement=Arrangement.spacedBy(Space.sm)) {
        for(hex in listOf("#FFFFFF","#000000","#006B5B","#FF5252")) {
            val description=stringResource(R.string.watermark_preset,hex)
            OutlinedButton(onClick={update(options.copy(colorHex=hex))},enabled=!state.exporting,
                modifier=Modifier.semantics{contentDescription=description},contentPadding=PaddingValues(Space.sm)) {
                Surface(Modifier.size(20.dp),color=Color(android.graphics.Color.parseColor(hex)),shape=MaterialTheme.shapes.small,
                    border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outline)) {}
            }
        }
    }
    Parameter(R.string.watermark_font,options.fontSize,1f..limits.first,R.string.watermark_px,!state.exporting) {update(options.copy(fontSize=it))}
    Parameter(R.string.watermark_gap_x,options.gapX,0f..limits.second,R.string.watermark_px,!state.exporting) {update(options.copy(gapX=it))}
    Parameter(R.string.watermark_gap_y,options.gapY,0f..limits.third,R.string.watermark_px,!state.exporting) {update(options.copy(gapY=it))}
    Parameter(R.string.watermark_opacity,options.opacity*100,0f..100f,R.string.zoom_percent,!state.exporting) {update(options.copy(opacity=it/100))}
    Parameter(R.string.watermark_angle,options.angle,-90f..90f,R.string.watermark_degrees,!state.exporting) {update(options.copy(angle=it))}
    Text(stringResource(R.string.watermark_note),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
}
@Composable private fun Parameter(label: Int,value: Float,range: ClosedFloatingPointRange<Float>,format: Int,enabled: Boolean,change: (Float)->Unit) {
    val description=stringResource(label)
    Column {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(description,style=MaterialTheme.typography.labelLarge)
            Text(stringResource(format,value.roundToInt()),style=MaterialTheme.typography.labelLarge)
        }
        Slider(value=value.coerceIn(range),onValueChange={change(it.roundToInt().toFloat())},valueRange=range,enabled=enabled,
            modifier=Modifier.semantics{contentDescription=description})
    }
}
