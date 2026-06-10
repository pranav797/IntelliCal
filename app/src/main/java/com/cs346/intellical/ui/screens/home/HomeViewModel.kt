package com.cs346.intellical.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MacroData(
    val name: String,
    val consumed: Int,
    val difference: Int,
    val unit: String,
    val total: Int,
    val colorHex: Long,
    val icon: String,
    val isOver: Boolean = false
)

data class LogItem(
    val title: String,
    val calories: Int,
    val time: String,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val imageEmoji: String,
    val healthScore: Double
)

data class HomeState(
    val caloriesConsumed: Int = 0,
    val caloriesRemainingOrSurplus: Int = 0,
    val caloriesTarget: Int = 0,
    val isCalorieOver: Boolean = false,
    val streak: Int = 0,
    val growthPts: Int = 0,
    val growthPtsMax: Int = 3,
    val maxGrowth: Int = 0,
    val treeCount: Int = 0,
    val level: Int = 1,
    val currentXp: Int = 0,
    val xpToNextLevel: Int = 100,
    val dayOffset: Int = 0,
    val dayLabel: String = "Today",
    val leveledUp: Boolean = false,
    val isLoaded: Boolean = false,

    val protein: MacroData = MacroData("Protein", 0, 0, "g", 0, 0xFFFF5252, "🥩"),
    val carbs: MacroData = MacroData("Carbs", 0, 0, "g", 0, 0xFFFF9800, "🌾"),
    val fats: MacroData = MacroData("Fats", 0, 0, "g", 0, 0xFF448AFF, "💧"),
    val logs: List<LogItem> = emptyList()
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private var _previousLevel: Int = -1

    init { refreshState() }

    fun goToPreviousDay() {
        val newOffset = _uiState.value.dayOffset - 1
        _uiState.update { it.copy(dayOffset = newOffset, dayLabel = labelFor(newOffset)) }
        refreshState()
    }

    fun goToNextDay() {
        val newOffset = (_uiState.value.dayOffset + 1).coerceAtMost(0)
        _uiState.update { it.copy(dayOffset = newOffset, dayLabel = labelFor(newOffset)) }
        refreshState()
    }

    fun consumeLevelUp() {
        _uiState.update { it.copy(leveledUp = false) }
    }

    private fun labelFor(offset: Int): String = when (offset) {
        0 -> "Today"
        -1 -> "Yesterday"
        else -> {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offset)
            SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
        }
    }

    fun refreshState() {
        viewModelScope.launch {
            val offset = _uiState.value.dayOffset
            val meals  = AppModel.getMealsForDate(offset)   // suspend — hits DB for past days
            updateStateWithMeals(meals)
        }
    }

    private fun updateStateWithMeals(meals: List<Meal>) {
        val forest = AppModel.getForest()
        val goal   = AppModel.getNutritionGoal()

        val caloriesConsumed = meals.sumOf { it.calories }
        val isCalorieOver    = caloriesConsumed > goal.dailyCalories
        val caloriesRemainingOrSurplus =
            if (isCalorieOver) caloriesConsumed - goal.dailyCalories
            else goal.dailyCalories - caloriesConsumed

        val totalProtein = meals.sumOf { it.proteinGrams }
        val totalCarbs   = meals.sumOf { it.carbsGrams }
        val totalFats    = meals.sumOf { it.fatGrams }

        val thresholds       = listOf(0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500, 5500)
        val currentLevel     = thresholds.indexOfLast { forest.totalXP >= it }.coerceAtLeast(0)
        val xpForCurrentLevel = thresholds.getOrElse(currentLevel) { 0 }
        val xpForNextLevel   = thresholds.getOrElse(currentLevel + 1) { thresholds.last() + 1000 }
        val xpInCurrentLevel = forest.totalXP - xpForCurrentLevel
        val xpNeeded         = xpForNextLevel - xpForCurrentLevel

        val didLevelUp = _previousLevel >= 0 && currentLevel > _previousLevel
        _previousLevel = currentLevel

        _uiState.update { cur ->
            cur.copy(
                caloriesConsumed = caloriesConsumed,
                caloriesRemainingOrSurplus = caloriesRemainingOrSurplus,
                caloriesTarget = goal.dailyCalories,
                isCalorieOver  = isCalorieOver,
                streak         = forest.streakDays,
                treeCount      = forest.treeCount,
                maxGrowth      = forest.maxGrowthPercent,
                growthPts      = AppModel.getDailyGrowthPoints(),
                growthPtsMax   = 3,
                level          = currentLevel,
                currentXp      = xpInCurrentLevel,
                xpToNextLevel  = xpNeeded,
                leveledUp      = cur.leveledUp || didLevelUp,
                protein = MacroData(
                    name = "Protein",
                    consumed = totalProtein,
                    difference = if (totalProtein >= goal.proteinGrams) totalProtein - goal.proteinGrams
                                 else goal.proteinGrams - totalProtein,
                    unit = "g", total = goal.proteinGrams, colorHex = 0xFFFF5252, icon = "🥩",
                    isOver = totalProtein >= goal.proteinGrams
                ),
                carbs = MacroData(
                    name = "Carb",
                    consumed = totalCarbs,
                    difference = if (totalCarbs >= goal.carbsGrams) totalCarbs - goal.carbsGrams
                                 else goal.carbsGrams - totalCarbs,
                    unit = "g", total = goal.carbsGrams, colorHex = 0xFFFF9800, icon = "🌾",
                    isOver = totalCarbs >= goal.carbsGrams
                ),
                fats = MacroData(
                    name = "Fat",
                    consumed = totalFats,
                    difference = if (totalFats >= goal.fatGrams) totalFats - goal.fatGrams
                                 else goal.fatGrams - totalFats,
                    unit = "g", total = goal.fatGrams, colorHex = 0xFF448AFF, icon = "💧",
                    isOver = totalFats >= goal.fatGrams
                ),
                logs = meals.map { it.toLogItem() },
                isLoaded = true
            )
        }
    }

    private fun Meal.toLogItem(): LogItem {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return LogItem(
            title = name, calories = calories,
            time = sdf.format(Date(timestamp)),
            protein = proteinGrams, carbs = carbsGrams, fats = fatGrams,
            imageEmoji = "🍽️",
            healthScore = computedHealthScore
        )
    }
}
