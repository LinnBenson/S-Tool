package io.softwarebyandroid.tool

import io.softwarebyandroid.tool.app.*

import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.softwarebyandroid.tool.core.ui.SToolTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.softwarebyandroid.tool.core.data.*
import io.softwarebyandroid.tool.feature.svg.SvgRepository
import io.softwarebyandroid.tool.core.data.ImageRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ToolInstrumentedTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val context get()=compose.activity
    private fun text(name: String, content: String): Uri {
        val file=File(context.filesDir,name);file.writeText(content);return Uri.fromFile(file)
    }
    private fun leaf(): Uri {
        val content=InstrumentationRegistry.getInstrumentation().context.assets.open("leaf.svg").bufferedReader().use{it.readText()}
        return text("leaf.svg",content)
    }
    private fun image(): Uri {
        val bitmap=Bitmap.createBitmap(400,200,Bitmap.Config.ARGB_8888)
        for(y in 0 until 200)for(x in 0 until 400)bitmap.setPixel(x,y,if(x<200)0xFF27BFA3.toInt() else 0xFFFFAA88.toInt())
        val file=File(context.filesDir,"colors.png")
        file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        return Uri.fromFile(file)
    }
    private fun shot(name: String) {
        // Optional only: some BlueStacks GPU backends do not support reliable readback.
        if (InstrumentationRegistry.getArguments().getString("screenshots") != "true") return
        val dir=File(context.getExternalFilesDir(null),"test-screenshots").apply{mkdirs()}
        compose.waitForIdle()
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(dir,"$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
            bitmap.recycle()
        }
    }
    private fun awaitSvg() {
        compose.waitUntil(10_000){!context.model.svg.state.value.svgLoading && context.model.svg.state.value.svg!=null}
        compose.waitForIdle()
    }
    @Test fun homeAndEmptyStates() {
        compose.onNodeWithText("全部工具").assertIsDisplayed();shot("home")
        compose.onNodeWithText("SVG 查看器").performClick()
        compose.onNodeWithText("打开一张矢量图").assertIsDisplayed()
        compose.onNodeWithContentDescription("导出 PNG").assertIsNotEnabled();shot("svg-empty")
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithText("取色器").performClick()
        compose.onNodeWithText("发现图片中的颜色").assertIsDisplayed()
        compose.onNodeWithContentDescription("复制色值").assertIsNotEnabled();shot("color-empty")
    }
    @Test fun svgZoomSourceExportAndRecreation() {
        val uri=leaf()
        compose.onNodeWithText("SVG 查看器").performClick()
        compose.runOnIdle{context.model.svg.loadSvg(uri)};awaitSvg()
        compose.onNodeWithContentDescription("放大").performClick()
        compose.onNodeWithText("150%").assertIsDisplayed()
        compose.onNodeWithContentDescription("缩小").performClick()
        compose.onNodeWithText("100%").assertIsDisplayed()
        compose.onNodeWithText("源代码").performClick()
        compose.onNodeWithText("<svg",substring=true).assertIsDisplayed();shot("svg-source")
        compose.onNodeWithText("预览").performClick()
        compose.onNodeWithContentDescription("全屏预览").performClick()
        compose.onNodeWithContentDescription("退出全屏").performClick()
        compose.onNodeWithText("适应屏幕").performClick();shot("svg-preview")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("leaf.svg").assertIsDisplayed()
        val doc=context.model.svg.state.value.svg!!
        val output=File(context.filesDir,"export.png")
        runBlocking{SvgRepository(context.contentResolver).export(doc,Uri.fromFile(output))}
        val bitmap=BitmapFactory.decodeFile(output.path)
        assertEquals(512,bitmap.width);assertEquals(512,bitmap.height)
        assertEquals(0,android.graphics.Color.alpha(bitmap.getPixel(0,0)))
        assertEquals(0xFF006B5B.toInt(),bitmap.getPixel(300,250));bitmap.recycle()
    }
    @Test fun imageSamplingAndClipboard() {
        val uri=image()
        compose.onNodeWithText("取色器").performClick()
        compose.runOnIdle{context.model.color.loadImage(uri)}
        compose.waitUntil(10_000){context.model.color.state.value.image!=null}
        compose.onNodeWithContentDescription("图片取色区域，可使用下方方向按钮移动取色点").performTouchInput {
            click(Offset(center.x/2,center.y))
        }
        compose.onNodeWithText("#27BFA3").assertIsDisplayed()
        compose.onNodeWithText("39, 191, 163").assertIsDisplayed()
        compose.onNodeWithContentDescription("复制色值").performClick()
        compose.runOnIdle { assertEquals("#27BFA3",context.getSystemService(ClipboardManager::class.java).primaryClip!!.getItemAt(0).text.toString()) }
        shot("color-sampled")
        compose.onNodeWithContentDescription("图片取色区域，可使用下方方向按钮移动取色点").performTouchInput {
            swipe(Offset(center.x/2,center.y),Offset(center.x*1.5f,center.y),500)
        }
        compose.onNodeWithText("#FFAA88").assertIsDisplayed()
        val previousX=context.model.color.state.value.sample!!.x
        compose.onNodeWithContentDescription("向左取色").performClick()
        compose.runOnIdle { assertEquals(previousX-1,context.model.color.state.value.sample!!.x) }
    }
    @Test fun twoFingerZoomInBothViewers() {
        compose.onNodeWithText("SVG 查看器").performClick()
        compose.runOnIdle{context.model.svg.loadSvg(leaf())};awaitSvg()
        compose.onNodeWithContentDescription("SVG 预览区域").performTouchInput {
            pinch(Offset(center.x-50,center.y),Offset(center.x+50,center.y),
                Offset(center.x-100,center.y),Offset(center.x+100,center.y),500)
        }
        compose.onNodeWithText("100%").assertDoesNotExist()
        compose.onNodeWithText("适应屏幕").performClick()
        compose.onNodeWithText("100%").assertIsDisplayed()
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithText("取色器").performClick()
        compose.runOnIdle{context.model.color.loadImage(image())}
        compose.waitUntil(10_000){context.model.color.state.value.image!=null}
        val canvas=compose.onNodeWithContentDescription("图片取色区域，可使用下方方向按钮移动取色点")
        canvas.performTouchInput {
            pinch(Offset(center.x-50,center.y),Offset(center.x+50,center.y),
                Offset(center.x-100,center.y),Offset(center.x+100,center.y),500)
        }
        compose.onNodeWithText("100%").assertDoesNotExist()
        compose.runOnIdle {assertNull(context.model.color.state.value.sample)}
        canvas.performTouchInput {click(Offset(center.x-40,center.y))}
        compose.onNodeWithText("#27BFA3").assertIsDisplayed();shot("color-pinch")
        canvas.performTouchInput {
            pinch(Offset(center.x-100,center.y),Offset(center.x+100,center.y),
                Offset(center.x-50,center.y),Offset(center.x+50,center.y),500)
        }
        compose.onNodeWithText("适应屏幕").performClick()
        compose.onNodeWithText("100%").assertIsDisplayed()
    }
    @Test fun invalidFileKeepsPreviousDocument() {
        compose.onNodeWithText("SVG 查看器").performClick()
        compose.runOnIdle{context.model.svg.loadSvg(leaf())};awaitSvg()
        compose.runOnIdle{context.model.svg.loadSvg(text("bad.svg","not an svg"))}
        compose.waitUntil(10_000){context.model.svg.state.value.svgError!=null}
        compose.onNodeWithText("无法解析此 SVG，请检查文件内容").assertIsDisplayed()
        compose.onNodeWithText("leaf.svg").assertIsDisplayed();shot("svg-error")
    }
    @Test fun parserBoundariesAndViewBox() = runBlocking {
        val repository=SvgRepository(context.contentResolver)
        val doc=repository.load(text("viewbox.svg","<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 240 120\"><rect width=\"240\" height=\"120\" fill=\"red\"/></svg>"))
        assertEquals(240,doc.width);assertEquals(120,doc.height)
        val cases=listOf(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"9000\" height=\"9000\"/>" to R.string.svg_dimensions,
            "<!DOCTYPE svg [<!ENTITY x 'test'>]><svg/>" to R.string.svg_entities,
            "x".repeat(4*1024*1024+1) to R.string.svg_limit
        )
        cases.forEachIndexed { index,(source,error) ->
            try {repository.load(text("invalid$index.svg",source));fail("Expected rejection")}
            catch(e: ToolFailure){assertEquals(error,e.messageRes)}
        }
        val warning=repository.load(text("filter.svg","<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\"><filter id=\"f\"/></svg>"))
        assertTrue(warning.warning)
    }
    @Test fun exactImagePixelsAndAlpha() = runBlocking {
        val repository=ImageRepository(context.contentResolver)
        val doc=repository.load(image())
        assertEquals(400,doc.bitmap.width);assertEquals(200,doc.bitmap.height)
        assertEquals(0xFF27BFA3.toInt(),doc.bitmap.getPixel(0,0))
        assertEquals(0xFFFFAA88.toInt(),doc.bitmap.getPixel(399,199))
        val bitmap=Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);bitmap.setPixel(0,0,0x80224466.toInt())
        val file=File(context.filesDir,"alpha.png");file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        val alpha=repository.load(Uri.fromFile(file)).bitmap.getPixel(0,0)
        assertEquals(128,alpha ushr 24)
        try{repository.load(text("broken.png","broken"));fail("Expected invalid image")}
        catch(e: ToolFailure){assertEquals(R.string.image_invalid,e.messageRes)}
    }
    @Test fun darkThemeAndNavigation() {
        compose.onNodeWithContentDescription("设置").performClick()
        compose.onNodeWithText("深色").performClick()
        compose.waitUntil(5_000){context.model.theme.value==ThemeMode.DARK}
        compose.onNodeWithContentDescription("返回").performClick();shot("home-dark")
        compose.onNodeWithText("取色器").performClick()
        compose.runOnIdle{context.model.color.loadImage(image())}
        compose.waitUntil(10_000){context.model.color.state.value.image!=null}
        compose.runOnIdle{context.model.color.sample(100,100)};shot("color-dark")
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithText("SVG 查看器").performClick()
        compose.runOnIdle{context.model.svg.loadSvg(leaf())};awaitSvg();shot("svg-dark")
        compose.runOnIdle{context.model.setTheme(ThemeMode.SYSTEM)}
        compose.waitUntil(5_000){context.model.theme.value==ThemeMode.SYSTEM}
    }
    @Test fun largeFontControlsRemainReachable() {
        compose.runOnIdle {
            context.setContent {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,2f)) {
                    SToolTheme(ThemeMode.LIGHT) {ToolApp(context.model,ThemeMode.LIGHT)}
                }
            }
            context.model.navigate(Page.COLOR)
            context.model.color.loadImage(image())
        }
        compose.waitUntil(10_000){context.model.color.state.value.image!=null}
        compose.runOnIdle{context.model.color.sample(100,100)}
        compose.onNodeWithContentDescription("复制色值").assertIsDisplayed();shot("color-font-200")
        compose.runOnIdle{context.model.navigate(Page.SVG);context.model.svg.loadSvg(leaf())};awaitSvg()
        compose.onNodeWithContentDescription("导出 PNG").assertIsDisplayed();shot("svg-font-200")
    }
    @Test fun landscapePreviewAndControlsRemainVisible() {
        try {
            compose.runOnIdle {
                context.model.navigate(Page.COLOR);context.model.color.loadImage(image())
                context.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            compose.waitUntil(10_000){context.resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE && context.model.color.state.value.image!=null}
            compose.onNodeWithContentDescription("选择图片").assertIsDisplayed();shot("color-landscape")
            compose.runOnIdle{context.model.navigate(Page.SVG);context.model.svg.loadSvg(leaf())};awaitSvg()
            compose.onNodeWithContentDescription("导出 PNG").assertIsDisplayed();shot("svg-landscape")
        } finally {
            compose.runOnIdle{context.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
        }
    }

    @Test fun twoFingerPanAccumulatesWithoutChangingZoomOrSample() {
        for(page in listOf(Page.SVG,Page.COLOR)) {
            compose.runOnIdle {
                context.model.navigate(page)
                if(page==Page.SVG) context.model.svg.loadSvg(leaf()) else context.model.color.loadImage(image())
            }
            compose.waitUntil(10_000) { if(page==Page.SVG) context.model.svg.state.value.svg!=null else context.model.color.state.value.image!=null }
            val canvas=compose.onNodeWithContentDescription(if(page==Page.SVG) "SVG 预览区域" else "图片取色区域，可使用下方方向按钮移动取色点")
            canvas.performTouchInput {
                down(0,Offset(center.x-50,center.y)); down(1,Offset(center.x+50,center.y))
                for(step in 1..10) {
                    updatePointerTo(0,Offset(center.x-50+step*10,center.y+step*5))
                    updatePointerTo(1,Offset(center.x+50+step*10,center.y+step*5))
                    move(16)
                }
                up(0);up(1)
            }
            val view=canvas.fetchSemanticsNode().config[io.softwarebyandroid.tool.core.ui.PreviewTransform]
            assertEquals(1f,view[0],0.001f);assertEquals(100f,view[1],1f);assertEquals(50f,view[2],1f)
            if(page==Page.COLOR) {
                compose.runOnIdle { assertNull(context.model.color.state.value.sample) }
                canvas.performTouchInput { click(Offset(center.x+60,center.y+50)) }
                compose.onNodeWithText("#27BFA3").assertIsDisplayed()
            }
        }
    }
    @Test fun foldPosturesSeparatePanesAndPreserveToolState() {
        val posture=androidx.compose.runtime.mutableStateOf<io.softwarebyandroid.tool.core.ui.FoldPosture?>(null)
        compose.runOnIdle {
            context.setContent {
                CompositionLocalProvider(io.softwarebyandroid.tool.core.ui.LocalFoldPosture provides posture.value) {
                    SToolTheme(ThemeMode.LIGHT) { ToolApp(context.model,ThemeMode.LIGHT) }
                }
            }
            context.model.navigate(Page.COLOR);context.model.color.loadImage(image())
        }
        compose.waitUntil(10_000){context.model.color.state.value.image!=null}
        compose.runOnIdle{context.model.color.sample(100,100)}
        compose.onNodeWithContentDescription("放大").performClick()
        for(vertical in listOf(true,false)) {
            val extent=if(vertical) context.resources.displayMetrics.widthPixels else context.resources.displayMetrics.heightPixels
            compose.runOnIdle { posture.value=io.softwarebyandroid.tool.core.ui.FoldPosture(vertical,extent/2-8,extent/2+8) }
            val preview=compose.onNodeWithTag("workspace-pane").fetchSemanticsNode().boundsInWindow
            val controls=compose.onNodeWithTag("controls-pane").fetchSemanticsNode().boundsInWindow
            if(vertical) { assertTrue(preview.right<extent/2-8);assertTrue(controls.left>extent/2+8) }
            else { assertTrue(preview.bottom<extent/2-8);assertTrue(controls.top>extent/2+8) }
            compose.onNodeWithText("150%").assertIsDisplayed()
            compose.onNodeWithText("#27BFA3").assertExists()
        }
        compose.runOnIdle { posture.value=null }
        compose.onNodeWithText("150%").assertIsDisplayed()
        compose.onNodeWithText("#27BFA3").assertIsDisplayed()
        compose.runOnIdle { context.model.navigate(Page.SVG);context.model.svg.loadSvg(leaf()) };awaitSvg()
        compose.onNodeWithText("源代码").performClick()
        compose.runOnIdle { val width=context.resources.displayMetrics.widthPixels;posture.value=io.softwarebyandroid.tool.core.ui.FoldPosture(true,width/2-8,width/2+8) }
        compose.onNodeWithText("<svg",substring=true).assertExists()
        compose.onNodeWithText("leaf.svg").assertExists()
        compose.runOnIdle { posture.value=null }
        compose.onNodeWithText("<svg",substring=true).assertIsDisplayed()
    }

    @Test fun explicitColorCopiesAndSessionCleanup() {
        val uri=image()
        val filesBefore=context.filesDir.walkTopDown().filter { it.isFile }.map { it.relativeTo(context.filesDir).path }.toSet()
        compose.runOnIdle { context.model.navigate(Page.COLOR);context.model.color.loadImage(uri) }
        compose.waitUntil(10_000) { context.model.color.state.value.image!=null }
        compose.runOnIdle { context.model.color.sample(100,100) }
        for((label,value) in listOf("复制 HEX" to "#27BFA3","复制 RGB" to "39, 191, 163")) {
            compose.onNodeWithContentDescription(label).performClick()
            compose.runOnIdle { assertEquals(value,context.getSystemService(ClipboardManager::class.java).primaryClip!!.getItemAt(0).text.toString()) }
        }
        compose.runOnIdle {
            assertTrue(context.contentResolver.persistedUriPermissions.isEmpty())
            assertEquals(filesBefore,context.filesDir.walkTopDown().filter { it.isFile }.map { it.relativeTo(context.filesDir).path }.toSet())
            context.model.navigate(Page.HOME)
            assertNull(context.model.color.state.value.image);assertNull(context.model.color.state.value.sample)
            context.model.color.loadImage(uri);context.model.navigate(Page.HOME)
        }
        compose.waitForIdle()
        compose.runOnIdle { assertNull(context.model.color.state.value.image);assertFalse(context.model.color.state.value.imageLoading) }
        compose.runOnIdle { context.model.navigate(Page.SVG);context.model.svg.loadSvg(leaf()) };awaitSvg()
        compose.runOnIdle { context.model.navigate(Page.HOME);assertNull(context.model.svg.state.value.svg) }
    }

    @Test fun backgroundRoundTripKeepsCurrentWork() {
        compose.runOnIdle { context.model.navigate(Page.COLOR);context.model.color.loadImage(image()) }
        compose.waitUntil(10_000){context.model.color.state.value.image!=null}
        compose.runOnIdle {context.model.color.sample(100,100)}
        compose.onNodeWithContentDescription("放大").performClick()
        val original=context.model.color.state.value.image
        compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
        assertSame(original,context.model.color.state.value.image)
        assertEquals(100,context.model.color.state.value.sample!!.x)
        compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
        compose.onNodeWithText("150%").assertIsDisplayed()
        compose.onNodeWithText("#27BFA3").assertIsDisplayed()
    }
}
