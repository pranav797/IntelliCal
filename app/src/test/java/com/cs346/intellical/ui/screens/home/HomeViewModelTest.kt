package com.cs346.intellical.ui.screens.home

import com.cs346.intellical.data.MockDatabase
import com.cs346.intellical.domain.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        AppModel.initialize(MockDatabase())
        runBlocking { AppModel.reset() }
        (AppModel.getTodayMeals() as MutableList).clear()
        viewModel = HomeViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state values match MockDB`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        val expectedGoal = AppModel.getNutritionGoal()
        val expectedForest = AppModel.getForest()

        assertEquals(expectedGoal.dailyCalories, state.caloriesTarget)
        assertEquals(0, state.caloriesConsumed)
        assertEquals(expectedGoal.dailyCalories, state.caloriesRemainingOrSurplus)
        assertFalse(state.isCalorieOver)

        assertEquals(expectedForest.streakDays, state.streak)
        assertEquals(expectedForest.treeCount, state.treeCount)
        assertEquals(expectedForest.totalXP - expectedForest.xpForCurrentLevel, state.currentXp)

        assertEquals("Protein", state.protein.name)
        assertEquals(0, state.protein.consumed)
        assertEquals(expectedGoal.proteinGrams, state.protein.difference)
        assertEquals(expectedGoal.proteinGrams, state.protein.total)
        assertEquals("Carb", state.carbs.name)
        assertEquals(0, state.carbs.consumed)
        assertEquals(expectedGoal.carbsGrams, state.carbs.difference)
        assertEquals(expectedGoal.carbsGrams, state.carbs.total)
        assertEquals("Fat", state.fats.name)
        assertEquals(0, state.fats.consumed)
        assertEquals(expectedGoal.fatGrams, state.fats.difference)
        assertEquals(expectedGoal.fatGrams, state.fats.total)
    }
}
