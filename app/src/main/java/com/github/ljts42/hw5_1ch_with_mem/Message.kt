package com.github.ljts42.hw5_1ch_with_mem

data class Message(
    val id: String = "",
    val from: String = "Test",
    val to: String = "1ch",
    val type: MessageType = MessageType.TEXT,
    val data: String = "",
    val time: String = ""
)

enum class MessageType {
    IMAGE, TEXT
}
