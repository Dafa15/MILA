package com.example.mila

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mila.constant.Constant
import com.example.mila.databinding.ActivityMainBinding
import com.example.mila.ui.BottomNavigationActivity
import com.example.mila.ui.WelcomeActivity
import com.example.mila.util.UserPreference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install the splash screen
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Delay of 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // Proceed with logic
            val userPreference = UserPreference(applicationContext)
            val isSignedIn = userPreference.getBoolean(Constant.KEY_IS_SIGNED_IN)
            val intent = if (isSignedIn) {
                Intent(this, BottomNavigationActivity::class.java)
            } else {
                Intent(this, WelcomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }, 2000) // 2000 milliseconds = 2 seconds
    }
}
