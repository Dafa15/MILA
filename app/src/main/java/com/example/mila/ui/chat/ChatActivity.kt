package com.example.mila.ui.chat

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mila.R
import com.example.mila.adapter.ChatAdapter
import com.example.mila.constant.Constant
import com.example.mila.databinding.ActivityChatBinding
import com.example.mila.di.ChatViewModelFactory
import com.example.mila.util.UserPreference
import java.util.Date


class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preference: UserPreference

    companion object {
        const val CHAT_DATE = "chat_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.WHITE

        // Initialize ViewBinding
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.textInputLayout) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()) // Handle keyboard insets

            // Apply padding based on whether the keyboard is visible or not
            val bottomInset = imeInsets.bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomInset)
            insets
        }


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
        chatAdapter =
            ChatAdapter(mutableListOf(), preference.getString((Constant.KEY_USER_ID)).toString())
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = chatAdapter
    }

    private fun setListeners() {

        binding.textInputLayout.setEndIconOnClickListener {
            if (
                binding.edMessage.text != null && binding.edMessage.text!!.isNotEmpty()
            ) {
                viewModel.sendMessage(binding.edMessage.text.toString())
                binding.edMessage.text = null
            }

        }
        binding.edMessage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // Call the function to send the message
                if (binding.edMessage.text != null && binding.edMessage.text!!.isNotEmpty()
                ) {
                    viewModel.sendMessage(binding.edMessage.text.toString())
                    binding.edMessage.text = null
                }

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
            binding.textInputLayout.visibility = View.GONE
        }
        viewModel.chatMessages.observe(this) { messages ->
            chatAdapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                val latestMessage = messages.last()
                if (latestMessage.tag == "warning") {
                    showWarningDialog()
                }
                binding.rvChat.visibility = View.VISIBLE
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showWarningDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.warning_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.edit_text_outline))
        dialog.show()
    }
}
