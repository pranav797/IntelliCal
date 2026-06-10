package com.cs346.intellical.ui.screens.forest

import androidx.lifecycle.ViewModel
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.Forest
import com.cs346.intellical.domain.Meal
import com.cs346.intellical.domain.Tree
import com.cs346.intellical.domain.TreeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class ForestUiState(
    val forest: Forest = AppModel.getForest(),
    val selectedTreeType: TreeType? = null,
    val selectedSlot: Tree? = null,
    val showTreeDetail: Boolean = false,
    val showShareSheet: Boolean = false,
    val showLevelUpDialog: Boolean = false,
    val newForestLevel: Int = 0
)

class ForestViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ForestUiState())
    val uiState: StateFlow<ForestUiState> = _uiState.asStateFlow()

    fun refreshForest() {
        val forest = AppModel.getForest()
        val didLevelUp = AppModel.consumeLevelUp()
        _uiState.update {
            it.copy(
                forest = forest,
                showLevelUpDialog = it.showLevelUpDialog || didLevelUp,
                newForestLevel = if (didLevelUp) forest.forestLevel else it.newForestLevel
            )
        }
    }

    fun onSlotClick(slot: Tree) {
        val state = _uiState.value
        if (state.selectedTreeType != null && slot.isEmpty) {
            val updatedForest = AppModel.plantTree(slot.row, slot.col, state.selectedTreeType)
            _uiState.update {
                it.copy(
                    forest = updatedForest,
                    selectedTreeType = null,
                    selectedSlot = null
                )
            }
        } else if (!slot.isEmpty) {
            val newSelected = if (state.selectedSlot == slot) null else slot
            _uiState.update {
                it.copy(
                    selectedSlot = newSelected,
                    showTreeDetail = newSelected != null
                )
            }
        }
    }

    fun onTreeTypeSelect(treeType: TreeType) {
        _uiState.update {
            it.copy(
                selectedTreeType = if (it.selectedTreeType == treeType) null else treeType,
                selectedSlot = null,
                showTreeDetail = false,
                showLevelUpDialog = false   // close dialog and enter planting mode
            )
        }
    }

    fun onDismissLevelUpDialog() {
        _uiState.update { it.copy(showLevelUpDialog = false) }
    }

    fun onLogMeal() {
        val selected = _uiState.value.selectedSlot ?: return

        val dummyMeal = Meal(
            id = UUID.randomUUID().toString(),
            name = "Quick Log",
            calories = 300,
            proteinGrams = 15,
            carbsGrams = 30,
            fatGrams = 10,
            healthScore = 5,
            timestamp = System.currentTimeMillis(),
            ingredients = emptyList()
        )

        val updatedForest = AppModel.logMeal(dummyMeal)
        val updatedSlot = updatedForest.getTree(selected.row, selected.col)
        val didLevelUp = AppModel.consumeLevelUp()

        _uiState.update {
            it.copy(
                forest = updatedForest,
                selectedSlot = updatedSlot,
                showTreeDetail = false,
                showLevelUpDialog = it.showLevelUpDialog || didLevelUp,
                newForestLevel = if (didLevelUp) updatedForest.forestLevel else it.newForestLevel
            )
        }
    }

    fun onDismissDetail() {
        _uiState.update { it.copy(showTreeDetail = false, selectedSlot = null) }
    }

    fun onShareClick() {
        _uiState.update { it.copy(showShareSheet = true) }
    }

    fun onDismissShare() {
        _uiState.update { it.copy(showShareSheet = false) }
    }
}
