package io.softwarebyandroid.tool

import io.softwarebyandroid.tool.feature.watermark.*
import org.junit.Assert.*
import org.junit.Test

class WatermarkOptionsTest {
    private val options=WatermarkOptions(text="Test")
    @Test fun validatesTextAndColor() {
        assertTrue(options.valid)
        assertEquals(0xFF12ABEF.toInt(),options.copy(colorHex="#12abEF").color)
        assertFalse(options.copy(text=" ").valid)
        assertFalse(options.copy(text="a".repeat(65)).valid)
        assertFalse(options.copy(colorHex="#12X456").valid)
        assertFalse(options.copy(opacity=Float.NaN).valid)
        assertFalse(options.copy(gapX=-1f).valid)
    }
    @Test fun gapsAreIndependentAndMeasuredBetweenTextBlocks() {
        val grid=watermarkGrid(400,200,50f,20f,options.copy(gapX=30f,gapY=10f,angle=0f))!!
        assertEquals(80f,grid.stepX,0.001f);assertEquals(30f,grid.stepY,0.001f)
        assertTrue(grid.left< -200f);assertTrue(grid.top< -100f)
        assertTrue(grid.left+(grid.columns-1)*grid.stepX>=200f)
        assertTrue(grid.top+(grid.rows-1)*grid.stepY>=100f)
    }
    @Test fun rotatedGridCoversBoundsAndRejectsExtremeDensity() {
        val rotated=watermarkGrid(400,200,50f,20f,options.copy(angle=90f))!!
        assertTrue(rotated.top< -200f)
        assertNull(watermarkGrid(8192,8192,1f,1f,options.copy(fontSize=1f,gapX=0f,gapY=0f)))
    }
}
