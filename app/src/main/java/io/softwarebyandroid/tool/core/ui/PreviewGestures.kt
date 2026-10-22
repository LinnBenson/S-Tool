package io.softwarebyandroid.tool.core.ui

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope

val PreviewTransform = androidx.compose.ui.semantics.SemanticsPropertyKey<List<Float>>("PreviewTransform")

/** Accumulate in the gesture coroutine, without waiting for a Compose frame between events. */
suspend fun PointerInputScope.previewGestures(
    current: () -> Triple<Float, Float, Float>,
    transform: (Float, Float, Float) -> Unit,
    pick: ((Offset, Triple<Float, Float, Float>) -> Unit)? = null
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var viewport = current()
        var multiTouch = false
        do {
            val event = awaitPointerEvent()
            val pressed = event.changes.count { it.pressed }
            if (pressed >= 2 || (pick == null && pressed == 1 && !multiTouch)) {
                if (pressed >= 2) multiTouch = true
                val (zoom, x, y) = viewport
                val next = (zoom * event.calculateZoom()).coerceIn(0.25f, 16f)
                val centroid = event.calculateCentroid(useCurrent = false)
                if (centroid != Offset.Unspecified) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val pan = centroid - center - (centroid - center - Offset(x, y)) * (next / zoom) + event.calculatePan()
                    viewport = Triple(next, pan.x.coerceIn(-size.width * next, size.width * next),
                        pan.y.coerceIn(-size.height * next, size.height * next))
                    transform(viewport.first, viewport.second, viewport.third)
                }
            } else if (!multiTouch && pick != null) {
                pick(event.changes.first().position, viewport)
            }
            // Own the gesture before a scrolling ancestor can consume its movement.
            event.changes.forEach { it.consume() }
        } while (event.changes.any { it.pressed })
    }
}
