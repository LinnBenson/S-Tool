package io.softwarebyandroid.tool.core.data

import io.softwarebyandroid.tool.R
import kotlinx.coroutines.sync.Mutex

// Serialize large allocations so cancelled native decodes finish before another begins.
internal val mediaWork = Mutex()
internal fun checkBitmapBudget(width: Int, height: Int) {
    val budget = minOf(64_000_000L, Runtime.getRuntime().maxMemory() / 4)
    if(width.toLong()*height*4 > budget) throw ToolFailure(R.string.memory_error)
}
