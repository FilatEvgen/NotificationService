package org.example

import kotlinx.serialization.Serializable

@Serializable
data class NotificationsMessage(
    val message: String,
    val title: String
)