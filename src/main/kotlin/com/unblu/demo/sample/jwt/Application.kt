package com.unblu.demo.sample.jwt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtConfiguration::class, UnbluConfiguration::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
