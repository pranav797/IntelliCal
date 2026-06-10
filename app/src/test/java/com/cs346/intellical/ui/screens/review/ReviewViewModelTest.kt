package com.cs346.intellical.ui.screens.review

import com.cs346.intellical.data.MockDatabase
import com.cs346.intellical.domain.AppModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReviewViewModelTest {

    @Before
    fun setup() {
        AppModel.initialize(MockDatabase())
        runBlocking { AppModel.reset() }
    }

    @Test
    fun `updateMealName reflects in state`() {
        val vm = ReviewViewModel()
        vm.updateMealName("Tacos")
        assertEquals("Tacos", vm.uiState.value.mealName)
    }

    @Test
    fun `updateCalories reflects in state`() {
        val vm = ReviewViewModel()
        vm.updateCalories(500)
        assertEquals(500, vm.uiState.value.calories)
    }

    @Test
    fun `updateProtein reflects in state`() {
        val vm = ReviewViewModel()
        vm.updateProtein(40)
        assertEquals(40, vm.uiState.value.protein)
    }

    @Test
    fun `updateCarbs reflects in state`() {
        val vm = ReviewViewModel()
        vm.updateCarbs(60)
        assertEquals(60, vm.uiState.value.carbs)
    }

    @Test
    fun `updateFat reflects in state`() {
        val vm = ReviewViewModel()
        vm.updateFat(15)
        assertEquals(15, vm.uiState.value.fat)
    }

    @Test
    fun `confirmMeal adds meal to AppModel`() {
        val vm = ReviewViewModel()
        vm.updateMealName("Test Meal")
        vm.updateCalories(300)
        vm.updateProtein(25)
        vm.updateCarbs(30)
        vm.updateFat(10)

        val before = AppModel.getTodayMeals().size
        vm.confirmMeal()
        val after = AppModel.getTodayMeals().size

        assertEquals(before + 1, after)
    }
}
