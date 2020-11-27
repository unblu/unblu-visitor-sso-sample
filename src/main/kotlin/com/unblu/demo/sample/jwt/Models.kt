package com.unblu.demo.sample.jwt

data class JwtRequest(
        val email: String,
        val firstname: String,
        val lastname: String
)

data class TokenResponse(val token: String)