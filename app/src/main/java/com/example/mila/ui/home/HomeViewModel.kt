package com.example.mila.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mila.constant.Constant
import com.example.mila.model.ChatMessage
import com.example.mila.model.LeaderBoard
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class HomeViewModel (private val preference: UserPreference) : ViewModel() {

    private val _profileImage = MutableLiveData<Bitmap>()
    val profileImage: LiveData<Bitmap> get() = _profileImage
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName
    private val _leaderBoardList = MutableLiveData<List<LeaderBoard>>()
    val leaderBoardList: LiveData<List<LeaderBoard>> get() = _leaderBoardList
    private val _leaderBoardListFull = MutableLiveData<List<LeaderBoard>>()
    val leaderBoardListFull: LiveData<List<LeaderBoard>> get() = _leaderBoardListFull
    private val db = FirebaseFirestore.getInstance()


    // Fungsi untuk memperbarui gambar
    fun setProfileImage(bitmap: Bitmap) {
        // Membandingkan konten bitmap
        if (_profileImage.value == null || !_profileImage.value!!.sameAs(bitmap)) {
            _profileImage.value = bitmap
        }
    }

    fun fetchChatMessages() {
        db.collection(Constant.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constant.KEY_RECEIVER_ID, preference.getString(Constant.KEY_USER_ID))
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Error handling
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    processChatMessages(snapshots)
                }
            }
    }

    private fun processChatMessages(snapshots: QuerySnapshot) {
        val chatMessages = mutableListOf<ChatMessage>()
        for (document in snapshots.documents) {
            val chatMessage = document.toObject(ChatMessage::class.java)
            chatMessage?.let {
                chatMessages.add(it)
            }
        }
        calculateLeaderBoard(chatMessages)
    }

    // Fungsi untuk menghitung leaderboard berdasarkan tag
    private fun calculateLeaderBoard(chatMessages: List<ChatMessage>) {
        val tagCountMap = mutableMapOf<String, Int>()

        // Menghitung jumlah kemunculan setiap tag
        for (message in chatMessages) {
            if (message.senderId == "botId" && message.tag != "sapaan") {
                val tag = message.tag ?: "Unknown"
                tagCountMap[tag] = tagCountMap.getOrDefault(tag, 0) + 1
            }

        }


        // Mengubah hasil perhitungan ke dalam bentuk LeaderBoard dan mengurutkannya
        val leaderBoardList = tagCountMap.map { LeaderBoard(it.key, it.value) }
            .sortedByDescending { it.total }
            .take(3) // Mengambil top 3 tag
        val leaderBoardListFull = tagCountMap.map { LeaderBoard(it.key, it.value) }
            .sortedByDescending { it.total }

        // Update nilai LiveData
        _leaderBoardList.value = leaderBoardList
        _leaderBoardListFull.value = leaderBoardListFull
    }

    fun setUserName(string: String) {
        if (_userName.value == null || _userName.value != string) {
            _userName.value = string
        }
    }

}