package io.softwarebyandroid.tool.core.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImageDocument(val uri: Uri, val name: String, val bitmap: Bitmap)
class ImageRepository(private val resolver: ContentResolver) {
    suspend fun load(uri: Uri): ImageDocument {
        var decoded: Bitmap? = null
        try {
            return withContext(Dispatchers.IO) {
                mediaWork.withLock {
                    currentCoroutineContext().ensureActive()
                    val bitmap = try {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                            if(info.size.width.toLong()*info.size.height>16_000_000) throw ToolFailure(R.string.image_limit)
                            checkBitmapBudget(info.size.width,info.size.height)
                            decoder.allocator=ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                            decoder.setOnPartialImageListener { false }
                        }
                    } catch(e: ToolFailure) { throw e }
                    catch(e: CancellationException) { throw e }
                    catch(_: Exception) { throw ToolFailure(R.string.image_invalid) }
                    decoded=bitmap
                    currentCoroutineContext().ensureActive()
                    // Keep full-resolution sRGB pixels; do not retain a second preview bitmap.
                    ImageDocument(uri,resolver.fileName(uri),bitmap)
                }
            }
        } catch(error: Throwable) {
            // A cancelled withContext can discard the result after native decoding completed.
            decoded?.recycle()
            throw error
        }
    }
}
