package io.softwarebyandroid.tool.feature.color

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.*
import io.softwarebyandroid.tool.core.ui.*

/** Compact readout with an explicit copy action for each color representation. */
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun ColorDetails(sample: Sample?, copy: (String) -> Unit, move: (Int, Int) -> Unit) {
    val hexCopy=stringResource(R.string.copy_hex)
    val rgbCopy=stringResource(R.string.copy_rgb)
    Surface(color=MaterialTheme.colorScheme.surfaceContainer,shape=MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(horizontal=Space.md,vertical=Space.sm)) {
            if(sample==null) {
                Text(stringResource(R.string.no_color),style=MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.color_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Space.md)) {
                    Canvas(Modifier.size(40.dp).clip(MaterialTheme.shapes.small).border(1.dp,MaterialTheme.colorScheme.outlineVariant,MaterialTheme.shapes.small)) {
                        checkerboard();drawRect(Color(sample.argb))
                    }
                    FlowRow(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(Space.xs)) {
                        TextButton(onClick={copy(colorHex(sample.argb))},modifier=Modifier.semantics { contentDescription=hexCopy }) {
                            Text(colorHex(sample.argb),fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.width(Space.sm));ToolIcon(Symbol.Copy)
                        }
                        TextButton(onClick={copy(colorRgb(sample.argb))},modifier=Modifier.semantics { contentDescription=rgbCopy }) {
                            Column {
                                Text(stringResource(R.string.rgb_label),style=MaterialTheme.typography.bodySmall)
                                Text(colorRgb(sample.argb),fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.width(Space.sm));ToolIcon(Symbol.Copy)
                        }
                    }
                }
                FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,
                    verticalArrangement=Arrangement.spacedBy(Space.xs)) {
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text(stringResource(R.string.pixel_position,sample.x,sample.y),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${stringResource(R.string.alpha_label)} ${alphaPercent(sample.argb)}%",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row {
                        listOf(Triple(Symbol.Left,R.string.move_left,-1 to 0),Triple(Symbol.Up,R.string.move_up,0 to -1),
                            Triple(Symbol.Down,R.string.move_down,0 to 1),Triple(Symbol.Right,R.string.move_right,1 to 0)).forEach { (symbol,label,delta) ->
                            IconButton(onClick={move(delta.first,delta.second)}) { ToolIcon(symbol,stringResource(label)) }
                        }
                    }
                }
            }
        }
    }
}
