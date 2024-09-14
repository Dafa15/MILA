//package com.example.mila.ui.profile
//
//import android.app.Activity
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import com.example.mila.databinding.FragmentEditProfileBinding
//import com.example.mila.databinding.FragmentProfileBinding
//import com.example.mila.ui.LoginActivity
//import com.example.mila.util.UserPreference
//import java.io.ByteArrayOutputStream
//import java.io.FileNotFoundException
//import java.io.InputStream
//import kotlin.io.encoding.Base64
//
//class EditProfileFragment : Fragment() {
//
//    private var _binding: FragmentEditProfileBinding? = null
//    private lateinit var userPreference: UserPreference
//    private var encodedImage: String? = null
//
//
//    // This property is only valid between onCreateView and
//    // onDestroyView.
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        userPreference = UserPreference(requireContext())
//        val profileViewModel =
//            ViewModelProvider(this).get(ProfileViewModel::class.java)
//
//        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//
//        binding.imageView5.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            pickImage.launch(intent)
//        }
//
//        return root
//    }
//
//    private fun encodeImage(bitmap: Bitmap): String {
//        val previewWidth = 150
//        val previewHeight = bitmap.height * previewWidth / bitmap.width
//        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
//        val byteArrayOutputStream = ByteArrayOutputStream()
//        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
//        val bytes = byteArrayOutputStream.toByteArray()
//        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
//    }
//
//    private val pickImage = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            result.data?.data?.let { imageUrl ->
//                try {
//                    val inputStream = requireContext().contentResolver.openInputStream(imageUrl)
//                    val bitmap = BitmapFactory.decodeStream(inputStream)
//                    binding.imageView5.setImageBitmap(bitmap)
//                    encodedImage = encodeImage(bitmap)
//                } catch (e: FileNotFoundException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}