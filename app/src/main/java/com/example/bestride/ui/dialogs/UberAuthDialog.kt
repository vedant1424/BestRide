// UberAuthDialog.kt
package com.example.bestride.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bestride.presentation.state.UberAuthState
import com.example.bestride.presentation.viewmodel.UberAuthViewModel

@Composable
fun UberAuthDialog(
    viewModel: UberAuthViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onPhoneSubmit: (String) -> Unit = {}, // Add default value
    onOTPSubmit: (String) -> Unit = {}    // Add default value
) {
    val authState by viewModel.authState.collectAsState()
    val credentials by viewModel.credentials.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = getDialogTitle(authState))
                Text(
                    text = "Step ${getStepNumber(authState)} of 4",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Error message
                error?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Loading indicator
                if (isLoading) {
                    Text("Loading...")
                }



                when (authState) {
                    is UberAuthState.PhoneNumberInput -> PhoneNumberInput(
                        initialValue = credentials?.phoneNumber ?: "",
                        isLoading = isLoading,
                        onSubmit = { phone ->
                            viewModel.updatePhoneNumber(phone)
                            onPhoneSubmit(phone)  // Add this
                        }
                    )

                    is UberAuthState.PhoneOTPInput -> OTPInput(
                        isLoading = isLoading,
                        onSubmit = { otp ->
                            viewModel.updatePhoneOTP(otp)
                            onOTPSubmit(otp)  // Add this
                        }
                    )

                    is UberAuthState.EmailInput -> EmailInput(
                        initialValue = credentials?.email ?: "",
                        isLoading = isLoading,
                        onSubmit = { email -> viewModel.updateEmail(email) }
                    )

                    is UberAuthState.EmailOTPInput -> OTPInput(
                        isLoading = isLoading,
                        onSubmit = { otp -> viewModel.updateEmailOTP(otp) }
                    )

                    is UberAuthState.Authenticated -> {
                        LaunchedEffect(Unit) {
                            onDismiss()
                        }
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        dismissButton = null
    )
}

@Composable
private fun PhoneNumberInput(
    initialValue: String,
    isLoading: Boolean,
    onSubmit: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf(initialValue) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it.take(10) },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = !isLoading,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("+91") }
        )

        Button(
            onClick = { onSubmit(phoneNumber) },
            modifier = Modifier.fillMaxWidth(),
            enabled = phoneNumber.length == 10 && !isLoading
        ) {
            Text(if (isLoading) "Please wait..." else "Send OTP")
        }
    }
}

@Composable
private fun EmailInput(
    initialValue: String,
    isLoading: Boolean,
    onSubmit: (String) -> Unit
) {
    var email by remember { mutableStateOf(initialValue) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSubmit(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank()
        ) {
            Text(if (isLoading) "Please wait..." else "Send OTP")
        }
    }
}

@Composable
private fun OTPInput(
    isLoading: Boolean,
    onSubmit: (String) -> Unit
) {
    var otp by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it.take(6) },
            label = { Text("OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSubmit(otp) },
            modifier = Modifier.fillMaxWidth(),
            enabled = otp.length == 6 && !isLoading
        ) {
            Text(if (isLoading) "Verifying..." else "Verify OTP")
        }
    }
}

private fun getDialogTitle(state: UberAuthState): String = when(state) {
    is UberAuthState.PhoneNumberInput -> "Enter Phone Number"
    is UberAuthState.PhoneOTPInput -> "Enter Phone OTP"
    is UberAuthState.EmailInput -> "Enter Email"
    is UberAuthState.EmailOTPInput -> "Enter Email OTP"
    else -> "Authentication"
}

private fun getStepNumber(state: UberAuthState): Int = when(state) {
    is UberAuthState.PhoneNumberInput -> 1
    is UberAuthState.PhoneOTPInput -> 2
    is UberAuthState.EmailInput -> 3
    is UberAuthState.EmailOTPInput -> 4
    else -> 0
}