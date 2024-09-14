package com.example.mila.remote.retrofit

import com.example.mila.remote.response.ChatResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {
    @POST("predict")
    suspend fun getResponse(
        @Body requestBody: RequestBody
    ): ChatResponse
}