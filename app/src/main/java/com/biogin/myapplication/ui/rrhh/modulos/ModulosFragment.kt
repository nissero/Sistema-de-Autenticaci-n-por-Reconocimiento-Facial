package com.biogin.myapplication.ui.rrhh.modulos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biogin.myapplication.databinding.FragmentModulosBinding
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.DialogUtils
import com.biogin.myapplication.utils.PopUpUtils
import com.biogin.myapplication.utils.StringUtils

class ModulosFragment : Fragment() {

    private var _binding: FragmentModulosBinding? = null
    private var areasUtils = AllowedAreasUtils()
    private val dialogUtils = DialogUtils()
    private val popUpUtils = PopUpUtils()
    private val stringUtils = StringUtils()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModulosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.areasIci.setOnClickListener {
            try {
                getAreas("ICI")
            } catch (e: UninitializedPropertyAccessException) {
                dialogUtils.showDialog(binding.root.context,
                    "Ocurrio un error, intente nuevamente en unos instantes")
                return@setOnClickListener
            }
        }
        binding.areasIco.setOnClickListener {
            try {
                getAreas("ICO")
            } catch (e: UninitializedPropertyAccessException) {
                dialogUtils.showDialog(binding.root.context,
                    "Ocurrio un error, intente nuevamente en unos instantes")
                return@setOnClickListener
            }
        }
        binding.areasIdei.setOnClickListener {
            try {
                getAreas("IDEI")
            } catch (e: UninitializedPropertyAccessException) {
                dialogUtils.showDialog(binding.root.context,
                    "Ocurrio un error, intente nuevamente en unos instantes")
                return@setOnClickListener
            }
        }
        binding.areasIdh.setOnClickListener {
            try {
                getAreas("IDH")
            } catch (e: UninitializedPropertyAccessException) {
                dialogUtils.showDialog(binding.root.context,
                    "Ocurrio un error, intente nuevamente en unos instantes")
                return@setOnClickListener
            }
        }

        binding.agregarButton.setOnClickListener {
            val intent = Intent(binding.root.context, ABMAreaActivity::class.java)
            intent.putExtra("type", "add")
            startActivity(intent)
        }

        binding.modificarButton.setOnClickListener {
            val area = stringUtils.normalizeAndSentenceCase(binding.modificarInput.text.toString())

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
                    popUpUtils.showPopUp(binding.root.context,
                        "Lugar físico $area restaurado", "Cerrar")
                    binding.modificarInput.text.clear()
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
            intent.putExtra("ICI", arrayInstitutes.contains("ICI"))
            intent.putExtra("ICO", arrayInstitutes.contains("ICO"))
            intent.putExtra("IDEI", arrayInstitutes.contains("IDEI"))
            intent.putExtra("IDH", arrayInstitutes.contains("IDH"))

            startActivity(intent)
        }

        binding.eliminarButton.setOnClickListener {
            val area = stringUtils.normalizeAndSentenceCase(binding.modificarInput.text.toString())

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

            val onYesFunction = {
                areasUtils.deactivateArea(area)

                popUpUtils.showPopUp(binding.root.context,
                    "$area ha sido desactivado exitosamente", "Cerrar")
                binding.modificarInput.text.clear()
            }

            val onNoFunction = {
                dialogUtils.showDialog(binding.root.context,
                    "Elija otro nombre")
            }

            dialogUtils.showDialogWithTwoFunctionOnClose(binding.root.context,
                "Desea desactivar el lugar físico $area?", onYesFunction, onNoFunction)
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