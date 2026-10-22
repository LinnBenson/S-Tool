package io.softwarebyandroid.tool

import android.net.Uri
import android.os.Process
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.softwarebyandroid.tool.core.data.ToolFailure
import io.softwarebyandroid.tool.feature.svg.SvgRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SvgSafetyInstrumentedTest {
    private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    private val context get()=instrumentation.targetContext
    private suspend fun reject(bytes: ByteArray, reason: Int) {
        val file=File.createTempFile("svg-safety-",".svg",context.cacheDir)
        try {
            file.writeBytes(bytes)
            try { SvgRepository(context.contentResolver).load(Uri.fromFile(file)); fail("Unsafe SVG accepted") }
            catch(e: ToolFailure) { assertEquals(reason,e.messageRes) }
        } finally { file.delete() }
    }
    @Test fun embeddedBitmapIsRejectedBeforeRenderer() = runBlocking {
        val bytes=instrumentation.context.assets.open("svg-embedded-large.svg").use{it.readBytes()}
        assertTrue(bytes.size<110_000)
        reject(bytes,R.string.svg_embedded_image)
    }
    @Test fun prefixedEncodedAndExternalImageElementsAreRejected() = runBlocking {
        val samples=listOf(
            "<svg xmlns='http://www.w3.org/2000/svg'><image href='data:image/png;base64,AA=='/></svg>",
            "<v:svg xmlns:v='http://www.w3.org/2000/svg'><v:image href='file:///private.png'/></v:svg>",
            "<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'><image xlink:href='https://invalid.example/image.png'/></svg>"
        )
        for(s in samples) {
            reject(s.toByteArray(),R.string.svg_embedded_image)
            reject(("\uFEFF"+s).toByteArray(Charsets.UTF_16LE),R.string.svg_embedded_image)
        }
        reject("<!DOCTYPE svg SYSTEM 'https://invalid.example/test.dtd'><svg/>".toByteArray(),R.string.svg_entities)
    }
    @Test fun vectorWithProcessingInstructionRendersWithoutContentLogs() = runBlocking {
        val marker="S_TOOL_PRIVATE_"+java.util.UUID.randomUUID().toString().replace("-","")
        val file=File.createTempFile("svg-safe-",".svg",context.cacheDir)
        try {
            file.writeText("<?xml version='1.0'?><?audit $marker?><svg xmlns='http://www.w3.org/2000/svg' width='100' height='100'><rect width='100' height='100' fill='url(#$marker)'/><rect width='50' height='50' fill='#006B5B'/></svg>")
            val document=SvgRepository(context.contentResolver).load(Uri.fromFile(file))
            assertEquals(100,document.width)
            assertTrue(document.source.contains(marker)) // Preserve original source for explicit viewing/copy.
            val bitmap=android.graphics.Bitmap.createBitmap(100,100,android.graphics.Bitmap.Config.ARGB_8888)
            try {android.graphics.Canvas(bitmap).drawPicture(document.picture);assertEquals(0xFF006B5B.toInt(),bitmap.getPixel(20,20))}
            finally {bitmap.recycle()}
            val descriptor=instrumentation.uiAutomation.executeShellCommand("logcat -d --pid="+Process.myPid()+" -v brief")
            val logs=android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use{it.readText()}
            assertFalse("SVG content was written to logcat",logs.contains(marker))
        } finally {file.delete()}
    }
}
