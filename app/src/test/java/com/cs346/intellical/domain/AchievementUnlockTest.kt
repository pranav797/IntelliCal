package com.cs346.intellical.domain

import com.cs346.intellical.data.MockDatabase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("Achievement Unlock Logic — Issue 116")
class AchievementUnlockTest {

    @BeforeEach
    fun setUp() {
        AppModel.initialize(MockDatabase(seedData = false))
        runBlocking { AppModel.reset() }
        AppModel.updateGoal(
            NutritionGoal(
                dailyCalories = 2000,
                proteinGrams  = 100,
                carbsGrams    = 200,
                fatGrams      = 50,
                distribution  = MacroDistribution.BALANCED
            )
        )
    }

    private fun meal(
        calories: Int = 200,
        protein: Int = 10,
        carbs: Int = 20,
        fat: Int = 5,
        health: Int = 7
    ) = Meal(
        id           = UUID.randomUUID().toString(),
        name         = "Test Meal",
        calories     = calories,
        proteinGrams = protein,
        carbsGrams   = carbs,
        fatGrams     = fat,
        healthScore  = health,
        timestamp    = System.currentTimeMillis(),
        ingredients  = emptyList()
    )

    private fun achievement(id: Int) =
        AppModel.getAchievements().first { it.id == id }

    @Nested
    @DisplayName("id=1 First Meal")
    inner class FirstMeal {

        @Test
        fun `unlocks after logging the first meal`() {
            assertFalse(achievement(1).isUnlocked)
            AppModel.logMeal(meal())
            assertTrue(achievement(1).isUnlocked)
        }

        @Test
        fun `currentValue increments with each meal`() {
            AppModel.logMeal(meal())
            AppModel.logMeal(meal())
            assertEquals(1, achievement(1).currentValue) // Clamped at requiredValue=1
        }
    }

    @Nested
    @DisplayName("id=2 Week Streak")
    inner class WeekStreak {

        @Test
        fun `does not unlock when streak is 6`() {
            AppModel.updateWeight(70f)
            val user = AppModel.getUser().copy(streak = 6)
            runBlocking { AppModel.load(1) }
            AppModel.logMeal(meal())
            assertFalse(achievement(2).isUnlocked)
        }

        @Test
        fun `currentValue reflects user streak`() {
            AppModel.logMeal(meal())
            assertEquals(AppModel.getUser().streak, achievement(2).currentValue)
        }
    }

    @Nested
    @DisplayName("id=3 Forest Keeper")
    inner class ForestKeeper {

        @Test
        fun `does not unlock before 5 trees`() {
            AppModel.plantTree(0, 0, TreeType.Oak)
            AppModel.plantTree(0, 1, TreeType.Oak)
            AppModel.plantTree(0, 2, TreeType.Oak)
            AppModel.plantTree(0, 3, TreeType.Oak)
            assertFalse(achievement(3).isUnlocked)
        }

        @Test
        fun `unlocks exactly when 5th tree is planted`() {
            AppModel.plantTree(0, 0, TreeType.Oak)
            AppModel.plantTree(0, 1, TreeType.Oak)
            AppModel.plantTree(0, 2, TreeType.Oak)
            AppModel.plantTree(0, 3, TreeType.Oak)
            AppModel.plantTree(0, 4, TreeType.Oak)
            assertTrue(achievement(3).isUnlocked)
        }

        @Test
        fun `currentValue tracks tree count`() {
            AppModel.plantTree(0, 0, TreeType.Oak)
            AppModel.plantTree(0, 1, TreeType.Oak)
            assertEquals(2, achievement(3).currentValue)
        }
    }

    @Nested
    @DisplayName("id=4 10 Trees")
    inner class TenTrees {

        @Test
        fun `does not unlock at 9 trees`() {
            for (col in 0..3) AppModel.plantTree(0, col, TreeType.Oak)
            for (col in 0..4) AppModel.plantTree(1, col, TreeType.Oak)
            assertEquals(9, AppModel.getForest().treeCount)
            assertFalse(achievement(4).isUnlocked)
        }

        @Test
        fun `unlocks exactly at 10 trees`() {
            for (col in 0..4) AppModel.plantTree(0, col, TreeType.Oak)
            for (col in 0..4) AppModel.plantTree(1, col, TreeType.Oak)
            assertEquals(10, AppModel.getForest().treeCount)
            assertTrue(achievement(4).isUnlocked)
        }
    }

    @Nested
    @DisplayName("id=5 100 Meals")
    inner class HundredMeals {

        @Test
        fun `currentValue mirrors mealsLogged`() {
            AppModel.logMeal(meal())
            assertEquals(AppModel.getUser().mealsLogged, achievement(5).currentValue)
        }
    }

    @Nested
    @DisplayName("id=6 21 Days")
    inner class TwentyOneDays {

        @Test
        fun `currentValue mirrors daysTracked`() {
            AppModel.logMeal(meal())
            assertEquals(AppModel.getUser().daysTracked, achievement(6).currentValue)
        }
    }

    @Nested
    @DisplayName("id=7 Ancient Tree")
    inner class AncientTree {

        @Test
        fun `does not unlock when no MATURE trees exist`() {
            AppModel.plantTree(0, 0, TreeType.Oak)
            assertFalse(achievement(7).isUnlocked)
        }

        @Test
        fun `currentValue equals matureCount`() {
            AppModel.logMeal(meal())
            assertEquals(AppModel.getForest().matureCount, achievement(7).currentValue)
        }
    }

    @Nested
    @DisplayName("id=8 Perfect Week")
    inner class PerfectWeek {

        @Test
        fun `currentValue reflects forest streakDays`() {
            AppModel.logMeal(meal())
            assertEquals(AppModel.getForest().streakDays, achievement(8).currentValue)
        }
    }

    @Nested
    @DisplayName("consumeUnlock")
    inner class ConsumeUnlock {

        @Test
        fun `returns null before any unlock`() {
            assertNull(AppModel.consumeUnlock())
        }

        @Test
        fun `returns the achievement on first call after unlock`() {
            AppModel.logMeal(meal())
            val unlocked = AppModel.consumeUnlock()
            assertNotNull(unlocked)
            assertEquals(1, unlocked!!.id)
        }

        @Test
        fun `returns null on second call after same unlock`() {
            AppModel.logMeal(meal())
            AppModel.consumeUnlock()
            assertNull(AppModel.consumeUnlock())
        }
    }

    @Nested
    @DisplayName("Already unlocked")
    inner class AlreadyUnlocked {

        @Test
        fun `already-unlocked achievement is not re-surfaced by consumeUnlock`() {
            AppModel.logMeal(meal())
            AppModel.consumeUnlock()
            AppModel.logMeal(meal())
            val second = AppModel.consumeUnlock()
            second?.let { assertNotEquals(1, it.id) }
        }

        @Test
        fun `progress of already-unlocked achievement stays at 1f`() {
            AppModel.logMeal(meal())
            AppModel.logMeal(meal())
            assertEquals(1f, achievement(1).progress)
        }
    }
}
