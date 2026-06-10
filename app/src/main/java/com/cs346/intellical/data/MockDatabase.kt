package com.cs346.intellical.data

import com.cs346.intellical.domain.*

class MockDatabase(seedData: Boolean = true) : DatabaseInterface {

    private val users      = mutableMapOf<Int, User>()
    private val meals      = mutableMapOf<Int, MutableList<Meal>>()
    private val forests    = mutableMapOf<Int, Forest>()
    private val goals      = mutableMapOf<Int, NutritionGoal>()
    private val achievements = mutableMapOf<Int, MutableList<Achievement>>()

    init {
        if (seedData) {
            seed()
        } else {
            seedEmpty()
        }
    }

    private fun seedEmpty() {
        val userId = 1
        users[userId] = User(id = userId, username = "Test", email = "test@app", memberSince = "", targetDate = "", startWeight = 0f, targetWeight = 0f, currentWeight = 0f, primaryGoal = UserGoal.WEIGHT_LOSS, streak = 0, daysTracked = 0, mealsLogged = 0)
        forests[userId] = Forest(slots = MutableList(25) { null }, totalXP = 0, streakDays = 0, gridSize = 5)
        goals[userId] = NutritionGoal(2000, 100, 200, 50, MacroDistribution.BALANCED)
        meals[userId] = mutableListOf()
        achievements[userId] = mutableListOf(
            Achievement(1, "First Meal", "", "", false, AchievementCategory.NUTRITION, 1, 0),
            Achievement(2, "Week Streak", "", "", false, AchievementCategory.STREAK, 7, 0),
            Achievement(3, "Forest Keeper", "", "", false, AchievementCategory.FOREST, 5, 0),
            Achievement(4, "10 Trees", "", "", false, AchievementCategory.FOREST, 10, 0),
            Achievement(5, "100 Meals", "", "", false, AchievementCategory.NUTRITION, 100, 0),
            Achievement(6, "21 Days", "", "", false, AchievementCategory.STREAK, 21, 0),
            Achievement(7, "Ancient Tree", "", "", false, AchievementCategory.FOREST, 1, 0),
            Achievement(8, "Perfect Week", "", "", false, AchievementCategory.MILESTONE, 7, 0)
        )
    }

    private fun seed() {
        val userId = 1

        users[userId] = User(
            id            = userId,
            username      = "Demo User",
            email         = "demo@example.com",
            memberSince   = "Member since Jan 2026",
            targetDate    = "June 1, 2026",
            currentWeight = 170f,
            targetWeight  = 165f,
            startWeight   = 175f,
            primaryGoal   = UserGoal.WEIGHT_LOSS,
            streak        = 5,
            daysTracked   = 20,
            mealsLogged   = 99
        )

        goals[userId] = NutritionGoal(
            dailyCalories = 2500,
            proteinGrams  = 125,
            carbsGrams    = 281,
            fatGrams      = 97,
            distribution  = MacroDistribution.BALANCED
        )

        val now = System.currentTimeMillis()
        val yesterday = now - 24 * 60 * 60 * 1000L   // 24 h ago (yesterday)

        meals[userId] = mutableListOf(
            // ---- Today ----
            Meal("1", "Apple Salmon Salad",        500, 78, 78, 78, 9, now -  7_200_000L, listOf("Salmon", "Apple", "Mixed Greens", "Olive Oil")),
            Meal("2", "Greek Yogurt with Berries", 320, 25, 45,  8, 8, now - 18_000_000L, listOf("Greek Yogurt", "Blueberries", "Strawberries", "Honey")),
            Meal("3", "Grilled Chicken Wrap",      450, 35, 40, 15, 7, now - 28_800_000L, listOf("Chicken Breast", "Tortilla", "Lettuce", "Tomato")),
            Meal("4", "Oatmeal with Banana",       280,  8, 52,  5, 7, now - 36_000_000L, listOf("Oats", "Banana", "Almond Milk", "Cinnamon")),
            Meal("5", "Protein Smoothie",          350, 30, 40, 10, 8, now - 43_200_000L, listOf("Whey Protein", "Banana", "Peanut Butter", "Milk")),
            // ---- Yesterday ----
            Meal("y1", "Avocado Toast",            380, 12, 45, 18, 8, yesterday -  1_800_000L, listOf("Sourdough", "Avocado", "Egg", "Chilli Flakes")),
            Meal("y2", "Chicken Caesar Salad",     520, 42, 18, 28, 9, yesterday - 14_400_000L, listOf("Chicken", "Romaine", "Parmesan", "Caesar Dressing")),
            Meal("y3", "Beef Stir Fry",            610, 38, 60, 22, 7, yesterday - 25_200_000L, listOf("Beef", "Broccoli", "Bell Pepper", "Soy Sauce", "Rice"))
        )

        val gridSize = 5
        val slots: MutableList<Tree?> = MutableList(gridSize * gridSize) { null }
        fun place(row: Int, col: Int, tree: Tree) { slots[row * gridSize + col] = tree }

        place(0, 0, Tree(0, 0, TreeType.Pine,   TreeGrowthStage.SAPLING,  TreeHealthState.HEALTHY,  now -  86_400_000L,  15,  1))
        place(0, 3, Tree(0, 3, TreeType.Pine,   TreeGrowthStage.SAPLING,  TreeHealthState.HEALTHY,  now - 172_800_000L,  20,  2))
        place(1, 1, Tree(1, 1, TreeType.Maple,  TreeGrowthStage.JUVENILE, TreeHealthState.HEALTHY,  now - 432_000_000L,  60,  5))
        place(1, 4, Tree(1, 4, TreeType.Maple,  TreeGrowthStage.JUVENILE, TreeHealthState.THRIVING, now - 518_400_000L,  70,  6))
        place(2, 2, Tree(2, 2, TreeType.Bamboo, TreeGrowthStage.MATURE,   TreeHealthState.THRIVING, now - 864_000_000L, 150, 10))
        place(3, 0, Tree(3, 0, TreeType.Sakura, TreeGrowthStage.MATURE,   TreeHealthState.THRIVING, now - 777_600_000L, 150,  9))
        place(4, 4, Tree(4, 4, TreeType.Palm,   TreeGrowthStage.MATURE,   TreeHealthState.HEALTHY,  now - 691_200_000L, 140,  8))

        forests[userId] = Forest(slots = slots, totalXP = 2095, streakDays = 5, gridSize = gridSize)

        achievements[userId] = mutableListOf(
            Achievement(1, "First Meal",    "Log your first meal",                     "🍽️", true,  AchievementCategory.NUTRITION, requiredValue = 1,   currentValue = 8),
            Achievement(2, "Week Streak",   "Maintain a 7-day streak",                 "🔥", false, AchievementCategory.STREAK,    requiredValue = 7,   currentValue = 5),
            Achievement(3, "Forest Keeper", "Plant 5 trees in your forest",            "🌲", true,  AchievementCategory.FOREST,    requiredValue = 5,   currentValue = 7),
            Achievement(4, "10 Trees",      "Plant 10 trees total",                    "🌳", false, AchievementCategory.FOREST,    requiredValue = 10,  currentValue = 7),
            Achievement(5, "100 Meals",     "Log 100 meals",                           "💯", false, AchievementCategory.NUTRITION, requiredValue = 100, currentValue = 99),
            Achievement(6, "21 Days",       "Maintain a 21-day streak",                "📅", false, AchievementCategory.STREAK,    requiredValue = 21,  currentValue = 20),
            Achievement(7, "Ancient Tree",  "Grow a tree to Mature stage",             "🏆", true,  AchievementCategory.FOREST,    requiredValue = 1,   currentValue = 3),
            Achievement(8, "Perfect Week",  "Hit all macro goals for 7 days straight", "⭐", false, AchievementCategory.MILESTONE, requiredValue = 7,   currentValue = 5)
        )
    }

    override suspend fun getUser(userId: Int): User? =
        users[userId]

    override suspend fun getMeals(userId: Int): List<Meal> =
        meals[userId]?.sortedByDescending { it.timestamp } ?: emptyList()

    override suspend fun getMealsByDate(userId: Int, startOfDay: Long, endOfDay: Long): List<Meal> =
        meals[userId]
            ?.filter { it.timestamp in startOfDay..endOfDay }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()

    override suspend fun getForest(userId: Int): Forest? =
        forests[userId]

    override suspend fun getNutritionGoal(userId: Int): NutritionGoal? =
        goals[userId]

    override suspend fun getAchievements(userId: Int): List<Achievement> =
        achievements[userId]?.toList() ?: emptyList()


    override suspend fun saveUser(user: User) {
        users[user.id] = user
    }

    override suspend fun saveMeal(userId: Int, meal: Meal) {
        val list = meals.getOrPut(userId) { mutableListOf() }
        val idx = list.indexOfFirst { it.id == meal.id }
        if (idx >= 0) list[idx] = meal else list.add(meal)
    }

    override suspend fun updateForest(userId: Int, forest: Forest) {
        forests[userId] = forest
    }

    override suspend fun updateNutritionGoal(userId: Int, goal: NutritionGoal) {
        goals[userId] = goal
    }

    override suspend fun updateAchievements(userId: Int, achievements: List<Achievement>) {
        this.achievements[userId] = achievements.toMutableList()
    }


    override suspend fun deleteMeal(mealId: String) {
        meals.values.forEach { list -> list.removeAll { it.id == mealId } }
    }

    override suspend fun deleteUser(userId: Int) {
        users.remove(userId)
        meals.remove(userId)
        forests.remove(userId)
        goals.remove(userId)
        achievements.remove(userId)
    }
}
