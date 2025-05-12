package ru.netology.nmedia.dto

data class PushToken(
    val token: String,
)

data class MessageData(
    val recipientId: Long,
    val content: String
)
