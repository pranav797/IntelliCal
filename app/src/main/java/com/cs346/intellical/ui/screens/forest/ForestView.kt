package com.cs346.intellical.ui.screens.forest

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.Forest
import com.cs346.intellical.domain.Tree
import com.cs346.intellical.domain.TreeGrowthStage
import com.cs346.intellical.domain.TreeHealthState
import com.cs346.intellical.domain.TreeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForestView(
    onLogMealClick: () -> Unit = {},
    viewModel: ForestViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val forest = uiState.forest
    val selectedTreeType = uiState.selectedTreeType
    val selectedSlot = uiState.selectedSlot
    val showTreeDetail = uiState.showTreeDetail
    val showShareSheet = uiState.showShareSheet
    val showLevelUpDialog = uiState.showLevelUpDialog
    val newForestLevel = uiState.newForestLevel

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshForest()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Forest",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { viewModel.onShareClick() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Forest",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            EnhancedStatsPanel(forest = forest)

            Spacer(modifier = Modifier.height(16.dp))

            ForestGrid(
                forest = forest,
                selectedSlot = selectedSlot,
                selectedTreeType = selectedTreeType,
                onSlotClick = { slot -> viewModel.onSlotClick(slot) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ForestLevelProgressBar(totalXP = forest.totalXP)

            Spacer(modifier = Modifier.height(16.dp))

            GrowthTipCard()

            Spacer(modifier = Modifier.height(16.dp))

            if (forest.treeCount == 0) {
                FirstTreeBanner(
                    selectedTreeType = selectedTreeType,
                    onTreeTypeSelect = { viewModel.onTreeTypeSelect(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showLevelUpDialog) {
            LevelUpDialog(
                newLevel = newForestLevel,
                onTreeSelected = { treeType -> viewModel.onTreeTypeSelect(treeType) },
                onDismiss = { viewModel.onDismissLevelUpDialog() }
            )
        }

        if (showTreeDetail && selectedSlot != null && !selectedSlot.isEmpty) {
            TreeDetailSheet(
                treeSlot = selectedSlot,
                onDismiss = { viewModel.onDismissDetail() },
                onLogMeal = {
                    viewModel.onDismissDetail()
                    onLogMealClick()
                }
            )
        }

        if (showShareSheet) {
            ForestShareSheet(
                forest = forest,
                onDismiss = { viewModel.onDismissShare() }
            )
        }
    }
}

@Composable
private fun EnhancedStatsPanel(forest: Forest) {
    val dailyGP = AppModel.getDailyGrowthPoints()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItemEnhanced(
                value = "${forest.treeCount}",
                label = "Trees",
                color = Color(0xFF4CAF50)
            )
            VerticalDivider()
            StatItemEnhanced(
                value = "${forest.maxGrowthPercent}%",
                label = "Max Growth",
                color = Color(0xFFFFC107)
            )
            VerticalDivider()
            StatItemEnhanced(
                value = "$dailyGP/3",
                label = "Growth Points",
                color = Color(0xFFFFD700)
            )
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun StatItemEnhanced(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ForestGrid(
    forest: Forest,
    selectedSlot: Tree?,
    selectedTreeType: TreeType?,
    onSlotClick: (Tree) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A2A)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in 0 until forest.gridSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until forest.gridSize) {
                        val slot = forest.getTree(row, col)
                        val displaySlot = slot ?: Tree(row = row, col = col)
                            ForestGridCell(
                            slot = displaySlot,
                            isSelected = selectedSlot == slot,
                            isPlacementMode = selectedTreeType != null,
                            modifier = Modifier.weight(1f),
                            onClick = { onSlotClick(displaySlot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForestGridCell(
    slot: Tree,
    isSelected: Boolean,
    isPlacementMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cellColor = when {
        isSelected -> Color(0xFFFFD54F).copy(alpha = 0.3f)
        isPlacementMode && slot.isEmpty -> Color(0xFFFFD54F).copy(alpha = 0.25f)
        else -> Color(0xFF2D5A3D).copy(alpha = 0.3f)
    }

    val borderMod = when {
        isSelected -> Modifier.border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
        isPlacementMode && slot.isEmpty -> Modifier.border(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
        else -> Modifier
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .then(borderMod)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onClick() },
        colors = CardDefaults.cardColors(containerColor = cellColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!slot.isEmpty && slot.type != null) {
                val stageEmoji = when (slot.growthStage) {
                    TreeGrowthStage.SAPLING -> "\uD83C\uDF31"
                    TreeGrowthStage.JUVENILE -> "\uD83E\uDEB4"
                    TreeGrowthStage.MATURE -> slot.type.emoji
                }
                val emojiSize = when (slot.growthStage) {
                    TreeGrowthStage.SAPLING -> 14.sp
                    TreeGrowthStage.JUVENILE -> 19.sp
                    TreeGrowthStage.MATURE -> 26.sp
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stageEmoji,
                        fontSize = emojiSize
                    )
                    Text(
                        text = slot.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = when (slot.healthState) {
                            TreeHealthState.THRIVING -> Color(0xFF4CAF50)
                            TreeHealthState.HEALTHY -> Color(0xFF8BC34A)
                            TreeHealthState.STRUGGLING -> Color(0xFFFFC107)
                            TreeHealthState.WITHERING -> Color(0xFFE91E63)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            } else if (isPlacementMode) {
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    color = Color(0xFFFFD54F).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ForestLevelProgressBar(totalXP: Int) {
    val levelThresholds = listOf(0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500, 5500)

    val currentLevel = levelThresholds.indexOfLast { totalXP >= it }.coerceAtLeast(0)
    val currentLevelXP = levelThresholds.getOrElse(currentLevel) { 0 }
    val nextLevelXP = levelThresholds.getOrElse(currentLevel + 1) { levelThresholds.last() + 1000 }
    val xpInCurrentLevel = totalXP - currentLevelXP
    val xpNeeded = nextLevelXP - currentLevelXP
    val progress = (xpInCurrentLevel.toFloat() / xpNeeded).coerceIn(0f, 1f)
    val xpRemaining = nextLevelXP - totalXP

    val levelColor = Color(0xFF00E676)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Forest Level $currentLevel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }
                Text(
                    text = "$xpRemaining XP to next level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = levelColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$xpInCurrentLevel / $xpNeeded XP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GrowthTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "💡", fontSize = 20.sp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Growth Tip",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Meals with high health scores (8–10) earn more XP and help you level up faster! Hit your daily macros to earn Growth Points for the day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TreeSelectionPanel(
    availableTreeTypes: List<TreeType>,
    selectedTreeType: TreeType?,
    selectedSlot: Tree?,
    onTreeTypeSelect: (TreeType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = when {
                    selectedTreeType != null -> "🌱 Tap an empty slot to plant ${selectedTreeType.name}"
                    selectedSlot?.type != null -> "🌳 ${selectedSlot.type.name} - ${selectedSlot.growthStage.displayName}"
                    else -> "Select a tree to plant"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedTreeType != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                availableTreeTypes.forEach { treeType ->
                    TreeTypeCard(
                        treeType = treeType,
                        isSelected = selectedTreeType == treeType,
                        onClick = { onTreeTypeSelect(treeType) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TreeTypeCard(
    treeType: TreeType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(75.dp)
            .then(
                if (isSelected) Modifier.border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = treeType.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = treeType.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TreeDetailSheet(
    treeSlot: Tree,
    onDismiss: () -> Unit,
    onLogMeal: () -> Unit
) {
    val treeName = treeSlot.type?.name ?: "Unknown"
    val treeDescription = treeSlot.type?.description ?: ""
    val stageName = treeSlot.growthStage.displayName
    val ageDays = treeSlot.ageDays
    val currentPts = treeSlot.growthPoints
    val nextThreshold = treeSlot.nextStageThreshold
    val isMaxStage = treeSlot.growthStage == TreeGrowthStage.MATURE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = treeName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stageName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(treeSlot.currentColorArgb),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = treeDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Planted $ageDays day${if (ageDays != 1) "s" else ""} ago",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isMaxStage) "Max Stage Reached" else "Growth Points",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isMaxStage) "$currentPts pts" else "$currentPts / $nextThreshold pts",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { treeSlot.stageProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color(treeSlot.currentColorArgb),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (!isMaxStage) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next stage: ${treeSlot.growthStage.nextStageLabel()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogMeal,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log a Healthy Meal")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun TreeGrowthStage.nextStageLabel(): String = when (this) {
    TreeGrowthStage.SAPLING -> "Juvenile"
    TreeGrowthStage.JUVENILE -> "Mature"
    TreeGrowthStage.MATURE -> "Max"
}

@Composable
private fun StatusChip(
    label: String,
    value: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ForestShareSheet(
    forest: Forest,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share Your Forest",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .drawWithCache {
                            onDrawWithContent {
                                graphicsLayer.record {
                                    this@onDrawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A3A2A)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "My IntelliCal Forest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = buildString {
                                    var index = 0
                                    for (row in 0 until forest.gridSize) {
                                        for (col in 0 until forest.gridSize) {
                                            val slot = forest.getTree(row, col)
                                            val displayEmoji = if (slot != null && slot.type != null) {
                                                when (slot.growthStage) {
                                                    TreeGrowthStage.SAPLING -> "\uD83C\uDF31"
                                                    TreeGrowthStage.JUVENILE -> "\uD83E\uDEB4"
                                                    TreeGrowthStage.MATURE -> slot.type.emoji
                                                }
                                            } else {
                                                "⬜"
                                            }
                                            append(displayEmoji)
                                            index++
                                        }
                                        if (row < forest.gridSize - 1) append("\n")
                                    }
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                lineHeight = MaterialTheme.typography.headlineMedium.lineHeight,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${forest.treeCount}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "Trees",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "85%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                                Text(
                                    text = "Max Growth",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${forest.streakDays}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5722)
                                )
                                Text(
                                    text = "Streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Share without revealing your calories or weight!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                shareForestImage(context, bitmap)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Share")
                    }
                }
            }
        }
    }
}

private fun shareForestImage(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "forest_share.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, "Check out my forest on IntelliCal! 🌲 #IntelliCal #HealthyHabits")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share your forest"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun LevelUpDialog(
    newLevel: Int,
    onTreeSelected: (TreeType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A2A1A),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("\uD83C\uDF89", fontSize = 40.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Level Up!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "You reached Forest Level $newLevel!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8F9BB3),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Choose a new tree to plant:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val availableTypes = AppModel.getUnlockedTreeDefinitions()
                val allDomainTypes = TreeType.allTypes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableTypes.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { treeInfo ->
                                val domainType = allDomainTypes.find { it.id == treeInfo.id }
                                Button(
                                    onClick = { domainType?.let { onTreeSelected(it) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A3A2A)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(treeInfo.emoji, fontSize = 24.sp)
                                        Text(
                                            text = treeInfo.name,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip", color = Color(0xFF8F9BB3))
            }
        }
    )
}

@Composable
private fun FirstTreeBanner(
    selectedTreeType: TreeType?,
    onTreeTypeSelect: (TreeType) -> Unit
) {
    val unlockedTypes = AppModel.getUnlockedTreeDefinitions()
        .mapNotNull { info -> TreeType.allTypes.find { it.id == info.id } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A2A)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (selectedTreeType == null) "\uD83C\uDF31 Plant your first tree!"
                       else "\uD83C\uDF31 Tap an empty plot to plant ${selectedTreeType.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (selectedTreeType == null) {
                Text(
                    text = "Every forest starts with a single seed. Choose a tree to begin!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    unlockedTypes.forEach { treeType ->
                        Button(
                            onClick = { onTreeTypeSelect(treeType) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A4A2A)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(treeType.emoji, fontSize = 24.sp)
                                Text(
                                    text = treeType.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
