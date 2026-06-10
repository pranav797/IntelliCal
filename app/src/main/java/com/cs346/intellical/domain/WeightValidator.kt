package com.cs346.intellical.domain

import kotlin.math.roundToInt

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}

object WeightValidator {
    const val ABSOLUTE_MIN_WEIGHT_LBS = 100.0
    const val ABSOLUTE_MAX_WEIGHT_LBS = 600.0
    const val MAX_LOSS_PERCENTAGE = 0.30

    fun validateTargetWeight(currentWeight: Double, targetWeight: Double): ValidationResult {
        if (targetWeight < ABSOLUTE_MIN_WEIGHT_LBS) {
            return ValidationResult.Invalid("Absolute minimum target weight is 100 lbs.")
        }

        if (targetWeight > ABSOLUTE_MAX_WEIGHT_LBS) {
            return ValidationResult.Invalid("Absolute maximum target weight is 600 lbs.")
        }

        val dynamicMinimumTarget = currentWeight * (1.0 - MAX_LOSS_PERCENTAGE)

        if (targetWeight < dynamicMinimumTarget) {
            return ValidationResult.Invalid(
                "Medical guidelines advise against losing more than 30% of your body weight in a single goal. Your minimum safe target for this phase is ${dynamicMinimumTarget.roundToInt()} lbs."
            )
        }

        return ValidationResult.Valid
    }
}
