package com.cs346.intellical.ui.screens.settings

import com.cs346.intellical.data.MockDatabase
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.CalorieCalculator
import com.cs346.intellical.domain.CalorieResult
import com.cs346.intellical.domain.MacroDistribution
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    @Before
    fun setup() {
        AppModel.initialize(MockDatabase())
        runBlocking { AppModel.reset() }
    }

    @Test
    fun `test onCurrentWeightChange updates state and AppModel`() {
        val viewModel = SettingsViewModel()
        val targetDate = LocalDate.now().plusWeeks(20)
            .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        viewModel.onTargetDateChange(targetDate)
        val originalCalories = viewModel.uiState.value.dailyCalories
        val timeframeWeeks = timeframeWeeksFor(viewModel.uiState.value.targetDate)
        val expectedResult = CalorieCalculator.calculateRecommendation(
            currentWeight = 180.0,
            targetWeight = viewModel.uiState.value.targetWeight.toDouble(),
            timeframeWeeks = timeframeWeeks
        )
        val expectedCalories = (expectedResult as CalorieResult.Success).recommendedDailyCalories
        viewModel.onCurrentWeightChange(180f)

        assertEquals(180f, viewModel.uiState.value.currentWeight)
        assertEquals(180f, AppModel.getCurrentWeight())
        assertEquals(expectedCalories, viewModel.uiState.value.dailyCalories)
        assertNotEquals(originalCalories, viewModel.uiState.value.dailyCalories)
    }

    @Test
    fun `test onDistributionChange updates state and goal in AppModel`() {
        val viewModel = SettingsViewModel()
        viewModel.onDistributionChange(MacroDistribution.LOW_CARB)
        val goal = AppModel.getNutritionGoal()

        assertEquals(MacroDistribution.LOW_CARB, viewModel.uiState.value.selectedDistribution)
        assertEquals(MacroDistribution.LOW_CARB, goal.distribution)
        assertEquals(goal.proteinGrams, viewModel.uiState.value.proteinGrams)
        assertEquals(goal.carbsGrams, viewModel.uiState.value.carbsGrams)
        assertEquals(goal.fatGrams, viewModel.uiState.value.fatGrams)
    }

    @Test
    fun `test saveWeightProgress updates target date and recalculates budget`() {
        val viewModel = SettingsViewModel()
        val targetDate = LocalDate.now().plusWeeks(8)
        val targetDateText = targetDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))

        viewModel.saveWeightProgress(
            currentWeight = 180f,
            targetWeight = 170f,
            targetDate = targetDateText
        )

        val expectedResult = CalorieCalculator.calculateRecommendation(
            currentWeight = 180.0,
            targetWeight = 170.0,
            timeframeWeeks = timeframeWeeksFor(targetDateText)
        )
        val expectedCalories = (expectedResult as CalorieResult.Success).recommendedDailyCalories

        assertEquals(targetDateText, viewModel.uiState.value.targetDate)
        assertEquals(expectedCalories, viewModel.uiState.value.dailyCalories)
        assertEquals(180f, AppModel.getCurrentWeight())
        assertEquals(170f, AppModel.getTargetWeight())
    }

    private fun timeframeWeeksFor(targetDate: String): Int {
        val parsed = LocalDate.parse(targetDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        val days = ChronoUnit.DAYS.between(LocalDate.now(), parsed).coerceAtLeast(1)
        return kotlin.math.ceil(days / 7.0).toInt().coerceAtLeast(1)
    }
}
