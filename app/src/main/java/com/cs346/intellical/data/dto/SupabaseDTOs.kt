package com.cs346.intellical.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("member_since") val memberSince: String,
    @SerialName("target_date") val targetDate: String,
    @SerialName("current_weight") val currentWeight: Float,
    @SerialName("target_weight") val targetWeight: Float,
    @SerialName("start_weight") val startWeight: Float,
    @SerialName("primary_goal") val primaryGoal: String,
    @SerialName("streak") val streak: Int,
    @SerialName("days_tracked") val daysTracked: Int,
    @SerialName("meals_logged") val mealsLogged: Int
)

@Serializable
data class MealDTO(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: Int,
    @SerialName("name") val name: String,
    @SerialName("calories") val calories: Int,
    @SerialName("protein_grams") val proteinGrams: Int,
    @SerialName("carbs_grams") val carbsGrams: Int,
    @SerialName("fat_grams") val fatGrams: Int,
    @SerialName("health_score") val healthScore: Int,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("ingredients") val ingredients: List<String>
)

@Serializable
data class ForestDTO(
    @SerialName("user_id") val userId: Int,
    @SerialName("total_xp") val totalXP: Int,
    @SerialName("streak_days") val streakDays: Int,
    @SerialName("grid_size") val gridSize: Int
)

@Serializable
data class TreeDTO(
    @SerialName("user_id") val userId: Int,
    @SerialName("row_pos") val rowPos: Int,
    @SerialName("col_pos") val colPos: Int,
    @SerialName("type") val type: String,
    @SerialName("growth_stage") val growthStage: String,
    @SerialName("health_state") val healthState: String,
    @SerialName("planted_date") val plantedDate: Long?,
    @SerialName("growth_points") val growthPoints: Int,
    @SerialName("consecutive_healthy_days") val consecutiveHealthyDays: Int
)

@Serializable
data class NutritionGoalDTO(
    @SerialName("user_id") val userId: Int,
    @SerialName("daily_calories") val dailyCalories: Int,
    @SerialName("protein_grams") val proteinGrams: Int,
    @SerialName("carbs_grams") val carbsGrams: Int,
    @SerialName("fat_grams") val fatGrams: Int,
    @SerialName("distribution") val distribution: String
)

@Serializable
data class AchievementDTO(
    @SerialName("id") val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("unlocked") val unlocked: Boolean,
    @SerialName("current_value") val currentValue: Int
)
