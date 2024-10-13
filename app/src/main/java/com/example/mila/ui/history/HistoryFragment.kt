package com.example.mila.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mila.R
import com.example.mila.adapter.HistoryAdapter
import com.example.mila.databinding.FragmentHistoryBinding
import com.example.mila.di.ChatViewModelFactory
import com.example.mila.model.ChatMessage
import com.example.mila.ui.chat.ChatActivity
import com.example.mila.util.UserPreference

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var preference: UserPreference
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize UserPreference and ViewModel
        preference = UserPreference(requireContext())
        val factory = ChatViewModelFactory(preference)
        historyViewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initRecyclerView()
        observeMessages()
        listeners()
        return root
    }

    private fun listeners() {

        val sortLayout = binding.sort
        sortLayout.outlineProvider = ViewOutlineProvider.BACKGROUND
        sortLayout.translationZ = 8f // Additional shadow effect

        val sortButton = binding.imageButton
        val sortTerbaru = binding.sortTerbaru
        val sortTerlama = binding.sortTerlama
        sortTerlama.isSelected = true

        sortButton.setOnClickListener {
            if (binding.sort.visibility == View.VISIBLE) {
                binding.sort.visibility = View.GONE // Hide the layout
            } else {
                binding.sort.visibility = View.VISIBLE // Show the layout
            }
        }


        sortTerbaru.setOnClickListener {
            sortTerbaru.isSelected = true
            sortTerlama.isSelected = false
            // Optionally change text color or perform sort logic here
            sortTerbaru.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            sortTerlama.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.sort.visibility = View.GONE
            historyViewModel.toggleSortOrder()  // Sort by newest first

        }

        sortTerlama.setOnClickListener {
            sortTerbaru.isSelected = false
            sortTerlama.isSelected = true
            // Optionally change text color or perform sort logic here
            sortTerbaru.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            sortTerlama.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.sort.visibility = View.GONE
            historyViewModel.toggleSortOrder()  // Sort by newest first
        }

    }

    private fun initRecyclerView() {
        historyViewModel.listenMessages()
        // Initialize HistoryAdapter with an empty list
        historyAdapter = HistoryAdapter(mutableListOf())
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        historyAdapter.setOnItemClickCallback(object : HistoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ChatMessage) {
                val intent = Intent(requireActivity(), ChatActivity::class.java)
                intent.putExtra(ChatActivity.CHAT_DATE, data.dataObject)
                startActivity(intent)
            }
        })
    }

    private fun observeMessages() {
        // Observe the chat history LiveData
        historyViewModel.chatHistory.observe(viewLifecycleOwner) { messages ->
            historyAdapter.updateMessages(messages)
            binding.rvHistory.visibility = View.VISIBLE
            if (messages.isNotEmpty()) {
                binding.emptyItem.visibility = View.GONE
                binding.rvHistory.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
