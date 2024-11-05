package org.example

import kotlinx.serialization.json.Json

val jsonConfig = Json {
    ignoreUnknownKeys = true
}