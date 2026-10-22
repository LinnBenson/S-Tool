package io.softwarebyandroid.tool.feature.svg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.ui.*
import kotlin.math.min

@Composable internal fun SvgCanvas(doc: SvgDocument, modifier: Modifier,
    viewport: State<Triple<Float,Float,Float>>, transform: (Float,Float,Float)->Unit,
    fullscreen: Boolean, setFullscreen: (Boolean)->Unit) {
    Box(modifier.clip(MaterialTheme.shapes.medium)) {
        val description=stringResource(R.string.svg_accessibility)
        Canvas(Modifier.fillMaxSize().semantics{contentDescription=description; this[PreviewTransform]=viewport.value.let { listOf(it.first,it.second,it.third) }}
            .pointerInput(doc.uri) {
                previewGestures({ viewport.value }, transform)
            }) {
            val (zoom,panX,panY)=viewport.value
            checkerboard()
            val fit=min((size.width-32.dp.toPx()).coerceAtLeast(1f)/doc.width,(size.height-32.dp.toPx()).coerceAtLeast(1f)/doc.height)
            drawIntoCanvas { canvas ->
                val native=canvas.nativeCanvas
                native.save()
                native.translate(size.width/2+panX,size.height/2+panY)
                native.scale(fit*zoom,fit*zoom)
                native.translate(-doc.width/2f,-doc.height/2f)
                native.drawPicture(doc.picture)
                native.restore()
            }
        }
        FilledTonalIconButton(onClick={setFullscreen(!fullscreen)},modifier=Modifier.align(Alignment.TopEnd).padding(Space.sm)) {
            ToolIcon(if(fullscreen) Symbol.Close else Symbol.Fit,stringResource(if(fullscreen) R.string.exit_fullscreen else R.string.fullscreen))
        }
    }
}
