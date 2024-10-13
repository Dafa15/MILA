package com.example.mila.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mila.adapter.HistoryAdapter
import com.example.mila.adapter.LeaderBoardAdapter
import com.example.mila.constant.Constant
import com.example.mila.databinding.FragmentHomeBinding
import com.example.mila.di.ChatViewModelFactory
import com.example.mila.ui.chat.ChatActivity
import com.example.mila.ui.history.HistoryViewModel
import com.example.mila.ui.profile.EditProfileActivity
import com.example.mila.ui.profile.ProfileViewModel
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.TimeZone

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var userPreference: UserPreference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: HomeViewModel
    private lateinit var leaderBoardAdapter: LeaderBoardAdapter
    private lateinit var preference: UserPreference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firestore = FirebaseFirestore.getInstance()
        preference = UserPreference(requireContext())
        val factory = ChatViewModelFactory(preference)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        userPreference = UserPreference(requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Set greeting message based on time of day
        binding.greetings.text = getGreetingMessage()

        viewModel.profileImage.observe(requireActivity()) {decodedImage->
            Glide.with(this)
                .load(decodedImage) // Replace with your image URL or URI
                .apply(RequestOptions.circleCropTransform())
                .into(binding.imageView2)
        }
        viewModel.userName.observe(requireActivity()) {userName->
            binding.userName.text = userName
        }

        // Set up chat button
        binding.buttonChat.setOnClickListener {
            val intent = Intent(requireActivity(), ChatActivity::class.java)
            startActivity(intent)
        }
        fetchUserData()
        leaderBoardAdapter = LeaderBoardAdapter(mutableListOf())
        binding.rvLeaderboardItem.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboardItem.adapter = leaderBoardAdapter
        viewModel.fetchChatMessages()

        viewModel.leaderBoardList.observe(viewLifecycleOwner) { leaderBoards ->
            if (leaderBoards != null && leaderBoards.isNotEmpty()) {
                binding.leaderBoard.visibility = View.VISIBLE
                val layoutParams = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.topMargin = 32 // You can also convert this to dp if needed
                binding.imageView3.layoutParams = layoutParams
            }
            leaderBoardAdapter.updateList(leaderBoards)
        }

        binding.leaderBoardArrow.setOnClickListener {
            val intent = Intent(requireActivity(), LeaderBoardActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    private fun fetchUserData() {
        val userId =
            userPreference.getString(Constant.KEY_USER_ID)

        // Fetch user data from Firestore
        if (userId != null) {
            firestore.collection(Constant.KEY_COLLECTION_USERS).document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val encodedImage = document.getString(Constant.KEY_IMAGE)
                        val userName = document.getString(Constant.KEY_NAME)
                        if (userName != null) {
                            viewModel.setUserName(userName)
                        }

                        // If profile image is not null, decode it and set to ImageView
                        if (!encodedImage.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
                            val decodedImage =
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            if (decodedImage != null) {
                                viewModel.setProfileImage(decodedImage)  // Update LiveData dengan gambar yang dipilih
                            }
                        }

                    } else {
                        Toast.makeText(requireContext(), "No user data found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to load user data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("GMT+7")
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..11 -> "Selamat pagi"
            in 12..15 -> "Selamat siang"
            in 16..18 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
