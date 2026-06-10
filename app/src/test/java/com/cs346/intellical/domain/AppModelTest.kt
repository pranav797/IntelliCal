package com.cs346.intellical.domain

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppModelTest {

    @Before
    fun setup() {
        runBlocking { AppModel.reset() }
        (AppModel.getTodayMeals() as MutableList).clear()
        AppModel.updateGoal(
                NutritionGoal(
                        dailyCalories = 2000,
                        proteinGrams = 100,
                        carbsGrams = 200,
                        fatGrams = 50,
                        distribution = MacroDistribution.BALANCED
                )
        )
    }

    private fun plantTestTrees() {
        AppModel.plantTree(0, 0, TreeType.Oak)
        AppModel.plantTree(1, 1, TreeType.Pine)
    }

    @Test
    fun `logMeal with 0 macros met adds only XP, no GP bonus`() {
        plantTestTrees()
        val initialXP = AppModel.getForest().totalXP
        val initialOakGP = AppModel.getForest().getTree(0, 0)!!.growthPoints

        val meal =
                Meal(
                        id = UUID.randomUUID().toString(),
                        name = "Small Snack",
                        calories = 100,
                        proteinGrams = 10,
                        carbsGrams = 10,
                        fatGrams = 10,
                        healthScore = 8,
                        timestamp = System.currentTimeMillis(),
                        ingredients = emptyList()
                )

        val updatedForest = AppModel.logMeal(meal)

        assertEquals(initialXP + 25, updatedForest.totalXP)
        val oak = updatedForest.getTree(0, 0)!!
        assertEquals(initialOakGP, oak.growthPoints)
    }

    @Test
    fun `logMeal with 1 macro met adds +1 GP to all trees`() {
        plantTestTrees()
        val initialXP = AppModel.getForest().totalXP
        val initialOakGP = AppModel.getForest().getTree(0, 0)!!.growthPoints
        val initialPineGP = AppModel.getForest().getTree(1, 1)!!.growthPoints

        val meal =
                Meal(
                        id = UUID.randomUUID().toString(),
                        name = "Protein Shake",
                        calories = 500,
                        proteinGrams = 110,
                        carbsGrams = 10,
                        fatGrams = 10,
                        healthScore = 6,
                        timestamp = System.currentTimeMillis(),
                        ingredients = emptyList()
                )

        val updatedForest = AppModel.logMeal(meal)

        assertEquals(initialXP + 15, updatedForest.totalXP)

        val oak = updatedForest.getTree(0, 0)!!
        val pine = updatedForest.getTree(1, 1)!!
        assertEquals(initialOakGP + 1, oak.growthPoints)
        assertEquals(initialPineGP + 1, pine.growthPoints)
    }

    @Test
    fun `logMeal with 2 macros met adds +2 GP to all trees`() {
        plantTestTrees()
        val initialXP = AppModel.getForest().totalXP
        val initialOakGP = AppModel.getForest().getTree(0, 0)!!.growthPoints
        val initialPineGP = AppModel.getForest().getTree(1, 1)!!.growthPoints

        val meal =
                Meal(
                        id = UUID.randomUUID().toString(),
                        name = "Carb Heavy Oil",
                        calories = 1600,
                        proteinGrams = 10,
                        carbsGrams = 250,
                        fatGrams = 60,
                        healthScore = 4,
                        timestamp = System.currentTimeMillis(),
                        ingredients = emptyList()
                )

        val updatedForest = AppModel.logMeal(meal)

        assertEquals(initialXP + 5, updatedForest.totalXP)

        val oak = updatedForest.getTree(0, 0)!!
        val pine = updatedForest.getTree(1, 1)!!
        assertEquals(initialOakGP + 2, oak.growthPoints)
        assertEquals(initialPineGP + 2, pine.growthPoints)
    }

    @Test
    fun `logMeal with 3 macros met adds +3 GP to all trees`() {
        plantTestTrees()
        val initialXP = AppModel.getForest().totalXP
        val initialOakGP = AppModel.getForest().getTree(0, 0)!!.growthPoints
        val initialPineGP = AppModel.getForest().getTree(1, 1)!!.growthPoints

        val meal =
                Meal(
                        id = UUID.randomUUID().toString(),
                        name = "Giant Feast",
                        calories = 2500,
                        proteinGrams = 150,
                        carbsGrams = 250,
                        fatGrams = 100,
                        healthScore = 10,
                        timestamp = System.currentTimeMillis(),
                        ingredients = emptyList()
                )

        val updatedForest = AppModel.logMeal(meal)

        assertEquals(initialXP + 25, updatedForest.totalXP)

        val oak = updatedForest.getTree(0, 0)!!
        val pine = updatedForest.getTree(1, 1)!!
        assertEquals(initialOakGP + 3, oak.growthPoints)
        assertEquals(initialPineGP + 3, pine.growthPoints)
    }
}
