package com.android.timberworkoutlogs.ui.screen.exercise.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.models.isDone
import com.android.timberworkoutlogs.models.toStringResource
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import com.android.timberworkoutlogs.util.getIconForEquipment

@Composable
fun ExerciseInputCard(
    exerciseDefinition: ExerciseDefinition?,
    workoutExercise: WorkoutExercise,
    unit: WeightUnit,
    onAddSet: () -> Unit,
    onDeleteSet: (ExerciseSet) -> Unit,
    onSetChanged: (setIndex: Int, updatedSet: ExerciseSet) -> Unit,
    onExerciseUnitChange: (newUnit: WeightUnit) -> Unit,
    onNavigateToSelectExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSetsDone = workoutExercise.sets.isNotEmpty() && workoutExercise.sets.all { it.isDone }
    var manuallyExpanded by remember(workoutExercise.id) { mutableStateOf<Boolean?>(null) }
    val expanded = manuallyExpanded ?: !allSetsDone

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToSelectExercise),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (exerciseDefinition != null) {
                            Icon(
                                imageVector = getIconForEquipment(exerciseDefinition.equipment),
                                contentDescription = "Equipment type",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = exerciseDefinition?.computedExerciseName ?: "Select Exercise...",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (exerciseDefinition != null && workoutExercise.sets.isNotEmpty()) {
                    IconButton(onClick = { manuallyExpanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse sets" else "Expand sets"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (exerciseDefinition != null) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        workoutExercise.sets.forEachIndexed { index, set ->
                            SwipeToDeleteContainer(
                                item = set,
                                onDismiss = onDeleteSet
                            ) {
                                when (set) {
                                    is WeightAndRepsSet -> WeightAndRepsInputRow(
                                        setNumber = index + 1,
                                        workoutSet = set,
                                        unit = workoutExercise.unit,
                                        onWeightChange = { newWeight ->
                                            onSetChanged(index, set.copy(weight = newWeight))
                                        },
                                        onRepsChange = { newReps ->
                                            onSetChanged(index, set.copy(reps = newReps))
                                        },
                                        onDoneChange = { isDone ->
                                            onSetChanged(index, set.copy(isDone = isDone))
                                        }
                                    )

                                    is RepsOnlySet -> RepsOnlyInputRow(
                                        setNumber = index + 1,
                                        workoutSet = set,
                                        onRepsChange = { newReps ->
                                            onSetChanged(index, set.copy(reps = newReps))
                                        },
                                        onDoneChange = { isDone ->
                                            onSetChanged(index, set.copy(isDone = isDone))
                                        }
                                    )

                                    is TimedSet -> TimedInputRow(
                                        setNumber = index + 1,
                                        workoutSet = set,
                                        onDurationChange = { newDuration ->
                                            onSetChanged(index, set.copy(durationSeconds = newDuration))
                                        },
                                        onDoneChange = { isDone ->
                                            onSetChanged(index, set.copy(isDone = isDone))
                                        }
                                    )

                                    is DistanceAndTimeSet -> DistanceAndTimeInputRow(
                                        setNumber = index + 1,
                                        workoutSet = set,
                                        onDistanceChange = { newDistance ->
                                            onSetChanged(index, set.copy(distance = newDistance))
                                        },
                                        onDurationChange = { newDuration ->
                                            onSetChanged(index, set.copy(durationSeconds = newDuration))
                                        },
                                        onDoneChange = { isDone ->
                                            onSetChanged(index, set.copy(isDone = isDone))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (exerciseDefinition?.logType == LogType.WEIGHT_AND_REPS) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        stringResource(id = WeightUnit.KG.toStringResource()),
                        fontWeight = if (workoutExercise.unit == WeightUnit.KG) FontWeight.Bold else FontWeight.Normal
                    )
                    Switch(
                        checked = workoutExercise.unit == WeightUnit.LB,
                        onCheckedChange = { isLbs ->
                            onExerciseUnitChange(if (isLbs) WeightUnit.LB else WeightUnit.KG)
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        stringResource(id = WeightUnit.LB.toStringResource()),
                        fontWeight = if (workoutExercise.unit == WeightUnit.LB) FontWeight.Bold else FontWeight.Normal
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            IconButton(onClick = onAddSet, enabled = exerciseDefinition != null) {
                Icon(
                    Icons.Filled.AddCircle,
                    contentDescription = "Add Set",
                    tint = if (exerciseDefinition != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("AddSetIcon")
                )
            }
        }
    }
}

@Preview
@Composable
fun ExerciseInputCardPreview() {
    val exerciseDefinition = ExerciseDefinition(
        name = "Bench Press",
        equipment = ExerciseEquipment.BARBELL,
        muscleGroups = emptyList(),
        logType = LogType.WEIGHT_AND_REPS
    )
    val workoutExercise = WorkoutExercise(
        workoutId = 1L,
        definitionId = exerciseDefinition.id,
        sets = listOf(
            WeightAndRepsSet(weight = 100.0, reps = 10, isDone = true),
            WeightAndRepsSet(weight = 100.0, reps = 8, isDone = false)
        )
    )
    TimberWorkoutLogsTheme {
        ExerciseInputCard(
            exerciseDefinition = exerciseDefinition,
            workoutExercise = workoutExercise,
            onAddSet = {},
            onDeleteSet = {},
            onSetChanged = { _, _ -> },
            onExerciseUnitChange = {},
            unit = WeightUnit.KG,
            onNavigateToSelectExercise = {})
    }
}
