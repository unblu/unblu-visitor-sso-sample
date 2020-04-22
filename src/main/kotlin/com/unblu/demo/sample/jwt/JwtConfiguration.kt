package com.unblu.demo.sample.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "jwt")
data class JwtConfiguration(
        val issuer: String,
        val audience: String,
        val validFor: Duration,
        val encryption: Boolean
)