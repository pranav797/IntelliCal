package com.cs346.intellical.data

import com.cs346.intellical.domain.Achievement
import com.cs346.intellical.domain.Forest
import com.cs346.intellical.domain.Meal
import com.cs346.intellical.domain.NutritionGoal
import com.cs346.intellical.domain.User


interface DatabaseInterface {

    suspend fun getUser(userId: Int): User?

    suspend fun getMeals(userId: Int): List<Meal>

    suspend fun getMealsByDate(userId: Int, startOfDay: Long, endOfDay: Long): List<Meal>

    suspend fun getForest(userId: Int): Forest?

    suspend fun getNutritionGoal(userId: Int): NutritionGoal?

    suspend fun getAchievements(userId: Int): List<Achievement>

    suspend fun saveUser(user: User)

    suspend fun saveMeal(userId: Int, meal: Meal)

    suspend fun deleteMeal(mealId: String)

    suspend fun updateForest(userId: Int, forest: Forest)

    suspend fun updateNutritionGoal(userId: Int, goal: NutritionGoal)

    suspend fun updateAchievements(userId: Int, achievements: List<Achievement>)

    suspend fun deleteUser(userId: Int)
}
