package io.softwarebyandroid.tool

import io.softwarebyandroid.tool.core.data.*
import org.junit.Assert.*
import org.junit.Test

class GeometryTest {
    @Test fun letterboxMarginsDoNotSamplePixels() {
        val rect=fitRect(400,200,200f,200f)
        assertEquals(50f,rect.top,0f)
        assertNull(rect.pixel(100f,20f,400,200))
        assertNull(rect.pixel(100f,150f,400,200))
        assertEquals(200 to 100,rect.pixel(100f,100f,400,200))
    }
    @Test fun borderAndLastPixelMapping() {
        val rect=fitRect(400,200,200f,100f)
        assertEquals(0 to 0,rect.pixel(0f,0f,400,200))
        assertEquals(399 to 199,rect.pixel(199.9f,99.9f,400,200))
        assertNull(rect.pixel(200f,100f,400,200))
    }
    @Test fun portraitImageIsCenteredHorizontally() {
        val rect=fitRect(100,400,200f,200f)
        assertEquals(75f,rect.left,0f)
        assertEquals(50 to 200,rect.pixel(100f,100f,100,400))
    }
    @Test fun zoomAndPanMapBackToOriginalPixel() {
        val rect=fitRect(400,200,200f,200f).transformed(200f,200f,2f,20f,-10f)
        assertEquals(200 to 100,rect.pixel(120f,90f,400,200))
        assertEquals(100 to 50,rect.pixel(20f,40f,400,200))
        assertNull(rect.pixel(-81f,90f,400,200))
    }
    @Test fun dimensionsRejectOverflowAndExcessiveMemory() {
        assertTrue(validDimensions(4000,4000))
        assertFalse(validDimensions(4001,4000))
        assertFalse(validDimensions(Int.MAX_VALUE,Int.MAX_VALUE))
        assertFalse(validDimensions(0,10))
        assertFalse(validDimensions(-1,10))
        assertFalse(validDimensions(8193,1))
    }
    @Test fun hexRgbAndAlphaAgree() {
        assertEquals("#27BFA3",colorHex(0xFF27BFA3.toInt()))
        assertEquals("39, 191, 163",colorRgb(0xFF27BFA3.toInt()))
        assertEquals(100,alphaPercent(0xFF27BFA3.toInt()))
        assertEquals(0,alphaPercent(0x0027BFA3))
        assertEquals(50,alphaPercent(0x8027BFA3.toInt()))
        assertEquals("#00000A",colorHex(10))
    }
}
