package com.biogin.myapplication.ui.rrhh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.Result
import com.biogin.myapplication.utils.DatePickerDialog
import com.biogin.myapplication.utils.PopUpUtils
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class TempUserAccess : AppCompatActivity() {

    private lateinit var fechaDesdeEditText: EditText
    private lateinit var fechaHastaEditText: EditText
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var continueButton: Button

    private var fechaDesdeWasSetted: Boolean = false

    private lateinit var dataSource: LoginDataSource

    private lateinit var options: MutableList<Option>
    private lateinit var adapter: OptionsAdapter

    private lateinit var dniUser: String

    private val popUpUtil = PopUpUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_temp_user_access)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dniUser = intent.getStringExtra("dniUser").toString()

        datePickerDialog = DatePickerDialog()

        fechaDesdeEditText = findViewById(R.id.register_fecha_desde)
        fechaHastaEditText = findViewById(R.id.register_fecha_hasta)

        continueButton = findViewById(R.id.continue_button)

        dataSource = LoginDataSource()

        val recyclerView: RecyclerView = findViewById(R.id.options_recycler_view)



        var result: Result<MutableList<String>>
        runBlocking {
            lifecycleScope.launch {
                result = dataSource.getLugares(dniUser)
                if (result is Result.Success) {
                    options = mutableListOf()

                    val lugares = (result as Result.Success<MutableList<String>>).data
                    Log.d("getLugares", "LUGARES NO ACCEDIBLES X USUARIO: $lugares")
                    for ((index, lugar) in lugares.withIndex()){
                        options.add(index, Option(lugar))
                    }

                    adapter = OptionsAdapter(options) {enableButton()}

                    recyclerView.layoutManager = LinearLayoutManager(this@TempUserAccess)
                    recyclerView.adapter = adapter
                } else {
                    Log.e("getLugares", "error")
                }
            }
        }

        // Create and set the adapter



        fechaDesdeEditText.setOnClickListener{
            datePickerDialog.showDatePickerDialog(fechaDesdeEditText, fechaHastaEditText, System.currentTimeMillis(), this) { enableButton() }
            fechaDesdeWasSetted = true
        }

        fechaHastaEditText.setOnClickListener {
            datePickerDialog.showDatePickerDialog(fechaHastaEditText, null, fechaDesdeEditText.text.toString().toCalendarDate().timeInMillis, this) { enableButton() }
        }

        continueButton.setOnClickListener {
            val optionsSelected = getSelectedOptions()
            dataSource.addTemportalUserAccessToLugares(
                dniUser,
                datePickerDialog.formatDate(fechaDesdeEditText.text.toString()),
                datePickerDialog.formatDate(fechaHastaEditText.text.toString()),
                optionsSelected
            ).addOnSuccessListener {
                popUpUtil.showPopUp(this, "El usuario tendrá acceso a $optionsSelected desde ${fechaDesdeEditText.text} hasta ${fechaHastaEditText.text}", "Salir")
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

    private fun getSelectedOptions(): List<String> {
        return options.filter { it.isSelected }.map { it.name }
    }

    private fun enableButton(){
        if (!fechaHastaEditText.isShown){
            fechaHastaEditText.visibility = View.VISIBLE
        }
        if (fechaDesdeWasSetted){
            fechaHastaEditText.setText("")
            fechaDesdeWasSetted = false
        }
        continueButton.isEnabled = fechaHastaEditText.text.toString().isNotEmpty() && fechaDesdeEditText.text.toString().isNotEmpty() && adapter.isAnyOptionSelected()
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