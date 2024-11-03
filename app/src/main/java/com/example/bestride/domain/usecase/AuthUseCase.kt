package com.example.bestride.domain.usecase

import com.example.bestride.data.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    fun login(email: String, password: String): Flow<Result<Boolean>> {
        return loginRepository.login(email, password)
            .map { result ->
                result.fold(
                    onSuccess = {
                        Result.success(true)
                    },
                    onFailure = { throwable ->
                        Result.failure(throwable)
                    }
                )
            }
            .catch { e ->
                emit(Result.failure(e))
            }
    }

    fun register(email: String, password: String): Flow<Result<Boolean>> {
        return loginRepository.register(email, password)
            .map { result ->
                result.fold(
                    onSuccess = {
                        Result.success(true)
                    },
                    onFailure = { throwable ->
                        Result.failure(throwable)
                    }
                )
            }
            .catch { e ->
                emit(Result.failure(e))
            }
    }

    private fun validateEmail(email: String): Result<Unit> {
        return when {
            email.isBlank() -> Result.failure(IllegalArgumentException("Email cannot be empty"))
            !email.contains("@") -> Result.failure(IllegalArgumentException("Invalid email format"))
            else -> Result.success(Unit)
        }
    }

    private fun validatePassword(password: String): Result<Unit> {
        return when {
            password.isBlank() -> Result.failure(IllegalArgumentException("Password cannot be empty"))
            password.length < 6 -> Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            !password.any { it.isDigit() } -> Result.failure(IllegalArgumentException("Password must contain at least one number"))
            !password.any { it.isLetter() } -> Result.failure(IllegalArgumentException("Password must contain at least one letter"))
            else -> Result.success(Unit)
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}