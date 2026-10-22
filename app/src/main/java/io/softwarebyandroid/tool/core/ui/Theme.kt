package io.softwarebyandroid.tool.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.core.data.ThemeMode

private val Light = lightColorScheme(
    primary=Color(0xFF006B5B), onPrimary=Color.White, primaryContainer=Color(0xFFA6F5DF), onPrimaryContainer=Color(0xFF002019),
    secondary=Color(0xFF49645B), onSecondary=Color.White, secondaryContainer=Color(0xFFCCE8DC), onSecondaryContainer=Color(0xFF0D2019),
    tertiary=Color(0xFF426275), onTertiary=Color.White, tertiaryContainer=Color(0xFFC6E7FF), onTertiaryContainer=Color(0xFF001E2C),
    background=Color(0xFFF7FAF9), onBackground=Color(0xFF17211F), surface=Color(0xFFF7FAF9), onSurface=Color(0xFF17211F),
    surfaceVariant=Color(0xFFDBE5DF), onSurfaceVariant=Color(0xFF465651), outline=Color(0xFF6F7975), outlineVariant=Color(0xFFBFC9C3),
    surfaceContainer=Color(0xFFEBF1EE), surfaceContainerLow=Color(0xFFF0F5F2), surfaceContainerHigh=Color(0xFFE5EBE8), surfaceContainerHighest=Color(0xFFDFE6E2),
    surfaceContainerLowest=Color.White, surfaceDim=Color(0xFFD7DED9), surfaceBright=Color(0xFFF7FAF9), surfaceTint=Color(0xFF006B5B),
    inverseSurface=Color(0xFF29332F), inverseOnSurface=Color(0xFFEDF4EF), inversePrimary=Color(0xFF27E8C2),
    error=Color(0xFFBA1A1A), onError=Color.White, errorContainer=Color(0xFFFFDAD6), onErrorContainer=Color(0xFF410002)
)
private val Dark = darkColorScheme(
    primary=Color(0xFF27E8C2), onPrimary=Color(0xFF00382E), primaryContainer=Color(0xFF005143), onPrimaryContainer=Color(0xFFA6F5DF),
    secondary=Color(0xFFB0CCBF), onSecondary=Color(0xFF1C352B), secondaryContainer=Color(0xFF334C41), onSecondaryContainer=Color(0xFFCCE8DC),
    tertiary=Color(0xFFA9CBE1), onTertiary=Color(0xFF103446), tertiaryContainer=Color(0xFF294B5D), onTertiaryContainer=Color(0xFFC6E7FF),
    background=Color(0xFF0D151C), onBackground=Color(0xFFE1E9E6), surface=Color(0xFF0D151C), onSurface=Color(0xFFE1E9E6),
    surfaceVariant=Color(0xFF34443E), onSurfaceVariant=Color(0xFFBACAC3), outline=Color(0xFF84948D), outlineVariant=Color(0xFF3E4C46),
    surfaceContainer=Color(0xFF19232A), surfaceContainerLow=Color(0xFF151F25), surfaceContainerHigh=Color(0xFF243038), surfaceContainerHighest=Color(0xFF2E3A42),
    surfaceContainerLowest=Color(0xFF081016), surfaceDim=Color(0xFF0D151C), surfaceBright=Color(0xFF303C43), surfaceTint=Color(0xFF27E8C2),
    inverseSurface=Color(0xFFE1E9E6), inverseOnSurface=Color(0xFF29332F), inversePrimary=Color(0xFF006B5B),
    error=Color(0xFFFFB4AB), onError=Color(0xFF690005), errorContainer=Color(0xFF93000A), onErrorContainer=Color(0xFFFFDAD6)
)
object Space { val xs=4.dp; val sm=8.dp; val md=12.dp; val page=16.dp; val section=24.dp; val large=32.dp }
@Composable fun SToolTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && isSystemInDarkTheme())
    MaterialTheme(colorScheme=if(dark) Dark else Light, typography=Typography(),
        shapes=Shapes(small=RoundedCornerShape(12.dp), medium=RoundedCornerShape(16.dp), large=RoundedCornerShape(28.dp)), content=content)
}
