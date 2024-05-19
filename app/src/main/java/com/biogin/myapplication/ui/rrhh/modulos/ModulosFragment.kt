package com.biogin.myapplication.ui.rrhh.modulos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.Popup
import com.biogin.myapplication.databinding.FragmentModulosBinding
import com.biogin.myapplication.utils.AllowedAreasUtils
import okhttp3.internal.wait

class ModulosFragment : Fragment() {

    private var _binding: FragmentModulosBinding? = null
    private val areasUtils = AllowedAreasUtils()

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

        return root
    }

    private fun getAreas(instituteName: String) {
        val arr = ArrayList<String>()
        arr.add(instituteName)
        var popUpText = ""
        var buttonText = "Cerrar"
        try {
            val areas = areasUtils.getAllowedAreas(arr)
            for(area in areas) {
                popUpText += area
                popUpText += "\n"
            }
            popUp(popUpText, buttonText)
        } catch(e: NullPointerException) {
            Thread.sleep(5000)
            val areas = areasUtils.getAllowedAreas(arr)
            for(area in areas) {
                popUpText += area
                popUpText += "\n"
            }
            popUp(popUpText, buttonText)
        }
    }

    private fun popUp(popUpText: String, buttonText: String) {
        val intent = Intent(binding.root.context, Popup::class.java)
        intent.putExtra("popup_text", popUpText)
        intent.putExtra("text_button", buttonText)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}