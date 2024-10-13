package com.example.mila.ui.profile

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mila.R
import com.example.mila.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = Color.WHITE

        binding.backAppHelp.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    fun toggleAnswer(view: View) {
        // Mengambil parent layout (LinearLayout) dari TextView yang diklik
        val parent = view.parent as ViewGroup

        // Mencari posisi TextView yang diklik (pertanyaan)
        val index = parent.indexOfChild(view)

        // Mengambil TextView jawaban yang terletak tepat setelah TextView pertanyaan
        // Pastikan index + 1 tidak melebihi jumlah child
        if (index + 1 < parent.childCount) {
            val answerTextView = parent.getChildAt(index + 1) as? TextView

            // Mengubah visibility dari TextView jawaban jika tidak null
            answerTextView?.let {
                it.visibility = if (it.visibility == View.GONE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }


}