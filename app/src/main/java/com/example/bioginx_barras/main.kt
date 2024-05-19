package com.example.bioginx_barras

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class main : AppCompatActivity() {
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        resultTextView = findViewById(R.id.textMain)
        val offlineButton: Button = findViewById(R.id.starOffline)
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)

        // Habria que implementar un metodo que verifique periodicamente si hay conexion a Internet, esto solo lo hace 1
        if(!isOnlineNet()) {
            builder.setTitle("Pasando a modo Offline")
            builder.setMessage("El programa NO tiene conexion a Internet")
            builder.setPositiveButton("OK", null)
            builder.create()
            builder.show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        offlineButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Metodos para ver si hay Internet
    private fun isOnlineNet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
}