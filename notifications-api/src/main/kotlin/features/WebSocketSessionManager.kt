package org.example

import io.ktor.websocket.*


object WebSocketSessionManager {
    private val sessions = mutableSetOf<SessionData>()

    fun addSession(session: DefaultWebSocketSession) {
        sessions.add(SessionData(session))
    }

    fun removeSession(session: DefaultWebSocketSession) {
        sessions.removeIf { it.session == session }
    }

    fun subscribeToChannel(session: DefaultWebSocketSession, channel: String) {
        sessions.find { it.session == session }?.subscribedChannels?.add(channel)
    }

    fun unsubscribeFromChannel(session: DefaultWebSocketSession, channel: String) {
        sessions.find { it.session == session }?.subscribedChannels?.remove(channel)
    }

    suspend fun sendToChannel(channel: String, message: String) {
        for (sessionData in sessions) {
            if (sessionData.subscribedChannels.contains(channel)) {
                sessionData.session.send(message)
            }
        }
    }
}