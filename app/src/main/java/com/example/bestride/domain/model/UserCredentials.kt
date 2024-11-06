package com.example.bestride.domain.model

data class UserCredentials(
    val phoneNumber: String,
    val phoneOTP: String? = null,
    val email: String? = null,
    val emailOTP: String? = null
)