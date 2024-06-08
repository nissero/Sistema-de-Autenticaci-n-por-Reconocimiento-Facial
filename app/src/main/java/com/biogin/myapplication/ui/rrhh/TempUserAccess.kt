package com.biogin.myapplication.ui.rrhh

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.utils.DatePickerDialog
import com.biogin.myapplication.utils.PopUpUtils
import java.util.Calendar

class TempUserAccess : AppCompatActivity() {

    private lateinit var fechaDesdeEditText: EditText
    private lateinit var fechaHastaEditText: EditText
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var continueButton: Button

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

        val recyclerView: RecyclerView = findViewById(R.id.options_recycler_view)

        options = mutableListOf()

        for (i in 0..15){
            options.add(i, Option("Option $i"))
        }

        // Create and set the adapter
        adapter = OptionsAdapter(options)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fechaDesdeEditText.setOnClickListener{
            datePickerDialog.showDatePickerDialog(fechaDesdeEditText, fechaHastaEditText, System.currentTimeMillis(), this) { enableButton() }
        }

        fechaHastaEditText.setOnClickListener {
            datePickerDialog.showDatePickerDialog(fechaHastaEditText, null, fechaDesdeEditText.text.toString().toCalendarDate().timeInMillis, this) { enableButton() }
        }

        continueButton.setOnClickListener {
            val selectedOptions = getSelectedOptions()
            // Do something with the selected options
            Toast.makeText(this, "Selected options: $selectedOptions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedOptions(): List<String> {
        return options.filter { it.isSelected }.map { it.name }
    }

    private fun enableButton(){
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