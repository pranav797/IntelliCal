package com.cs346.intellical.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MealTest {

    private fun createMeal(healthScore: Int): Meal {
        return Meal(
            id = "test-id",
            name = "Test Meal",
            calories = 500,
            proteinGrams = 20,
            carbsGrams = 50,
            fatGrams = 20,
            healthScore = healthScore,
            timestamp = System.currentTimeMillis(),
            ingredients = listOf("Ingredient 1", "Ingredient 2")
        )
    }

    @Test
    fun qualityBonus_highScore_returns25() {
        val meal = createMeal(8)
        assertEquals(25, meal.qualityBonus)

        val mealHigher = createMeal(10)
        assertEquals(25, mealHigher.qualityBonus)
    }

    @Test
    fun qualityBonus_mediumHighScore_returns15() {
        val meal = createMeal(6)
        assertEquals(15, meal.qualityBonus)

        val mealUpper = createMeal(7)
        assertEquals(15, mealUpper.qualityBonus)
    }

    @Test
    fun qualityBonus_mediumScore_returns5() {
        val meal = createMeal(4)
        assertEquals(5, meal.qualityBonus)

        val mealUpper = createMeal(5)
        assertEquals(5, mealUpper.qualityBonus)
    }

    @Test
    fun qualityBonus_lowScore_returns0() {
        val meal = createMeal(3)
        assertEquals(0, meal.qualityBonus)

        val mealLowest = createMeal(0)
        assertEquals(0, mealLowest.qualityBonus)
    }
}
