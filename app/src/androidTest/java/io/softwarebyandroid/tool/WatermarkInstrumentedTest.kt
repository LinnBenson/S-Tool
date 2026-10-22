package io.softwarebyandroid.tool

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.softwarebyandroid.tool.app.*
import io.softwarebyandroid.tool.core.data.*
import io.softwarebyandroid.tool.core.ui.*
import io.softwarebyandroid.tool.feature.watermark.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WatermarkInstrumentedTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val activity get()=compose.activity
    private fun fixture(width: Int=400,height: Int=200): Uri {
        val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        val file=File(activity.filesDir,"watermark-input.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
        return Uri.fromFile(file)
    }
    private fun ready() {
        compose.waitUntil(10_000) { activity.model.watermark.state.value.let { it.overlay!=null && !it.loading && !it.rendering } }
    }
    private fun load() {
        val uri=fixture()
        compose.runOnIdle {activity.model.navigate(Page.WATERMARK);activity.model.watermark.load(uri)}
        ready()
    }
    @Test fun allSettingsAndExportSnapshot() {
        compose.onNodeWithText("水印工具").performClick()
        compose.onNodeWithContentDescription("导出 PNG").assertIsNotEnabled()
        load()
        compose.onNodeWithText("水印文字").performScrollTo().performTextReplacement("我的水印")
        compose.onNodeWithText("文字颜色 HEX").performScrollTo().performTextReplacement("#FF0000")
        for((name,value) in listOf("文字大小" to 30f,"横向间隔" to 55f,"纵向间隔" to 37f,"不透明度" to 45f,"倾斜角度" to -45f)) {
            compose.onNodeWithContentDescription(name).performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(value) }
        }
        ready()
        val controller=activity.model.watermark
        val options=controller.state.value.options
        assertEquals("我的水印",options.text);assertEquals(30f,options.fontSize,0f)
        assertEquals(55f,options.gapX,0f);assertEquals(37f,options.gapY,0f)
        assertEquals(.45f,options.opacity,.001f);assertEquals(-45f,options.angle,0f)
        val output=File(activity.filesDir,"watermark-snapshot.png")
        compose.runOnIdle {
            assertEquals("watermark-input-watermark.png",controller.prepareExport())
            controller.update(options.copy(opacity=0f))
            controller.export(Uri.fromFile(output))
        }
        compose.waitUntil(10_000) {!controller.state.value.exporting && output.length()>0}
        val bitmap=BitmapFactory.decodeFile(output.path)
        assertEquals(400,bitmap.width);assertEquals(200,bitmap.height)
        assertTrue((0 until 400).any { x -> (0 until 200).any { y -> bitmap.getPixel(x,y) ushr 24>0 } })
        bitmap.recycle()
    }
    @Test fun renderedOverlayMatchesExportAndOpacity() = runBlocking {
        val image=ImageRepository(activity.contentResolver).load(fixture())
        val renderer=WatermarkRenderer(activity.contentResolver)
        val options=WatermarkOptions(text="水印 TEST",fontSize=24f,gapX=12f,gapY=10f,colorHex="#FF0000",opacity=.5f,angle=-35f)
        val overlay=renderer.record(400,200,options)
        val expected=Bitmap.createBitmap(400,200,Bitmap.Config.ARGB_8888)
        Canvas(expected).drawPicture(overlay)
        val output=File(activity.filesDir,"watermark-render.png")
        renderer.export(image,overlay,Uri.fromFile(output))
        val actual=BitmapFactory.decodeFile(output.path)
        val expectedPixels=IntArray(80_000);val actualPixels=IntArray(80_000)
        expected.getPixels(expectedPixels,0,400,0,0,400,200)
        actual.getPixels(actualPixels,0,400,0,0,400,200)
        assertArrayEquals(expectedPixels,actualPixels)
        assertEquals(128,actualPixels.maxOf { it ushr 24 })
        for(qy in 0..1)for(qx in 0..1) assertTrue((qy*100 until (qy+1)*100).any { y -> (qx*200 until (qx+1)*200).any { x -> actual.getPixel(x,y) ushr 24>0 } })
        for(angle in listOf(-90f,0f,90f)) {
            val invisible=renderer.record(400,200,options.copy(opacity=0f,angle=angle))
            renderer.export(image,invisible,Uri.fromFile(output))
            val zero=BitmapFactory.decodeFile(output.path)
            assertTrue((0 until 400 step 5).all {x -> (0 until 200 step 5).all {y -> zero.getPixel(x,y)==0}})
            zero.recycle()
        }
        actual.recycle();expected.recycle()
    }
    @Test fun invalidInputAndExitClearPrivateContent() {
        load()
        val previous=activity.model.watermark.state.value.options
        compose.onNodeWithText("文字颜色 HEX").performScrollTo().performTextReplacement("#GGGGGG")
        compose.onNodeWithText("请输入 6 位十六进制颜色，如 #FFFFFF").assertExists()
        compose.onNodeWithContentDescription("导出 PNG").assertIsNotEnabled()
        compose.runOnIdle {
            assertTrue(activity.contentResolver.persistedUriPermissions.isEmpty())
            activity.model.navigate(Page.HOME)
            assertNull(activity.model.watermark.state.value.image)
            assertNull(activity.model.watermark.state.value.overlay)
            assertEquals(previous.text,activity.model.watermark.state.value.options.text)
        }
    }
    @Test fun backgroundAndFoldKeepWatermarkState() {
        val posture=mutableStateOf<FoldPosture?>(null)
        compose.runOnIdle { activity.setContent { CompositionLocalProvider(LocalFoldPosture provides posture.value) {SToolTheme(ThemeMode.DARK){ToolApp(activity.model,ThemeMode.DARK)}} } }
        load()
        compose.runOnIdle { activity.model.watermark.update(activity.model.watermark.state.value.options.copy(text="保留状态")) };ready()
        compose.onNodeWithContentDescription("放大").performClick()
        val original=activity.model.watermark.state.value
        compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
        assertSame(original.image,activity.model.watermark.state.value.image)
        assertEquals(original.options,activity.model.watermark.state.value.options)
        for(vertical in listOf(true,false)) {
            val extent=if(vertical)activity.resources.displayMetrics.widthPixels else activity.resources.displayMetrics.heightPixels
            compose.runOnIdle {posture.value=FoldPosture(vertical,extent/2-8,extent/2+8)}
            val preview=compose.onNodeWithTag("workspace-pane").fetchSemanticsNode().boundsInWindow
            val controls=compose.onNodeWithTag("controls-pane").fetchSemanticsNode().boundsInWindow
            if(vertical){assertTrue(preview.right<extent/2-8);assertTrue(controls.left>extent/2+8)}
            else{assertTrue(preview.bottom<extent/2-8);assertTrue(controls.top>extent/2+8)}
            compose.onNodeWithText("150%").assertIsDisplayed()
            assertEquals("保留状态",activity.model.watermark.state.value.options.text)
        }
    }
    @Test fun largeFontControlsAndPinchRemainUsable() {
        compose.runOnIdle { activity.setContent { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,2f)) {SToolTheme(ThemeMode.LIGHT){ToolApp(activity.model,ThemeMode.LIGHT)}} } }
        load()
        compose.onNodeWithContentDescription("水印图片预览区域").performTouchInput {
            pinch(androidx.compose.ui.geometry.Offset(center.x-30,center.y),androidx.compose.ui.geometry.Offset(center.x+30,center.y),
                androidx.compose.ui.geometry.Offset(center.x-60,center.y),androidx.compose.ui.geometry.Offset(center.x+60,center.y),400)
        }
        compose.onNodeWithText("100%").assertDoesNotExist()
        compose.onNodeWithContentDescription("倾斜角度").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("导出 PNG").assertIsDisplayed()
    }

    @Test fun parameterPositionsStayStableAndLastSettingsSurviveNewController() {
        load()
        val controller=activity.model.watermark
        val slider=compose.onNodeWithContentDescription("文字大小")
        slider.performScrollTo()
        val before=slider.fetchSemanticsNode().boundsInRoot
        slider.performSemanticsAction(SemanticsActions.SetProgress) { it(34f) }
        assertEquals(before,slider.fetchSemanticsNode().boundsInRoot)
        ready()
        assertEquals(before,slider.fetchSemanticsNode().boundsInRoot)
        compose.onNodeWithContentDescription("使用颜色 #006B5B").performScrollTo().performClick()
        ready()
        val color=compose.onNodeWithText("文字颜色 HEX")
        color.performScrollTo().performClick().assertIsFocused()
        compose.waitForIdle()
        color.assertIsFocused()
        color.performTextReplacement("#006B5B")
        ready()
        color.assertIsFocused()
        val expected=controller.state.value.options
        compose.runOnIdle {controller.clear();controller.load(fixture(40,20))}
        ready()
        assertEquals(expected,controller.state.value.options)
        runBlocking {
            kotlinx.coroutines.withTimeout(5000) {
                while(WatermarkPreferences(activity).read()!=expected) kotlinx.coroutines.delay(20)
            }
            val scope=kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()+kotlinx.coroutines.Dispatchers.Main)
            try {
                val fresh=WatermarkController(activity.application,scope,{})
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {fresh.load(fixture())}
                kotlinx.coroutines.withTimeout(5000) {
                    while(fresh.state.value.image==null || fresh.state.value.rendering) kotlinx.coroutines.delay(20)
                }
                assertEquals(expected,fresh.state.value.options)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {fresh.clear()}
                assertNull(fresh.state.value.image)
            } finally {scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()}
        }
    }

    @Test fun wideWindowSeparatesPreviewAndParameters() {
        try {
            load()
            compose.runOnIdle {activity.requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}
            compose.waitUntil(10_000) {activity.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE}
            val preview=compose.onNodeWithTag("workspace-pane").fetchSemanticsNode().boundsInWindow
            val controls=compose.onNodeWithTag("controls-pane").fetchSemanticsNode().boundsInWindow
            assertTrue(preview.right<controls.left)
            compose.onNodeWithContentDescription("导出 PNG").assertIsDisplayed()
            compose.onNodeWithContentDescription("倾斜角度").performScrollTo().assertIsDisplayed()
            compose.onNodeWithContentDescription("水印图片预览区域").assertIsDisplayed()
        } finally {compose.runOnIdle {activity.requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}}
    }
}
