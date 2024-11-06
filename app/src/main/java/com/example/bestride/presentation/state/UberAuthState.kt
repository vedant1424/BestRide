// com.example.bestride.presentation.state/UberAuthState.kt
package com.example.bestride.presentation.state

sealed class UberAuthState {
    object Initial : UberAuthState()
    object PhoneNumberInput : UberAuthState()
    object PhoneOTPInput : UberAuthState()
    object EmailInput : UberAuthState()
    object EmailOTPInput : UberAuthState()
    object Authenticated : UberAuthState()
    data class Error(val message: String) : UberAuthState()
}