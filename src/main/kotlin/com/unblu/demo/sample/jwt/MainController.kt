package com.unblu.demo.sample.jwt

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController(private val unbluConfiguration: UnbluConfiguration, private val appConfiguration: AppConfiguration) {

    @GetMapping
    fun index(model: Model): String {
        model["unbluServerUrl"] = unbluConfiguration.serverUrl
        model["unbluEntryPath"] = unbluConfiguration.entryPath
        model["unbluApiKey"] = unbluConfiguration.apiKey
        model["appBaseUrl"] = appConfiguration.baseUrl
        return "index"
    }

}
