package com.unblu.demo.sample.jwt

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.server.session.CookieWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver
import java.util.concurrent.ConcurrentHashMap

@Configuration
@EnableSpringWebSession
class SessionConfig {
    @Bean
    fun reactiveSessionRepository() = ReactiveMapSessionRepository(ConcurrentHashMap())

    @Bean
    fun webSessionIdResolver(): WebSessionIdResolver {
        val resolver = CookieWebSessionIdResolver()
        resolver.cookieName = "JSESSIONID"
        resolver.addCookieInitializer { builder ->
            builder.path("/").sameSite("Strict")
        }
        return resolver
    }
}
