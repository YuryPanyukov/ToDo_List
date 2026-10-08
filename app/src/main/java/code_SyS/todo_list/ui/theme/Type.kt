package code_SyS.todo_list.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import code_SyS.todo_list.R

/**
 * Playfair Display (заголовки) и Nunito Sans (весь остальной текст).
 * Статические инстансы генерируются скриптом `tools/generate_fonts.py`
 * из вариативных шрифтов Google Fonts (см. `tools/fonts/`, лицензия OFL).
 */
private val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
)

private val NunitoSans = FontFamily(
    Font(R.font.nunito_sans_regular, FontWeight.Normal),
    Font(R.font.nunito_sans_medium, FontWeight.Medium),
    Font(R.font.nunito_sans_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_sans_bold, FontWeight.Bold),
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontSize = 44.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.25).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoSans,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoSans,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoSans,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = NunitoSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
    ),
)
