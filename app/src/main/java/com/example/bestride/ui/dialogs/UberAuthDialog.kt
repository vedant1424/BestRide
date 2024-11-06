package com.example.bestride.ui.dialogs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bestride.presentation.state.UberAuthState
import com.example.bestride.presentation.viewmodel.UberAuthViewModel

@Composable
fun UberAuthDialog(
    viewModel: UberAuthViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val credentials by viewModel.credentials.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = getDialogTitle(authState)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (authState) {
                    is UberAuthState.PhoneNumberInput -> PhoneNumberInput(
                        initialValue = credentials?.phoneNumber ?: "",
                        onSubmit = { phone ->
                            viewModel.updatePhoneNumber(phone)
                        }
                    )

                    is UberAuthState.PhoneOTPInput -> OTPInput(
                        onSubmit = { otp ->
                            viewModel.updatePhoneOTP(otp)
                        }
                    )

                    is UberAuthState.EmailInput -> EmailInput(
                        initialValue = credentials?.email ?: "",
                        onSubmit = { email ->
                            viewModel.updateEmail(email)
                        }
                    )

                    is UberAuthState.EmailOTPInput -> OTPInput(
                        onSubmit = { otp ->
                            viewModel.updateEmailOTP(otp)
                            onDismiss()
                        }
                    )

                    is UberAuthState.Authenticated -> {
                        onDismiss()
                    }

                    else -> Text("Unknown State")
                }
            }
        },
        confirmButton = { },
        dismissButton = { }
    )
}

@Composable
private fun PhoneNumberInput(
    initialValue: String,
    onSubmit: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf(initialValue) }

    Column {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Button(
            onClick = { onSubmit(phoneNumber) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun EmailInput(
    initialValue: String,
    onSubmit: (String) -> Unit
) {
    var email by remember { mutableStateOf(initialValue) }

    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Button(
            onClick = { onSubmit(email) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun OTPInput(
    onSubmit: (String) -> Unit
) {
    var otp by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = { onSubmit(otp) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify")
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