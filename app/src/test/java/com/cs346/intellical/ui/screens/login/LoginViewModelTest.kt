package com.cs346.intellical.ui.screens.login

import com.cs346.intellical.data.AuthRepository
import com.cs346.intellical.data.SupabaseRepository
import com.cs346.intellical.domain.AppModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(AuthRepository)
        mockkObject(AppModel)
        mockkConstructor(SupabaseRepository::class)
        viewModel = LoginViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `updateEmail updates uiState`() {
        viewModel.updateEmail("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `updatePassword updates uiState`() {
        viewModel.updatePassword("password123")
        assertEquals("password123", viewModel.uiState.value.password)
    }

    @Test
    fun `login success navigates and updates loading state`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val userId = 123
        
        coEvery { AuthRepository.signIn(email, password) } returns userId
        coEvery { AppModel.initialize(any()) } just Runs
        coEvery { AppModel.load(userId) } just Runs
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        var successCalled = false
        viewModel.login { successCalled = true }
        
        advanceUntilIdle()
        
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(true, successCalled)
        
        coVerify { AuthRepository.signIn(email, password) }
        coVerify { AppModel.load(userId) }
    }

    @Test
    fun `login failure updates error message and resets loading`() = runTest {
        val email = "test@example.com"
        val password = "wrong"
        val errorMsg = "Invalid credentials"
        
        coEvery { AuthRepository.signIn(email, password) } throws Exception(errorMsg)
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        var successCalled = false
        viewModel.login { successCalled = true }
        
        advanceUntilIdle()
        
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
        assertEquals(false, successCalled)
    }
}
