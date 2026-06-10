package com.cs346.intellical.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs346.intellical.domain.CalorieCalculator
import com.cs346.intellical.domain.MacroDistribution
import com.cs346.intellical.domain.ValidationResult
import com.cs346.intellical.domain.WeightValidator
import com.cs346.intellical.ui.theme.LocalThemeState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeState = LocalThemeState.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            WeightProgressCard(
                currentWeight = uiState.currentWeight,
                targetWeight = uiState.targetWeight,
                targetDate = uiState.targetDate,
                weeklyRate = uiState.weeklyRate,
                isLosing = uiState.isLosing,
                onSave = { currentWeight, targetWeight, targetDate ->
                    viewModel.saveWeightProgress(currentWeight, targetWeight, targetDate)
                }
            )

            MacroGoalsCard(
                dailyCalories = uiState.dailyCalories,
                proteinGrams = uiState.proteinGrams,
                carbsGrams = uiState.carbsGrams,
                fatGrams = uiState.fatGrams,
                selectedDistribution = uiState.selectedDistribution,
                onDistributionChange = { viewModel.onDistributionChange(it) }
            )

            SettingsToggleButton(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                isChecked = themeState.isDarkMode,
                onCheckedChange = { themeState.toggleDarkMode() }
            )

            SettingsButton(
                icon = Icons.Default.Info,
                title = "About IntelliCal",
                onClick = onAboutClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Logout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightProgressCard(
    currentWeight: Float,
    targetWeight: Float,
    targetDate: String,
    weeklyRate: Float,
    isLosing: Boolean,
    onSave: (Float, Float, String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var tempCurrentWeight by remember(currentWeight) { mutableStateOf(currentWeight.toInt().toString()) }
    var tempTargetWeight by remember(targetWeight) { mutableStateOf(targetWeight.toInt().toString()) }
    var tempTargetDate by remember(targetDate) { mutableStateOf(targetDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationErrorTitle by remember { mutableStateOf<String?>(null) }
    var validationErrorMessage by remember { mutableStateOf<String?>(null) }
    val today = remember { LocalDate.now() }
    val parsedCurrentWeightForValidation = tempCurrentWeight.toDoubleOrNull() ?: currentWeight.toDouble()
    val parsedTargetWeightForValidation = tempTargetWeight.toDoubleOrNull() ?: targetWeight.toDouble()
    val earliestSafeDate = remember(parsedCurrentWeightForValidation, parsedTargetWeightForValidation) {
        earliestAllowedTargetDate(
            currentWeight = parsedCurrentWeightForValidation,
            targetWeight = parsedTargetWeightForValidation,
            currentDate = today
        )
    }
    val earliestSelectableDate = remember(earliestSafeDate) {
        maxOf(today, earliestSafeDate)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = if (isEditing) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            isEditing = false
                            tempCurrentWeight = currentWeight.toInt().toString()
                            tempTargetWeight = targetWeight.toInt().toString()
                            tempTargetDate = targetDate
                            validationErrorTitle = null
                            validationErrorMessage = null
                            showDatePicker = false
                        }) {
                            Text(
                                text = "Cancel",
                                maxLines = 1
                            )
                        }
                        Button(
                            onClick = {
                                val parsedCurrentWeight = tempCurrentWeight.toFloatOrNull()
                                val parsedTargetWeight = tempTargetWeight.toFloatOrNull()
                                val parsedTargetDate = parseTargetDate(tempTargetDate)
                                val validatedCurrentWeight = parsedCurrentWeight?.toDouble() ?: currentWeight.toDouble()
                                val validatedTargetWeight = parsedTargetWeight?.toDouble() ?: targetWeight.toDouble()
                                val earliestAllowedDate = earliestAllowedTargetDate(
                                    currentWeight = validatedCurrentWeight,
                                    targetWeight = validatedTargetWeight,
                                    currentDate = today
                                )
                                val weightValidationResult = WeightValidator.validateTargetWeight(
                                    currentWeight = validatedCurrentWeight,
                                    targetWeight = validatedTargetWeight
                                )

                                if (parsedCurrentWeight == null || parsedTargetWeight == null) {
                                    validationErrorTitle = "Invalid Target Weight"
                                    validationErrorMessage = "This is an unhealthy target weight"
                                    tempTargetWeight = targetWeight.toInt().toString()
                                } else if (weightValidationResult is ValidationResult.Invalid) {
                                    tempTargetWeight = targetWeight.toInt().toString()
                                    validationErrorTitle = "Invalid Target Weight"
                                    validationErrorMessage = when {
                                        validatedTargetWeight < WeightValidator.ABSOLUTE_MIN_WEIGHT_LBS -> "This is an unhealthy target weight"
                                        validatedTargetWeight > WeightValidator.ABSOLUTE_MAX_WEIGHT_LBS -> "This is an unhealthy target weight"
                                        else -> "This weight is below your recommended minimum weight"
                                    }
                                } else if (parsedTargetDate == null || parsedTargetDate.isBefore(today)) {
                                    tempTargetDate = targetDate
                                    showDatePicker = false
                                    validationErrorTitle = "Invalid Target Date"
                                    validationErrorMessage = "Invalid date. You are not allowed to enter a date before the current date."
                                } else if (parsedTargetDate.isBefore(earliestAllowedDate)) {
                                    tempTargetDate = targetDate
                                    showDatePicker = false
                                    validationErrorTitle = "Invalid Target Date"
                                    validationErrorMessage = "The date you have chosen is unreasonable and does not provide enough time to reach your target in a healthy manner."
                                } else if (parsedCurrentWeight != null && parsedTargetWeight != null) {
                                    onSave(parsedCurrentWeight, parsedTargetWeight, tempTargetDate)
                                    validationErrorTitle = null
                                    validationErrorMessage = null
                                    isEditing = false
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Save",
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = tempCurrentWeight,
                                onValueChange = { tempCurrentWeight = it },
                                label = { Text("Current (lbs)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = tempTargetWeight,
                                onValueChange = { tempTargetWeight = it },
                                label = { Text("Target (lbs)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = tempTargetDate,
                                onValueChange = {},
                                label = { Text("Target Date") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Select target date"
                                    )
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showDatePicker = true }
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeightStatItem("Current", "${currentWeight.toInt()} lbs")
                    WeightStatItem("Target", "${targetWeight.toInt()} lbs")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeightStatItem("Target Date", targetDate)
                WeightStatItem(
                    label = "Weekly Rate",
                    value = "${if (isLosing) "-" else "+"}${String.format(Locale.ENGLISH, "%.2f", weeklyRate)} lbs/wk"
                )
            }
        }
    }

    if (showDatePicker) {
        val selectedDate = parseTargetDate(tempTargetDate) ?: today
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = maxOf(selectedDate, earliestSelectableDate)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val selected = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    return !selected.isBefore(earliestSelectableDate)
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year >= earliestSelectableDate.year
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            tempTargetDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (validationErrorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                validationErrorTitle = null
                validationErrorMessage = null
            },
            confirmButton = {
                TextButton(onClick = {
                    validationErrorTitle = null
                    validationErrorMessage = null
                }) {
                    Text("OK")
                }
            },
            title = {
                Text(validationErrorTitle ?: "Invalid Input")
            },
            text = {
                Text(validationErrorMessage!!)
            }
        )
    }
}

@Composable
private fun WeightStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MacroGoalsCard(
    dailyCalories: Int,
    proteinGrams: Int,
    carbsGrams: Int,
    fatGrams: Int,
    selectedDistribution: MacroDistribution,
    onDistributionChange: (MacroDistribution) -> Unit
) {
    val carbsColor = Color(0xFF4FC3F7)
    val proteinColor = Color(0xFFEF5350)
    val fatColor = Color(0xFFFFCA28)

    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedDistribution.label, style = MaterialTheme.typography.labelMedium)
                        }

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.width(280.dp)
                        ) {
                            MacroDistribution.entries.forEach { distribution ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    distribution.label,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Row {
                                                    Text(
                                                        "${distribution.carbsPercent}",
                                                        color = carbsColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(":")
                                                    Text(
                                                        "${distribution.proteinPercent}",
                                                        color = proteinColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(":")
                                                    Text(
                                                        "${distribution.fatPercent}",
                                                        color = fatColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                distribution.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onDistributionChange(distribution)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Goal Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedDistribution.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = selectedDistribution.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                WeightStatItem("Daily Budget", "$dailyCalories kcal")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = "Macro Targets",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MacroPieChart(
                        carbsPercent = selectedDistribution.carbsPercent,
                        proteinPercent = selectedDistribution.proteinPercent,
                        fatPercent = selectedDistribution.fatPercent,
                        carbsColor = carbsColor,
                        proteinColor = proteinColor,
                        fatColor = fatColor
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MacroLegendItem(
                        color = carbsColor,
                        name = "Carbs",
                        percent = selectedDistribution.carbsPercent,
                        grams = carbsGrams
                    )
                    MacroLegendItem(
                        color = proteinColor,
                        name = "Protein",
                        percent = selectedDistribution.proteinPercent,
                        grams = proteinGrams
                    )
                    MacroLegendItem(
                        color = fatColor,
                        name = "Fat",
                        percent = selectedDistribution.fatPercent,
                        grams = fatGrams
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroPieChart(
    carbsPercent: Int,
    proteinPercent: Int,
    fatPercent: Int,
    carbsColor: Color,
    proteinColor: Color,
    fatColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 24f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        val carbsSweep = carbsPercent / 100f * 360f
        val proteinSweep = proteinPercent / 100f * 360f
        val fatSweep = fatPercent / 100f * 360f

        var startAngle = -90f

        drawArc(
            color = carbsColor,
            startAngle = startAngle,
            sweepAngle = carbsSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
        startAngle += carbsSweep

        drawArc(
            color = proteinColor,
            startAngle = startAngle,
            sweepAngle = proteinSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
        startAngle += proteinSweep

        drawArc(
            color = fatColor,
            startAngle = startAngle,
            sweepAngle = fatSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
private fun MacroLegendItem(
    color: Color,
    name: String,
    percent: Int,
    grams: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "($percent%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${grams}g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsToggleButton(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

private fun parseTargetDate(targetDate: String): LocalDate? {
    val formatters = listOf(
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
    )
    return formatters.firstNotNullOfOrNull { formatter ->
        runCatching { LocalDate.parse(targetDate, formatter) }.getOrNull()
    }
}

private fun earliestAllowedTargetDate(
    currentWeight: Double,
    targetWeight: Double,
    currentDate: LocalDate = LocalDate.now()
): LocalDate {
    val absoluteMinWeeks = CalorieCalculator.minimumSafeWeeks(currentWeight, targetWeight)
    return currentDate.plusWeeks(absoluteMinWeeks.toLong())
}
