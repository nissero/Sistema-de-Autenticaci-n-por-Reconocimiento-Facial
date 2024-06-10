package com.biogin.myapplication.ui.rrhh.modulos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.ActivityAbmAreaBinding
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.DialogUtils
import com.biogin.myapplication.utils.StringUtils

class ABMAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbmAreaBinding
    private val dialogUtils = DialogUtils()
    private val areasUtils = AllowedAreasUtils()
    private val stringUtils = StringUtils()
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

            checkboxICI.isChecked = intent.getBooleanExtra("ICI", false)
            checkboxICO.isChecked = intent.getBooleanExtra("ICO", false)
            checkboxIDH.isChecked = intent.getBooleanExtra("IDH", false)
            checkboxIDEI.isChecked = intent.getBooleanExtra("IDEI", false)

            instructions.text = binding.root.context.getString(R.string.texto_ayuda_modificar)
            button.text = binding.root.context.getString(R.string.modificar_lugar_fisico)

        }

        button.setOnClickListener {
            val areaName = stringUtils.normalizeAndSentenceCase(input.text.toString())
            if(areaName.isEmpty()) {
                dialogUtils.showDialog(binding.root.context,
                    "El campo de nombre no puede estar vacío")
                return@setOnClickListener
            }

            if(!checkboxICI.isChecked && !checkboxIDEI.isChecked &&
                !checkboxICO.isChecked && !checkboxIDH.isChecked) {
                dialogUtils.showDialog(binding.root.context,
                    "Debe seleccionar al menos un instituto")
                return@setOnClickListener
            }

            if(intent.getStringExtra("type") == "add") {
                if(checkIfAreaExists(areaName)) {
                    dialogUtils.showDialog(binding.root.context,
                        "Ya existe un lugar físico de nombre $areaName, para modificarlo\n" +
                                "debe ir a la sección previa y seguir las instrucciones")
                    return@setOnClickListener
                }

                if(checkIfAreaIsInactive(areaName)) {
                    val onYesFunction = {
                        areasUtils.activateArea(areaName)
                        dialogUtils.showDialogWithFunctionOnClose(binding.root.context,
                            "Lugar físico $areaName restaurado") {
                            finish()
                        }
                    }
                    val onNoFunction = {
                        dialogUtils.showDialog(binding.root.context,
                            "Elija otro nombre")
                    }
                    dialogUtils.showDialogWithTwoFunctionOnClose(binding.root.context,
                        "Ya existe un lugar físico de nombre $areaName que se encuentra " +
                                "inactivo, desea restaurarlo?", onYesFunction, onNoFunction)
                    return@setOnClickListener
                }

                val message =
                    addAreaAndReturnMessage(areaName, checkboxICI.isChecked, checkboxICO.isChecked,
                        checkboxIDH.isChecked, checkboxIDEI.isChecked)
                dialogUtils.showDialogWithFunctionOnClose(binding.root.context, message) {
                    finish()
                }
            } else if(intent.getStringExtra("type") == "modify") {
                modify(intent.getStringExtra("name").toString(), areaName,
                    checkboxICI.isChecked, checkboxICO.isChecked,
                    checkboxIDH.isChecked, checkboxIDEI.isChecked)
                val message = "$areaName ha sido modificado exitosamente"
                dialogUtils.showDialogWithFunctionOnClose(binding.root.context, message) {
                    finish()
                }
            }
        }

    }

    private fun addAreaAndReturnMessage(name: String, ici: Boolean, ico: Boolean,
                                        idh: Boolean, idei: Boolean): String {
        var text = "$name se agregó a los siguientes institutos:\n"
        if (ici) text += "ICI\n"
        if (ico) text += "ICO\n"
        if (idh) text += "IDH\n"
        if (idei) text += "IDEI"

        areasUtils.addArea(name, ici, ico, idei, idh)

        return text
    }

    private fun modify(oldName: String, newName: String, ici: Boolean, ico: Boolean,
                       idh: Boolean, idei: Boolean) {
        if(oldName != newName) {
            areasUtils.addArea(newName, ici, ico, idei, idh)
            areasUtils.deactivateArea(oldName)
        } else {
            areasUtils.modifyArea(newName, ici, ico, idei, idh)
        }
    }

    private fun checkIfAreaExists(area: String): Boolean {
        return areasUtils.getAllActiveAreas().contains(area)
    }

    private fun checkIfAreaIsInactive(area: String): Boolean {
        return areasUtils.getAllInactiveAreas().contains(area)
    }
}