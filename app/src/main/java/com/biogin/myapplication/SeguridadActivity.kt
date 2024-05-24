package com.biogin.myapplication

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.biogin.myapplication.databinding.ActivitySeguridadBinding

class SeguridadActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguridadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySeguridadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dniMaster = intent.getStringExtra("dniMaster").toString()
        Log.d("SEGURIDAD", dniMaster)
        val bundle = Bundle()
        bundle.putString("dniMaster", dniMaster)



        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_admin_autenticacion)
        navController.navigate(R.id.navigation_autenticacion, bundle)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_autenticacion, R.id.navigation_logs, R.id.navigation_acerca
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }
}