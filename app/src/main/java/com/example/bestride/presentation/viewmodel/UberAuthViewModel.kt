package com.example.bestride.presentation.viewmodel

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bestride.domain.model.UserCredentials
import com.example.bestride.presentation.state.UberAuthState
import com.example.bestride.util.WebViewStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UberAuthViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _authState = MutableStateFlow<UberAuthState>(UberAuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _credentials = MutableStateFlow<UserCredentials?>(null)
    val credentials = _credentials.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var webViewStateManager: WebViewStateManager? = null
    private var stateDetectionJob: Job? = null

    companion object {
        private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    init {
        viewModelScope.launch {
            loadSavedCredentials()
        }
    }

    fun initializeWebView(webView: WebView) {
        webViewStateManager = WebViewStateManager(webView, _authState)
        startStateDetection()
    }

    private fun startStateDetection() {
        stateDetectionJob?.cancel()
        stateDetectionJob = viewModelScope.launch {
            while (true) {
                webViewStateManager?.detectAndUpdateState()
                delay(1000) // Check every second
            }
        }
    }

    fun clearWebView() {
        stateDetectionJob?.cancel()
        webViewStateManager = null
    }

    fun updatePhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (!isValidPhoneNumber(phoneNumber)) {
                    _error.value = "Please enter a valid phone number"
                    return@launch
                }

                _credentials.update {
                    it?.copy(phoneNumber = phoneNumber) ?: UserCredentials(phoneNumber = phoneNumber)
                }
                saveCredentials()
                _authState.value = UberAuthState.PhoneOTPInput
                webViewStateManager?.injectCredentials(UberAuthState.PhoneNumberInput, phoneNumber)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePhoneOTP(otp: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (!isValidOTP(otp)) {
                    _error.value = "Please enter a valid OTP"
                    return@launch
                }

                _credentials.update { it?.copy(phoneOTP = otp) }
                webViewStateManager?.injectCredentials(UberAuthState.PhoneOTPInput, otp)
                _authState.value = UberAuthState.EmailInput
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEmail(email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (!isValidEmail(email)) {
                    _error.value = "Please enter a valid email"
                    return@launch
                }

                _credentials.update { it?.copy(email = email) }
                saveCredentials()
                webViewStateManager?.injectCredentials(UberAuthState.EmailInput, email)
                _authState.value = UberAuthState.EmailOTPInput
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEmailOTP(otp: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (!isValidOTP(otp)) {
                    _error.value = "Please enter a valid OTP"
                    return@launch
                }

                _credentials.update { it?.copy(emailOTP = otp) }
                webViewStateManager?.injectCredentials(UberAuthState.EmailOTPInput, otp)
                _authState.value = UberAuthState.Authenticated
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveCredentials() {
        _credentials.value?.let { creds ->
            dataStore.edit { preferences ->
                preferences[PHONE_NUMBER_KEY] = creds.phoneNumber
                creds.email?.let { email ->
                    preferences[EMAIL_KEY] = email
                }
            }
        }
    }

    private suspend fun loadSavedCredentials() {
        try {
            _isLoading.value = true
            dataStore.data.first().let { preferences ->
                val savedPhone = preferences[PHONE_NUMBER_KEY]
                val savedEmail = preferences[EMAIL_KEY]
                if (savedPhone != null) {
                    _credentials.value = UserCredentials(
                        phoneNumber = savedPhone,
                        email = savedEmail
                    )
                }
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load saved credentials"
        } finally {
            _isLoading.value = false
        }
    }

    fun updateAuthState(newState: UberAuthState) {
        _authState.value = newState
    }

    fun clearError() {
        _error.value = null
    }

    private fun isValidPhoneNumber(phone: String): Boolean =
        phone.length == 10 && phone.all { it.isDigit() }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidOTP(otp: String): Boolean =
        otp.length == 6 && otp.all { it.isDigit() }

    override fun onCleared() {
        super.onCleared()
        clearWebView()
    }
}