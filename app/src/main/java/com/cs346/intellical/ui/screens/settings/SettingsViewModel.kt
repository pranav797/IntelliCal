package com.cs346.intellical.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.CalorieCalculator
import com.cs346.intellical.domain.CalorieResult
import com.cs346.intellical.domain.GoalType
import com.cs346.intellical.domain.MacroDistribution
import com.cs346.intellical.domain.NutritionGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.util.Locale

data class SettingsUiState(
    val currentWeight: Float = 175f,
    val targetWeight: Float = 160f,
    val targetDate: String = "May 15, 2026",
    val weeklyRate: Float = 1.0f,
    val isLosing: Boolean = true,
    val selectedDistribution: MacroDistribution = MacroDistribution.BALANCED,
    val dailyCalories: Int = 1800,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val dateFormatters = listOf(
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMMM d, yyyy").toFormatter(Locale.ENGLISH),
        DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM d, yyyy").toFormatter(Locale.ENGLISH)
    )

    init {
        val goal = AppModel.getNutritionGoal()
        _uiState.update {
            it.copy(
                currentWeight = AppModel.getCurrentWeight(),
                targetWeight = AppModel.getTargetWeight(),
                targetDate = AppModel.getTargetDate(),
                selectedDistribution = goal.distribution,
                dailyCalories = goal.dailyCalories,
                proteinGrams = goal.proteinGrams,
                carbsGrams = goal.carbsGrams,
                fatGrams = goal.fatGrams
            )
        }
        recalculatePlan()
    }

    fun onCurrentWeightChange(weight: Float) {
        _uiState.update { it.copy(currentWeight = weight) }
        AppModel.updateWeight(weight)
        recalculatePlan()
    }

    fun onTargetWeightChange(weight: Float) {
        _uiState.update { it.copy(targetWeight = weight) }
        AppModel.updateTargetWeight(weight)
        recalculatePlan()
    }

    fun onTargetDateChange(date: String) {
        _uiState.update { it.copy(targetDate = date) }
        recalculatePlan()
    }

    fun saveWeightProgress(currentWeight: Float, targetWeight: Float, targetDate: String) {
        _uiState.update {
            it.copy(
                currentWeight = currentWeight,
                targetWeight = targetWeight,
                targetDate = targetDate
            )
        }
        AppModel.updateWeightProgress(currentWeight, targetWeight, targetDate)
        recalculatePlan()
    }

    fun onDistributionChange(distribution: MacroDistribution) {
        _uiState.update { it.copy(selectedDistribution = distribution) }
        updateGoalInAppModel()
    }

    fun onCaloriesChange(calories: Int) {
        _uiState.update { it.copy(dailyCalories = calories) }
        updateGoalInAppModel()
    }

    private fun recalculatePlan() {
        val currentState = _uiState.value
        val timeframeWeeks = calculateTimeframeWeeks(currentState.targetDate)
        val recommendation = CalorieCalculator.calculateRecommendation(
            currentWeight = currentState.currentWeight.toDouble(),
            targetWeight = currentState.targetWeight.toDouble(),
            timeframeWeeks = timeframeWeeks
        )

        when (recommendation) {
            is CalorieResult.Success -> {
                _uiState.update {
                    it.copy(
                        dailyCalories = recommendation.recommendedDailyCalories,
                        weeklyRate = recommendation.weeklyWeightChangeRate.toFloat(),
                        isLosing = recommendation.goalType != GoalType.BULK
                    )
                }
            }
            is CalorieResult.Error -> {
                val safeRecommendation = CalorieCalculator.calculateRecommendation(
                    currentWeight = currentState.currentWeight.toDouble(),
                    targetWeight = currentState.targetWeight.toDouble(),
                    timeframeWeeks = recommendation.minimumSafeWeeks
                )
                if (safeRecommendation is CalorieResult.Success) {
                    _uiState.update {
                        it.copy(
                            dailyCalories = safeRecommendation.recommendedDailyCalories,
                            weeklyRate = safeRecommendation.weeklyWeightChangeRate.toFloat(),
                            isLosing = safeRecommendation.goalType != GoalType.BULK
                        )
                    }
                }
            }
        }
        updateGoalInAppModel()
    }

    private fun updateGoalInAppModel() {
        val currentState = _uiState.value
        val newGoal = NutritionGoal(
            dailyCalories = currentState.dailyCalories,
            proteinGrams = (currentState.dailyCalories * currentState.selectedDistribution.proteinPercent / 100) / 4,
            carbsGrams = (currentState.dailyCalories * currentState.selectedDistribution.carbsPercent / 100) / 4,
            fatGrams = (currentState.dailyCalories * currentState.selectedDistribution.fatPercent / 100) / 9,
            distribution = currentState.selectedDistribution
        )
        _uiState.update {
            it.copy(
                proteinGrams = newGoal.proteinGrams,
                carbsGrams = newGoal.carbsGrams,
                fatGrams = newGoal.fatGrams
            )
        }
        AppModel.updateGoal(newGoal)
    }

    private fun calculateTimeframeWeeks(targetDate: String): Int {
        val parsedDate = dateFormatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(targetDate, formatter) }.getOrNull()
        } ?: return 1

        val daysUntilTarget = ChronoUnit.DAYS.between(LocalDate.now(), parsedDate).coerceAtLeast(1)
        return kotlin.math.ceil(daysUntilTarget / 7.0).toInt().coerceAtLeast(1)
    }
}
