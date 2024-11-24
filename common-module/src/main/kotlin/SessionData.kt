package org.example

import io.ktor.websocket.*

data class SessionData(
    val session: DefaultWebSocketSession,
    val subscribedChannels: MutableSet<String> = mutableSetOf()
)


