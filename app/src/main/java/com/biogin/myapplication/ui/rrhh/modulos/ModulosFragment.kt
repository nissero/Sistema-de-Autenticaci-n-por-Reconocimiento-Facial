package com.biogin.myapplication.ui.rrhh.modulos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.databinding.FragmentModulosBinding
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.DialogUtils

class ModulosFragment : Fragment() {

    private var _binding: FragmentModulosBinding? = null
    private var areasUtils = AllowedAreasUtils()
    private val dialogUtils = DialogUtils()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(ModulosViewModel::class.java)

        _binding = FragmentModulosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.areasIci.setOnClickListener {
            getAreas("ICI")
        }
        binding.areasIco.setOnClickListener {
            getAreas("ICO")
        }
        binding.areasIdei.setOnClickListener {
            getAreas("IDEI")
        }
        binding.areasIdh.setOnClickListener {
            getAreas("IDH")
        }

        binding.agregarButton.setOnClickListener {
            val intent = Intent(binding.root.context, ABMAreaActivity::class.java)
            intent.putExtra("type", "add")
            startActivity(intent)
        }

        binding.modificarButton.setOnClickListener {
            val area = binding.modificarInput.text.toString().lowercase().replaceFirstChar(Char::titlecase)

            if(area.isEmpty()) {
                dialogUtils.showDialog(binding.root.context, "El campo no puede estar vacío")
                return@setOnClickListener
            }

            val arrayInstitutes = areasUtils.getInstitutesFromActiveArea(area)
            val areaIsInactive = areasUtils.getAllInactiveAreas().contains(area)

            if(arrayInstitutes.isEmpty() && !areaIsInactive) {
                dialogUtils.showDialog(binding.root.context, "No existe un lugar físico\n" +
                        "de nombre $area")
                return@setOnClickListener
            }

            if(areaIsInactive) {
                val onYesFunction = {
                    areasUtils.activateArea(area)
                    dialogUtils.showDialogWithFunctionOnClose(binding.root.context,
                        "Lugar físico $area restaurado") {
                    }
                }
                val onNoFunction = {
                    dialogUtils.showDialog(binding.root.context,
                        "Elija otro nombre")
                }
                dialogUtils.showDialogWithTwoFunctionOnClose(binding.root.context,
                    "Ya existe un lugar físico de nombre $area que se encuentra " +
                            "inactivo, desea restaurarlo?", onYesFunction, onNoFunction)
                return@setOnClickListener
            }

            val intent = Intent(binding.root.context, ABMAreaActivity::class.java)
            intent.putExtra("type", "modify")
            intent.putExtra("name", area)
            if(arrayInstitutes.contains("ICI"))
                intent.putExtra("ICI", true)
            else intent.putExtra("ICI", false)
            if(arrayInstitutes.contains("ICO"))
                intent.putExtra("ICO", true)
            else intent.putExtra("ICO", false)
            if(arrayInstitutes.contains("IDEI"))
                intent.putExtra("IDEI", true)
            else intent.putExtra("IDEI", false)
            if(arrayInstitutes.contains("IDH"))
                intent.putExtra("IDH", true)
            else intent.putExtra("IDH", false)

            startActivity(intent)
        }

        binding.eliminarButton.setOnClickListener {
            val area = binding.modificarInput.text.toString().lowercase().replaceFirstChar(Char::titlecase)

            if(area.isEmpty()) {
                dialogUtils.showDialog(binding.root.context, "El campo no puede estar vacío")
                return@setOnClickListener
            }

            val areas = areasUtils.getAllActiveAreas()

            if(!areas.contains(area)) {
                dialogUtils.showDialog(binding.root.context, "No existe un lugar físico\n" +
                        "de nombre $area")
                return@setOnClickListener
            }

            areasUtils.deactivateArea(area)

            dialogUtils.showDialog(binding.root.context, "$area ha sido eliminado exitosamente")
            binding.modificarInput.text.clear()
            areasUtils = AllowedAreasUtils()
        }

        return root
    }

    private fun getAreas(instituteName: String) {
        val arr = ArrayList<String>()
        arr.add(instituteName)
        var dialogText = ""
        try {
            val areas = areasUtils.getAllowedAreas(arr)
            for(area in areas) {
                dialogText += area
                dialogText += ",\n"
            }
            dialogText = dialogText.subSequence(0, dialogText.length - 2).toString()
            dialogUtils.showDialog(binding.root.context, dialogText)
        } catch(e: NullPointerException) {
            try {
                Thread.sleep(5000)
                val areas = areasUtils.getAllowedAreas(arr)
                for(area in areas) {
                    dialogText += area
                    dialogText += ",\n"
                }
                dialogText = dialogText.subSequence(0, dialogText.length - 2).toString()
                dialogUtils.showDialog(binding.root.context, dialogText)
            } catch(e: Exception) {
                dialogUtils.showDialog(binding.root.context,
                    "No se pudo obtener la información en este momento\npruebe mas tarde")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        areasUtils = AllowedAreasUtils()
    }
}