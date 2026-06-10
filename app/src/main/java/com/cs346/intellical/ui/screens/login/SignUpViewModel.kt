package com.cs346.intellical.ui.screens.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs346.intellical.data.AuthRepository
import com.cs346.intellical.data.SupabaseRepository
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.User
import com.cs346.intellical.domain.UserGoal
import kotlinx.coroutines.launch
import java.util.Date

class SignUpViewModel : ViewModel() {
    private val _uiState = mutableStateOf(LoginUiState())
    val uiState: State<LoginUiState> = _uiState

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password, errorMessage = null)
    }

    fun signUp(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Passwords do not match")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                val userId = AuthRepository.signUp(state.email, state.password)
                
                val db = SupabaseRepository()
                val newUser = User(
                    id = userId,
                    username = state.email.split("@")[0],
                    email = state.email,
                    memberSince = Date().toString(),
                    targetDate = "June 30, 2026",
                    currentWeight = 170.0f,
                    targetWeight = 165.0f,
                    startWeight = 170.0f,
                    primaryGoal = UserGoal.CLEAN_EATING,
                    streak = 0,
                    daysTracked = 0,
                    mealsLogged = 0
                )
                db.saveUser(newUser)
                
                AppModel.initialize(db)
                AppModel.load(userId)
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Sign up failed")
            }
        }
    }
}
