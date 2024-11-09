// WebViewStateManager.kt
package com.example.bestride.util

import android.webkit.WebView
import com.example.bestride.presentation.state.UberAuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class WebViewStateManager(
    private val webView: WebView,
    private val currentState: MutableStateFlow<UberAuthState>
) {
    suspend fun detectAndUpdateState() {
        webView.evaluateJavascript(WebViewScripts.detectAuthState()) { result ->
            when (result.trim('"')) {
                "PHONE" -> {
                    if (currentState.value is UberAuthState.Initial) {
                        currentState.value = UberAuthState.PhoneNumberInput
                    }
                }
                "OTP" -> {
                    when (currentState.value) {
                        is UberAuthState.PhoneNumberInput -> {
                            currentState.value = UberAuthState.PhoneOTPInput
                        }
                        is UberAuthState.EmailInput -> {
                            currentState.value = UberAuthState.EmailOTPInput
                        }
                        else -> {}
                    }
                }
                "EMAIL" -> {
                    if (currentState.value is UberAuthState.PhoneOTPInput) {
                        currentState.value = UberAuthState.EmailInput
                    }
                }
            }
        }
        delay(500) // Add a small delay before next check
    }

    suspend fun injectCredentials(state: UberAuthState, credentials: String) {
        val script = when (state) {
            is UberAuthState.PhoneNumberInput -> WebViewScripts.buildPhoneNumberScript(credentials)
            is UberAuthState.EmailInput -> WebViewScripts.buildEmailScript(credentials)
            is UberAuthState.PhoneOTPInput,
            is UberAuthState.EmailOTPInput -> WebViewScripts.buildOTPScript(credentials)
            else -> return
        }

        webView.evaluateJavascript(script) { result ->
            // Handle injection result if needed
        }
    }
}