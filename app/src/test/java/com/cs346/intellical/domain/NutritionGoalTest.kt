package com.cs346.intellical.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionGoalTest {

    private val goal = NutritionGoal(
        dailyCalories = 2000,
        proteinGrams = 100,
        carbsGrams = 200,
        fatGrams = 50,
        distribution = MacroDistribution.BALANCED
    )

    private fun createMeal(p: Int, c: Int, f: Int, cal: Int): Meal {
        return Meal("id", "name", cal, p, c, f, 5, 0L, emptyList())
    }

    @Test
    fun isProteinMet_returnsTrueWhenMet() {
        val meals = listOf(createMeal(60, 0, 0, 240), createMeal(40, 0, 0, 160))
        assertTrue(goal.isProteinMet(meals))
    }

    @Test
    fun isProteinMet_returnsFalseWhenNotMet() {
        val meals = listOf(createMeal(99, 0, 0, 396))
        assertFalse(goal.isProteinMet(meals))
    }

    @Test
    fun isCarbsMet_returnsTrueWhenMet() {
        val meals = listOf(createMeal(0, 200, 0, 800))
        assertTrue(goal.isCarbsMet(meals))
    }

    @Test
    fun isCarbsMet_returnsFalseWhenNotMet() {
        val meals = listOf(createMeal(0, 199, 0, 796))
        assertFalse(goal.isCarbsMet(meals))
    }

    @Test
    fun isFatsMet_returnsTrueWhenMet() {
        val meals = listOf(createMeal(0, 0, 50, 450))
        assertTrue(goal.isFatsMet(meals))
    }

    @Test
    fun isFatsMet_returnsFalseWhenNotMet() {
        val meals = listOf(createMeal(0, 0, 49, 441))
        assertFalse(goal.isFatsMet(meals))
    }

    @Test
    fun caloriesFromMeals_sumsCorrectly() {
        val meals = listOf(createMeal(10, 10, 10, 500), createMeal(10, 10, 10, 300))
        assertEquals(800, goal.caloriesFromMeals(meals))
    }

    @Test
    fun remainingCalories_calculatesCorrectly() {
        val meals = listOf(createMeal(10, 10, 10, 500))
        assertEquals(1500, goal.remainingCalories(meals))
    }
}
