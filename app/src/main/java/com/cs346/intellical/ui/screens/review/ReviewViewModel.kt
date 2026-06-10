package com.cs346.intellical.ui.screens.review

import androidx.lifecycle.ViewModel
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.round

private fun computeHealthScore(calories: Int, protein: Int, carbs: Int, fat: Int): Float {
    val proteinRatio   = if (calories > 0) (protein * 4.0) / calories else 0.0
    val fatRatio       = if (calories > 0) (fat * 9.0) / calories else 0.0
    val carbRatio      = if (calories > 0) (carbs * 4.0) / calories else 0.0
    val proteinBonus   = ((proteinRatio - 0.15) * 10.0).coerceIn(0.0, 3.0)
    val fatPenalty     = ((fatRatio - 0.35) * 10.0).coerceIn(0.0, 3.0)
    val carbPenalty    = ((carbRatio - 0.55) * 10.0).coerceIn(0.0, 2.0)
    val caloriePenalty = ((calories - 700.0) / 150.0).coerceIn(0.0, 3.0)
    val raw = 7.0 + proteinBonus - fatPenalty - carbPenalty - caloriePenalty
    return (kotlin.math.round(raw * 10.0) / 10.0).coerceIn(0.0, 10.0).toFloat()
}

data class MealAnalysis(
    val mealName: String = "Grilled Chicken Bowl",
    val description: String = "",
    val calories: Int = 620,
    val carbs: Int = 48,
    val protein: Int = 55,
    val fat: Int = 22,
    val healthScore: Float = computeHealthScore(620, 55, 48, 22)
)

class ReviewViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        AppModel.pendingEstimate?.let {
            val cal  = it.calories
            val pro  = it.proteinGrams
            val carb = it.carbsGrams
            val fat  = it.fatGrams
            MealAnalysis(
                mealName    = it.name,
                description = it.description,
                calories    = cal,
                carbs       = carb,
                protein     = pro,
                fat         = fat,
                healthScore = computeHealthScore(cal, pro, carb, fat)
            )
        } ?: MealAnalysis()
    )
    val uiState: StateFlow<MealAnalysis> = _uiState.asStateFlow()

    init {
        AppModel.pendingEstimate = null
    }

    private fun recalc(state: MealAnalysis): MealAnalysis =
        state.copy(healthScore = computeHealthScore(state.calories, state.protein, state.carbs, state.fat))

    fun updateCalories(value: Int) { _uiState.value = recalc(_uiState.value.copy(calories = value)) }
    fun updateCarbs(value: Int)    { _uiState.value = recalc(_uiState.value.copy(carbs    = value)) }
    fun updateProtein(value: Int)  { _uiState.value = recalc(_uiState.value.copy(protein  = value)) }
    fun updateFat(value: Int)      { _uiState.value = recalc(_uiState.value.copy(fat      = value)) }

    fun updateMealName(value: String) {
        _uiState.value = _uiState.value.copy(mealName = value)
    }

    fun confirmMeal() {
        val s = _uiState.value
        val meal = Meal(
            id           = UUID.randomUUID().toString(),
            name         = s.mealName,
            calories     = s.calories,
            proteinGrams = s.protein,
            carbsGrams   = s.carbs,
            fatGrams     = s.fat,
            healthScore  = s.healthScore.toInt().coerceIn(0, 10),
            timestamp    = System.currentTimeMillis(),
            ingredients  = emptyList()
        )
        AppModel.logMeal(meal)
    }
}
