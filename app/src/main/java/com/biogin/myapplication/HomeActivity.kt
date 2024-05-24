package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.ActivityHomeBinding


class HomeActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MasterUserDataSession.clearUserData()
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewBinding.buttonFaceRecognition.setOnClickListener {
            val intent = Intent(this, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "seguridad")
            startActivity(intent)
        }
        viewBinding.buttonFaceRecognitionRrhh.setOnClickListener {
            val intent = Intent(this, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "rrhh")
            startActivity(intent)
        }
    }
}