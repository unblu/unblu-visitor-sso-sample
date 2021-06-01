package com.unblu.demo.sample.jwt

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController(private val unbluConfiguration: UnbluConfiguration) {

    @GetMapping
    fun index(model: Model): String {
        model["unbluServerUrl"] = unbluConfiguration.serverUrl
        model["unbluEntryPath"] = unbluConfiguration.entryPath
        model["unbluApiKey"] = unbluConfiguration.apiKey
        return "index"
    }

}

