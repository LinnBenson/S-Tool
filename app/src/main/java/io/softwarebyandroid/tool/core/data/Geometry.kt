package io.softwarebyandroid.tool.core.data

import java.util.Locale
import kotlin.math.floor
import kotlin.math.min

data class FitRect(val left: Float, val top: Float, val width: Float, val height: Float) {
    fun transformed(viewportWidth: Float, viewportHeight: Float, zoom: Float, panX: Float, panY: Float): FitRect =
        FitRect((left-viewportWidth/2)*zoom+viewportWidth/2+panX,
            (top-viewportHeight/2)*zoom+viewportHeight/2+panY,width*zoom,height*zoom)
    fun pixel(x: Float, y: Float, imageWidth: Int, imageHeight: Int): Pair<Int, Int>? {
        if (width <= 0f || height <= 0f || x < left || y < top || x >= left + width || y >= top + height) return null
        return floor((x - left) / width * imageWidth).toInt().coerceIn(0, imageWidth - 1) to
            floor((y - top) / height * imageHeight).toInt().coerceIn(0, imageHeight - 1)
    }
}
fun fitRect(imageWidth: Int, imageHeight: Int, viewportWidth: Float, viewportHeight: Float): FitRect {
    require(imageWidth > 0 && imageHeight > 0)
    val scale = min(viewportWidth / imageWidth, viewportHeight / imageHeight).coerceAtLeast(0f)
    val width = imageWidth * scale
    val height = imageHeight * scale
    return FitRect((viewportWidth - width) / 2f, (viewportHeight - height) / 2f, width, height)
}
fun colorHex(argb: Int): String = String.format(Locale.ROOT, "#%06X", argb and 0xFFFFFF)
fun colorRgb(argb: Int): String = "${argb shr 16 and 255}, ${argb shr 8 and 255}, ${argb and 255}"
fun alphaPercent(argb: Int): Int = ((argb ushr 24) / 255f * 100).toInt()
fun validDimensions(width: Int, height: Int): Boolean = width in 1..8192 && height in 1..8192 && width.toLong() * height <= 16_000_000
