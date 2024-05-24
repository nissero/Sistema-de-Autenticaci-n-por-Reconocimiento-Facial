package com.biogin.myapplication.ui.rrhh.modulos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.ActivityAbmAreaBinding
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.PopUpUtil

class ABMAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbmAreaBinding
    private val popUpUtil = PopUpUtil()
    private val areasUtil = AllowedAreasUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbmAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val type = intent.getStringExtra("type").toString()

        val instructions = binding.instrucciones
        val button = binding.agregarLugarFisico
        val input = binding.nombreInput
        val checkboxICO = binding.checkboxICO
        val checkboxICI = binding.checkboxICI
        val checkboxIDH = binding.checkboxIDH
        val checkboxIDEI = binding.checkboxIDEI

        if(type == "modify") {
            input.setText(intent.getStringExtra("name"))
            if(intent.getBooleanExtra("ICI", false)) {
                checkboxICI.isChecked = true
            }
            if(intent.getBooleanExtra("ICO", false)) {
                checkboxICO.isChecked = true
            }
            if(intent.getBooleanExtra("IDH", false)) {
                checkboxIDH.isChecked = true
            }
            if(intent.getBooleanExtra("IDEI", false)) {
                checkboxIDEI.isChecked = true
            }

            instructions.text = binding.root.context.getString(R.string.texto_ayuda_modificar)
            button.text = binding.root.context.getString(R.string.modificar_lugar_fisico)

        }

        button.setOnClickListener {
            val areaName = input.text.toString()
            if(areaName.isEmpty()) {
                popUpUtil.showPopUp(binding.root.context,
                    "El campo de nombre no puede estar vacío",
                    "Cerrar")
                return@setOnClickListener
            }

            if(!checkboxICI.isChecked && !checkboxIDEI.isChecked &&
                !checkboxICO.isChecked && !checkboxIDH.isChecked) {
                popUpUtil.showPopUp(binding.root.context,
                    "Debe seleccionar al menos un instituto",
                    "Cerrar")
                return@setOnClickListener
            }

            if(intent.getStringExtra("type") == "add") {
                if(!checkIfAreaExists(areaName)) {
                    add(areaName, checkboxICI.isChecked, checkboxICO.isChecked,
                        checkboxIDH.isChecked, checkboxIDEI.isChecked)
                } else {
                    popUpUtil.showPopUp(binding.root.context,
                        "Ya existe un lugar físico de " +
                                "nombre $areaName", "Cerrar")
                    return@setOnClickListener
                }
            } else if(intent.getStringExtra("type") == "modify") {
                remove(intent.getStringExtra("name").toString())
                Thread.sleep(2000)
                add(areaName, checkboxICI.isChecked, checkboxICO.isChecked,
                    checkboxIDH.isChecked, checkboxIDEI.isChecked)
            }


//            var text = "Se agregó ${input.text} a los siguientes institutos:\n"
//
//            if(checkboxICI.isChecked) {
//                areasUtil.addAreaToInstitute("ICI", input.text.toString())
//                checkboxICI.toggle()
//                text += "ICI, "
//            }
//            if(checkboxICO.isChecked) {
//                areasUtil.addAreaToInstitute("ICO", input.text.toString())
//                checkboxICO.toggle()
//                text += "ICO, "
//            }
//            if(checkboxIDH.isChecked) {
//                areasUtil.addAreaToInstitute("IDH", input.text.toString())
//                checkboxIDH.toggle()
//                text += "IDH, "
//            }
//            if(checkboxIDEI.isChecked) {
//                areasUtil.addAreaToInstitute("IDEI", input.text.toString())
//                checkboxIDEI.toggle()
//                text += "IDEI"
//            }
//            Thread.sleep(2000)
//
//            input.text.clear()
//
//            if(text.endsWith(", ")) {
//                text = text.subSequence(0, text.length - 2).toString()
//            }
//
//            popUpUtil.showPopUp(binding.root.context, text, "Cerrar")
        }

    }

    private fun add(name: String, ici: Boolean, ico: Boolean,
                    idh: Boolean, idei: Boolean) {
        if (ici) areasUtil.addAreaToInstitute("ICI", name)
        if (ico) areasUtil.addAreaToInstitute("ICO", name)
        if (idh) areasUtil.addAreaToInstitute("IDH", name)
        if (idei) areasUtil.addAreaToInstitute("IDEI", name)
    }

    private fun remove(name: String) {
        areasUtil.removeAreaFromInstitute("ICI", name)
        areasUtil.removeAreaFromInstitute("ICO", name)
        areasUtil.removeAreaFromInstitute("IDH", name)
        areasUtil.removeAreaFromInstitute("IDEI", name)
    }

    private fun checkIfAreaExists(area: String): Boolean {
        return areasUtil.getAllAreas().contains(area)
    }
}