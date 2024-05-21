package com.biogin.myapplication.ui.rrhh.modulos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.ActivityAddAreaBinding
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.PopUpUtil

class AddAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAreaBinding
    private val popUpUtil = PopUpUtil()
    private val areasUtil = AllowedAreasUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_add_area)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = binding.agregarLugarFisico
        val input = binding.nombreInput
        val checkboxICO = binding.checkboxICO
        val checkboxICI = binding.checkboxICI
        val checkboxIDH = binding.checkboxIDH
        val checkboxIDEI = binding.checkboxIDEI

        button.setOnClickListener {
            if(input.text.toString().isEmpty()) {
                println("esta vacio el string")
                popUpUtil.showPopUp(binding.root.context,
                    "El campo de nombre no puede estar vacío",
                    "Cerrar")
                return@setOnClickListener
            }

            if(!checkboxICI.isChecked && !checkboxIDEI.isChecked &&
                !checkboxICO.isChecked && !checkboxIDH.isChecked) {
                println("estan vacios los checks")
                popUpUtil.showPopUp(binding.root.context,
                    "Debe seleccionar al menos un instituto",
                    "Cerrar")
                return@setOnClickListener
            }

            var text = "Se agregó ${input.text} a los siguientes institutos:\n"

            if(checkboxICI.isChecked) {
                areasUtil.addAreaToInstitute("ICI", input.text.toString())
                checkboxICI.toggle()
                text += "ICI, "
            }
            if(checkboxICO.isChecked) {
                areasUtil.addAreaToInstitute("ICO", input.text.toString())
                checkboxICO.toggle()
                text += "ICO, "
            }
            if(checkboxIDH.isChecked) {
                areasUtil.addAreaToInstitute("IDH", input.text.toString())
                checkboxIDH.toggle()
                text += "IDH, "
            }
            if(checkboxIDEI.isChecked) {
                areasUtil.addAreaToInstitute("IDEI", input.text.toString())
                checkboxIDEI.toggle()
                text += "IDEI"
            }
            Thread.sleep(2000)

            input.text.clear()

            if(text.endsWith(", ")) {
                text = text.subSequence(0, text.length - 2).toString()
            }

            popUpUtil.showPopUp(binding.root.context, text, "Cerrar")
        }

    }
}