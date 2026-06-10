package com.cs346.intellical.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class UserTest {

    private fun defaultUser() = User(
        id = 1,
        username = "Demo User",
        email = "demo@example.com",
        memberSince = "Member since Jan 2026",
        targetDate = "June 1, 2026",
        currentWeight = 170f,
        targetWeight = 165f,
        startWeight = 175f,
        primaryGoal = UserGoal.WEIGHT_LOSS,
        streak = 5,
        daysTracked = 47,
        mealsLogged = 187
    )

    @Test
    fun initial_returnsFirstCharUppercase() {
        assertEquals("D", defaultUser().initial)
    }

    @Test
    fun equality_sameFields_areEqual() {
        assertEquals(defaultUser(), defaultUser())
    }

    @Test
    fun equality_differentFields_areNotEqual() {
        assertNotEquals(defaultUser(), defaultUser().copy(username = "Saad"))
    }

    @Test
    fun weightProgress_halfwayThere() {
        assertEquals(0.5f, defaultUser().weightProgress, 0.01f)
    }

    @Test
    fun weightProgress_atGoal_returnsOne() {
        assertEquals(1.0f, defaultUser().copy(currentWeight = 165f).weightProgress, 0.01f)
    }

    @Test
    fun weightRemaining_calculatesCorrectly() {
        assertEquals(5f, defaultUser().weightRemaining, 0.01f)
    }

    @Test
    fun hasReachedGoal_false_whenAboveTarget() {
        assertFalse(defaultUser().hasReachedGoal)
    }

    @Test
    fun averageMealsPerDay_calculatesCorrectly() {
        assertEquals(187f / 47f, defaultUser().averageMealsPerDay, 0.01f)
    }

    @Test
    fun streakTier_bronze() {
        assertEquals("Bronze", defaultUser().streakTier)
    }

    @Test
    fun streakTier_legendary() {
        assertEquals("Legendary", defaultUser().copy(streak = 30).streakTier)
    }

    @Test
    fun updateWeight_returnsNewUser() {
        assertEquals(168f, defaultUser().updateWeight(168f).currentWeight, 0.01f)
    }

    @Test
    fun incrementStreak_addsOne() {
        assertEquals(6, defaultUser().incrementStreak().streak)
    }

    @Test
    fun resetStreak_setsToZero() {
        assertEquals(0, defaultUser().resetStreak().streak)
    }

    @Test
    fun logMealTracked_incrementsMealsLogged() {
        assertEquals(188, defaultUser().logMealTracked().mealsLogged)
    }

    @Test
    fun achievement_unlockedFlag_true() {
        val a = Achievement(1, "First Meal", "Log your first meal", "🍽️", true)
        assertTrue(a.isUnlocked)
    }

    @Test
    fun achievement_unlockedFlag_false() {
        val a = Achievement(2, "21 Days", "Maintain a 21-day streak", "📅", false)
        assertFalse(a.isUnlocked)
    }

    @Test
    fun achievement_progress_halfwayThrough() {
        val a = Achievement(6, "21 Days", "desc", "📅", false,
            requiredValue = 21, currentValue = 10)
        assertEquals(10f / 21f, a.progress, 0.01f)
    }

    @Test
    fun achievement_updateProgress_autoUnlocks() {
        val a = Achievement(6, "21 Days", "desc", "📅", false,
            requiredValue = 21, currentValue = 5)
        assertTrue(a.updateProgress(21).isUnlocked)
    }

    @Test
    fun achievement_updateProgress_staysLocked() {
        val a = Achievement(6, "21 Days", "desc", "📅", false,
            requiredValue = 21, currentValue = 5)
        assertFalse(a.updateProgress(15).isUnlocked)
    }

    @Test
    fun achievement_summaryText_correct() {
        val list = listOf(
            Achievement(1, "A", "a", "🍽️", true),
            Achievement(2, "B", "b", "🔥", false)
        )
        assertEquals("1/2 unlocked", Achievement.summaryText(list))
    }
}
