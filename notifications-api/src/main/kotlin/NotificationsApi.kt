package org.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import org.example.features.notificationFeatures

fun main() {
    embeddedServer(Netty, port = 8081, module = Application::module).start(wait = true)
}
fun Application.module(){
    install(ContentNegotiation) {
        json(jsonConfig)
    }
    notificationFeatures()
}