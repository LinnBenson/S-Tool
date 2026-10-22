package io.softwarebyandroid.tool.feature.watermark

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.ui.*

@Composable fun WatermarkActions(state: WatermarkState, model: WatermarkController) {
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){it?.let(model::load)}
    val saver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png"),model::export)
    IconButton(onClick={picker.launch(arrayOf("image/*"))},enabled=!state.loading && !state.exporting) {
        ToolIcon(Symbol.Image,stringResource(R.string.select_image))
    }
    IconButton(onClick={model.prepareExport()?.let(saver::launch)},enabled=state.image!=null && state.overlay!=null && !state.loading && !state.rendering && !state.exporting && state.options.valid) {
        ToolIcon(Symbol.Download,stringResource(R.string.export_png))
    }
}
