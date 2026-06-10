package com.cs346.intellical.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs346.intellical.domain.MealEstimate
import com.cs346.intellical.services.MockNutritionEstimationService
import com.cs346.intellical.services.NutritionEstimationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CameraState {
    IDLE,
    ANALYZING,
    RESULT,
    ERROR
}

class CameraViewModel(
    private val estimationService: NutritionEstimationService = com.cs346.intellical.services.AINutritionEstimationService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraState.IDLE)
    val uiState: StateFlow<CameraState> = _uiState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _estimate = MutableStateFlow<MealEstimate?>(null)
    val estimate: StateFlow<MealEstimate?> = _estimate.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onImageSelected(uri: Uri?) {
        if (uri != null) {
            _selectedImageUri.value = uri
            _uiState.value = CameraState.IDLE
            _errorMessage.value = null
        }
    }

    fun analyzeImage(context: Context) {
        val uri = _selectedImageUri.value ?: return
        
        _uiState.value = CameraState.ANALYZING
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) 
                    ?: throw Exception("Failed to open selected image.")
                val imageBytes = inputStream.readBytes()
                inputStream.close()
                
                val result = estimationService.estimateFromImage(imageBytes)
                
                com.cs346.intellical.domain.AppModel.pendingEstimate = result
                _estimate.value = result
                _uiState.value = CameraState.RESULT

            } catch (e: Exception) {
                android.util.Log.e("CameraViewModel", "Analysis failed: ${e.message}", e)
                val causeMsg = e.cause?.let { " Cause: ${it.javaClass.simpleName}: ${it.message}" } ?: ""
                _errorMessage.value = "${e.javaClass.simpleName}: ${e.message ?: "No message"}$causeMsg"
                _uiState.value = CameraState.ERROR
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraState.IDLE
        _selectedImageUri.value = null
        _estimate.value = null
        _errorMessage.value = null
    }
}
