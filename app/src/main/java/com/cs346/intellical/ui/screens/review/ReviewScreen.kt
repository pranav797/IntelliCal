package com.cs346.intellical.ui.screens.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = viewModel(),
    onConfirmClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isEditing) {
                BasicTextField(
                    value = uiState.mealName,
                    onValueChange = { viewModel.updateMealName(it) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.mealName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReviewMacroCard(
                    modifier = Modifier.weight(1f),
                    title = "Calories",
                    value = uiState.calories.toString(),
                    isEditing = isEditing,
                    onValueChange = { viewModel.updateCalories(it.toIntOrNull() ?: 0) },
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
                ReviewMacroCard(
                    modifier = Modifier.weight(1f),
                    title = "Carbs",
                    value = uiState.carbs.toString(),
                    isEditing = isEditing,
                    onValueChange = { viewModel.updateCarbs(it.toIntOrNull() ?: 0) },
                    suffix = "g",
                    icon = Icons.Default.Grass,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReviewMacroCard(
                    modifier = Modifier.weight(1f),
                    title = "Protein",
                    value = uiState.protein.toString(),
                    isEditing = isEditing,
                    onValueChange = { viewModel.updateProtein(it.toIntOrNull() ?: 0) },
                    suffix = "g",
                    icon = Icons.Default.Bolt,
                    iconTint = MaterialTheme.colorScheme.secondary
                )
                ReviewMacroCard(
                    modifier = Modifier.weight(1f),
                    title = "Fats",
                    value = uiState.fat.toString(),
                    isEditing = isEditing,
                    onValueChange = { viewModel.updateFat(it.toIntOrNull() ?: 0) },
                    suffix = "g",
                    icon = Icons.Default.WaterDrop,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }
        }

        HealthScoreCard(score = uiState.healthScore)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (uiState.description.isNotEmpty()) {
                Text(
                    text = uiState.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isEditing = !isEditing },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isEditing) "Save Changes" else "Edit Details",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                if (!isEditing) {
                    Button(
                        onClick = {
                            viewModel.confirmMeal()
                            onConfirmClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Log Meal",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewMacroCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    isEditing: Boolean = false,
    onValueChange: (String) -> Unit = {},
    suffix: String = "",
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = if (isEditing) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Row(verticalAlignment = Alignment.Bottom) {
                if (isEditing) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    )
                } else {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (suffix.isNotEmpty()) {
                    Text(
                        text = suffix,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HealthScoreCard(score: Float) {
    val scoreColor = when {
        score >= 7.5f -> Color(0xFF4CAF50)
        score >= 5.0f -> Color(0xFFFF9800)
        else          -> Color(0xFFF44336)
    }
    val label = when {
        score >= 8.0f -> "Excellent"
        score >= 6.5f -> "Good"
        score >= 5.0f -> "Fair"
        score >= 3.0f -> "Needs Work"
        else          -> "Unhealthy"
    }
    val displayScore = if (score % 1f == 0f) "${score.toInt()}" else "$score"

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Health Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = displayScore,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = " / 10",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(64.dp),
                        color = scoreColor.copy(alpha = 0.15f),
                        strokeWidth = 7.dp
                    )
                    CircularProgressIndicator(
                        progress = { score / 10f },
                        modifier = Modifier.size(64.dp),
                        color = scoreColor,
                        strokeWidth = 7.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = label,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            LinearProgressIndicator(
                progress = { score / 10f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = scoreColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}