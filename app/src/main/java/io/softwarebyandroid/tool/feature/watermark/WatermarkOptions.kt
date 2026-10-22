package io.softwarebyandroid.tool.feature.watermark

import kotlin.math.*

data class WatermarkOptions(
    val text: String = "", val fontSize: Float = 24f,
    val gapX: Float = 80f, val gapY: Float = 60f,
    val colorHex: String = "#FFFFFF", val opacity: Float = 0.3f, val angle: Float = -30f
) {
    val color: Int? get() = colorHex.removePrefix("#").takeIf { it.matches(Regex("[0-9a-fA-F]{6}")) }
        ?.toLong(16)?.toInt()?.or(0xFF000000.toInt())
    val valid: Boolean get() = text.isNotBlank() && text.codePointCount(0,text.length)<=64 &&
        color!=null && fontSize.isFinite() && fontSize>=1 && gapX.isFinite() && gapX>=0 &&
        gapY.isFinite() && gapY>=0 && opacity.isFinite() && opacity in 0f..1f && angle.isFinite() && angle in -90f..90f
}
data class TileGrid(val columns: Int,val rows: Int,val left: Float,val top: Float,val stepX: Float,val stepY: Float)
fun watermarkGrid(width: Int,height: Int,textWidth: Float,textHeight: Float,options: WatermarkOptions): TileGrid? {
    if(!options.valid || width<=0 || height<=0 || !textWidth.isFinite() || !textHeight.isFinite()) return null
    val angle=Math.toRadians(options.angle.toDouble())
    val spanX=(abs(width*cos(angle))+abs(height*sin(angle))).toFloat()
    val spanY=(abs(width*sin(angle))+abs(height*cos(angle))).toFloat()
    val sx=(textWidth+options.gapX).coerceAtLeast(1f)
    val sy=(textHeight+options.gapY).coerceAtLeast(1f)
    val cols=ceil(spanX/sx).toLong()+3
    val rows=ceil(spanY/sy).toLong()+3
    if(cols*rows>8000 || cols>8000 || rows>8000) return null
    return TileGrid(cols.toInt(),rows.toInt(),-spanX/2-sx,-spanY/2-sy,sx,sy)
}
