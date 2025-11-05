//интерфейс окна калькулятора

package com.example.calculator

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.ui.theme.CalculatorThemeName

//CalculatorScreen - главный контейнер для всего окна калькулятора
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    currentThemeName: CalculatorThemeName,
    onThemeChange: (CalculatorThemeName) -> Unit
) {
    val displayText by viewModel.currentInput.observeAsState("0")

    Scaffold(
        topBar = {
            CalculatorTopAppBar(
                currentThemeName = currentThemeName,
                onThemeChange = onThemeChange
            )
        },
        containerColor = CalculatorTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val configuration = LocalConfiguration.current
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                PortraitLayout(viewModel, displayText)
            } else {
                LandscapeLayout(viewModel, displayText)
            }
        }
    }
}

//вертикалььный режим
@Composable
fun PortraitLayout(viewModel: CalculatorViewModel, displayText: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        //дисплей
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            DisplayTextView(text = displayText, fontSize = 80.sp)
        }
        //кнопки
        ButtonGrid(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            buttonSize = 80.dp,
            fontSize = 32.sp
        )
    }
}

//горизонтальный режим
@Composable
fun LandscapeLayout(viewModel: CalculatorViewModel, displayText: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            DisplayTextView(text = displayText, fontSize = 60.sp)
        }
        ButtonGrid(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            buttonSize = Dp.Unspecified,
            fontSize = 26.sp,
            fillHeight = true
        )
    }
}

//сетка для кнопок
@Composable
fun ButtonGrid(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier,
    buttonSize: Dp,
    fontSize: TextUnit,
    fillHeight: Boolean = false
) {
    val buttons = listOf(
        "AC", "xʸ", "√", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "0", ".", "±", "="
    )

    Column(
        modifier = modifier.then(if (fillHeight) Modifier.fillMaxHeight() else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (fillHeight) Arrangement.SpaceEvenly else Arrangement.spacedBy(8.dp)
    ) {
        buttons.chunked(4).forEach { rowButtons ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (fillHeight) Modifier.weight(1f) else Modifier), // Строки растягиваются
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                for (text in rowButtons) {
                    val (color, shape) = when (text) {
                        "AC", "xʸ", "√" -> CalculatorTheme.colors.specialButton to CalculatorTheme.shapes.specialButton
                        "÷", "×", "-", "+", "=", "±" -> CalculatorTheme.colors.operatorButton to CalculatorTheme.shapes.operatorButton
                        else -> CalculatorTheme.colors.button to CalculatorTheme.shapes.button
                    }
                    val onClick = {
                        when (text) {
                            in "0".."9" -> viewModel.onDigitPressed(text)
                            "." -> viewModel.onDecimalPointPressed()
                            "AC" -> viewModel.onClearPressed()
                            "√" -> viewModel.onRootPressed()
                            "±" -> viewModel.onSignPressed()
                            "=" -> viewModel.onEqualsPressed()
                            else -> viewModel.onOperatorPressed(text)
                        }
                    }

                    val buttonModifier = if (fillHeight) {
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp)
                    } else {
                        Modifier.size(buttonSize)
                    }

                    CalculatorButton(
                        text = text,
                        onClick = onClick,
                        backgroundColor = color,
                        shape = shape,
                        modifier = buttonModifier,
                        baseFontSize = fontSize
                    )
                }
            }
        }
    }
}

//дисплей
@Composable
fun DisplayTextView(text: String, fontSize: androidx.compose.ui.unit.TextUnit = 80.sp) {
    val scrollState = rememberScrollState()
    LaunchedEffect(text) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    Text(
        text = text,
        color = CalculatorTheme.colors.text,
        fontSize = fontSize,
        fontWeight = FontWeight.Light,
        fontFamily = FontFamily.SansSerif,
        textAlign = TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Visible,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState, reverseScrolling = true)
    )
}

//кнопка
@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    baseFontSize: TextUnit = 32.sp
) {
    val fontSize = if (text.length > 1) (baseFontSize.value * 0.85f).sp else baseFontSize

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = CalculatorTheme.colors.text
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(0.dp) // Убираем внутренние отступы
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Light
        )
    }
}

//панель названия программы
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTopAppBar(
    currentThemeName: CalculatorThemeName,
    onThemeChange: (CalculatorThemeName) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Калькулятор", color = CalculatorTheme.colors.text) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CalculatorTheme.colors.background
        ),
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Сменить тему",
                    tint = CalculatorTheme.colors.text
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Синяя тема") },
                    onClick = {
                        onThemeChange(CalculatorThemeName.Blue)
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Оранжевая тема") },
                    onClick = {
                        onThemeChange(CalculatorThemeName.Orange)
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Розовая тема") },
                    onClick = {
                        onThemeChange(CalculatorThemeName.Pink)
                        menuExpanded = false
                    }
                )
            }
        }
    )
}