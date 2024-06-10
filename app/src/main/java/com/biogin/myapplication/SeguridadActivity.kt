package com.biogin.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
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

        val navController = findNavController(R.id.nav_host_fragment_activity_admin_autenticacion)
        navController.navigate(R.id.navigation_autenticacion, bundle)
        navView.setupWithNavController(navController)
    }

    private fun showAuthorizationDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_authorization, null) //

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            alertDialog.dismiss()
        }, 5000)

        alertDialog.window?.decorView?.findViewById<View>(android.R.id.content)?.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}