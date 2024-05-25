package com.biogin.myapplication.ui.rrhh.modulos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.ActivityAbmAreaBinding
import com.biogin.myapplication.ui.LoadingDialog
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.DialogUtil

class ABMAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbmAreaBinding
    private val dialogUtil = DialogUtil()
    private val areasUtil = AllowedAreasUtils()
    private val loadingUtil = LoadingDialog(this)
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
            val areaName = input.text.toString().uppercase()
            if(areaName.isEmpty()) {
                dialogUtil.showDialog(binding.root.context,
                    "El campo de nombre no puede estar vacío")
                return@setOnClickListener
            }

            if(!checkboxICI.isChecked && !checkboxIDEI.isChecked &&
                !checkboxICO.isChecked && !checkboxIDH.isChecked) {
                dialogUtil.showDialog(binding.root.context,
                    "Debe seleccionar al menos un instituto")
                return@setOnClickListener
            }

            if(intent.getStringExtra("type") == "add") {
                if(checkIfAreaExists(areaName)) {
                    dialogUtil.showDialog(binding.root.context,
                        "Ya existe un lugar físico de nombre $areaName, para modificarlo\n" +
                                "debe ir a la sección previa y seguir las instrucciones")
                    return@setOnClickListener
                }

                val message =
                    addAreaAndReturnMessage(areaName, checkboxICI.isChecked, checkboxICO.isChecked,
                        checkboxIDH.isChecked, checkboxIDEI.isChecked)
                dialogUtil.showDialogWithFunctionOnClose(binding.root.context, message) {
                    finish()
                }
            } else if(intent.getStringExtra("type") == "modify") {
                remove(intent.getStringExtra("name").toString())
                Thread.sleep(2000)
                addAreaAndReturnMessage(areaName, checkboxICI.isChecked, checkboxICO.isChecked,
                    checkboxIDH.isChecked, checkboxIDEI.isChecked)
                val message = "$areaName ha sido modificado exitosamente"
                dialogUtil.showDialogWithFunctionOnClose(binding.root.context, message) {
                    finish()
                }
            }
        }

    }

    private fun addAreaAndReturnMessage(name: String, ici: Boolean, ico: Boolean,
                                        idh: Boolean, idei: Boolean): String {
        var text = "$name se agregó a los siguientes institutos:\n"
        if (ici) {
            areasUtil.addAreaToInstitute("ICI", name)
            text += "ICI\n"
        }
        if (ico) {
            areasUtil.addAreaToInstitute("ICO", name)
            text += "ICO\n"
        }
        if (idh) {
            areasUtil.addAreaToInstitute("IDH", name)
            text += "IDH\n"
        }
        if (idei) {
            areasUtil.addAreaToInstitute("IDEI", name)
            text += "IDEI"
        }

        return text
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