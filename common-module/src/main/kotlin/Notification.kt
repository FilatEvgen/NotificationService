package org.example

import kotlinx.serialization.Serializable

@Serializable

data class Notification(
    var id: String?,
    val title: String,
    val message: String,
    val channels: List<String>
)

@Serializable

data class NotificationCreateRequest(
    val title: String,
    val message: String,
    val channels: List<String>
)