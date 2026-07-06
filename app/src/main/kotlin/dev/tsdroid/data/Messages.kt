package dev.tsdroid.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Shared JSON instance with stable output format. */
val messageJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Serializable
data class FileAttachment(
    @kotlinx.serialization.SerialName("fn") val fileName: String,
    @kotlinx.serialization.SerialName("fs") val fileSize: Long,
    @kotlinx.serialization.SerialName("fi") val fileId: String,
    @kotlinx.serialization.SerialName("im") val isImage: Boolean,
    @kotlinx.serialization.SerialName("ch") val channelId: Long = 0L,
)

@Serializable
data class ChatMessage(
    @kotlinx.serialization.SerialName("s") val sender: String,
    @kotlinx.serialization.SerialName("t") val text: String,
    @kotlinx.serialization.SerialName("ts") val timestamp: Long = System.currentTimeMillis(),
    @kotlinx.serialization.SerialName("me") val isMe: Boolean = false,
    @kotlinx.serialization.SerialName("sid") val senderId: Int = 0,
    @kotlinx.serialization.SerialName("fa") val fileAttachment: FileAttachment? = null,
) {
    /** Transient — not serialized; derived from message container at runtime. */
    val isPrivate: Boolean get() = false
}

/**
 * Wrapper matching the on-disk JSON structure:
 * {"channel": [...], "private": { "userId": [...], ... }}
 */
@Serializable
data class ServerMessages(
    val channel: List<ChatMessage> = emptyList(),
    val private: Map<Int, List<ChatMessage>> = emptyMap(),
)
