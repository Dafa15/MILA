package com.example.mila.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mila.constant.Constant
import com.example.mila.databinding.ActivitySignUpBinding
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


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
                lifecycleScope.launch {
                    showAlertDialogAwait("Sukses", "Berhasil daftar, silakan masuk ke akun anda!")
                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
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
        val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")
        when {
            binding.nameEditText.text?.isEmpty() == true -> {
                showToast("Masukkan nama!")
                return false
            }
            binding.emailEditText.text?.isEmpty() == true -> {
                showToast("Masukkan email!")
                return false
            }
            !binding.emailEditText.text.toString().matches(emailPattern) -> {
                showToast("Masukkan email yang benar!")
                return false
            }
            binding.passwordEditText.text?.isEmpty() == true -> {
                showToast("Masukkan password!")
                return false
            }
            binding.etPasswordConfirm.text?.isEmpty() == true || binding.etPasswordConfirm.text.toString() != binding.passwordEditText.text.toString() -> {
                showToast("Konfirmasi password tidak sama!")
                return false
            }
        }
        return true
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

    private suspend fun showAlertDialogAwait(title: String, subtitle: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(subtitle)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                continuation.resume(Unit)
            }
            val dialog = builder.create()
            dialog.setOnCancelListener {
                continuation.resume(Unit)  // Resume when the dialog is canceled
            }
            dialog.show()
        }
    }
}