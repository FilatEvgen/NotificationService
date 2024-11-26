package org.example

import kotlinx.serialization.Serializable

@Serializable

data class Notification(
    val title: String,
    val message: String,
    val channels: List<String>
)