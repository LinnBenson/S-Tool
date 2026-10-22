package io.softwarebyandroid.tool.core.data

enum class PaneMode { STACKED, SIDE_BY_SIDE, BOOK, TABLETOP }
data class PanePlan(val mode: PaneMode, val first: Float = 0f, val gap: Float = 0f)

// All dimensions are local dp, including a hinge translated from window coordinates.
fun planPanes(width: Float, height: Float, vertical: Boolean?, hingeStart: Float,
    hingeEnd: Float, fontScale: Float): PanePlan {
    val extent = if (vertical == true) width else height
    if (vertical != null && hingeStart > 0 && hingeEnd < extent && hingeEnd >= hingeStart) {
        val first = (hingeStart - 12f).coerceAtLeast(0f)
        val gap = hingeEnd - hingeStart + 24f
        if (first >= 80f && extent - first - gap >= 80f) {
            return PanePlan(if (vertical) PaneMode.BOOK else PaneMode.TABLETOP, first, gap)
        }
    }
    if (width >= 600f && height >= 300f && fontScale <= 1.5f) {
        val controls = (width * 0.4f).coerceIn(280f, 360f)
        return PanePlan(PaneMode.SIDE_BY_SIDE, width - controls - 24f, 24f)
    }
    return PanePlan(PaneMode.STACKED)
}
