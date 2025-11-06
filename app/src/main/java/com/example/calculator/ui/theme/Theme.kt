//файл тем приложения

package com.example.calculator.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

//атрибуты темы
data class CalculatorColors(
    val background: Color,
    val text: Color,
    val button: Color,
    val operatorButton: Color,
    val specialButton: Color
)

data class CalculatorShapes(
    val button: Shape,
    val operatorButton: Shape,
    val specialButton: Shape
)

//цветовые схемы для каждой темы
private val LightBlueColorScheme = CalculatorColors(
    background = md_theme_light_blue_background,
    text = md_theme_light_blue_text,
    button = md_theme_light_blue_button,
    operatorButton = md_theme_light_blue_operator,
    specialButton = md_theme_light_blue_special
)
private val DarkBlueColorScheme = CalculatorColors(
    background = md_theme_dark_blue_background,
    text = md_theme_dark_blue_text,
    button = md_theme_dark_blue_button,
    operatorButton = md_theme_dark_blue_operator,
    specialButton = md_theme_dark_blue_special
)

private val LightOrangeColorScheme = CalculatorColors(
    background = md_theme_light_orange_background,
    text = md_theme_light_orange_text,
    button = md_theme_light_orange_button,
    operatorButton = md_theme_light_orange_operator,
    specialButton = md_theme_light_orange_special
)
private val DarkOrangeColorScheme = CalculatorColors(
    background = md_theme_dark_orange_background,
    text = md_theme_dark_orange_text,
    button = md_theme_dark_orange_button,
    operatorButton = md_theme_dark_orange_operator,
    specialButton = md_theme_dark_orange_special
)

private val LightPinkColorScheme = CalculatorColors(
    background = md_theme_light_pink_background,
    text = md_theme_light_pink_text,
    button = md_theme_light_pink_button,
    operatorButton = md_theme_light_pink_operator,
    specialButton = md_theme_light_pink_special
)
private val DarkPinkColorScheme = CalculatorColors(
    background = md_theme_dark_pink_background,
    text = md_theme_dark_pink_text,
    button = md_theme_dark_pink_button,
    operatorButton = md_theme_dark_pink_operator,
    specialButton = md_theme_dark_pink_special
)

val LocalCalculatorColors = staticCompositionLocalOf { LightBlueColorScheme }
val LocalCalculatorShapes = staticCompositionLocalOf {
    CalculatorShapes(CircleShape, CircleShape, CircleShape)
}

enum class CalculatorThemeName {
    Blue, Orange, Pink
}

//главная Composable-функция темы
@Composable
fun CalculatorAppTheme(
    themeName: CalculatorThemeName = CalculatorThemeName.Blue,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when (themeName) {
        CalculatorThemeName.Blue -> if (darkTheme) DarkBlueColorScheme else LightBlueColorScheme
        CalculatorThemeName.Orange -> if (darkTheme) DarkOrangeColorScheme else LightOrangeColorScheme
        CalculatorThemeName.Pink -> if (darkTheme) DarkPinkColorScheme else LightPinkColorScheme
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val shapes = if (isLandscape) {
        CalculatorShapes(
            button = RoundedCornerShape(16.dp),
            operatorButton = RoundedCornerShape(16.dp),
            specialButton = RoundedCornerShape(16.dp)
        )
    } else {
        CalculatorShapes(
            button = CircleShape,
            operatorButton = CircleShape,
            specialButton = CircleShape
        )
    }

    CompositionLocalProvider(
        LocalCalculatorColors provides colors,
        LocalCalculatorShapes provides shapes
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme(background = colors.background)
            else lightColorScheme(background = colors.background),
            typography = Typography,
            content = content
        )
    }
}

//объект для легкого доступа к теме
object CalculatorTheme {
    val colors: CalculatorColors
        @Composable
        get() = LocalCalculatorColors.current
    val shapes: CalculatorShapes
        @Composable
        get() = LocalCalculatorShapes.current
}