package com.unblu.demo.sample.jwt

import java.net.URI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.server.ServerWebExchange

@Configuration
class CORSConfiguration(private val jwtConfiguration: JwtConfiguration) {

    private val logger: Logger = LoggerFactory.getLogger(CORSConfiguration::class.java)

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val source = CorsConfigurationSource { exchange: ServerWebExchange ->
            val origin = exchange.request.headers.origin
            val validateOrigin = !(jwtConfiguration.allowedOrigins.size == 1 && jwtConfiguration.allowedOrigins[0] == "*")

            logger.info("CORS request from: {} -> {}, validateOrigin: {}", origin, jwtConfiguration.allowedOrigins, validateOrigin)

            if (validateOrigin && !isAllowedOrigin(origin, jwtConfiguration.allowedOrigins)) return@CorsConfigurationSource null

            CorsConfiguration().apply {
                allowedOrigins = listOf(origin)
                allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                allowedHeaders = listOf("*")
                allowCredentials = validateOrigin
            }
        }

        return CorsWebFilter(source)
    }

    private fun isAllowedOrigin(origin: String?, allowedPatterns: List<String>): Boolean {
        if (origin.isNullOrBlank()) return false
        if (allowedPatterns.size == 1 && allowedPatterns[0] == "*") return true

        val originUri = try {
            URI(origin)
        } catch(ex: Exception) {
            return false
        }

        val originScheme = originUri.scheme ?: return false
        val originHost = originUri.host ?: return false
        val originPort = if (originUri.port == -1) defaultPort(originScheme) else originUri.port

        return allowedPatterns.any { pattern ->
            matchOriginPattern(originScheme, originHost, originPort, pattern)
        }
    }

    private fun matchOriginPattern(scheme: String, host: String, port: Int, pattern: String): Boolean {
        val patternUri = try {
            URI(pattern.replace("*.", "placeholder."))
        } catch (ex: Exception) {
            return false
        }

        val patternScheme = patternUri.scheme ?: return false
        val patternHostRaw = pattern.removePrefix("$patternScheme://")
            .substringBefore(":")
            .substringBefore("/")
        val patternPort = if (patternUri.port == -1) defaultPort(patternScheme) else patternUri.port

        if (scheme != patternScheme) return false
        if (port != patternPort) return false

        return when {
            patternHostRaw.startsWith("*.") -> {
                val domain = patternHostRaw.removePrefix("*.")
                host == domain || host.endsWith(".$domain")
            }
            else -> host == patternHostRaw
        }
    }

    private fun defaultPort(scheme: String): Int =
        when (scheme.lowercase()) {
            "http" -> 80
            "https" -> 443
            else -> -1
        }
}