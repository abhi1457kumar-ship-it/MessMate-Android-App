package com.messmate.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class MessMateColors(
    val Canvas: Color,
    val Paper: Color,
    val Forest: Color,
    val ForestDark: Color,
    val Saffron: Color,
    val SaffronSoft: Color,
    val Sage: Color,
    val SageDark: Color,
    val Ink: Color,
    val Muted: Color,
    val Line: Color,
    val White: Color,
)

private val LightColors = MessMateColors(
    Canvas = Color(0xFFF7F5EF), Paper = Color(0xFFFFFEFA), Forest = Color(0xFF173F35),
    ForestDark = Color(0xFF10372F), Saffron = Color(0xFFDF8443), SaffronSoft = Color(0xFFF6E7D9),
    Sage = Color(0xFFE5EEE7), SageDark = Color(0xFF547667), Ink = Color(0xFF202A25),
    Muted = Color(0xFF7C837D), Line = Color(0xFFE8E5DC), White = Color(0xFFFFFFFF),
)

private val DarkColors = MessMateColors(
    Canvas = Color(0xFF101915), Paper = Color(0xFF1B2721), Forest = Color(0xFFAED5C0),
    ForestDark = Color(0xFF244C3A), Saffron = Color(0xFFF0A36B), SaffronSoft = Color(0xFF493326),
    Sage = Color(0xFF293B32), SageDark = Color(0xFFB2C9B9), Ink = Color(0xFFEAF1EC),
    Muted = Color(0xFFA5B2AA), Line = Color(0xFF34443B), White = Color(0xFFFFFFFF),
)

private val LocalMessMateColors = staticCompositionLocalOf { LightColors }

/** Existing screens read this palette, which now follows the selected light or dark theme. */
object MessMatePalette {
    val Canvas: Color @Composable get() = LocalMessMateColors.current.Canvas
    val Paper: Color @Composable get() = LocalMessMateColors.current.Paper
    val Forest: Color @Composable get() = LocalMessMateColors.current.Forest
    val ForestDark: Color @Composable get() = LocalMessMateColors.current.ForestDark
    val Saffron: Color @Composable get() = LocalMessMateColors.current.Saffron
    val SaffronSoft: Color @Composable get() = LocalMessMateColors.current.SaffronSoft
    val Sage: Color @Composable get() = LocalMessMateColors.current.Sage
    val SageDark: Color @Composable get() = LocalMessMateColors.current.SageDark
    val Ink: Color @Composable get() = LocalMessMateColors.current.Ink
    val Muted: Color @Composable get() = LocalMessMateColors.current.Muted
    val Line: Color @Composable get() = LocalMessMateColors.current.Line
    val White: Color @Composable get() = LocalMessMateColors.current.White
}

private val MessMateTypography = Typography(
    displaySmall = Typography().displaySmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 38.sp,
        lineHeight = 43.sp,
        letterSpacing = (-0.7).sp,
    ),
    headlineLarge = Typography().headlineLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 25.sp,
        lineHeight = 31.sp,
    ),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = Typography().bodyLarge.copy(fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = Typography().bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
)

private val MessMateShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MessMateTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkColors else LightColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.Forest,
            onPrimary = Color(0xFF12372A),
            secondary = palette.Saffron,
            onSecondary = Color(0xFF40220E),
            tertiary = palette.SageDark,
            background = palette.Canvas,
            onBackground = palette.Ink,
            surface = palette.Paper,
            onSurface = palette.Ink,
            surfaceVariant = palette.Sage,
            onSurfaceVariant = palette.SageDark,
            outline = palette.Line,
        )
    } else {
        lightColorScheme(
            primary = palette.Forest,
            onPrimary = palette.White,
            secondary = palette.Saffron,
            onSecondary = palette.White,
            tertiary = palette.SageDark,
            background = palette.Canvas,
            onBackground = palette.Ink,
            surface = palette.Paper,
            onSurface = palette.Ink,
            surfaceVariant = palette.Sage,
            onSurfaceVariant = palette.SageDark,
            outline = palette.Line,
        )
    }
    CompositionLocalProvider(LocalMessMateColors provides palette) {
        MaterialTheme(colorScheme = scheme, typography = MessMateTypography, shapes = MessMateShapes, content = content)
    }
}
