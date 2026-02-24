package com.unblu.demo.sample.jwt

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebJarTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun testBootstrapWebJar() {
        webTestClient.get()
            .uri("/webjars/bootstrap/css/bootstrap.min.css")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType("text/css")
    }

    @Test
    fun testBootstrapWebJarWithVersion() {
        // Checking if the actual versioned path works
        webTestClient.get()
            .uri("/webjars/bootstrap/5.3.8/css/bootstrap.min.css")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType("text/css")
    }
}
