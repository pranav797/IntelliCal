package com.cs346.intellical.ui.screens.camera

import com.cs346.intellical.domain.MealEstimate
import com.cs346.intellical.services.MockNutritionEstimationService
import com.cs346.intellical.services.NutritionEstimationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class FailingNutritionService : NutritionEstimationService {
    override suspend fun estimateFromImage(imageBytes: ByteArray): MealEstimate {
        throw Exception("Simulated AI failure for testing")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        com.cs346.intellical.domain.AppModel.initialize(com.cs346.intellical.data.MockDatabase())
        kotlinx.coroutines.runBlocking { com.cs346.intellical.domain.AppModel.reset() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is IDLE`() {
        val vm = CameraViewModel(MockNutritionEstimationService())
        assertEquals(CameraState.IDLE, vm.uiState.value)
    }

    @Test
    fun `resetState returns to IDLE and clears all fields`() {
        val vm = CameraViewModel(MockNutritionEstimationService())
        vm.resetState()
        assertEquals(CameraState.IDLE, vm.uiState.value)
        assertNull(vm.selectedImageUri.value)
        assertNull(vm.estimate.value)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `secondary constructor accepts injected service`() {
        val vm = CameraViewModel(MockNutritionEstimationService())
        assertEquals(CameraState.IDLE, vm.uiState.value)
    }

    @Test
    fun `MockNutritionEstimationService returns non-null estimate with all fields`() = runTest {
        val estimate = MockNutritionEstimationService().estimateFromImage(ByteArray(0))
        assertNotNull(estimate)
        assertNotNull(estimate.name)
        assert(estimate.calories > 0) { "calories should be > 0" }
        assert(estimate.proteinGrams >= 0) { "proteinGrams should be >= 0" }
        assert(estimate.carbsGrams >= 0) { "carbsGrams should be >= 0" }
        assert(estimate.fatGrams >= 0) { "fatGrams should be >= 0" }
    }

    @Test
    fun `FailingNutritionService throws with descriptive message`() = runTest {
        val service = FailingNutritionService()
        val thrown = runCatching { service.estimateFromImage(ByteArray(0)) }.exceptionOrNull()
        assertNotNull("Expected exception from failing service", thrown)
        assertEquals("Simulated AI failure for testing", thrown!!.message)
    }
}
