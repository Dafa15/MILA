package com.example.mila.model

import java.util.Date

data class ChatMessage (
    var id: String? = null,
    var senderId: String? = null,
    var receiverId: String? = null,
    var message: String? = null,
    var dateTime: String? = null,
    var dataObject: Date? = null,
    var tag: String? = null,
)