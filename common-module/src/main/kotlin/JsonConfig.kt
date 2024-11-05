package org.example

import io.ktor.server.application.*
import kotlinx.serialization.json.Json

fun Application.jsonConfiguration() : Json {
    return Json {
        ignoreUnknownKeys = true
    }
}