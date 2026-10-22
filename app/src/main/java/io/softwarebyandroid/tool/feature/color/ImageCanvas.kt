package io.softwarebyandroid.tool.feature.color

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import io.softwarebyandroid.tool.core.ui.*

@Composable internal fun ImageCanvas(doc: ImageDocument, sample: Sample?, modifier: Modifier,
    viewport: State<Triple<Float,Float,Float>>, transform: (Float,Float,Float)->Unit, pick: (Int,Int)->Unit) {
    val bitmap=remember(doc){doc.bitmap.asImageBitmap()}
    val paint=remember { android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG) }
    val description=stringResource(R.string.image_accessibility)
    Canvas(modifier.clip(MaterialTheme.shapes.medium).semantics{contentDescription=description; this[PreviewTransform]=viewport.value.let { listOf(it.first,it.second,it.third) }}
        .pointerInput(doc.uri) {
            previewGestures({ viewport.value }, transform) { point, view ->
                val (zoom,panX,panY)=view
                fitRect(bitmap.width,bitmap.height,size.width.toFloat(),size.height.toFloat())
                    .transformed(size.width.toFloat(),size.height.toFloat(),zoom,panX,panY)
                    .pixel(point.x,point.y,bitmap.width,bitmap.height)?.let { pick(it.first,it.second) }
            }
        }) {
        checkerboard()
        val (zoom,panX,panY)=viewport.value
        val rect=fitRect(bitmap.width,bitmap.height,size.width,size.height).transformed(size.width,size.height,zoom,panX,panY)
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawBitmap(doc.bitmap,null,
                android.graphics.RectF(rect.left,rect.top,rect.left+rect.width,rect.top+rect.height),paint)
        }
        if(sample!=null) {
            val point=Offset(rect.left+(sample.x+0.5f)/bitmap.width*rect.width,rect.top+(sample.y+0.5f)/bitmap.height*rect.height)
            val arm=10.dp.toPx()
            fun cross(at: Offset) {
                for((color,stroke) in listOf(Color.Black to 5.dp.toPx(),Color.White to 2.dp.toPx())) {
                    drawLine(color,at-Offset(arm,0f),at+Offset(arm,0f),strokeWidth=stroke)
                    drawLine(color,at-Offset(0f,arm),at+Offset(0f,arm),strokeWidth=stroke)
                }
            }
            cross(point)
            // A 9x9 source-pixel loupe; nearest neighbour cells show the actual sampled pixel.
            val cell=9.dp.toPx();val edge=cell*9;val padding=12.dp.toPx()
            val origin=Offset(if(point.x<size.width/2)size.width-edge-padding else padding,padding)
            for(row in 0..8)for(col in 0..8) {
                val px=(sample.x+col-4).coerceIn(0,bitmap.width-1)
                val py=(sample.y+row-4).coerceIn(0,bitmap.height-1)
                val at=origin+Offset(col*cell,row*cell)
                drawRect(if((row+col)%2==0)Color.White else Color.LightGray,at,Size(cell,cell))
                drawRect(Color(doc.bitmap.getPixel(px,py)),at,Size(cell,cell))
                drawRect(Color.White.copy(alpha=0.2f),at,Size(cell,cell),style=Stroke(0.5.dp.toPx()))
            }
            drawRect(Color.White,origin,Size(edge,edge),style=Stroke(2.dp.toPx()))
            drawRect(Color.Black,origin+Offset(4*cell,4*cell),Size(cell,cell),style=Stroke(2.dp.toPx()))
            drawRect(Color.White,origin+Offset(4*cell,4*cell),Size(cell,cell),style=Stroke(1.dp.toPx()))
        }
    }
}
