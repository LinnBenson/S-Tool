package io.softwarebyandroid.tool.feature.svg

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.RectF
import android.net.Uri
import com.caverock.androidsvg.SVG
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import kotlin.math.ceil
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.withLock
import java.io.FilterOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Keep the recorded vector drawing so zooming never enlarges a raster preview.
data class SvgDocument(val uri: Uri, val name: String, val source: String, val width: Int,
    val height: Int, val picture: Picture, val warning: Boolean)

class SvgRepository(private val resolver: ContentResolver) {
    suspend fun load(uri: Uri): SvgDocument = withContext(Dispatchers.IO) {
        mediaWork.withLock {
        currentCoroutineContext().ensureActive()
        val bytes = resolver.readLimited(uri, 4 * 1024 * 1024)
        val source = when {
            bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> bytes.toString(Charsets.UTF_16LE).removePrefix("\uFEFF")
            bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> bytes.toString(Charsets.UTF_16BE).removePrefix("\uFEFF")
            else -> bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF")
        }
        if (Regex("<!ENTITY", RegexOption.IGNORE_CASE).containsMatchIn(source)) throw ToolFailure(R.string.svg_entities)
        // Disable DTD/entity expansion; never resolve network or local file references.
        SVG.setInternalEntitiesEnabled(false)
        currentCoroutineContext().ensureActive()
        val svg = try { SVG.getFromString(source) } catch (_: Exception) { throw ToolFailure(R.string.svg_invalid) }
        if (svg.documentWidth == 0f || svg.documentHeight == 0f) throw ToolFailure(R.string.svg_dimensions)
        val box = svg.documentViewBox
        val ratio = box?.let { it.width() / it.height() }?.takeIf { it.isFinite() && it > 0 }
        val declaredWidth = svg.documentWidth.takeIf { it.isFinite() && it > 0 }
        val declaredHeight = svg.documentHeight.takeIf { it.isFinite() && it > 0 }
        val rawWidth = declaredWidth ?: if (declaredHeight != null && ratio != null) declaredHeight * ratio else box?.width() ?: 300f
        val rawHeight = declaredHeight ?: if (declaredWidth != null && ratio != null) declaredWidth / ratio else box?.height() ?: 150f
        if (!rawWidth.isFinite() || !rawHeight.isFinite()) throw ToolFailure(R.string.svg_dimensions)
        val width = ceil(rawWidth).toInt()
        val height = ceil(rawHeight).toInt()
        if (!validDimensions(width, height)) throw ToolFailure(R.string.svg_dimensions)
        currentCoroutineContext().ensureActive()
        val picture = try { svg.renderToPicture(width, height) } catch (_: Exception) { throw ToolFailure(R.string.svg_invalid) }
        val warning = Regex("<(?:[\\w-]+:)?(?:filter|script|foreignObject|animate\\w*|set|image)\\b|(?:href|url)\\s*[=(]", RegexOption.IGNORE_CASE).containsMatchIn(source)
        currentCoroutineContext().ensureActive()
        SvgDocument(uri, resolver.fileName(uri), source, width, height, picture, warning)
        }
    }
    suspend fun export(document: SvgDocument, destination: Uri) = withContext(Dispatchers.IO) {
        mediaWork.withLock {
        val jobContext=currentCoroutineContext()
        jobContext.ensureActive()
        checkBitmapBudget(document.width,document.height)
        val bitmap = Bitmap.createBitmap(document.width, document.height, Bitmap.Config.ARGB_8888)
        try {
            // Export the original viewport, not the user's zoom/pan or checkerboard.
            Canvas(bitmap).drawPicture(document.picture, RectF(0f, 0f, document.width.toFloat(), document.height.toFloat()))
            resolver.openOutputStream(destination, "wt")?.use {
                val cancellable = object : FilterOutputStream(it) {
                    override fun write(bytes: ByteArray,offset: Int,length: Int) { jobContext.ensureActive();out.write(bytes,offset,length) }
                    override fun write(value: Int) { jobContext.ensureActive();out.write(value) }
                }
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, cancellable)) throw ToolFailure(R.string.export_error)
            } ?: throw ToolFailure(R.string.export_error)
        } finally { bitmap.recycle() }
        }
    }
}
