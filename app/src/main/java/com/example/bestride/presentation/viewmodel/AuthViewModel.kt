package com.example.bestride.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bestride.domain.usecase.AuthUseCase
import com.example.bestride.presentation.state.AuthUiState
import com.example.bestride.presentation.state.ValidationState
import com.example.bestride.presentation.state.AuthFormState
import com.example.bestride.presentation.state.isValidEmail
import com.example.bestride.presentation.state.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                Log.d("AuthViewModel", "Starting login process")

                if (!validateLoginInput(email, password)) {
                    Log.e("AuthViewModel", "Login validation failed")
                    _uiState.value = AuthUiState.Error("Please check your input")
                    return@launch
                }

                authUseCase.login(email, password)
                    .collect { result ->
                        _uiState.value = when {
                            result.isSuccess -> {
                                Log.d("AuthViewModel", "Login successful")
                                AuthUiState.Success
                            }
                            else -> {
                                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                                Log.e("AuthViewModel", "Login failed: $error")
                                AuthUiState.Error(error)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                Log.d("AuthViewModel", "Starting registration process")

                if (!validateRegistrationInput(email, password)) {
                    Log.e("AuthViewModel", "Registration validation failed")
                    _uiState.value = AuthUiState.Error("Please check your input")
                    return@launch
                }

                authUseCase.register(email, password)
                    .collect { result ->
                        _uiState.value = when {
                            result.isSuccess -> {
                                Log.d("AuthViewModel", "Registration successful")
                                AuthUiState.Success
                            }
                            else -> {
                                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                                Log.e("AuthViewModel", "Registration failed: $error")
                                AuthUiState.Error(error)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error", e)
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun validateEmail(email: String) {
        _formState.update { currentState ->
            currentState.copy(
                email = if (email.isValidEmail()) {
                    ValidationState.Valid
                } else {
                    ValidationState.Invalid("Invalid email format")
                }
            )
        }
    }

    fun validatePassword(password: String) {
        _formState.update { currentState ->
            currentState.copy(
                password = if (password.isValidPassword()) {
                    ValidationState.Valid
                } else {
                    ValidationState.Invalid("Password must be at least 6 characters")
                }
            )
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String) {
        _formState.update { currentState ->
            currentState.copy(
                confirmPassword = if (password == confirmPassword) {
                    ValidationState.Valid
                } else {
                    ValidationState.Invalid("Passwords don't match")
                }
            )
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        validateEmail(email)
        validatePassword(password)
        return formState.value.email is ValidationState.Valid &&
                formState.value.password is ValidationState.Valid
    }

    private fun validateRegistrationInput(email: String, password: String): Boolean {
        validateEmail(email)
        validatePassword(password)
        return formState.value.isValid()
    }

    fun resetState() {
        _uiState.value = AuthUiState.Initial
        _formState.value = AuthFormState()
    }
}