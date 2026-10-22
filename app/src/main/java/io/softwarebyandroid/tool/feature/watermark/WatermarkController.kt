package io.softwarebyandroid.tool.feature.watermark

import android.app.Application
import android.graphics.Picture
import android.net.Uri
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

 data class WatermarkState(
    val image: ImageDocument? = null, val options: WatermarkOptions = WatermarkOptions(),
    val overlay: Picture? = null, val loading: Boolean = false, val rendering: Boolean = false,
    val exporting: Boolean = false, val error: Int? = null
)
class WatermarkController(application: Application,private val scope: CoroutineScope,private val notify: suspend (Int)->Unit) {
    private val images=ImageRepository(application.contentResolver)
    private val renderer=WatermarkRenderer(application.contentResolver)
    private val defaults=WatermarkOptions(text=application.getString(R.string.watermark_default))
    private val preferences=WatermarkPreferences(application)
    private var lastValid: WatermarkOptions?=null
    private val restored=scope.async { preferences.read().also { lastValid=it } }
    private val writes=kotlinx.coroutines.channels.Channel<WatermarkOptions>(kotlinx.coroutines.channels.Channel.CONFLATED)
    init { scope.launch { for(value in writes) { try { preferences.write(value) } catch(_: java.io.IOException) { notify(R.string.watermark_settings_error) } } } }
    private val mutable=MutableStateFlow(WatermarkState(options=defaults))
    val state=mutable.asStateFlow()
    private var loadJob: Job?=null
    private var renderJob: Job?=null
    private var exportJob: Job?=null
    private var pending: Pair<ImageDocument,Picture>?=null
    fun load(uri: Uri) {
        loadJob?.cancel()
        loadJob=scope.launch {
            mutable.update { it.copy(loading=true,error=null) }
            try {
                restored.await()
                val image=images.load(uri)
                val edge=minOf(image.bitmap.width,image.bitmap.height).toFloat()
                val options=lastValid ?: defaults.copy(fontSize=(edge*.06f).coerceAtLeast(1f),gapX=edge*.2f,gapY=edge*.16f)
                mutable.update { it.copy(image=image,options=options,overlay=null) }
                render()
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { mutable.update { it.copy(error=(e as? ToolFailure)?.messageRes?:R.string.image_invalid) } }
            catch(_: OutOfMemoryError) { mutable.update { it.copy(error=R.string.memory_error) } }
            finally { if(currentCoroutineContext().isActive) mutable.update { it.copy(loading=false) } }
        }
    }
    fun update(options: WatermarkOptions) {
        if(mutable.value.exporting) return
        if(options.valid) { lastValid=options; writes.trySend(options) }
        mutable.update { it.copy(options=options,error=null) }
        render()
    }
    private fun render() {
        renderJob?.cancel()
        val snapshot=mutable.value
        val image=snapshot.image ?: return
        if(!snapshot.options.valid) { mutable.update { it.copy(overlay=null,rendering=false) };return }
        mutable.update { it.copy(rendering=true) }
        renderJob=scope.launch {
            try {
                delay(100) // Coalesce slider events instead of recording a picture for every pixel moved.
                val overlay=renderer.record(image.bitmap.width,image.bitmap.height,snapshot.options)
                mutable.update { it.copy(overlay=overlay) }
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { mutable.update { it.copy(overlay=null,error=(e as? ToolFailure)?.messageRes?:R.string.watermark_render_error) } }
            catch(_: OutOfMemoryError) { mutable.update { it.copy(overlay=null,error=R.string.memory_error) } }
            finally { if(currentCoroutineContext().isActive) mutable.update { it.copy(rendering=false) } }
        }
    }
    fun prepareExport(): String? {
        val current=mutable.value
        if(current.loading || current.rendering || current.exporting || !current.options.valid) return null
        val image=current.image?:return null
        pending=image to (current.overlay?:return null)
        return image.name.substringBeforeLast('.',image.name)+"-watermark.png"
    }
    fun export(uri: Uri?) {
        val snapshot=pending;pending=null
        if(uri==null || snapshot==null) return
        exportJob=scope.launch {
            mutable.update { it.copy(exporting=true) }
            try { renderer.export(snapshot.first,snapshot.second,uri);notify(R.string.saved) }
            catch(e: CancellationException) { throw e }
            catch(_: Exception) { notify(R.string.export_error) }
            catch(_: OutOfMemoryError) { notify(R.string.memory_error) }
            finally { if(currentCoroutineContext().isActive) mutable.update { it.copy(exporting=false) } }
        }
    }
    fun clear() {
        loadJob?.cancel();renderJob?.cancel();exportJob?.cancel()
        loadJob=null;renderJob=null;exportJob=null;pending=null
        mutable.value=WatermarkState(options=lastValid ?: defaults)
    }
}
