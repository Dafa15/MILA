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
import com.example.mila.databinding.ActivityLoginBinding
import com.example.mila.util.UserPreference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(applicationContext)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setListeners()
    }

    private fun login() {
        loading(true)
        val db = FirebaseFirestore.getInstance()
        db.collection(Constant.KEY_COLLECTION_USERS)
            .whereEqualTo(Constant.KEY_EMAIL, binding.emailEditText.text.toString())
            .whereEqualTo(Constant.KEY_PASSWORD, binding.passwordEditText.text.toString())
            .get()
            .addOnCompleteListener{task ->
                if (task.isSuccessful && task.result != null && task.result.size() > 0) {
                    val documentSnapshot: DocumentSnapshot = task.result.documents[0]
                    userPreference.putBoolean(Constant.KEY_IS_SIGNED_IN, true)
                    userPreference.putString(Constant.KEY_USER_ID, documentSnapshot.id)
                    documentSnapshot.getString(Constant.KEY_NAME)
                        ?.let { userPreference.putString(Constant.KEY_NAME, it) }
                    documentSnapshot.getString(Constant.KEY_EMAIL)
                        ?.let { userPreference.putString(Constant.KEY_EMAIL, it) }
                    documentSnapshot.getString(Constant.KEY_PASSWORD)
                        ?.let { userPreference.putString(Constant.KEY_PASSWORD, it) }
                    loading(false)
                    lifecycleScope.launch {
                        showAlertDialogAwait("Sukses", "Berhasil login, silakan konsultasi dengan MILA!")
                        val intent = Intent(this@LoginActivity, BottomNavigationActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }else {
                    loading(false)
                    showToast("Unable to login")
                }
            }.addOnFailureListener {
                loading(false)
                showToast("Failed to login")
            }
    }

    private fun isValidSignIn(): Boolean {
        if (binding.emailEditText.text?.isEmpty() == true) {
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

    private fun setListeners() {
        binding.loginButton.setOnClickListener {
            if (isValidSignIn()){
                login()
            }
        }
        binding.daftarAkun.setOnClickListener{
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
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

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
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