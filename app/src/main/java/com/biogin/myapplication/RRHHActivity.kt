package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.biogin.myapplication.databinding.ActivityRrhhBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class RRHHActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRrhhBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRrhhBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(binding.root.context, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
                finish()
            }
        })

        val navController = findNavController(R.id.nav_host_fragment_activity_rrhhactivity)

        navView.setupWithNavController(navController)
    }
}