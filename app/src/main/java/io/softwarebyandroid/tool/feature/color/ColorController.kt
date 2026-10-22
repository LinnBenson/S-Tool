package io.softwarebyandroid.tool.feature.color

import android.app.Application
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class ColorState(val image: ImageDocument? = null, val sample: Sample? = null, val imageLoading: Boolean = false, val imageError: Int? = null)
class ColorController(application: Application,
    private val scope: CoroutineScope, private val notify: suspend (Int) -> Unit) {
    private val resolver = application.contentResolver
    private val images = ImageRepository(resolver)
    private var imageJob: Job? = null
    private val mutable = MutableStateFlow(ColorState())
    val state = mutable.asStateFlow()
    fun loadImage(uri: Uri) {
        imageJob?.cancel()
        imageJob = scope.launch {
            mutable.update { it.copy(imageLoading=true, imageError=null) }
            try {
                val doc = images.load(uri)
                mutable.update { it.copy(image=doc, sample=null) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(imageError=(e as? ToolFailure)?.messageRes ?: R.string.file_error) } }
            catch (_: OutOfMemoryError) { mutable.update { it.copy(imageError=R.string.memory_error) } }
            finally { if(currentCoroutineContext().isActive) mutable.update { it.copy(imageLoading=false) } }
        }
    }
    fun pause() { imageJob?.cancel(); imageJob=null; mutable.update { it.copy(imageLoading=false) } }
    fun clear() { pause(); mutable.value=ColorState() }
    fun sample(x: Int, y: Int) {
        val bitmap = mutable.value.image?.bitmap ?: return
        val px=x.coerceIn(0,bitmap.width-1); val py=y.coerceIn(0,bitmap.height-1)
        mutable.update { it.copy(sample=Sample(px,py,bitmap.getPixel(px,py))) }
    }
    fun moveSample(dx: Int, dy: Int) {
        val current=mutable.value; val bitmap=current.image?.bitmap ?: return
        sample((current.sample?.x ?: bitmap.width/2)+dx,(current.sample?.y ?: bitmap.height/2)+dy)
    }
}
