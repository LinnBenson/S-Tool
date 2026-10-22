package io.softwarebyandroid.tool.feature.svg

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


data class SvgState(val svg: SvgDocument? = null, val svgLoading: Boolean = false, val exporting: Boolean = false, val svgError: Int? = null)
class SvgController(application: Application,
    private val scope: CoroutineScope, private val notify: suspend (Int) -> Unit) {
    private val resolver = application.contentResolver
    private val svgs = SvgRepository(resolver)
    private var exportJob: Job? = null
    private var svgJob: Job? = null
    private var pendingExport: SvgDocument? = null
    private val mutable = MutableStateFlow(SvgState())
    val state = mutable.asStateFlow()
    fun loadSvg(uri: Uri) {
        svgJob?.cancel()
        svgJob = scope.launch {
            mutable.update { it.copy(svgLoading=true, svgError=null) }
            try {
                val doc = svgs.load(uri)
                mutable.update { it.copy(svg=doc) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(svgError=(e as? ToolFailure)?.messageRes ?: R.string.file_error) } }
            catch (_: OutOfMemoryError) { mutable.update { it.copy(svgError=R.string.memory_error) } }
            finally { if(currentCoroutineContext().isActive) mutable.update { it.copy(svgLoading=false) } }
        }
    }
    fun pause() { svgJob?.cancel(); exportJob?.cancel(); svgJob=null; exportJob=null
        mutable.update { it.copy(svgLoading=false,exporting=false) }
    }
    fun clear() { pause(); pendingExport=null; mutable.value=SvgState() }
    fun prepareExport(): String? {
        val doc=mutable.value.svg ?: return null
        pendingExport=doc
        return doc.name.substringBeforeLast('.',doc.name)+".png"
    }
    fun export(uri: Uri?) {
        val doc=pendingExport ?: mutable.value.svg
        pendingExport=null
        if(uri == null || doc == null || mutable.value.exporting) return
        exportJob=scope.launch {
            mutable.update { it.copy(exporting=true) }
            try { svgs.export(doc,uri); notify(R.string.saved) }
            catch(e: CancellationException) { throw e }
            catch(_: Exception) { notify(R.string.export_error) }
            catch(_: OutOfMemoryError) { notify(R.string.memory_error) }
            finally { if(currentCoroutineContext().isActive) mutable.update { it.copy(exporting=false) } }
        }
    }
}
