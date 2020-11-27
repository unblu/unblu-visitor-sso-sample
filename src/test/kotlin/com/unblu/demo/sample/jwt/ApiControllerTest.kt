package com.unblu.demo.sample.jwt

import com.nimbusds.jwt.SignedJWT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(ApiController::class)
@ExtendWith(SpringExtension::class)
class ApiControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun createJwtToken() {
        val email = "peter.muster@example.com"
        val firstname = "Peter"
        val lastname = "Muster"
        val jwtRequest = JwtRequest(email, firstname, lastname)

        val verifyResponse = fun(tokenResponse: TokenResponse?) {
            assertThat(tokenResponse).isNotNull
            val jwt = SignedJWT.parse(tokenResponse?.token)
            val claims = jwt.jwtClaimsSet.claims
            assertThat(claims["email"]).isEqualTo(email)
            assertThat(claims["firstName"]).isEqualTo(firstname)
            assertThat(claims["lastName"]).isEqualTo(lastname)
        }

        webTestClient.post()
                .uri("/api/token")
                .bodyValue(jwtRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody
                .let(verifyResponse)
    }

}