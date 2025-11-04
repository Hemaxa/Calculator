// app/src/main/java/com/example/calculator/ui/theme/Type.kt
package com.example.calculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Наша кастомная Typography
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light, // Как в вашем XML (sans-serif-light)
        fontSize = 32.sp
    )
    /* ... остальные стили по умолчанию ... */
)