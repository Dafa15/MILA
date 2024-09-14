package com.example.mila.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mila.constant.Constant
import com.example.mila.databinding.ActivitySignUpBinding
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userPreference = UserPreference(applicationContext)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setListeners()
    }
    private fun signUp() {
        loading(true)
        val db = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any>()
        user[Constant.KEY_NAME] = binding.nameEditText.text.toString()
        user[Constant.KEY_EMAIL] = binding.emailEditText.text.toString()
        user[Constant.KEY_PASSWORD] = binding.passwordEditText.text.toString()
        db.collection(Constant.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener{
                loading(false)
                val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .addOnFailureListener{exception ->
                loading(false)
                showToast(exception.message.toString())
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignUp(): Boolean {
        if (binding.nameEditText.text?.isEmpty() == true) {
            showToast("Enter name")
            return false
        } else if (binding.emailEditText.text?.isEmpty() == true) {
            showToast("Enter email")
            return false
        }
         else if (binding.passwordEditText.text?.isEmpty() == true) {
             showToast("Enter password")
            return false
        }
        else {
            return true
        }
    }

    private  fun setListeners() {
        binding.daftarButton.setOnClickListener {
            if (isValidSignUp()) {
                signUp()
            }
        }
        binding.masukAkun.setOnClickListener{
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}