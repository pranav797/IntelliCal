package com.cs346.intellical.services

import com.cs346.intellical.domain.MealEstimate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AINutritionEstimationServiceTest {

    @Test
    fun `MockNutritionEstimationService returns a fully populated MealEstimate`() {
        val service = MockNutritionEstimationService()
        val result: MealEstimate = runBlocking { service.estimateFromImage(ByteArray(1)) }

        assertNotNull("name should not be null", result.name)
        assertNotNull("description should not be null", result.description)
        assertNotNull("ingredients should not be null", result.ingredients)
        assertTrue("calories must be > 0", result.calories > 0)
        assertTrue("proteinGrams must be >= 0", result.proteinGrams >= 0)
        assertTrue("carbsGrams must be >= 0", result.carbsGrams >= 0)
        assertTrue("fatGrams must be >= 0", result.fatGrams >= 0)
        assertTrue("healthScore must be 1-10", result.healthScore in 1..10)
        assertTrue("confidence must be 0.0-1.0", result.confidence in 0f..1f)
    }

    @Test
    fun `MockNutritionEstimationService returns non-empty name`() {
        val service = MockNutritionEstimationService()
        val result = runBlocking { service.estimateFromImage(ByteArray(1)) }
        assertTrue("name should not be blank", result.name.isNotBlank())
    }

    @Test
    fun `MockNutritionEstimationService returns at least one ingredient`() {
        val service = MockNutritionEstimationService()
        val result = runBlocking { service.estimateFromImage(ByteArray(1)) }
        assertTrue("ingredients should not be empty", result.ingredients.isNotEmpty())
    }

    @Test
    fun `AINutritionEstimationService throws descriptive error on non-200 response`() {
        val minimalJpeg = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00, 0x10,
            0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01,
            0x00, 0x00, 0xFF.toByte(), 0xDB.toByte(), 0x00, 0x43, 0x00, 0x08, 0x06,
            0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A,
            0x0C, 0x14, 0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14,
            0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27,
            0x20, 0x22, 0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31,
            0x34, 0x34, 0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33,
            0x34, 0x32, 0xFF.toByte(), 0xC0.toByte(), 0x00, 0x0B, 0x08, 0x00, 0x01,
            0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xFF.toByte(), 0xC4.toByte(), 0x00,
            0x1F, 0x00, 0x00, 0x01, 0x05, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
            0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0xFF.toByte(), 0xDA.toByte(), 0x00,
            0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 0xFB.toByte(), 0xD2.toByte(),
            0x8A.toByte(), 0x00, 0xFF.toByte(), 0xD9.toByte()
        )

        val service = AINutritionEstimationService()
        val exception = try {
            runBlocking { service.estimateFromImage(minimalJpeg) }
            null
        } catch (e: Exception) {
            e
        }

        assertNotNull("Expected an exception but none was thrown", exception)
        val msg = exception!!.message ?: ""
        assertTrue(
            "Exception message should describe the error, got: $msg",
            msg.isNotBlank()
        )
    }
}
