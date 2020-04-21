package com.unblu.demo.sample.jwt

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class ApiController {

    @PostMapping("token")
    fun createJwt(): TokenResponse {
        // TODO generate a valid JWT
        return TokenResponse("a.b.c")
    }

}