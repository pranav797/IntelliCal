package com.cs346.intellical.domain

enum class UserGoal(val displayName: String) {
    WEIGHT_LOSS("Weight Loss"),
    MUSCLE_GAIN("Muscle Gain"),
    CLEAN_EATING("Clean Eating")
}

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val memberSince: String,
    val targetDate: String,
    val currentWeight: Float,
    val targetWeight: Float,
    val startWeight: Float,
    val primaryGoal: UserGoal,
    val streak: Int,
    val daysTracked: Int,
    val mealsLogged: Int
) {
    val initial: String get() = username.firstOrNull()?.uppercase() ?: ""

    val weightLost: Float get() = startWeight - currentWeight

    val weightRemaining: Float get() = (currentWeight - targetWeight).coerceAtLeast(0f)

    val weightProgress: Float get() {
        val totalToLose = startWeight - targetWeight
        if (totalToLose <= 0f) return 1f
        return (weightLost / totalToLose).coerceIn(0f, 1f)
    }

    val hasReachedGoal: Boolean get() = currentWeight <= targetWeight

    val averageMealsPerDay: Float get() {
        if (daysTracked <= 0) return 0f
        return mealsLogged.toFloat() / daysTracked
    }

    val streakTier: String get() = when {
        streak >= 30 -> "Legendary"
        streak >= 21 -> "Diamond"
        streak >= 14 -> "Gold"
        streak >= 7 -> "Silver"
        streak >= 3 -> "Bronze"
        else -> "Starter"
    }

    fun updateWeight(newWeight: Float): User = copy(currentWeight = newWeight)

    fun updateGoal(newGoal: UserGoal): User = copy(primaryGoal = newGoal)

    fun incrementStreak(): User = copy(streak = streak + 1)

    fun resetStreak(): User = copy(streak = 0)

    fun logMealTracked(): User = copy(mealsLogged = mealsLogged + 1)
}
