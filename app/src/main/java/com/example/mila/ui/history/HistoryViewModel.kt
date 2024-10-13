package com.example.mila.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mila.adapter.ChatAdapter
import com.example.mila.constant.Constant
import com.example.mila.model.ChatMessage
import com.example.mila.remote.retrofit.ApiConfig
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel (private val preference: UserPreference) : ViewModel() {
    private val _chatHistory = MutableLiveData<List<ChatMessage>>()
    val chatHistory: LiveData<List<ChatMessage>> = _chatHistory
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var chatAdapter: ChatAdapter
    private var isAscending: Boolean = true

    fun listenMessages() {
        chatAdapter = ChatAdapter(mutableListOf(), preference.getString(Constant.KEY_USER_ID).toString())
        val messages = mutableListOf<ChatMessage>()
        val dateSet = HashSet<String>()  // Menyimpan tanggal yang sudah ada
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())  // Format untuk tanggal

        db.collection(Constant.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constant.KEY_SENDER_ID, Constant.KEY_BOT_ID)
            .whereEqualTo(Constant.KEY_RECEIVER_ID, preference.getString(Constant.KEY_USER_ID))
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    for (documentChange in value.documentChanges) {
                        val chatMessage = ChatMessage(
                            senderId = documentChange.document.getString(Constant.KEY_SENDER_ID),
                            receiverId = documentChange.document.getString(Constant.KEY_RECEIVER_ID),
                            message = documentChange.document.getString(Constant.KEY_MESSAGE),
                            dateTime = documentChange.document.getDate(Constant.KEY_TIMESTAMP)
                                ?.let { dateFormat.format(it) },  // Memformat tanggal
                            dataObject = documentChange.document.getDate(Constant.KEY_TIMESTAMP),
                            tag = documentChange.document.getString(Constant.KEY_TAG)
                        )

                        val messageDate = chatMessage.dateTime

                        if (messageDate != null && !dateSet.contains(messageDate)) {
                            // Tambahkan pesan pertama dari setiap tanggal yang berbeda
                            messages.add(chatMessage)
                            chatAdapter.notifyItemInserted(messages.size - 1)
                            dateSet.add(messageDate)  // Memasukkan tanggal ke dalam set
                        }
                    }
                    sortMessages(messages)
                }
            }
    }

    private fun sortMessages(messages: MutableList<ChatMessage>) {
        // Sort the list based on the `dataObject` field
        if (isAscending) {
            messages.sortBy { it.dataObject }  // Ascending order (oldest first)
        } else {
            messages.sortByDescending { it.dataObject }  // Descending order (newest first)
        }
        _chatHistory.postValue(messages)  // Mem-post value list messages ke LiveData
    }

    // Toggle sorting order between ascending and descending
    fun toggleSortOrder() {
        isAscending = !isAscending
        // Re-sort the messages and update LiveData
        _chatHistory.value?.let { messages ->
            sortMessages(messages.toMutableList())
        }
    }

    private fun getReadableText(date: Date): String {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(date)
    }
}