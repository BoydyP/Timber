package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlin.math.max

@Composable
fun PlateCalculatorDialog(
    viewModel: PlateCalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Plate Calculator", style = MaterialTheme.typography.titleLarge)

            // Weight Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.targetWeight,
                    onValueChange = viewModel::onWeightChange,
                    label = { Text("Total Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.barbellWeight,
                    onValueChange = viewModel::onBarbellWeightChange,
                    label = { Text("Barbell") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Unit Toggle
                val isOverridden = uiState.unit != viewModel.weightUnitFromSettings
                val nonSelectedUnit =
                    if (viewModel.weightUnitFromSettings == WeightUnit.LB) WeightUnit.KG else WeightUnit.LB

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = viewModel.weightUnitFromSettings.toString(),
                        color = if (!isOverridden) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Switch(
                        checked = isOverridden,
                        onCheckedChange = { isChecked ->
                            val newUnit =
                                if (isChecked) nonSelectedUnit else viewModel.weightUnitFromSettings
                            viewModel.onUnitChange(newUnit)
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("unit_switch")
                    )
                    Text(
                        text = nonSelectedUnit.toString(),
                        color = if (isOverridden) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }

            // Available Plates
            Text("Available Plates", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.availablePlates.keys.sorted().forEach { plateWeight ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(plateWeight.toString())
                        OutlinedTextField(
                            value = uiState.availablePlates[plateWeight] ?: "",
                            onValueChange = { viewModel.onPlateQuantityChange(plateWeight, it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(50.dp)
                                .testTag("plate_quantity_${plateWeight}"),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                        )
                    }
                }
            }

            HorizontalDivider()

            Text("Required Plates", style = MaterialTheme.typography.titleMedium)
            Barbell(plates = uiState.platesOnBar, unit = uiState.unit)
        }
    }
}

@Composable
fun Barbell(plates: List<Double>, unit: WeightUnit) {
    val defaultPlateWidth = 20.dp
    val minPlateWidth = 10.dp
    val maxPlatesBeforeShrink = 7

    val plateWidth = if (plates.size <= maxPlatesBeforeShrink) {
        defaultPlateWidth
    } else {
        val calculatedWidth = defaultPlateWidth * (maxPlatesBeforeShrink.toFloat() / plates.size)
        max(minPlateWidth.value, calculatedWidth.value).dp
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        // Left side plates (in Right-to-Left layout direction)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start // Start in RTL means flush right
            ) {
                plates.sortedDescending()
                    .forEach { plateWeight -> Plate(weight = plateWeight, width = plateWidth, unit = unit) }
            }
        }

        // The Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(25.dp)
                    .width(10.dp)
                    .background(Color.DarkGray, RoundedCornerShape(2.dp))
            )
            Box(modifier = Modifier
                .height(8.dp)
                .width(80.dp)
                .background(Color.LightGray))
            Box(
                modifier = Modifier
                    .height(25.dp)
                    .width(10.dp)
                    .background(Color.DarkGray, RoundedCornerShape(2.dp))
            )
        }

        // Right side plates (in default Left-to-Right layout direction)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            plates.sortedDescending()
                .forEach { plateWeight -> Plate(weight = plateWeight, width = plateWidth, unit = unit) }
        }
    }
}

@Composable
fun Plate(weight: Double, width: Dp, unit: WeightUnit) {
    val height = if (unit == WeightUnit.KG) {
        when (weight) {
            25.0 -> 120.dp
            20.0 -> 110.dp
            15.0 -> 100.dp
            10.0 -> 85.dp
            5.0 -> 70.dp
            else -> 50.dp
        }
    } else { // LB
        when (weight) {
            45.0 -> 120.dp
            35.0 -> 110.dp
            25.0 -> 100.dp
            10.0 -> 85.dp
            5.0 -> 70.dp
            else -> 50.dp
        }
    }

    val (backgroundColor, textColor) = when (weight) {
        25.0 -> Color(0xFFD32F2F) to Color.White
        20.0 -> Color(0xFF1976D2) to Color.White
        15.0 -> Color(0xFFFBC02D) to Color.Black
        10.0 -> Color(0xFF388E3C) to Color.White
        5.0 -> Color.Black to Color.White
        1.25 -> Color.White to Color.Black
        45.0 -> Color(0xFF1976D2) to Color.White
        35.0 -> Color(0xFFFBC02D) to Color.Black
        else -> Color.DarkGray to Color.White
    }

    Box(
        modifier = Modifier
            .height(height)
            .width(width)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .testTag("plate_${weight}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (weight % 1 == 0.0) weight.toInt().toString() else weight.toString(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlateCalculatorDialogPreview() {
    TimberWorkoutLogsTheme {
        PlateCalculatorDialog()
    }
}
