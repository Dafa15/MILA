package com.example.mila.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mila.constant.Constant
import com.example.mila.databinding.FragmentProfileBinding
import com.example.mila.ui.LoginActivity
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var userPreference: UserPreference
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize user preference
        userPreference = UserPreference(requireContext())
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Fetch user data from Firestore
        fetchUserData()

        // Set up logout button
        binding.logoutButton.setOnClickListener {
            signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        viewModel.profileImage.observe(viewLifecycleOwner) { bitmap ->
            Glide.with(this)
                .load(bitmap    ) // Replace with your image URL or URI
                .apply(RequestOptions.circleCropTransform())
                .override(100, 100)  // Ubah ukuran ini sesuai kebutuhan
                .into(binding.imageView2)
        }

        viewModel.userName.observe(viewLifecycleOwner) {userName ->
            binding.userName.text = userName
        }

        // Set up edit button to open EditProfileActivity
        binding.editButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.appInfo.setOnClickListener {
            val intent = Intent(requireContext(), AppInfoActivity::class.java)
            startActivity(intent)
        }

        // Set up edit button to open EditProfileActivity
        binding.help.setOnClickListener {
            val intent = Intent(requireContext(), HelpActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    private fun fetchUserData() {
        val userId =
            userPreference.getString(Constant.KEY_USER_ID)

        // Fetch user data from Firestore
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString(Constant.KEY_NAME)
                        val encodedImage = document.getString(Constant.KEY_IMAGE)

                        // Update UI with user data
                        if (username != null) {
                            viewModel.setUserName(username)
                        }

                        // If profile image is not null, decode it and set to ImageView
                        if (!encodedImage.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            viewModel.setProfileImage(decodedImage)  // Update LiveData dengan gambar yang dipilih
                        }
                    } else {
                        Toast.makeText(requireContext(), "No user data found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to load user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun signOut() {
        userPreference.clear() // Clear user data from local storage
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
