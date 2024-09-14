package com.example.mila.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mila.adapter.ChatAdapter
import com.example.mila.constant.Constant
import com.example.mila.databinding.ActivityChatBinding
import com.example.mila.di.ChatViewModelFactory
import com.example.mila.model.ChatMessage
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preference: UserPreference

    companion object {
        const val  CHAT_DATE = "chat_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        preference = UserPreference(applicationContext)
        val factory = ChatViewModelFactory(preference)
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        // Use ViewBinding to access views
        ViewCompat.setOnApplyWindowInsetsListener(binding.chat) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        setListeners()
        observeMessages()
    }

    private fun init() {
        chatAdapter = ChatAdapter(mutableListOf(), preference.getString((Constant.KEY_USER_ID)).toString())
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = chatAdapter
    }

    private fun setListeners() {
        binding.buttonSend.setOnClickListener {
            viewModel.sendMessage(binding.edMessage.text.toString())
            binding.edMessage.text = null
        }
        binding.edMessage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // Call the function to send the message
                viewModel.sendMessage(binding.edMessage.text.toString())
                binding.edMessage.text = null
                true  // Return true to indicate that the event has been handled
            } else {
                false
            }
        }
    }

    private fun observeMessages() {
        val chatDate = intent.getSerializableExtra(CHAT_DATE) as? Date
        if (chatDate != null) {
            viewModel.listenMessages(chatDate)
            binding.etMessage.visibility = View.GONE
        }
        viewModel.chatMessages.observe(this) { messages ->
            chatAdapter.updateMessages(messages)
            binding.rvChat.visibility = View.VISIBLE
            if (messages.isNotEmpty()) {
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }
    }
}
