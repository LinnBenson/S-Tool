package io.softwarebyandroid.tool.core.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import io.softwarebyandroid.tool.R

class ToolFailure(val messageRes: Int) : Exception()
fun ContentResolver.fileName(uri: Uri): String = runCatching {
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/') ?: "file"
suspend fun ContentResolver.readLimited(uri: Uri, limit: Int): ByteArray = openInputStream(uri)?.use { input ->
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        currentCoroutineContext().ensureActive()
        val count = input.read(buffer)
        if (count < 0) break
        if (output.size().toLong() + count > limit) throw ToolFailure(R.string.svg_limit)
        output.write(buffer, 0, count)
    }
    output.toByteArray()
} ?: throw ToolFailure(R.string.file_error)
