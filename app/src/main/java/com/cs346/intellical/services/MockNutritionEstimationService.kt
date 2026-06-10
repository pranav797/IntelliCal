package com.cs346.intellical.services

import com.cs346.intellical.domain.MealEstimate
import kotlinx.coroutines.delay

class MockNutritionEstimationService : NutritionEstimationService {

    private val sampleEstimates = listOf(
        MealEstimate(
            name = "Grilled Chicken Salad",
            description = "A light and protein-packed salad with tender grilled chicken breast over fresh mixed greens.",
            calories = 450,
            proteinGrams = 45,
            carbsGrams = 20,
            fatGrams = 22,
            healthScore = 9,
            ingredients = listOf("Chicken Breast", "Mixed Greens", "Tomato", "Vinaigrette"),
            confidence = 0.92f
        ),
        MealEstimate(
            name = "Pepperoni Pizza Slice",
            description = "A classic pepperoni pizza slice with gooey mozzarella and a crispy crust.",
            calories = 300,
            proteinGrams = 12,
            carbsGrams = 35,
            fatGrams = 14,
            healthScore = 3,
            ingredients = listOf("Pizza Dough", "Tomato Sauce", "Mozzarella", "Pepperoni"),
            confidence = 0.85f
        ),
        MealEstimate(
            name = "Avocado Toast with Egg",
            description = "Hearty whole wheat toast topped with mashed avocado and a perfectly fried egg.",
            calories = 380,
            proteinGrams = 18,
            carbsGrams = 30,
            fatGrams = 24,
            healthScore = 8,
            ingredients = listOf("Whole Wheat Bread", "Avocado", "Fried Egg", "Everything Bagel Seasoning"),
            confidence = 0.95f
        ),
        MealEstimate(
            name = "Spaghetti Bolognese",
            description = "A comforting bowl of spaghetti with a rich, slow-cooked meat sauce and parmesan.",
            calories = 650,
            proteinGrams = 32,
            carbsGrams = 85,
            fatGrams = 20,
            healthScore = 6,
            ingredients = listOf("Spaghetti", "Ground Beef", "Tomato Sauce", "Parmesan"),
            confidence = 0.88f
        ),
        MealEstimate(
            name = "Blueberry Protein Smoothie",
            description = "A refreshing, nutrient-dense smoothie packed with antioxidants and lean protein.",
            calories = 280,
            proteinGrams = 25,
            carbsGrams = 35,
            fatGrams = 5,
            healthScore = 9,
            ingredients = listOf("Whey Protein", "Frozen Blueberries", "Almond Milk", "Spinach"),
            confidence = 0.97f
        )
    )

    override suspend fun estimateFromImage(imageBytes: ByteArray): MealEstimate {

        delay((1500L..3000L).random())
        

        return sampleEstimates.random()
    }
}
