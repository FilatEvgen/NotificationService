package org.example

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.isActive

object WebSocketSessionManager {
    private val sessions = mutableSetOf<SessionData>()

    fun addSession(session: DefaultWebSocketSession) {
        sessions.add(SessionData(session))
    }

    fun removeSession(session: DefaultWebSocketSession) {
        sessions.removeIf { it.session == session }
    }

    fun subscribeToChannel(session: DefaultWebSocketSession, channel: String) {
        sessions.find { it.session == session }?.let { sessionData ->
            sessionData.subscribedChannels.add(channel)
            println("Сессия ${session.hashCode()} подписана на канал '$channel'")
        }
    }

    fun unsubscribeFromChannel(session: DefaultWebSocketSession, channel: String) {
        sessions.find { it.session == session }?.let { sessionData ->
            sessionData.subscribedChannels.remove(channel)
            println("Сессия ${session.hashCode()} отписана от канала '$channel'")
        }
    }

    suspend fun sendMessageToChannel(channel: String, message: String) {
        sessions.filter { it.subscribedChannels.contains(channel) }.forEach { sessionData ->
            if (sessionData.session.isActive) {
                sessionData.session.send(message)
                println("Сообщение '$message' отправлено сессии ${sessionData.session.hashCode()} на канал '$channel'")
            } else {
                println("Сессия ${sessionData.session.hashCode()} неактивна, сообщение не отправлено.")
            }
        }
    }

    fun getSubscribedChannels(session: DefaultWebSocketSession): Set<String>? {
        return sessions.find { it.session == session }?.subscribedChannels
    }
}