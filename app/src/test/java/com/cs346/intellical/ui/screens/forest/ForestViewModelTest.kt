package com.cs346.intellical.ui.screens.forest

import com.cs346.intellical.data.MockDatabase
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.TreeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ForestViewModelTest {

    @Before
    fun setup() {
        AppModel.initialize(MockDatabase())
        runBlocking { AppModel.reset() }
    }

    @Test
    fun `test onSlotClick without tree type selects slot if tree exists`() {
        val viewModel = ForestViewModel()
        val tree = AppModel.getForest().getTree(1, 1)!!
        viewModel.onSlotClick(tree)

        assertEquals(tree, viewModel.uiState.value.selectedSlot)
        assertTrue(viewModel.uiState.value.showTreeDetail)
    }

    @Test
    fun `test onTreeTypeSelect selects type and clears slot selection`() {
        val viewModel = ForestViewModel()
        val tree = AppModel.getForest().getTree(1, 1)!!
        viewModel.onSlotClick(tree)

        viewModel.onTreeTypeSelect(TreeType.Pine)

        assertEquals(TreeType.Pine, viewModel.uiState.value.selectedTreeType)
        assertNull(viewModel.uiState.value.selectedSlot)
        assertFalse(viewModel.uiState.value.showTreeDetail)
    }

    @Test
    fun `test onDismissDetail clears selection`() {
        val viewModel = ForestViewModel()
        val tree = AppModel.getForest().getTree(1, 1)!!
        viewModel.onSlotClick(tree)

        viewModel.onDismissDetail()

        assertNull(viewModel.uiState.value.selectedSlot)
        assertFalse(viewModel.uiState.value.showTreeDetail)
    }
}
