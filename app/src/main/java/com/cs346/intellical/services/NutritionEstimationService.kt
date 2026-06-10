package com.cs346.intellical.services

import com.cs346.intellical.domain.MealEstimate

interface NutritionEstimationService {
    /**
     * Attempts to estimate the nutritional breakdown of a food image.
     * @param imageBytes The raw bytes of the captured or selected image.
     * @return the estimated [MealEstimate]
     * @throws Exception if analysis fails or network is down
     */
    suspend fun estimateFromImage(imageBytes: ByteArray): MealEstimate
}
