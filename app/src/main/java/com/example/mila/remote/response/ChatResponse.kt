package com.example.mila.remote.response

import com.google.gson.annotations.SerializedName

data class ChatResponse (
    @field:SerializedName("tag")
    val tag: String,

    @field:SerializedName("response")
    val response: String,
)