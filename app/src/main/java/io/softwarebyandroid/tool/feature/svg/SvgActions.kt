package io.softwarebyandroid.tool.feature.svg

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.ui.*

@Composable fun SvgActions(state: SvgState, model: SvgController) {
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){it?.let(model::loadSvg)}
    val saver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png"),model::export)
    IconButton(onClick={picker.launch(arrayOf("*/*"))},enabled=!state.svgLoading && !state.exporting) {
        ToolIcon(Symbol.Folder,stringResource(R.string.select_file))
    }
    IconButton(onClick={model.prepareExport()?.let(saver::launch)},enabled=state.svg!=null && !state.svgLoading && !state.exporting) {
        ToolIcon(Symbol.Download,stringResource(R.string.export_png))
    }
}
