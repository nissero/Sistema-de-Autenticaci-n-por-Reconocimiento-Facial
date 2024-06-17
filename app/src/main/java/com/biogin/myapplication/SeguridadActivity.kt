package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.ActivitySeguridadBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SeguridadActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguridadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySeguridadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dniMaster = MasterUserDataSession.getDniUser()
        Log.d("SEGURIDAD", "DNI MASTER: $dniMaster")

        val bundle = Bundle()
        bundle.putString("dniMaster", dniMaster)

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

        val navController = findNavController(R.id.nav_host_fragment_activity_admin_autenticacion)
        navController.navigate(R.id.navigation_autenticacion, bundle)
        navView.setupWithNavController(navController)
    }
}