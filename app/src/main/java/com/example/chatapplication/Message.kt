package com.example.chatapplication

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable

data class Message(
    val senderID : String = "",
    val msgContent : String = "",
    val timestamp: Long = 0
)
