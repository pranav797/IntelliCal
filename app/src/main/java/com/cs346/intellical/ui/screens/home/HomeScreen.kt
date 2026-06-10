package com.cs346.intellical.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

@Composable
fun HomeScreen(
    onForestClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onLevelUp: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.leveledUp) {
        if (state.leveledUp) {
            viewModel.consumeLevelUp()
            onLevelUp()
        }
    }

    var showFirstTreePrompt by remember { mutableStateOf(true) }

    if (showFirstTreePrompt && state.isLoaded && state.treeCount == 0) {
        AlertDialog(
            onDismissRequest = { showFirstTreePrompt = false },
            title = {
                Text(
                    text = "🌱 Welcome to Your Forest!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Before you do anything else, head to the Forest tab below and scroll down to plant your first tree.\n\nYour first tree is completely free — no meals need to be logged first. Your forest grows as you stay consistent with your nutrition goals!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFirstTreePrompt = false
                        onForestClick()
                    }
                ) {
                    Text("Take Me There!", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFirstTreePrompt = false }) {
                    Text("Got It")
                }
            }
        )
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HomeHeader(state.streak, MaterialTheme.colorScheme.tertiary, onCameraClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { 
                ForestSnapshotCard(
                    maxGrowth = state.maxGrowth,
                    trees = state.treeCount,
                    growthPts = state.growthPts,
                    growthPtsMax = state.growthPtsMax,
                    level = state.level,
                    currentXp = state.currentXp,
                    xpToNextLevel = state.xpToNextLevel,
                    onClick = onForestClick
                ) 
            }

            item { 
                CalorieCard(
                    consumed = state.caloriesConsumed,
                    remainingOrSurplus = state.caloriesRemainingOrSurplus,
                    target = state.caloriesTarget,
                    isOver = state.isCalorieOver,
                    accentColor = MaterialTheme.colorScheme.primary
                ) 
            }

            item { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroCard(state.protein, Modifier.weight(1f))
                    MacroCard(state.carbs, Modifier.weight(1f))
                    MacroCard(state.fats, Modifier.weight(1f))
                }
            }

            item { DayNavigator(
                label = state.dayLabel,
                canGoForward = state.dayOffset < 0,
                onPrevious = { viewModel.goToPreviousDay() },
                onNext = { viewModel.goToNextDay() }
            ) }

            item {
                Text(
                    text = "Recently uploaded",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(state.logs) { log ->
                LogCard(log)
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ForestSnapshotCard(
    maxGrowth: Int,
    trees: Int,
    growthPts: Int,
    growthPtsMax: Int,
    level: Int,
    currentXp: Int,
    xpToNextLevel: Int,
    onClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onClick() },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Wellness Woods",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "tap to visit",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column {
                        Text(text = "$growthPts/$growthPtsMax", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Growth Pts", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Column {
                        Text(text = "$maxGrowth%", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Max Growth", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Column {
                        Text(text = "$trees", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Trees", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Forest Level $level",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$currentXp / $xpToNextLevel XP",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { currentXp.toFloat() / xpToNextLevel.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(streak: Int, fireColor: Color, onCameraClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "IntelliCal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = fireColor),
            contentPadding = PaddingValues(horizontal = 12.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("🔥 Streak: $streak", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
        }
    }
}

@Composable
fun DayNavigator(
    label: String,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Text("<", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(
            onClick = onNext,
            enabled = canGoForward
        ) {
            Text(
                text = ">",
                color = if (canGoForward) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun CalorieCard(
    consumed: Int,
    remainingOrSurplus: Int,
    target: Int,
    isOver: Boolean,
    accentColor: Color
) {
    val progress = (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val ringColor = if (isOver) MaterialTheme.colorScheme.error else accentColor

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(160.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$consumed",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$remainingOrSurplus calories ${if (isOver) "over" else "left"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(contentAlignment = Alignment.Center) {
                OverlappingProgressRing(
                    currentValue = consumed.toFloat(),
                    targetValue = target.toFloat(),
                    ringColor = ringColor,
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 12.dp
                )
                Text(text = "🔥", fontSize = 22.sp)
            }
        }
    }
}

@Composable
fun MacroCard(data: MacroData, modifier: Modifier) {
    val ringColor = Color(data.colorHex)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(180.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = data.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${data.consumed}${data.unit}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                Text(
                    text = "${data.difference}${data.unit} ${if (data.isOver) "over" else "left"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                OverlappingProgressRing(
                    currentValue = data.consumed.toFloat(),
                    targetValue = data.total.toFloat(),
                    ringColor = ringColor,
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 6.dp
                )
                Text(text = data.icon, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun LogCard(item: LogItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.time,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "${item.calories} kcal",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Protein: ${item.protein}g  Carbs: ${item.carbs}g  Fats: ${item.fats}g",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            HealthScoreBadge(score = item.healthScore)
        }
    }
}

@Composable
private fun HealthScoreBadge(score: Double) {
    val color = when {
        score >= 7.5 -> Color(0xFF4CAF50)
        score >= 5.0 -> Color(0xFFFF9800)
        else         -> Color(0xFFF44336)
    }
    val display = if (score % 1.0 == 0.0) "${score.toInt()}" else "$score"
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = display,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 16.sp
        )
    }
}

@Composable
fun OverlappingProgressRing(
    currentValue: Float,
    targetValue: Float,
    ringColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp
) {
    val progress = if (targetValue > 0f) currentValue / targetValue else 0f
    val safeProgress = progress.coerceAtLeast(0f)
    val laps = floor(safeProgress)
    val remainderArc = safeProgress % 1.0f
    val sweepAngle = remainderArc * 360f
    val overlapColor = ringColor.darken(0.28f)

    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val diameter = size.minDimension - strokePx
        val arcTopLeft = androidx.compose.ui.geometry.Offset(inset, inset)
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
        val radius = diameter / 2f
        val centerPoint = center
        val fullSweep = 359.9f

        drawArc(
            color = ringColor.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = fullSweep,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        if (safeProgress <= 1.0f) {
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = safeProgress * 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        } else {
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = fullSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        if (laps >= 1f && sweepAngle > 0f) {
            val tipAngleDegrees = -90f + sweepAngle
            val tipAngleRadians = tipAngleDegrees * (PI.toFloat() / 180f)
            val tipX = centerPoint.x + radius * cos(tipAngleRadians)
            val tipY = centerPoint.y + radius * sin(tipAngleRadians)
            val tangentX = -sin(tipAngleRadians)
            val tangentY = cos(tipAngleRadians)
            val shadowX = tipX - tangentX * strokePx * 0.18f
            val shadowY = tipY - tangentY * strokePx * 0.18f
            val shadowRadius = strokePx * 0.18f

            drawIntoCanvas { canvas ->
                val shadowPaint = Paint()
                val frameworkPaint = shadowPaint.asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        strokePx * 0.2f,
                        tangentX * strokePx * 0.04f,
                        tangentY * strokePx * 0.04f,
                        android.graphics.Color.argb(70, 0, 0, 0)
                    )
                }
                canvas.nativeCanvas.drawCircle(shadowX, shadowY, shadowRadius, frameworkPaint)
            }

            drawArc(
                color = overlapColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MacroRingsRow(modifier: Modifier = Modifier) {
    val samples = listOf(
        RingSample("Calories", 2350f, 2000f, Color(0xFF18C7B7), "2350 / 2000 kcal"),
        RingSample("Protein", 160f, 150f, Color(0xFFFF6B6B), "160 / 150 g"),
        RingSample("Carbs", 220f, 250f, Color(0xFFFFA000), "220 / 250 g"),
        RingSample("Fats", 92f, 80f, Color(0xFF4D8DFF), "92 / 80 g")
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        samples.forEach { sample ->
            SampleRingCard(
                sample = sample,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SampleRingCard(sample: RingSample, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverlappingProgressRing(
                currentValue = sample.currentValue,
                targetValue = sample.targetValue,
                ringColor = sample.ringColor,
                modifier = Modifier.size(72.dp),
                strokeWidth = 9.dp
            )
            Text(
                text = sample.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = sample.valueText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class RingSample(
    val label: String,
    val currentValue: Float,
    val targetValue: Float,
    val ringColor: Color,
    val valueText: String
)

private fun Color.darken(amount: Float): Color {
    val factor = 1f - amount.coerceIn(0f, 1f)
    return Color(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha
    )
}
