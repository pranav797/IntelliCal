package com.cs346.intellical.data

import com.cs346.intellical.domain.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("MockDatabase")
class MockDatabaseTest {

    private lateinit var db: MockDatabase

    @BeforeEach
    fun setUp() {
        db = MockDatabase()
    }


    @Nested
    @DisplayName("getUser")
    inner class GetUser {

        @Test
        fun `returns seeded user for userId 1`() = runTest {
            val user = db.getUser(1)
            assertNotNull(user)
            assertEquals("Demo User", user!!.username)
            assertEquals("demo@example.com", user.email)
        }

        @Test
        fun `returns correct weight fields`() = runTest {
            val user = db.getUser(1)!!
            assertEquals(170f, user.currentWeight, 0.01f)
            assertEquals(165f, user.targetWeight,  0.01f)
            assertEquals(175f, user.startWeight,   0.01f)
        }

        @Test
        fun `returns correct tracking fields`() = runTest {
            val user = db.getUser(1)!!
            assertEquals(5,   user.streak)
            assertEquals(20,  user.daysTracked)
            assertEquals(99,  user.mealsLogged)
        }

        @Test
        fun `returns null for unknown userId`() = runTest {
            assertNull(db.getUser(999))
        }
    }

    @Nested
    @DisplayName("getMeals")
    inner class GetMeals {

        @Test
        fun `returns 8 seeded meals for userId 1`() = runTest {
            assertEquals(8, db.getMeals(1).size)
        }

        @Test
        fun `meals are sorted newest first`() = runTest {
            val meals = db.getMeals(1)
            for (i in 0 until meals.size - 1) {
                assertTrue(meals[i].timestamp >= meals[i + 1].timestamp)
            }
        }

        @Test
        fun `returns empty list for unknown userId`() = runTest {
            assertTrue(db.getMeals(999).isEmpty())
        }
    }

    @Nested
    @DisplayName("getForest")
    inner class GetForest {

        @Test
        fun `returns seeded forest for userId 1`() = runTest {
            val forest = db.getForest(1)
            assertNotNull(forest)
            assertEquals(2095, forest!!.totalXP)
            assertEquals(5,    forest.streakDays)
            assertEquals(5,    forest.gridSize)
        }

        @Test
        fun `forest has exactly 7 planted trees`() = runTest {
            assertEquals(7, db.getForest(1)!!.treeCount)
        }

        @Test
        fun `forest has 25 slots`() = runTest {
            assertEquals(25, db.getForest(1)!!.slots.size)
        }

        @Test
        fun `maple is at row 1 col 1`() = runTest {
            val tree = db.getForest(1)!!.getTree(1, 1)
            assertNotNull(tree)
            assertEquals("Maple", tree!!.type!!.name)
        }

        @Test
        fun `returns null for unknown userId`() = runTest {
            assertNull(db.getForest(999))
        }
    }

    @Nested
    @DisplayName("getNutritionGoal")
    inner class GetNutritionGoal {

        @Test
        fun `returns seeded goal for userId 1`() = runTest {
            val goal = db.getNutritionGoal(1)
            assertNotNull(goal)
            assertEquals(2500, goal!!.dailyCalories)
            assertEquals(125,  goal.proteinGrams)
            assertEquals(281,  goal.carbsGrams)
            assertEquals(97,   goal.fatGrams)
            assertEquals(MacroDistribution.BALANCED, goal.distribution)
        }

        @Test
        fun `returns null for unknown userId`() = runTest {
            assertNull(db.getNutritionGoal(999))
        }
    }

    @Nested
    @DisplayName("getAchievements")
    inner class GetAchievements {

        @Test
        fun `returns 8 seeded achievements for userId 1`() = runTest {
            assertEquals(8, db.getAchievements(1).size)
        }

        @Test
        fun `3 achievements are unlocked`() = runTest {
            assertEquals(3, db.getAchievements(1).count { it.isUnlocked })
        }

        @Test
        fun `5 achievements are locked`() = runTest {
            assertEquals(5, db.getAchievements(1).count { !it.isUnlocked })
        }

        @Test
        fun `all achievement ids are unique`() = runTest {
            val list = db.getAchievements(1)
            assertEquals(list.size, list.map { it.id }.toSet().size)
        }

        @Test
        fun `returns empty list for unknown userId`() = runTest {
            assertTrue(db.getAchievements(999).isEmpty())
        }
    }


    @Nested
    @DisplayName("saveUser")
    inner class SaveUser {

        @Test
        fun `saved changes are reflected on next read`() = runTest {
            val updated = db.getUser(1)!!.copy(username = "Updated User", streak = 10)
            db.saveUser(updated)
            val read = db.getUser(1)!!
            assertEquals("Updated User", read.username)
            assertEquals(10, read.streak)
        }

        @Test
        fun `can insert a new user`() = runTest {
            val newUser = User(2, "Alice", "alice@test.com", "Jan 2026", "July 1, 2026", 150f, 145f, 155f, UserGoal.MUSCLE_GAIN, 1, 1, 1)
            db.saveUser(newUser)
            assertNotNull(db.getUser(2))
            assertEquals("Alice", db.getUser(2)!!.username)
        }
    }

    @Nested
    @DisplayName("saveMeal")
    inner class SaveMeal {

        @Test
        fun `new meal appears on next read`() = runTest {
            val meal = Meal("99", "Test Salad", 300, 20, 30, 10, 8, System.currentTimeMillis(), listOf("Lettuce"))
            db.saveMeal(1, meal)
            assertEquals(9, db.getMeals(1).size)
            assertTrue(db.getMeals(1).any { it.id == "99" })
        }

        @Test
        fun `updating existing meal reflects on read`() = runTest {
            val original = db.getMeals(1).first { it.id == "1" }
            val updated  = original.copy(calories = 999)
            db.saveMeal(1, updated)
            val read = db.getMeals(1).first { it.id == "1" }
            assertEquals(999, read.calories)
            assertEquals(8, db.getMeals(1).size)   // count unchanged
        }
    }

    @Nested
    @DisplayName("updateForest")
    inner class UpdateForest {

        @Test
        fun `updated forest is returned on next read`() = runTest {
            val original = db.getForest(1)!!
            val updated  = original.addXP(500)
            db.updateForest(1, updated)
            assertEquals(2095 + 500, db.getForest(1)!!.totalXP)
        }
    }

    @Nested
    @DisplayName("updateNutritionGoal")
    inner class UpdateNutritionGoal {

        @Test
        fun `updated goal is returned on next read`() = runTest {
            val newGoal = NutritionGoal(2000, 150, 200, 80, MacroDistribution.LOW_CARB)
            db.updateNutritionGoal(1, newGoal)
            val read = db.getNutritionGoal(1)!!
            assertEquals(2000, read.dailyCalories)
            assertEquals(MacroDistribution.LOW_CARB, read.distribution)
        }
    }

    @Nested
    @DisplayName("updateAchievements")
    inner class UpdateAchievements {

        @Test
        fun `updated list is returned on next read`() = runTest {
            val updated = db.getAchievements(1).map { it.copy(isUnlocked = true) }
            db.updateAchievements(1, updated)
            assertEquals(8, db.getAchievements(1).count { it.isUnlocked })
        }
    }


    @Nested
    @DisplayName("deleteMeal")
    inner class DeleteMeal {

        @Test
        fun `deleted meal no longer appears on read`() = runTest {
            db.deleteMeal("1")
            assertFalse(db.getMeals(1).any { it.id == "1" })
        }

        @Test
        fun `meal count decreases by 1 after delete`() = runTest {
            db.deleteMeal("2")
            assertEquals(7, db.getMeals(1).size)
        }

        @Test
        fun `deleting non-existent meal does not crash`() = runTest {
            db.deleteMeal("does-not-exist") // no exception thrown = test passes
        }
    }

    @Nested
    @DisplayName("deleteUser")
    inner class DeleteUser {

        @Test
        fun `deleted user is no longer found`() = runTest {
            db.deleteUser(1)
            assertNull(db.getUser(1))
        }

        @Test
        fun `meals forest goal achievements all removed with user`() = runTest {
            db.deleteUser(1)
            assertTrue(db.getMeals(1).isEmpty())
            assertNull(db.getForest(1))
            assertNull(db.getNutritionGoal(1))
            assertTrue(db.getAchievements(1).isEmpty())
        }

        @Test
        fun `deleting non-existent user does not crash`() = runTest {
            db.deleteUser(999) // no exception thrown = test passes
        }
    }
}
