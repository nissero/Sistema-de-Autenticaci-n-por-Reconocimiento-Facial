package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.databinding.ActivityHomeBinding
import com.biogin.myapplication.ui.login.RegisterActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewBinding.viewTextRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        viewBinding.buttonFaceRecognition.setOnClickListener {
            startActivity(Intent(this, FaceRecognitionActivity::class.java))
        }
    }
}