package com.biogin.myapplication.ui.rrhh.modulos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.text.toUpperCase
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.databinding.FragmentModulosBinding
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.DialogUtil
import com.biogin.myapplication.utils.PopUpUtil
import java.util.Locale

class ModulosFragment : Fragment() {

    private var _binding: FragmentModulosBinding? = null
    private val areasUtils = AllowedAreasUtils()
    private val dialogUtil = DialogUtil()

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
            val area = binding.modificarInput.text.toString().uppercase()
            if(area.isNotEmpty()) {
                val arrayInstitutes = areasUtils.getInstitutesFromArea(area)
                if(arrayInstitutes.isNotEmpty()) {
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
                } else {
                    dialogUtil.showDialog(binding.root.context, "No existe un lugar físico\n" +
                            "de nombre $area")
                    return@setOnClickListener
                }
            } else {
                dialogUtil.showDialog(binding.root.context, "El campo no puede estar vacío")
                return@setOnClickListener
            }
        }

        binding.eliminarButton.setOnClickListener {
            val area = binding.modificarInput.text.toString().uppercase()
            if(area.isNotEmpty()) {
                val arrayInstitutes = areasUtils.getInstitutesFromArea(area)
                if(arrayInstitutes.isNotEmpty()) {
                    for (institute in arrayInstitutes) {
                        areasUtils.removeAreaFromInstitute(institute, area)
                    }
                } else {
                    dialogUtil.showDialog(binding.root.context, "No existe un lugar físico\n" +
                            "de nombre $area")
                    return@setOnClickListener
                }
            } else {
                dialogUtil.showDialog(binding.root.context, "El campo no puede estar vacío")
                return@setOnClickListener
            }
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
            dialogUtil.showDialog(binding.root.context, dialogText)
        } catch(e: NullPointerException) {
            try {
                Thread.sleep(5000)
                val areas = areasUtils.getAllowedAreas(arr)
                for(area in areas) {
                    dialogText += area
                    dialogText += ",\n"
                }
                dialogText = dialogText.subSequence(0, dialogText.length - 2).toString()
                dialogUtil.showDialog(binding.root.context, dialogText)
            } catch(e: Exception) {
                dialogUtil.showDialog(binding.root.context,
                    "No se pudo obtener la información en este momento\npruebe mas tarde")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}