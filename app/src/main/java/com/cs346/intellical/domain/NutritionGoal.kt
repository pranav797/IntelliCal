package com.cs346.intellical.domain

enum class MacroDistribution(
    val label: String,
    val description: String,
    val carbsPercent: Int,
    val proteinPercent: Int,
    val fatPercent: Int
) {
    BALANCED("Balanced", "Even mix for general health", 45, 20, 35),
    LOW_CARB("Low-Carb", "High protein & fat for ketosis", 20, 40, 40),
    LOW_FAT("Low-Fat", "Higher carbs, lower fat intake", 50, 20, 30)
}

data class NutritionGoal(
    val dailyCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val distribution: MacroDistribution
) {
    fun isProteinMet(meals: List<Meal>): Boolean =
        meals.sumOf { it.proteinGrams } >= proteinGrams

    fun isFatsMet(meals: List<Meal>): Boolean =
        meals.sumOf { it.fatGrams } >= fatGrams

    fun isCarbsMet(meals: List<Meal>): Boolean =
        meals.sumOf { it.carbsGrams } >= carbsGrams

    fun caloriesFromMeals(meals: List<Meal>): Int =
        meals.sumOf { it.calories }

    fun remainingCalories(meals: List<Meal>): Int =
        dailyCalories - caloriesFromMeals(meals)
}
