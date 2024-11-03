package com.example.bestride.presentation.state


sealed class AuthUiState {
    data object Initial : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// Optional validation state class if you want to handle input validation separately
sealed class ValidationState {
    data object Valid : ValidationState()
    data class Invalid(val message: String) : ValidationState()
    data object None : ValidationState()
}

// Helper data class for form fields validation
data class AuthFormState(
    val email: ValidationState = ValidationState.None,
    val password: ValidationState = ValidationState.None,
    val confirmPassword: ValidationState = ValidationState.None
) {
    fun isValid(): Boolean {
        return email is ValidationState.Valid &&
                password is ValidationState.Valid &&
                confirmPassword is ValidationState.Valid
    }
}

// Extension function for email validation
fun String.isValidEmail(): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return this.matches(emailPattern.toRegex())
}

// Extension function for password validation
fun String.isValidPassword(): Boolean {
    return this.length >= 6 // Basic validation, can be enhanced
}
