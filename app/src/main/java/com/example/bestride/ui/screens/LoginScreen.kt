package com.example.bestride.ui.screens



import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bestride.navigation.Screen
import com.example.bestride.presentation.state.AuthUiState
import com.example.bestride.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()




    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                try {
                    navController.navigate(Screen.Booking.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } catch (e: Exception) {
                    Log.e("LoginScreen", "Navigation error", e)
                }
            }
            is AuthUiState.Error -> {
                Log.e("LoginScreen", "Login error: ${(uiState as AuthUiState.Error).message}")
            }
            else -> {}
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Login")
            }

            TextButton(
                onClick = { navController.navigate(Screen.Register.route) }
            ) {
                Text("Don't have an account? Register")
            }

            when (uiState) {
                is AuthUiState.Loading -> {
                    Text("loading.....")
                }
                is AuthUiState.Error -> {
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is AuthUiState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Booking.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}