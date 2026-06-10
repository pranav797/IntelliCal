package com.cs346.intellical.data

import android.util.Log
import com.cs346.intellical.BuildConfig
import com.cs346.intellical.data.dto.*
import com.cs346.intellical.domain.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class SupabaseRepository : DatabaseInterface {

    private val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    override suspend fun getUser(userId: Int): User? = withContext(Dispatchers.IO) {
        try {
            Log.d("SupabaseRepository", "getUser started for $userId")
            val dto = withTimeout(15000L) {
                client.from("users").select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingleOrNull<UserDTO>()
            }
            Log.d("SupabaseRepository", "getUser decoded: $dto")
            
            dto?.let {
                User(
                    id = it.id,
                    username = it.username,
                    email = it.email,
                    memberSince = it.memberSince,
                    targetDate = it.targetDate,
                    currentWeight = it.currentWeight,
                    targetWeight = it.targetWeight,
                    startWeight = it.startWeight,
                    primaryGoal = UserGoal.valueOf(it.primaryGoal),
                    streak = it.streak,
                    daysTracked = it.daysTracked,
                    mealsLogged = it.mealsLogged
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error getting user: ${e.message}", e)
            null
        }
    }

    override suspend fun getMeals(userId: Int): List<Meal> = withContext(Dispatchers.IO) {
        try {
            Log.d("SupabaseRepository", "getMeals started for $userId")
            val dtos = withTimeout(15000L) {
                client.from("meals").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<MealDTO>()
            }
            Log.d("SupabaseRepository", "getMeals received ${dtos.size} meals")

            dtos.map { dto: MealDTO ->
                Meal(
                    id = dto.id,
                    name = dto.name,
                    calories = dto.calories,
                    proteinGrams = dto.proteinGrams,
                    carbsGrams = dto.carbsGrams,
                    fatGrams = dto.fatGrams,
                    healthScore = dto.healthScore,
                    timestamp = dto.timestamp,
                    ingredients = dto.ingredients
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error getting meals", e)
            emptyList()
        }
    }

    override suspend fun getMealsByDate(userId: Int, startOfDay: Long, endOfDay: Long): List<Meal> =
        withContext(Dispatchers.IO) {
            try {
                val dtos = withTimeout(15000L) {
                    client.from("meals").select {
                        filter {
                            eq("user_id", userId)
                            gte("timestamp", startOfDay)
                            lte("timestamp", endOfDay)
                        }
                    }.decodeList<MealDTO>()
                }
                dtos.map { dto ->
                    Meal(
                        id = dto.id,
                        name = dto.name,
                        calories = dto.calories,
                        proteinGrams = dto.proteinGrams,
                        carbsGrams = dto.carbsGrams,
                        fatGrams = dto.fatGrams,
                        healthScore = dto.healthScore,
                        timestamp = dto.timestamp,
                        ingredients = dto.ingredients
                    )
                }.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error getting meals by date", e)
                emptyList()
            }
        }

    override suspend fun getForest(userId: Int): Forest? = withContext(Dispatchers.IO) {
        try {
            Log.d("SupabaseRepository", "getForest started for $userId")
            val forestDto = withTimeout(15000L) {
                client.from("forests").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<ForestDTO>()
            }
            Log.d("SupabaseRepository", "getForest received DTO: $forestDto")
            
            if (forestDto == null) return@withContext null

            val treeDtos = withTimeout(15000L) {
                client.from("trees").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<TreeDTO>()
            }
            Log.d("SupabaseRepository", "getForest received ${treeDtos.size} trees")

            val slots = MutableList<Tree?>(forestDto.gridSize * forestDto.gridSize) { null }
            for (treeDto in treeDtos) {
                val type = TreeType.allTypes.find { it.name == treeDto.type } ?: TreeType.Oak
                slots[treeDto.rowPos * forestDto.gridSize + treeDto.colPos] = Tree(
                    row = treeDto.rowPos,
                    col = treeDto.colPos,
                    type = type,
                    growthStage = TreeGrowthStage.valueOf(treeDto.growthStage),
                    healthState = TreeHealthState.valueOf(treeDto.healthState),
                    plantedDate = treeDto.plantedDate,
                    growthPoints = treeDto.growthPoints,
                    consecutiveHealthyDays = treeDto.consecutiveHealthyDays
                )
            }

            Forest(
                slots = slots,
                totalXP = forestDto.totalXP,
                streakDays = forestDto.streakDays,
                gridSize = forestDto.gridSize
            )
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error getting forest", e)
            null
        }
    }

    override suspend fun getNutritionGoal(userId: Int): NutritionGoal? = withContext(Dispatchers.IO) {
        try {
            Log.d("SupabaseRepository", "getNutritionGoal started for $userId")
            val dto = withTimeout(15000L) {
                client.from("nutrition_goals").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<NutritionGoalDTO>()
            }
            Log.d("SupabaseRepository", "getNutritionGoal received DTO: $dto")
            
            dto?.let {
                NutritionGoal(
                    dailyCalories = it.dailyCalories,
                    proteinGrams = it.proteinGrams,
                    carbsGrams = it.carbsGrams,
                    fatGrams = it.fatGrams,
                    distribution = MacroDistribution.valueOf(it.distribution)
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error getting nutrition goal", e)
            null
        }
    }

    override suspend fun getAchievements(userId: Int): List<Achievement> = withContext(Dispatchers.IO) {
        try {
            Log.d("SupabaseRepository", "getAchievements started for $userId")
            val dtos = withTimeout(15000L) {
                client.from("achievements").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<AchievementDTO>()
            }
            Log.d("SupabaseRepository", "getAchievements received ${dtos.size} achievements")

            val baseAchievements = listOf(
                Achievement(1, "First Meal",    "Log your first meal",                     "🍽️", false,  AchievementCategory.NUTRITION, 1, 0),
                Achievement(2, "Week Streak",   "Maintain a 7-day streak",                 "🔥", false,  AchievementCategory.STREAK,    7, 0),
                Achievement(3, "Forest Keeper", "Plant 5 trees in your forest",            "🌲", false,  AchievementCategory.FOREST,    5, 0),
                Achievement(4, "10 Trees",      "Plant 10 trees total",                    "🌳", false,  AchievementCategory.FOREST,    10, 0),
                Achievement(5, "100 Meals",     "Log 100 meals",                           "💯", false,  AchievementCategory.NUTRITION, 100, 0),
                Achievement(6, "21 Days",       "Maintain a 21-day streak",                "📅", false,  AchievementCategory.STREAK,    21, 0),
                Achievement(7, "Ancient Tree",  "Grow a tree to Mature stage",             "🏆", false,  AchievementCategory.FOREST,    1, 0),
                Achievement(8, "Perfect Week",  "Hit all macro goals for 7 days straight", "⭐", false,  AchievementCategory.MILESTONE, 7, 0)
            )

            baseAchievements.map { base ->
                val status = dtos.find { it.id == base.id }
                base.copy(
                    isUnlocked = status?.unlocked ?: false,
                    currentValue = status?.currentValue ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error getting achievements", e)
            emptyList()
        }
    }

    override suspend fun saveUser(user: User) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("SupabaseRepository", "saveUser started for ${user.id}")
                val dto = UserDTO(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    memberSince = user.memberSince,
                    targetDate = user.targetDate,
                    currentWeight = user.currentWeight,
                    targetWeight = user.targetWeight,
                    startWeight = user.startWeight,
                    primaryGoal = user.primaryGoal.name,
                    streak = user.streak,
                    daysTracked = user.daysTracked,
                    mealsLogged = user.mealsLogged
                )
                client.from("users").upsert(dto)
                Log.d("SupabaseRepository", "saveUser finished")
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error saving user", e)
            }
        }
    }

    override suspend fun saveMeal(userId: Int, meal: Meal) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("SupabaseRepository", "saveMeal started for user $userId, meal ${meal.name}")
                val dto = MealDTO(
                    id = meal.id,
                    userId = userId,
                    name = meal.name,
                    calories = meal.calories,
                    proteinGrams = meal.proteinGrams,
                    carbsGrams = meal.carbsGrams,
                    fatGrams = meal.fatGrams,
                    healthScore = meal.healthScore,
                    timestamp = meal.timestamp,
                    ingredients = meal.ingredients
                )
                client.from("meals").upsert(dto)
                Log.d("SupabaseRepository", "saveMeal finished")
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error saving meal", e)
            }
        }
    }

    override suspend fun deleteMeal(mealId: String) {
        withContext(Dispatchers.IO) {
            try {
                client.from("meals").delete {
                    filter {
                        eq("id", mealId)
                    }
                }
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error deleting meal", e)
            }
        }
    }

    override suspend fun updateForest(userId: Int, forest: Forest) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("SupabaseRepository", "updateForest started for $userId")
                val forestDto = ForestDTO(
                    userId = userId,
                    totalXP = forest.totalXP,
                    streakDays = forest.streakDays,
                    gridSize = forest.gridSize
                )
                client.from("forests").upsert(forestDto)

                client.from("trees").delete {
                    filter {
                        eq("user_id", userId)
                    }
                }

                val treeDtos = forest.slots.filterNotNull().map { tree ->
                    TreeDTO(
                        userId = userId,
                        rowPos = tree.row,
                        colPos = tree.col,
                        type = tree.type?.name ?: "Oak",
                        growthStage = tree.growthStage.name,
                        healthState = tree.healthState.name,
                        plantedDate = tree.plantedDate,
                        growthPoints = tree.growthPoints,
                        consecutiveHealthyDays = tree.consecutiveHealthyDays
                    )
                }
                if (treeDtos.isNotEmpty()) {
                    client.from("trees").insert(treeDtos)
                }
                Log.d("SupabaseRepository", "updateForest finished")
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error updating forest", e)
            }
        }
    }

    override suspend fun updateNutritionGoal(userId: Int, goal: NutritionGoal) {
        withContext(Dispatchers.IO) {
            try {
                val dto = NutritionGoalDTO(
                    userId = userId,
                    dailyCalories = goal.dailyCalories,
                    proteinGrams = goal.proteinGrams,
                    carbsGrams = goal.carbsGrams,
                    fatGrams = goal.fatGrams,
                    distribution = goal.distribution.name
                )
                client.from("nutrition_goals").upsert(dto)
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error updating nutrition goal", e)
            }
        }
    }

    override suspend fun updateAchievements(userId: Int, achievements: List<Achievement>) {
        withContext(Dispatchers.IO) {
            try {
                val dtos = achievements.map { ach ->
                    AchievementDTO(
                        id = ach.id,
                        userId = userId,
                        unlocked = ach.isUnlocked,
                        currentValue = ach.currentValue
                    )
                }
                if (dtos.isNotEmpty()) {
                    client.from("achievements").upsert(dtos)
                }
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error updating achievements", e)
            }
        }
    }

    override suspend fun deleteUser(userId: Int) {
        withContext(Dispatchers.IO) {
            try {
                client.from("users").delete {
                    filter {
                        eq("id", userId)
                    }
                }
            } catch (e: Exception) {
                Log.e("SupabaseRepository", "Error deleting user", e)
            }
        }
    }
}
