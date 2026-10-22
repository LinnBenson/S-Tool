package io.softwarebyandroid.tool.feature.watermark

import android.content.ContentResolver
import android.graphics.*
import android.net.Uri
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import java.io.FilterOutputStream
import kotlin.math.roundToInt

class WatermarkRenderer(private val resolver: ContentResolver) {
    suspend fun record(width: Int,height: Int,options: WatermarkOptions): Picture = withContext(Dispatchers.Default) {
        mediaWork.withLock {
            if(!options.valid) throw ToolFailure(R.string.watermark_render_error)
            val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color=options.color!!;alpha=(options.opacity*255).roundToInt();textSize=options.fontSize
                typeface=Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL)
            }
            val metrics=paint.fontMetrics
            val grid=watermarkGrid(width,height,paint.measureText(options.text),metrics.bottom-metrics.top,options)
                ?: throw ToolFailure(R.string.watermark_density)
            val picture=Picture()
            val canvas=picture.beginRecording(width,height)
            try {
                canvas.clipRect(0,0,width,height)
                canvas.translate(width/2f,height/2f)
                canvas.rotate(options.angle)
                for(row in 0 until grid.rows) {
                    currentCoroutineContext().ensureActive()
                    for(column in 0 until grid.columns) {
                        canvas.drawText(options.text,grid.left+column*grid.stepX,grid.top+row*grid.stepY-metrics.top,paint)
                    }
                }
            } finally { picture.endRecording() }
            picture
        }
    }
    suspend fun export(image: ImageDocument, overlay: Picture, destination: Uri) = withContext(Dispatchers.IO) {
        mediaWork.withLock {
            val job=currentCoroutineContext();job.ensureActive()
            checkBitmapBudget(image.bitmap.width,image.bitmap.height)
            val result=Bitmap.createBitmap(image.bitmap.width,image.bitmap.height,Bitmap.Config.ARGB_8888)
            try {
                val canvas=Canvas(result)
                canvas.drawBitmap(image.bitmap,0f,0f,null)
                canvas.drawPicture(overlay)
                resolver.openOutputStream(destination,"wt")?.use { output ->
                    val stream=object: FilterOutputStream(output) {
                        override fun write(bytes: ByteArray,offset: Int,length: Int) { job.ensureActive();out.write(bytes,offset,length) }
                        override fun write(value: Int) { job.ensureActive();out.write(value) }
                    }
                    if(!result.compress(Bitmap.CompressFormat.PNG,100,stream)) throw ToolFailure(R.string.export_error)
                } ?: throw ToolFailure(R.string.export_error)
            } finally { result.recycle() }
        }
    }
}
