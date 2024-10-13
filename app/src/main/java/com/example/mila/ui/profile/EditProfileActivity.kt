package com.example.mila.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mila.R
import com.example.mila.constant.Constant
import com.example.mila.databinding.ActivityEditProfileBinding
import com.example.mila.ui.BottomNavigationActivity
import com.example.mila.ui.chat.ChatActivity
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import kotlin.coroutines.resume

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userPreference: UserPreference
    private lateinit var firestore: FirebaseFirestore
    private var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        userPreference = UserPreference(this)
        firestore = FirebaseFirestore.getInstance()

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set OnClickListener for selecting an image
        binding.imageView5.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }


        // Set OnClickListener for saving profile changes
        binding.simpanButton.setOnClickListener {
            saveProfile()
        }
        binding.toolbar.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        Glide.with(this)
            .load(R.drawable.edit_text_outline) // Replace with your drawable resource
            .apply(RequestOptions.circleCropTransform())
            .into(binding.imageView5)
        fetchUserData()
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
                        val nama = document.getString(Constant.KEY_NAME)
                        val email = document.getString(Constant.KEY_EMAIL)
                        val encodedImage = document.getString(Constant.KEY_IMAGE)

                        // Update UI with user data
                        binding.etNamaLengkap.setText(nama)
                        binding.etEmail.setText(email)

                        // If profile image is not null, decode it and set to ImageView
                        if (!encodedImage.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
                            val decodedImage =
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            Glide.with(this)
                                .load(decodedImage) // Replace with your image URL or URI
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.imageView5)
                            binding.addImage.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to load user data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    // Method to encode the image to Base64
    private fun encodeImage(bitmap: Bitmap): String {
        // Ensure the image is square by cropping it to the minimum dimension
        val minDimension = Math.min(bitmap.width, bitmap.height)
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            (bitmap.width - minDimension) / 2,  // X coordinate to start cropping
            (bitmap.height - minDimension) / 2, // Y coordinate to start cropping
            minDimension, minDimension
        )         // The width and height of the cropped square

        // Scale the cropped square bitmap to a smaller size (circle shape)
        val previewWidth = 150
        val previewBitmap =
            Bitmap.createScaledBitmap(croppedBitmap, previewWidth, previewWidth, false)

        // Compress the bitmap and encode it to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
    }


    // Result handler for image picker
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUrl ->
                try {
                    val inputStream = contentResolver.openInputStream(imageUrl)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageView5.setImageBitmap(bitmap)
                    encodedImage = encodeImage(bitmap)
                    binding.addImage.visibility = View.GONE
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Method to save profile to Firestore
    private fun saveProfile() {
        val userId =
            userPreference.getString(Constant.KEY_USER_ID) // Assuming you have a way to get the current user ID
        val nama = binding.etNamaLengkap.text.toString()
        val userEmail = binding.etEmail.text.toString()

        if (nama.isEmpty() || userEmail.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Data to be updated in Firestore
        val userUpdates = hashMapOf<String, Any>(
            "nama" to nama,
            "email" to userEmail,
        )

        // Add encoded image to the update if available
        encodedImage?.let {
            userUpdates[Constant.KEY_IMAGE] = it
        }

        // Update Firestore document with user data
        if (userId != null) {
            firestore.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener {
                    lifecycleScope.launch {
                        showAlertDialogAwait("Sukses", "Berhasil mengedit data profile")
                        val intent = Intent(this@EditProfileActivity, BottomNavigationActivity::class.java)
                        startActivity(intent)
                    }

                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed to update profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    private suspend fun showAlertDialogAwait(title: String, subtitle: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(subtitle)
            val dialog = builder.create()
            dialog.setOnCancelListener {
                continuation.resume(Unit)  // Resume when the dialog is canceled
            }
            dialog.show()
        }
    }
}

