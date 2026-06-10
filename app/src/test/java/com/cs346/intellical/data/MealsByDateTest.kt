package com.cs346.intellical.data

import com.cs346.intellical.domain.Meal
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Calendar

@DisplayName("getMealsByDate — Task 115.4")
class MealsByDateTest {

    private lateinit var db: MockDatabase

    private val DAY_MS = 24 * 60 * 60 * 1000L

    private fun startOfDay(dayOffset: Int = 0): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun endOfDay(dayOffset: Int = 0) = startOfDay(dayOffset) + DAY_MS - 1

    @BeforeEach
    fun setUp() {
        db = MockDatabase()   // fresh seed: 5 today + 3 yesterday
    }

    
    @Nested
    @DisplayName("Today (offset 0)")
    inner class Today {

        @Test
        fun `returns exactly 5 meals for today`() = runTest {
            val result = db.getMealsByDate(1, startOfDay(0), endOfDay(0))
            assertEquals(5, result.size)
        }

        @Test
        fun `all returned meals have timestamps within today`() = runTest {
            val start = startOfDay(0)
            val end   = endOfDay(0)
            db.getMealsByDate(1, start, end).forEach { meal ->
                assertTrue(
                    meal.timestamp in start..end,
                    "'${meal.name}' timestamp ${meal.timestamp} outside [$start, $end]"
                )
            }
        }

        @Test
        fun `does not include yesterday meals`() = runTest {
            val ids = db.getMealsByDate(1, startOfDay(0), endOfDay(0)).map { it.id }
            assertFalse(ids.contains("y1"))
            assertFalse(ids.contains("y2"))
            assertFalse(ids.contains("y3"))
        }

        @Test
        fun `results are sorted newest first`() = runTest {
            val result = db.getMealsByDate(1, startOfDay(0), endOfDay(0))
            for (i in 0 until result.size - 1)
                assertTrue(result[i].timestamp >= result[i + 1].timestamp)
        }
    }

    
    @Nested
    @DisplayName("Yesterday (offset -1)")
    inner class Yesterday {

        @Test
        fun `returns exactly 3 meals for yesterday`() = runTest {
            val result = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1))
            assertEquals(3, result.size)
        }

        @Test
        fun `returned meal ids are y1 y2 y3`() = runTest {
            val ids = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1)).map { it.id }.toSet()
            assertEquals(setOf("y1", "y2", "y3"), ids)
        }

        @Test
        fun `does not include today meals`() = runTest {
            val ids = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1)).map { it.id }
            for (todayId in listOf("1", "2", "3", "4", "5"))
                assertFalse(ids.contains(todayId), "Today meal '$todayId' leaked into yesterday results")
        }

        @Test
        fun `results are sorted newest first`() = runTest {
            val result = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1))
            for (i in 0 until result.size - 1)
                assertTrue(result[i].timestamp >= result[i + 1].timestamp)
        }

        @Test
        fun `calorie total matches expected`() = runTest {
            val total = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1)).sumOf { it.calories }
            assertEquals(380 + 520 + 610, total)   // Avocado Toast + Caesar Salad + Stir Fry
        }
    }

    @Nested
    @DisplayName("Empty day (two days ago)")
    inner class EmptyDay {

        @Test
        fun `returns empty list for a day with no meals`() = runTest {
            val result = db.getMealsByDate(1, startOfDay(-2), endOfDay(-2))
            assertTrue(result.isEmpty())
        }

        @Test
        fun `returns empty list for unknown userId`() = runTest {
            val result = db.getMealsByDate(999, startOfDay(-1), endOfDay(-1))
            assertTrue(result.isEmpty())
        }
    }


    @Nested
    @DisplayName("Day boundary values")
    inner class BoundaryValues {

        private fun addMealAt(id: String, timestamp: Long) {
            // Use saveMeal synchronously via runTest; we just need to call it
            // This works because MockDatabase.saveMeal upserts into the internal map
            kotlinx.coroutines.runBlocking {
                db.saveMeal(1, Meal(id, "Boundary Meal $id", 100, 10, 10, 5, 5, timestamp, emptyList()))
            }
        }

        @Test
        fun `meal at exact start-of-yesterday is included`() = runTest {
            addMealAt("b-start", startOfDay(-1))
            val ids = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1)).map { it.id }
            assertTrue(ids.contains("b-start"))
        }

        @Test
        fun `meal at exact end-of-yesterday is included`() = runTest {
            addMealAt("b-end", endOfDay(-1))
            val ids = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1)).map { it.id }
            assertTrue(ids.contains("b-end"))
        }

        @Test
        fun `meal 1ms before start-of-yesterday is excluded`() = runTest {
            addMealAt("b-before", startOfDay(-1) - 1)
            val ids = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1)).map { it.id }
            assertFalse(ids.contains("b-before"))
        }

        @Test
        fun `meal 1ms after end-of-yesterday is excluded`() = runTest {
            addMealAt("b-after", endOfDay(-1) + 1)
            val ids = db.getMealsByDate(1, startOfDay(-1), endOfDay(-1)).map { it.id }
            assertFalse(ids.contains("b-after"))
        }
    }
}
