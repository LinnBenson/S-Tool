package io.softwarebyandroid.tool

import io.softwarebyandroid.tool.core.data.*
import org.junit.Assert.*
import org.junit.Test

class PaneLayoutTest {
    @Test fun narrowAndWideWindowsReflow() {
        assertEquals(PaneMode.STACKED,planPanes(400f,800f,null,0f,0f,1f).mode)
        val wide=planPanes(900f,600f,null,0f,0f,1f)
        assertEquals(PaneMode.SIDE_BY_SIDE,wide.mode)
        assertEquals(360f,900f-wide.first-wide.gap,0.01f)
        assertEquals(PaneMode.STACKED,planPanes(900f,600f,null,0f,0f,2f).mode)
    }
    @Test fun hingeIsExcludedInBothOrientations() {
        for(vertical in listOf(true,false)) {
            val plan=planPanes(900f,900f,vertical,430f,470f,2f)
            assertEquals(if(vertical)PaneMode.BOOK else PaneMode.TABLETOP,plan.mode)
            assertTrue(plan.first<430f)
            assertTrue(plan.first+plan.gap>470f)
        }
    }
    @Test fun outsideHingeDoesNotCreateInvalidPane() {
        assertEquals(PaneMode.STACKED,planPanes(400f,800f,true,-10f,0f,1f).mode)
        assertEquals(PaneMode.STACKED,planPanes(400f,800f,false,900f,920f,1f).mode)
    }
}
