package io.softwarebyandroid.tool.app

import io.softwarebyandroid.tool.R
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.softwarebyandroid.tool.core.data.*
import io.softwarebyandroid.tool.feature.color.*
import io.softwarebyandroid.tool.feature.svg.*
import io.softwarebyandroid.tool.feature.watermark.WatermarkController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Page { HOME, SVG, COLOR, WATERMARK, SETTINGS }
class AppViewModel(application: Application, private val saved: SavedStateHandle) : AndroidViewModel(application) {
    private val preferences = Preferences(application)
    val theme = preferences.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)
    val page = saved.getStateFlow("page", Page.HOME.name)
    private val messages = Channel<Int>(Channel.BUFFERED)
    val events = messages.receiveAsFlow()
    val svg = SvgController(application, viewModelScope) { messages.send(it) }
    val color = ColorController(application, viewModelScope) { messages.send(it) }
    val watermark=WatermarkController(application,viewModelScope) { messages.send(it) }
    init {
        // Remove URI state and long-lived grants created by earlier versions.
        saved.remove<String>("svgUri");saved.remove<String>("imageUri")
        application.contentResolver.persistedUriPermissions.forEach { permission ->
            val flags=(if(permission.isReadPermission) android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION else 0) or
                (if(permission.isWritePermission) android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
            runCatching { application.contentResolver.releasePersistableUriPermission(permission.uri,flags) }
        }
    }
    fun navigate(page: Page) {
        if(page!=Page.SVG) svg.clear()
        if(page!=Page.COLOR) color.clear()
        if(page!=Page.WATERMARK) watermark.clear()
        saved["page"] = page.name
    }
    fun releaseContent() { svg.clear();color.clear();watermark.clear() }
    override fun onCleared() { releaseContent();super.onCleared() }
    fun setTheme(mode: ThemeMode) { viewModelScope.launch {
        try { preferences.setTheme(mode) } catch (e: CancellationException) { throw e }
        catch (_: Exception) { messages.send(R.string.theme_error) }
    } }
}
