package com.example.mila.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mila.R
import com.example.mila.adapter.LeaderBoardAdapter
import com.example.mila.databinding.ActivityLeaderBoardBinding
import com.example.mila.di.ChatViewModelFactory
import com.example.mila.util.UserPreference

class LeaderBoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderBoardBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var leaderBoardAdapter: LeaderBoardAdapter
    private lateinit var preference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLeaderBoardBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = Color.WHITE
        preference = UserPreference(this)
        val factory = ChatViewModelFactory(preference)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        leaderBoardAdapter = LeaderBoardAdapter(mutableListOf())
        binding.rvLeaderboardItem.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboardItem.adapter = leaderBoardAdapter
        viewModel.fetchChatMessages()

        viewModel.leaderBoardListFull.observe(this) { leaderBoards ->
            leaderBoardAdapter.updateList(leaderBoards)
        }

        binding.backAppLeaderboard.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}