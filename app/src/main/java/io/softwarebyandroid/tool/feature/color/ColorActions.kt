package io.softwarebyandroid.tool.feature.color

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.ui.*
import io.softwarebyandroid.tool.core.data.colorHex

@Composable fun ColorActions(state: ColorState, model: ColorController, copy: (String)->Unit) {
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){it?.let(model::loadImage)}
    IconButton(onClick={picker.launch(arrayOf("image/*"))},enabled=!state.imageLoading) {
        ToolIcon(Symbol.Image,stringResource(R.string.select_image))
    }
    IconButton(onClick={state.sample?.let{copy(colorHex(it.argb))}},enabled=state.sample!=null && !state.imageLoading) {
        ToolIcon(Symbol.Copy,stringResource(R.string.copy_color))
    }
}
