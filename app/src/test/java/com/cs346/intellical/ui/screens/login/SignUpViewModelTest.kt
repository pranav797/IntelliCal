package com.cs346.intellical.ui.screens.login

import com.cs346.intellical.data.AuthRepository
import com.cs346.intellical.data.SupabaseRepository
import com.cs346.intellical.domain.AppModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(AuthRepository)
        mockkObject(AppModel)
        mockkConstructor(SupabaseRepository::class)
        viewModel = SignUpViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `passwords mismatch sets error message`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("different")
        
        viewModel.signUp { }
        
        assertEquals("Passwords do not match", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `short password sets error message`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("123")
        viewModel.updateConfirmPassword("123")
        
        viewModel.signUp { }
        
        assertEquals("Password must be at least 6 characters", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `signUp success seeds user and loads model`() = runTest {
        val email = "new@example.com"
        val password = "password123"
        val userId = 456
        
        coEvery { AuthRepository.signUp(email, password) } returns userId
        coEvery { anyConstructed<SupabaseRepository>().saveUser(any()) } just Runs
        coEvery { AppModel.initialize(any()) } just Runs
        coEvery { AppModel.load(userId) } just Runs
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.updateConfirmPassword(password)
        
        var successCalled = false
        viewModel.signUp { successCalled = true }
        
        advanceUntilIdle()
        
        assertTrue(successCalled)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        
        coVerify { AuthRepository.signUp(email, password) }
        coVerify { AppModel.load(userId) }
    }

    @Test
    fun `signUp failure sets error message`() = runTest {
        val email = "exist@example.com"
        val password = "password123"
        val errorMsg = "Account already exists"
        
        coEvery { AuthRepository.signUp(email, password) } throws Exception(errorMsg)
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.updateConfirmPassword(password)
        
        viewModel.signUp { }
        
        advanceUntilIdle()
        
        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
