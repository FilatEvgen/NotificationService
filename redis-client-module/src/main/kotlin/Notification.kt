package org.example

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val message: String,
    val title: String
)