package code_SyS.todo_list.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val LightColors = lightColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    secondary = SageSecondary,
    onSecondary = Color(0xFF1E3329),
    tertiary = SandTertiary,
    onTertiary = Color(0xFF3D3113),
    surface = CreamSurface,
    onSurface = InkOnSurface,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = Color(0xFF6E655D),
    background = CreamSurface,
    onBackground = InkOnSurface,
    outline = WarmOutline,
    outlineVariant = CreamSurfaceVariant,
    error = RoseError,
    onError = Color.White,
    primaryContainer = Color(0xFFF9DCE4),
    onPrimaryContainer = Color(0xFF3D1A2A),
    secondaryContainer = Color(0xFFDDEAE3),
    onSecondaryContainer = Color(0xFF1E3329),
)

val DarkColors = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = Color(0xFF3D1A2A),
    secondary = SageSecondaryDark,
    onSecondary = Color(0xFF10201A),
    tertiary = SandTertiaryDark,
    onTertiary = Color(0xFF2C240D),
    surface = CocoaSurface,
    onSurface = CreamOnSurfaceDark,
    surfaceVariant = CocoaSurfaceVariant,
    onSurfaceVariant = Color(0xFFB5ACA4),
    background = CocoaSurface,
    onBackground = CreamOnSurfaceDark,
    outline = CocoaOutline,
    outlineVariant = CocoaSurfaceVariant,
    error = RoseErrorDark,
    onError = Color(0xFF3D0909),
    primaryContainer = Color(0xFF5A2A3C),
    onPrimaryContainer = Color(0xFFF9DCE4),
    secondaryContainer = Color(0xFF2E463C),
    onSecondaryContainer = Color(0xFFDDEAE3),
)

@Immutable
data class CustomShapes(
    val taskCard: RoundedCornerShape = RoundedCornerShape(24.dp),
    val widgetCard: RoundedCornerShape = RoundedCornerShape(20.dp),
    val bottomSheet: RoundedCornerShape = RoundedCornerShape(28.dp),
    val chip: RoundedCornerShape = RoundedCornerShape(100.dp),
    val fab: RoundedCornerShape = RoundedCornerShape(28.dp),
    val calendarCell: RoundedCornerShape = RoundedCornerShape(16.dp),
)

val LocalCustomShapes = staticCompositionLocalOf { CustomShapes() }

val MaterialTheme.customShapes: CustomShapes
    @Composable get() = LocalCustomShapes.current

private val AppShapes = Shapes()

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    androidx.compose.runtime.CompositionLocalProvider(LocalCustomShapes provides CustomShapes()) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
