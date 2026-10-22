package io.softwarebyandroid.tool.feature.watermark

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.io.IOException

private val Context.watermarkSettings by preferencesDataStore("watermark_settings")

/** Only the last explicitly edited parameters. Never stores images or file identifiers. */
internal class WatermarkPreferences(context: Context) {
    private val store = context.applicationContext.watermarkSettings
    private val text = stringPreferencesKey("text")
    private val color = stringPreferencesKey("color")
    private val size = floatPreferencesKey("size")
    private val horizontal = floatPreferencesKey("horizontal")
    private val vertical = floatPreferencesKey("vertical")
    private val opacity = floatPreferencesKey("opacity")
    private val angle = floatPreferencesKey("angle")
    suspend fun read(): WatermarkOptions? {
        val p = try { store.data.first() } catch (_: IOException) { return null }
        return WatermarkOptions(
            text = p[text] ?: return null, colorHex = p[color] ?: "#FFFFFF",
            fontSize = p[size] ?: 24f, gapX = p[horizontal] ?: 80f,
            gapY = p[vertical] ?: 60f, opacity = p[opacity] ?: .3f, angle = p[angle] ?: -30f
        ).takeIf { it.valid }
    }
    suspend fun write(value: WatermarkOptions) {
        store.edit {
            it[text] = value.text; it[color] = value.colorHex; it[size] = value.fontSize
            it[horizontal] = value.gapX; it[vertical] = value.gapY
            it[opacity] = value.opacity; it[angle] = value.angle
        }
    }
}
