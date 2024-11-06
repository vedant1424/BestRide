package com.example.bestride.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bestride.domain.model.UserCredentials
import com.example.bestride.presentation.state.UberAuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UberAuthViewModel @Inject constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>
) : ViewModel() {

    private val _authState = MutableStateFlow<UberAuthState>(UberAuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _credentials = MutableStateFlow<UserCredentials?>(null)
    val credentials = _credentials.asStateFlow()

    companion object {
        private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    init {
        viewModelScope.launch {
            loadSavedCredentials()
        }
    }

    fun updatePhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            _credentials.update {
                it?.copy(phoneNumber = phoneNumber) ?: UserCredentials(phoneNumber = phoneNumber)
            }
            saveCredentials()
            _authState.value = UberAuthState.PhoneOTPInput
        }
    }

    fun updatePhoneOTP(otp: String) {
        viewModelScope.launch {
            _credentials.update { it?.copy(phoneOTP = otp) }
            _authState.value = UberAuthState.EmailInput
        }
    }

    fun updateEmail(email: String) {
        viewModelScope.launch {
            _credentials.update { it?.copy(email = email) }
            saveCredentials()
            _authState.value = UberAuthState.EmailOTPInput
        }
    }

    fun updateEmailOTP(otp: String) {
        viewModelScope.launch {
            _credentials.update { it?.copy(emailOTP = otp) }
            _authState.value = UberAuthState.Authenticated
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
    }

    fun updateAuthState(newState: UberAuthState) {
        _authState.value = newState
    }
}