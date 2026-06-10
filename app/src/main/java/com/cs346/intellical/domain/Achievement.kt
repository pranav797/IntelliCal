package com.cs346.intellical.domain

enum class AchievementCategory(val displayName: String) {
    NUTRITION("Nutrition"),
    FOREST("Forest"),
    STREAK("Streak"),
    MILESTONE("Milestone")
}

data class Achievement(
    val id: Int,
    val label: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val category: AchievementCategory = AchievementCategory.MILESTONE,
    val requiredValue: Int = 0,
    val currentValue: Int = 0
) {
    val progress: Float get() {
        if (isUnlocked) return 1f
        if (requiredValue <= 0) return 0f
        return (currentValue.toFloat() / requiredValue).coerceIn(0f, 1f)
    }

    fun unlock(): Achievement = copy(isUnlocked = true, currentValue = requiredValue)

    fun updateProgress(newValue: Int): Achievement {
        val updated = copy(currentValue = newValue)
        return if (newValue >= requiredValue) updated.unlock() else updated
    }

    companion object {
        fun unlockedCount(achievements: List<Achievement>): Int =
            achievements.count { it.isUnlocked }

        fun summaryText(achievements: List<Achievement>): String {
            val unlocked = unlockedCount(achievements)
            val total = achievements.size
            return "$unlocked/$total unlocked"
        }

        fun filterByCategory(
            achievements: List<Achievement>,
            category: AchievementCategory
        ): List<Achievement> = achievements.filter { it.category == category }
    }
}
