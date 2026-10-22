package io.softwarebyandroid.tool.feature.watermark

import android.graphics.Paint
import android.graphics.Picture
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import io.softwarebyandroid.tool.core.ui.*

@Composable internal fun WatermarkCanvas(image: ImageDocument,overlay: Picture?,modifier: Modifier,
    viewport: State<Triple<Float,Float,Float>>,transform: (Float,Float,Float)->Unit) {
    val paint=remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val description=stringResource(R.string.watermark_preview)
    Canvas(modifier.clip(MaterialTheme.shapes.medium).semantics {
        contentDescription=description;this[PreviewTransform]=viewport.value.let { listOf(it.first,it.second,it.third) }
    }.pointerInput(image) { previewGestures({viewport.value},transform) }) {
        checkerboard()
        val (zoom,x,y)=viewport.value
        val rect=fitRect(image.bitmap.width,image.bitmap.height,size.width,size.height).transformed(size.width,size.height,zoom,x,y)
        drawIntoCanvas {
            val canvas=it.nativeCanvas
            canvas.save()
            canvas.translate(rect.left,rect.top)
            canvas.scale(rect.width/image.bitmap.width,rect.height/image.bitmap.height)
            canvas.clipRect(0,0,image.bitmap.width,image.bitmap.height)
            canvas.drawBitmap(image.bitmap,0f,0f,paint)
            overlay?.let(canvas::drawPicture)
            canvas.restore()
        }
    }
}
