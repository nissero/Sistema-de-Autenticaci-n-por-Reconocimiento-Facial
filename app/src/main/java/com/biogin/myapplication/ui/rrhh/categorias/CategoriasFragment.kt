package com.biogin.myapplication.ui.rrhh.categorias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biogin.myapplication.databinding.FragmentCategoriasBinding
import com.biogin.myapplication.utils.CategoriesUtils
import com.biogin.myapplication.utils.DialogUtils

class CategoriasFragment : Fragment() {

    private var _binding: FragmentCategoriasBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var categoriesUtils = CategoriesUtils()
    private val dialogUtils = DialogUtils()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonConsultarCategorias.setOnClickListener {
            var text = "Las categorías son:\n"
            for(category in categoriesUtils.getCategories()) {
                text += category + "\n"
            }
            dialogUtils.showDialog(binding.root.context, text)
        }

        binding.buttonAgregarCategoria.setOnClickListener {
            categoriesUtils.addCategory("docente", false, true, true)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        categoriesUtils = CategoriesUtils()
    }
}