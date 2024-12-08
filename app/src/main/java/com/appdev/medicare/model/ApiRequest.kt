package com.appdev.medicare.model

data class RegisterRequest(
    val username: String,
    val passwordHash: String
)

data class LoginRequest(
    val username: String,
    val passwordHash: String
)
