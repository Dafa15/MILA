package com.example.mila.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mila.adapter.ChatAdapter
import com.example.mila.constant.Constant
import com.example.mila.model.ChatMessage
import com.example.mila.remote.retrofit.ApiConfig
import com.example.mila.remote.retrofit.ApiService
import com.example.mila.util.UserPreference
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatViewModel(private val preference: UserPreference) : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var chatAdapter: ChatAdapter
    val currentTime: Timestamp = Timestamp.now()
    val messages = mutableListOf<ChatMessage>()

    private fun storeMessage(text: String) {
        val message = HashMap<String, Any>()
        message[Constant.KEY_SENDER_ID] = preference.getString(Constant.KEY_USER_ID).toString()
        message[Constant.KEY_RECEIVER_ID] = Constant.KEY_BOT_ID
        message[Constant.KEY_MESSAGE] = text
        message[Constant.KEY_TIMESTAMP] = Date()
        db.collection(Constant.KEY_COLLECTION_CHAT).add(message)
    }


    fun sendMessage(text: String) {
        _isLoading.value = true
        storeMessage(text)

        val senderMessage = ChatMessage(
            senderId = preference.getString(Constant.KEY_USER_ID),
            receiverId = Constant.KEY_BOT_ID,
            message = text,
            dateTime = getReadableText(Date()),
            dataObject = Date()
        )
        // Tambahkan pesan senderMessage ke daftar tanpa mengganti data yang sudah ada
        val currentMessages =
            _chatMessages.value.orEmpty().toMutableList() // Salin data yang sudah ada
        currentMessages.add(senderMessage)
        _chatMessages.postValue(currentMessages) // Perbarui LiveData dengan data yang sudah diperbarui

        viewModelScope.launch {
            try {
                // Hit API
                val jsonBody = JSONObject().apply {
                    put("text", text)
                }

                val requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), jsonBody.toString()
                )

                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().getResponse(requestBody)
                }

                // Post response ke Firebase Firestore
                val message = hashMapOf(
                    Constant.KEY_SENDER_ID to Constant.KEY_BOT_ID,
                    Constant.KEY_RECEIVER_ID to preference.getString(Constant.KEY_USER_ID)
                        .toString(),
                    Constant.KEY_MESSAGE to response.response,
                    Constant.KEY_TIMESTAMP to Date(),
                    Constant.KEY_TAG to response.tag
                )
                db.collection(Constant.KEY_COLLECTION_CHAT).add(message)

                val botMessage = ChatMessage(
                    senderId = Constant.KEY_BOT_ID,
                    receiverId = preference.getString(Constant.KEY_SENDER_ID),
                    message = response.response,
                    dateTime = getReadableText(Date()),
                    dataObject = Date(),
                    tag = response.tag
                )

                // Tambahkan botMessage ke daftar tanpa mengganti data yang sudah ada
                currentMessages.add(botMessage)
                _chatMessages.postValue(currentMessages)

            } catch (e: Exception) {
                _isLoading.value = false
                e.message?.let { Log.e("helloerror", it) }
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun listenMessages(date: Date) {
        val messages = mutableListOf<ChatMessage>()

        // Create start and end timestamps for the selected day
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = Timestamp(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = Timestamp(calendar.time)

        // Perform Firestore query
        db.collection(Constant.KEY_COLLECTION_CHAT)
            .whereIn(
                Constant.KEY_SENDER_ID,
                listOf(preference.getString(Constant.KEY_USER_ID), Constant.KEY_BOT_ID)
            )
            .whereIn(
                Constant.KEY_RECEIVER_ID,
                listOf(preference.getString(Constant.KEY_USER_ID), Constant.KEY_BOT_ID)
            )
            .whereGreaterThanOrEqualTo(Constant.KEY_TIMESTAMP, startOfDay)
            .whereLessThanOrEqualTo(Constant.KEY_TIMESTAMP, endOfDay)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val chatMessage = ChatMessage(
                        senderId = document.getString(Constant.KEY_SENDER_ID),
                        receiverId = document.getString(Constant.KEY_RECEIVER_ID),
                        message = document.getString(Constant.KEY_MESSAGE),
                        dateTime = document.getDate(Constant.KEY_TIMESTAMP)
                            ?.let { getReadableText(it) },
                        dataObject = document.getDate(Constant.KEY_TIMESTAMP),
                        tag = document.getString(Constant.KEY_TAG)
                    )
                    messages.add(chatMessage)
                }
                // Sort messages by timestamp and post the list to LiveData
                messages.sortBy { it.dataObject }
                _chatMessages.postValue(messages)
            }
            .addOnFailureListener { exception ->
                // Handle the error if necessary
                Log.e("Firestore Error", "Error getting documents: ", exception)
            }
    }


    private fun getReadableText(date: Date): String {
        return SimpleDateFormat("hh:mm", Locale.getDefault()).format(date)
    }
}