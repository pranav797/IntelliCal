package com.cs346.intellical.domain

data class Meal(
    val id: String,
    val name: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val healthScore: Int, // 0-10
    val timestamp: Long,
    val ingredients: List<String>
) {
    val qualityBonus: Int
        get() = when {
            healthScore >= 8 -> 25
            healthScore >= 6 -> 15
            healthScore >= 4 -> 5
            else -> 0
        }

    val computedHealthScore: Double
        get() {
            val proteinRatio   = if (calories > 0) (proteinGrams * 4.0) / calories else 0.0
            val fatRatio       = if (calories > 0) (fatGrams * 9.0) / calories else 0.0
            val carbRatio      = if (calories > 0) (carbsGrams * 4.0) / calories else 0.0
            val proteinBonus   = ((proteinRatio - 0.15) * 10.0).coerceIn(0.0, 3.0)
            val fatPenalty     = ((fatRatio - 0.35) * 10.0).coerceIn(0.0, 3.0)
            val carbPenalty    = ((carbRatio - 0.55) * 10.0).coerceIn(0.0, 2.0)
            val caloriePenalty = ((calories - 700.0) / 150.0).coerceIn(0.0, 3.0)
            val raw = 7.0 + proteinBonus - fatPenalty - carbPenalty - caloriePenalty
            return (kotlin.math.round(raw * 10.0) / 10.0).coerceIn(0.0, 10.0)
        }
}
