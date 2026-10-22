package io.softwarebyandroid.tool.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.window.layout.FoldingFeature

// Window-relative physical pixels; translated by each workspace before layout.
data class FoldPosture(val vertical: Boolean, val start: Int, val end: Int)
val LocalFoldPosture = staticCompositionLocalOf<FoldPosture?> { null }
fun FoldingFeature.toPosture(): FoldPosture? {
    if (!isSeparating && occlusionType != FoldingFeature.OcclusionType.FULL) return null
    return if (orientation == FoldingFeature.Orientation.VERTICAL) FoldPosture(true,bounds.left,bounds.right)
    else FoldPosture(false,bounds.top,bounds.bottom)
}
