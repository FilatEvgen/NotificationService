package org.example

import io.ktor.websocket.*

object WebSocketSessionManager {
    private val sessions = mutableSetOf<DefaultWebSocketSession>()

    fun addSession(session: DefaultWebSocketSession) {
        sessions.add(session)
    }

    fun removeSession(session: DefaultWebSocketSession) {
        sessions.remove(session)
    }

    suspend fun sendToAll(message: String) {
        for (session in sessions) {
            session.send(message)
        }
    }
}