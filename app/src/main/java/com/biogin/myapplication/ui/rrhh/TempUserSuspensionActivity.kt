package com.biogin.myapplication.ui.rrhh

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.utils.DatePickerDialog
import com.biogin.myapplication.utils.PopUpUtils
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Calendar

class TempUserSuspensionActivity : AppCompatActivity() {

    private var fechaDesdeWasSetted: Boolean = false
    private lateinit var fechaDesdeEditText: EditText
    private lateinit var fechaHastaEditText: EditText
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var continueButton: Button

    private lateinit var dataSource: LoginDataSource

    private lateinit var dniUser: String

    private val popUpUtil = PopUpUtils()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_temp_user_suspension)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dniUser = intent.getStringExtra("dniUser").toString()

        dataSource = LoginDataSource()

        datePickerDialog = DatePickerDialog()

        fechaDesdeEditText = findViewById(R.id.register_fecha_desde)
        fechaHastaEditText = findViewById(R.id.register_fecha_hasta)


        continueButton = findViewById(R.id.continue_button)

        fechaDesdeEditText.setOnClickListener{
            datePickerDialog.showDatePickerDialog(fechaDesdeEditText, fechaHastaEditText, System.currentTimeMillis(), this) { enableButton() }
            fechaDesdeWasSetted = true
        }

        fechaHastaEditText.setOnClickListener {
            datePickerDialog.showDatePickerDialog(fechaHastaEditText, null, fechaDesdeEditText.text.toString().toCalendarDate().timeInMillis, this) { enableButton() }
        }

        continueButton.setOnClickListener {
            dataSource.disableForSomeTime(
                dniUser,
                datePickerDialog.formatDate(fechaDesdeEditText.text.toString()),
                datePickerDialog.formatDate(fechaHastaEditText.text.toString()))
                .addOnSuccessListener {
                    if (dataSource.ifSuspensionDateEqualsToday(fechaDesdeEditText.text.toString(), dniUser)){
                        Log.d("suspensionUsuario", "BIEN")
                        popUpUtil.showPopUp(this,
                        "El usuario se suspendera desde ${fechaDesdeEditText.text} hasta ${fechaHastaEditText.text} con efecto inmediato",
                            "Salir")
                    } else{
                        popUpUtil.showPopUp(this,
                            "El usuario se suspendera desde ${fechaDesdeEditText.text} hasta ${fechaHastaEditText.text}",
                            "Salir")
                    }
                    finish()
            }.addOnFailureListener { ex ->
                try {
                    throw ex
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                        popUpUtil.showPopUp(this,
                            "USUARIO NO ENCONTRADO",
                            "Reintentar")
                        finish()
                    } else if (e.code == FirebaseFirestoreException.Code.INVALID_ARGUMENT) {
                        popUpUtil.showPopUp(this,
                            "USUARIO NO ENCONTRADO",
                            "Reintentar")
                        finish()
                    }
                }
            }
        }
    }

    private fun enableButton(){
        if (!fechaHastaEditText.isShown){
            fechaHastaEditText.visibility = View.VISIBLE
        }
        if (fechaDesdeWasSetted){
            fechaHastaEditText.setText("")
            fechaDesdeWasSetted = false
        }
        continueButton.isEnabled = fechaHastaEditText.text.toString().isNotEmpty() && fechaDesdeEditText.text.toString().isNotEmpty()
    }

    private fun String.toCalendarDate(): Calendar {
        val splitDate = split("/")
        val year = splitDate[2].toInt()
        val month = splitDate[1].toInt() - 1 // Month in Calendar is 0-based
        val day = splitDate[0].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar
    }


}