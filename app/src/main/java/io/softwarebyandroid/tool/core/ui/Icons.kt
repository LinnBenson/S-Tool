package io.softwarebyandroid.tool.core.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Local outline vectors keep a consistent 24dp grid without a large icon dependency.
enum class Symbol(val path: String) {
 Watermark("M4,4 H20 V20 H4 Z M8,8 H16 M12,8 V16 M9,16 H15"),
 Back("M19,12 H5 M12,5 L5,12 L12,19"),
 Chevron("M9,5 L16,12 L9,19"),
 Vector("M7,5 H17 M19,7 V17 M17,19 H7 M5,17 V7 M3,3 H7 V7 H3 Z M17,3 H21 V7 H17 Z M3,17 H7 V21 H3 Z M17,17 H21 V21 H17 Z"),
 Dropper("M14,4 L20,10 M16,2 L22,8 L18,12 L12,6 Z M12,8 L3,17 L3,21 L7,21 L16,12"),
 Settings("M9,3 H15 L16,6 L19,7 L21,10 L19,13 L19,17 L16,18 L14,21 H10 L8,18 L5,17 L5,13 L3,10 L5,7 L8,6 Z M15,12 A3,3 0,1 1,9 12 A3,3 0,1 1,15 12"),
 Folder("M3,6 H9 L11,8 H21 V20 H3 Z"),
 Image("M3,3 H21 V21 H3 Z M3,17 L9,11 L13,15 L17,11 L21,15 M16,7 H16.01"),
 Download("M12,3 V15 M7,10 L12,15 L17,10 M4,16 V21 H20 V16"),
 Copy("M9,8 H21 V21 H9 Z M16,4 V3 H3 V16 H5"),
 Plus("M12,5 V19 M5,12 H19"), Minus("M5,12 H19"),
 Fit("M8,3 H3 V8 M16,3 H21 V8 M3,16 V21 H8 M16,21 H21 V16"),
 Close("M6,6 L18,18 M6,18 L18,6"),
 Info("M12,10 V17 M12,7 V7.1 M22,12 A10,10 0,1 1,2 12 A10,10 0,1 1,22 12"),
 Up("M5,15 L12,8 L19,15"), Down("M5,9 L12,16 L19,9"),
 Left("M15,5 L8,12 L15,19"), Right("M9,5 L16,12 L9,19")
}
@Composable fun ToolIcon(symbol: Symbol, description: String? = null, modifier: Modifier = Modifier, tint: Color = androidx.compose.material3.LocalContentColor.current) {
    val vector = remember(symbol) {
        ImageVector.Builder(symbol.name,24.dp,24.dp,24f,24f).apply {
            addPath(PathParser().parsePathString(symbol.path).toNodes(), fill=null, stroke=SolidColor(Color.Black), strokeLineWidth=1.8f,
                strokeLineCap=StrokeCap.Round, strokeLineJoin=StrokeJoin.Round)
        }.build()
    }
    Icon(vector, description, modifier, tint)
}
