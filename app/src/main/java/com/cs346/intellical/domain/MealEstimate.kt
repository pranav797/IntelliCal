package com.cs346.intellical.domain

data class MealEstimate(
    val name: String,
    val description: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val healthScore: Int,
    val ingredients: List<String>,
    val confidence: Float
)
