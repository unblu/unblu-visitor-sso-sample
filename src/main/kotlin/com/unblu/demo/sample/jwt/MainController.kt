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
        model["unbluBaseUrl"] = unbluBaseUrl
        model["unbluApiKey"] = apiKey
        return "index"
    }

}

