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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme

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
                        modifier = Modifier.padding(horizontal = 8.dp)
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
                            modifier = Modifier.width(50.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                        )
                    }
                }
            }

            HorizontalDivider()

            Text("Required Plates", style = MaterialTheme.typography.titleMedium)
            Barbell(plates = uiState.platesOnBar)
        }
    }
}

@Composable
fun Barbell(plates: List<Double>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        // Left side plates (sorted smallest to largest)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            plates.sorted().forEach { plateWeight -> Plate(weight = plateWeight) }
        }

        // The Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(25.dp)
                    .width(10.dp)
                    .background(Color.DarkGray, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(80.dp)
                    .background(Color.LightGray)
            )
            Box(
                modifier = Modifier
                    .height(25.dp)
                    .width(10.dp)
                    .background(Color.DarkGray, RoundedCornerShape(2.dp))
            )
        }

        // Right side plates (sorted largest to smallest)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            plates.sortedDescending().forEach { plateWeight -> Plate(weight = plateWeight) }
        }
    }
}

@Composable
fun Plate(weight: Double) {
    val height = when (weight) {
        25.0, 45.0 -> 120.dp
        20.0, 35.0 -> 110.dp
        15.0, 25.0 -> 100.dp
        10.0 -> 85.dp
        5.0 -> 70.dp
        else -> 50.dp
    }

    val (backgroundColor, textColor) = when (weight) {
        // IWF KG Colors (prioritized)
        25.0 -> Color(0xFFD32F2F) to Color.White // Red
        20.0 -> Color(0xFF1976D2) to Color.White // Blue
        15.0 -> Color(0xFFFBC02D) to Color.Black // Yellow
        10.0 -> Color(0xFF388E3C) to Color.White // Green
        5.0 -> Color.Black to Color.White
        1.25 -> Color.White to Color.Black

        // Common LB plate colors that don't conflict
        45.0 -> Color(0xFF1976D2) to Color.White // Blue (like 20kg)
        35.0 -> Color(0xFFFBC02D) to Color.Black // Yellow (like 15kg)

        // Default for fractional plates (e.g., 2.5kg, 5lb, 2.5lb)
        else -> Color.DarkGray to Color.White
    }

    Box(
        modifier = Modifier
            .height(height)
            .width(20.dp)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
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
