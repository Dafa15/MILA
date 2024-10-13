package com.example.mila.ui.profile

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mila.R
import com.example.mila.databinding.ActivityAppInfoBinding

class AppInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppInfoBinding.inflate(layoutInflater)
        window.statusBarColor = Color.WHITE

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val textView: TextView = binding.appInfoText
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
        }

        binding.backAppInfo.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}