package com.example.bestride.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor() {

    // Single users map with test data
    private val users = mutableMapOf(
        "test@test.com" to "password123",
        "user@example.com" to "user123"
    )

    fun login(email: String, password: String): Flow<Result<Boolean>> = flow {
        try {
            Log.d("LoginRepository", "Attempting login for email: $email")

            // Simple validation
            if (email.isBlank() || password.isBlank()) {
                Log.e("LoginRepository", "Email or password is blank")
                emit(Result.failure(Exception("Email and password cannot be empty")))
                return@flow
            }

            // Check if credentials match
            val storedPassword = users[email]
            if (storedPassword == password) {
                Log.d("LoginRepository", "Login successful")
                emit(Result.success(true))
            } else {
                Log.e("LoginRepository", "Invalid credentials")
                emit(Result.failure(Exception("Invalid credentials")))
            }
        } catch (e: Exception) {
            Log.e("LoginRepository", "Login error", e)
            emit(Result.failure(e))
        }
    }

    fun register(email: String, password: String): Flow<Result<Boolean>> = flow {
        try {
            Log.d("LoginRepository", "Attempting registration for email: $email")

            // Simple validation
            if (email.isBlank() || password.isBlank()) {
                Log.e("LoginRepository", "Email or password is blank")
                emit(Result.failure(Exception("Email and password cannot be empty")))
                return@flow
            }

            // Check if user already exists
            if (users.containsKey(email)) {
                Log.e("LoginRepository", "User already exists")
                emit(Result.failure(Exception("User already exists")))
                return@flow
            }

            // Store new user
            users[email] = password
            Log.d("LoginRepository", "Registration successful")
            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("LoginRepository", "Registration error", e)
            emit(Result.failure(e))
        }
    }
}