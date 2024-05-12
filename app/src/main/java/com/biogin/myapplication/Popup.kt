package com.biogin.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.databinding.ActivityPopupBinding

class Popup : AppCompatActivity() {
    private lateinit var binding: ActivityPopupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPopupBinding.inflate(layoutInflater)
        binding.buttonPopup.text = intent.getStringExtra("text_button")
        binding.textPopup.text = intent.getStringExtra("popup_text")
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.buttonPopup.setOnClickListener {
            this.finish()
        }
    }

}