package com.unblu.demo.sample.jwt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class ApplicationTests {

    @Test
    fun contextLoads() {
    }

}
