package com.appdev.medicare.model

data class RegisterRequest(
    val username: String,
    val passwordHash: String
)
