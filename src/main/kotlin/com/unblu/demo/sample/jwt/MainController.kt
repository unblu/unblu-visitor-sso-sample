package com.unblu.demo.sample.jwt

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController(@Value("\${unblu.baseUrl}")
                     private val unbluBaseUrl: String,
                     @Value("\${unblu.apiKey}")
                     private val apiKey: String) {

    @GetMapping
    fun index(model: Model): String {
        model["unbluTokenActivation"] = "$unbluBaseUrl/rest/v3/authenticator/loginWithSecureToken?x-unblu-apikey=$apiKey"
        return "index"
    }

    @GetMapping("secure")
    fun secure(model: Model): String {
        model["unbluSnippet"] = "$unbluBaseUrl/visitor.js?x-unblu-apikey=$apiKey"
        model["unbluLogout"] = "$unbluBaseUrl/rest/v3/authenticator/logout"
        return "secure"
    }

}

