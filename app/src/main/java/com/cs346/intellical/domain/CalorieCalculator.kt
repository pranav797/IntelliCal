package com.cs346.intellical.domain

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

enum class GoalType {
    CUT,
    BULK,
    MAINTAIN
}

sealed class CalorieResult {
    data class Success(
        val recommendedDailyCalories: Int,
        val weeklyWeightChangeRate: Double,
        val goalType: GoalType
    ) : CalorieResult()

    data class Error(
        val errorMessage: String,
        val minimumSafeWeeks: Int
    ) : CalorieResult()
}

object CalorieCalculator {
    fun minimumSafeWeeks(
        currentWeight: Double,
        targetWeight: Double
    ): Int {
        val weightDifference = targetWeight - currentWeight
        val goalType = when {
            weightDifference < 0 -> GoalType.CUT
            weightDifference > 0 -> GoalType.BULK
            else -> GoalType.MAINTAIN
        }
        val minWeeksByRate = when (goalType) {
            GoalType.CUT -> abs(weightDifference) / 2.0
            GoalType.BULK -> weightDifference / 1.0
            GoalType.MAINTAIN -> 0.0
        }
        val minWeeksByCalories = if (goalType == GoalType.CUT) {
            val baseMaintenance = currentWeight * 15.0
            val maxDailyDeficit = baseMaintenance - 1200.0
            val maxSafeWeeklyLoss = (maxDailyDeficit * 7.0) / 3500.0
            if (maxSafeWeeklyLoss > 0.0) abs(weightDifference) / maxSafeWeeklyLoss else Double.POSITIVE_INFINITY
        } else {
            0.0
        }
        return ceil(max(minWeeksByRate, minWeeksByCalories.coerceAtMost(520.0))).toInt().coerceIn(0, 520)
    }

    fun calculateRecommendation(
        currentWeight: Double,
        targetWeight: Double,
        timeframeWeeks: Int
    ): CalorieResult {
        val weightDifference = targetWeight - currentWeight
        val goalType = when {
            weightDifference < 0 -> GoalType.CUT
            weightDifference > 0 -> GoalType.BULK
            else -> GoalType.MAINTAIN
        }
        val safeTimeframeWeeks = timeframeWeeks.coerceAtLeast(1)
        val absoluteMinWeeks = minimumSafeWeeks(currentWeight, targetWeight)

        if (safeTimeframeWeeks < absoluteMinWeeks) {
            return CalorieResult.Error(
                errorMessage = "Unsafe timeline. The minimum safe timeframe for this goal is $absoluteMinWeeks weeks.",
                minimumSafeWeeks = absoluteMinWeeks
            )
        }

        val weeklyWeightChangeRate = abs(weightDifference) / safeTimeframeWeeks
        val dailyCalorieAdjustment = (weightDifference / safeTimeframeWeeks) * (3500.0 / 7.0)
        val recommendedDailyCalories = ((currentWeight * 15.0) + dailyCalorieAdjustment).roundToInt()

        return CalorieResult.Success(
            recommendedDailyCalories = recommendedDailyCalories,
            weeklyWeightChangeRate = weeklyWeightChangeRate,
            goalType = goalType
        )
    }
}
