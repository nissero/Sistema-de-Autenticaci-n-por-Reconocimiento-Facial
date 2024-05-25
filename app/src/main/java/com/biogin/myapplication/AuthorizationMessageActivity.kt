package com.biogin.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AuthorizationMessageActivity : AppCompatActivity() {

    private lateinit var authorizationMessageTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authorization_message)

        authorizationMessageTextView = findViewById(R.id.authorization_message)

        val buttonContinuar = findViewById<Button>(R.id.button_continuar)

        // Get the authorization result, DNI, and apellido from the intent
        val authorizationResult = intent.getStringExtra("authorizationResult")
        val typeOfLogIn = intent.getStringExtra("typeOfLogIn")
        val dni = intent.getStringExtra("dni")
        val apellido = intent.getStringExtra("apellido")

        val message: SpannableString
        val colorResource: Int

        if (authorizationResult == "authorized") {
            message = SpannableString("ACCESO AUTORIZADO\n" +
                    "DNI: $dni \n APELLIDO: $apellido")
            message.setSpan(ForegroundColorSpan(Color.GREEN), 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // Green for "ACCESO AUTORIZADO"
            colorResource = R.color.black  // Black for details
        } else {
            message = SpannableString("ACCESO DENEGADO")
            message.setSpan(ForegroundColorSpan(Color.RED), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // Red for "ACCESO DENEGADO"
            colorResource = R.color.black  // Black for details
        }

        authorizationMessageTextView.text = message
        authorizationMessageTextView.setTextColor(resources.getColor(colorResource))

        buttonContinuar.setOnClickListener {
            if (typeOfLogIn == "visitor"){
                finish()
            } else {
                if (authorizationResult == "authorized"){
                    val dniMaster = intent.getStringExtra("dni")
                    val intent = Intent(this, SeguridadActivity::class.java)
                    intent.putExtra("dniMaster", dniMaster)
                    startActivity(intent)
                    finish()
                }
                else{
                    finish()
                }
            }
        }
    }
}