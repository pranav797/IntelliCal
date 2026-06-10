package com.cs346.intellical.domain

import com.cs346.intellical.data.DatabaseInterface
import com.cs346.intellical.data.MockDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

object AppModel {

    private var db: DatabaseInterface = MockDatabase()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentUserId: Int = 1

    fun initialize(database: DatabaseInterface) {
        db = database
    }

    private lateinit var _forest: Forest
    private lateinit var _user: User
    private lateinit var _achievements: List<Achievement>
    private lateinit var _nutritionGoal: NutritionGoal
    private val _meals = mutableListOf<Meal>()

    var pendingEstimate: MealEstimate? = null

    private var _dailyGrowthPoints: Int = 0

    private var _lastKnownLevel: Int = 1
    private var _pendingLevelUp: Boolean = false
    private var _newlyUnlocked: Achievement? = null

    val allTreeDefinitions: List<TreeTypeInfo> =
            listOf(
                    TreeTypeInfo(1, "Oak", "\uD83C\uDF33", true),
                    TreeTypeInfo(2, "Pine", "\uD83C\uDF32", true),
                    TreeTypeInfo(3, "Cherry", "\uD83C\uDF38", true),
                    TreeTypeInfo(4, "Maple", "\uD83C\uDF41", true),
                    TreeTypeInfo(5, "Willow", "\uD83C\uDF3F", false),
                    TreeTypeInfo(6, "Bamboo", "\uD83C\uDF8B", false),
                    TreeTypeInfo(7, "Palm", "\uD83C\uDF34", false),
                    TreeTypeInfo(8, "Cactus", "\uD83C\uDF35", false),
                    TreeTypeInfo(9, "Birch", "\uD83C\uDF32", false),
                    TreeTypeInfo(10, "Sequoia", "\uD83C\uDF33", false),
                    TreeTypeInfo(11, "Bonsai", "\uD83C\uDF3F", false),
                    TreeTypeInfo(12, "Sakura", "\uD83C\uDF38", false),
                    TreeTypeInfo(13, "Eucalyptus", "\uD83C\uDF3F", false),
                    TreeTypeInfo(14, "Baobab", "🌴", false),
                    TreeTypeInfo(15, "Redwood", "🌲", false),
                    TreeTypeInfo(16, "Olive", "🌾", false),
                    TreeTypeInfo(17, "Mangrove", "🌿", false),
                    TreeTypeInfo(18, "Cypress", "🌲", false),
                    TreeTypeInfo(19, "Dogwood", "🌸", false),
                    TreeTypeInfo(20, "Fir", "🌲", false)
            )

    private val _unlockedTreeIds = mutableSetOf<Int>()

    suspend fun load(userId: Int) {
        currentUserId = userId
        _user =
                db.getUser(userId)
                        ?: User(
                                id = userId,
                                username = "NewUser",
                                email = "",
                                memberSince = java.util.Date().toString(),
                                targetDate = "June 30, 2026",
                                currentWeight = 170f,
                                targetWeight = 165f,
                                startWeight = 170f,
                                primaryGoal = UserGoal.WEIGHT_LOSS,
                                streak = 0,
                                daysTracked = 0,
                                mealsLogged = 0
                        )
        _forest = db.getForest(userId) ?: Forest(totalXP = 0, slots = List(25) { null })
        _achievements = db.getAchievements(userId)
        _nutritionGoal =
                db.getNutritionGoal(userId)
                        ?: NutritionGoal(
                                dailyCalories = 2000,
                                proteinGrams = 150,
                                carbsGrams = 200,
                                fatGrams = 60,
                                distribution = MacroDistribution.BALANCED
                        )
        _meals.clear()
        _meals.addAll(db.getMeals(userId))
        _dailyGrowthPoints = 0
        _lastKnownLevel = _forest.forestLevel
        _pendingLevelUp = false
        _newlyUnlocked = null
        _unlockedTreeIds.clear()
        val totalUnlocked = (2 + _forest.forestLevel * 2).coerceAtMost(allTreeDefinitions.size)
        _unlockedTreeIds.addAll(1..totalUnlocked)

        val latestMealTimestamp = _meals.maxByOrNull { it.timestamp }?.timestamp
        if (latestMealTimestamp != null) {
            if (!isToday(latestMealTimestamp) && !isYesterday(latestMealTimestamp)) {
                _user = _user.resetStreak()
                _forest = _forest.copy(streakDays = 0)
                val resetUser = _user
                val resetForest = _forest
                scope.launch {
                    db.saveUser(resetUser)
                    db.updateForest(currentUserId, resetForest)
                }
            }
        }
        _forest = _forest.copy(streakDays = _user.streak)

        checkAndUpdateAchievements()
    }

    fun getUnlockedTreeDefinitions(): List<TreeTypeInfo> =
            allTreeDefinitions.filter { it.id in _unlockedTreeIds }

    fun getTreeDefinitionsWithLockState(): List<TreeTypeInfo> =
            allTreeDefinitions.map { it.copy(unlocked = it.id in _unlockedTreeIds) }

    /** Unlocks the next 2 locked trees. Called when a level-up is detected. */
    private fun unlockNextTrees() {
        val nextLocked = allTreeDefinitions.filter { it.id !in _unlockedTreeIds }.take(2)
        nextLocked.forEach { _unlockedTreeIds.add(it.id) }
    }

    private fun checkAndUpdateAchievements() {
        val newValues = mapOf(
            1 to _meals.size,
            2 to _user.streak,
            3 to _forest.treeCount,
            4 to _forest.treeCount,
            5 to _user.mealsLogged,
            6 to _user.daysTracked,
            7 to _forest.matureCount,
            8 to _forest.streakDays
        )
        val updated = _achievements.map { ach ->
            val wasUnlocked = ach.isUnlocked
            val newAch = ach.updateProgress(newValues[ach.id] ?: ach.currentValue)
            if (!wasUnlocked && newAch.isUnlocked) _newlyUnlocked = newAch
            newAch
        }
        _achievements = updated
        val finalAchievements = updated
        scope.launch { db.updateAchievements(currentUserId, finalAchievements) }
    }

    fun getForest(): Forest = _forest
    fun getUser(): User = _user
    fun getAchievements(): List<Achievement> = _achievements
    fun getNutritionGoal(): NutritionGoal = _nutritionGoal
    fun getDailyGrowthPoints(): Int = _dailyGrowthPoints

    fun consumeLevelUp(): Boolean {
        val result = _pendingLevelUp
        _pendingLevelUp = false
        return result
    }

    fun consumeUnlock(): Achievement? {
        val result = _newlyUnlocked
        _newlyUnlocked = null
        return result
    }

    fun updateGoal(goal: NutritionGoal) {
        _nutritionGoal = goal
        scope.launch { db.updateNutritionGoal(currentUserId, goal) }
    }

    fun getCurrentWeight(): Float = _user.currentWeight
    fun getTargetWeight(): Float = _user.targetWeight
    fun getTargetDate(): String = _user.targetDate

    fun updateWeight(weight: Float) {
        _user = _user.copy(currentWeight = weight)
        val finalUser = _user
        scope.launch { db.saveUser(finalUser) }
    }

    fun updateWeight(current: Float, target: Float) {
        _user = _user.copy(currentWeight = current, targetWeight = target)
        val finalUser = _user
        scope.launch { db.saveUser(finalUser) }
    }

    fun updateWeightProgress(current: Float, target: Float, targetDate: String) {
        _user = _user.copy(currentWeight = current, targetWeight = target, targetDate = targetDate)
        val finalUser = _user
        scope.launch { db.saveUser(finalUser) }
    }

    fun updateTargetWeight(weight: Float) {
        _user = _user.copy(targetWeight = weight)
        val finalUser = _user
        scope.launch { db.saveUser(finalUser) }
    }

    fun getTodayMeals(): List<Meal> = _meals

  
    suspend fun getMealsForDate(dayOffset: Int): List<Meal> {
        if (dayOffset == 0) return _meals.toList()

        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val endOfDay   = startOfDay + 24 * 60 * 60 * 1000L - 1

        return db.getMealsByDate(currentUserId, startOfDay, endOfDay)
    }

    fun logMeal(meal: Meal): Forest {
        val oldProteinMet = _nutritionGoal.isProteinMet(_meals)
        val oldCarbsMet = _nutritionGoal.isCarbsMet(_meals)
        val oldFatsMet = _nutritionGoal.isFatsMet(_meals)

        val hadMealToday = _meals.any { isToday(it.timestamp) }
        _meals.add(meal)
        _user = _user.logMealTracked()
        if (!hadMealToday && isToday(meal.timestamp)) {
            _user = _user.incrementStreak().copy(daysTracked = _user.daysTracked + 1)
            _forest = _forest.copy(streakDays = _user.streak)
        }

        val newProteinMet = _nutritionGoal.isProteinMet(_meals)
        val newCarbsMet = _nutritionGoal.isCarbsMet(_meals)
        val newFatsMet = _nutritionGoal.isFatsMet(_meals)

        var gpBonus = 0
        if (!oldProteinMet && newProteinMet) {
            gpBonus++
            _dailyGrowthPoints++
        }
        if (!oldCarbsMet && newCarbsMet) {
            gpBonus++
            _dailyGrowthPoints++
        }
        if (!oldFatsMet && newFatsMet) {
            gpBonus++
            _dailyGrowthPoints++
        }

        val oldLevel = _forest.forestLevel
        val xpFromMeal = meal.qualityBonus
        _forest = _forest.addXP(xpFromMeal)

        if (gpBonus > 0) {
            val updatedSlots = _forest.slots.map { t -> t?.addGrowthPoints(gpBonus) }
            _forest = _forest.copy(slots = updatedSlots)
        }

        val newLevel = _forest.forestLevel
        if (newLevel > oldLevel) {
            _pendingLevelUp = true
            _lastKnownLevel = newLevel
            unlockNextTrees()
        }

        val finalUser = _user
        val finalForest = _forest
        scope.launch {
            db.saveMeal(currentUserId, meal)
            db.saveUser(finalUser)
            db.updateForest(currentUserId, finalForest)
        }

        checkAndUpdateAchievements()
        return _forest
    }

    fun plantTree(row: Int, col: Int, type: TreeType): Forest {
        _forest = _forest.plantTree(row, col, type)
        val finalForest = _forest
        scope.launch { db.updateForest(currentUserId, finalForest) }
        checkAndUpdateAchievements()
        return _forest
    }

    suspend fun reset() {
        load(currentUserId)
    }


    private fun startOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun isToday(timestamp: Long): Boolean {
        return startOfDay(timestamp) == startOfDay(System.currentTimeMillis())
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        return startOfDay(timestamp) == startOfDay(yesterdayCal.timeInMillis)
    }
}

data class TreeTypeInfo(
        val id: Int,
        val name: String,
        val emoji: String,
        val unlocked: Boolean = false
)
