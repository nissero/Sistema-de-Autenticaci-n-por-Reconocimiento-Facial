package com.biogin.myapplication.ui.rrhh.categorias

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biogin.myapplication.databinding.FragmentCategoriasBinding
import com.biogin.myapplication.utils.CategoriesUtils
import com.biogin.myapplication.utils.DialogUtils
import com.biogin.myapplication.utils.PopUpUtils
import com.biogin.myapplication.utils.StringUtils

class CategoriasFragment : Fragment() {

    private var _binding: FragmentCategoriasBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var categoriesUtils = CategoriesUtils()
    private val dialogUtils = DialogUtils()
    private val popUpUtils = PopUpUtils()
    private val stringUtils = StringUtils()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonConsultarCategorias.setOnClickListener {
            var text = "Las categorías son:\n"
            try {
                for(category in categoriesUtils.getActiveCategories()) {
                    text += category + "\n"
                }
            } catch (e: UninitializedPropertyAccessException) {
                dialogUtils.showDialog(binding.root.context,
                    "Ocurrio un error, intente nuevamente en unos instantes")
                return@setOnClickListener
            }
            dialogUtils.showDialog(binding.root.context, text)
        }

        binding.buttonAgregarCategoria.setOnClickListener {
            val intent = Intent(root.context, ABMCategoryActivity::class.java)

            intent.putExtra("type", "add")

            startActivity(intent)
        }

        val input = binding.modificarCategoriaInput

        binding.buttonModificarCategoria.setOnClickListener {
            val categoryName =
                stringUtils.normalizeAndSentenceCase(input.text.toString())

            if(categoryName.isEmpty()) {
                dialogUtils.showDialog(binding.root.context, "El campo no puede estar vacío")
                return@setOnClickListener
            }

            if(categoryName == "Seguridad" || categoryName == "Administrador" ||
                categoryName == "Admin" ||categoryName == "Jerarquico" || categoryName == "Rrhh"
                || categoryName == "Rr.hh" || categoryName == "Recursos humanos") {
                dialogUtils.showDialog(binding.root.context,
                    "Las siguientes categorías no pueden ser modificadas:\nAdministrador\n" +
                            "Seguridad\nJerarquico\nRRHH")
                return@setOnClickListener
            }

            val category = categoriesUtils.getCategoryFromName(categoryName)

            val categoryIsInactive = categoriesUtils.getInactiveCategories().contains(categoryName)

            if(category == null && !categoryIsInactive) {
                dialogUtils.showDialog(binding.root.context, "No existe una categoría\n" +
                        "de nombre $categoryName")
                return@setOnClickListener
            }

            if(categoryIsInactive) {
                val onYesFunction = {
                    categoriesUtils.activateCategory(categoryName)
                    popUpUtils.showPopUp(binding.root.context,
                        "Categoría $categoryName restaurada", "Cerrar")
                    binding.modificarCategoriaInput.text.clear()
                }
                val onNoFunction = {
                    dialogUtils.showDialog(binding.root.context,
                        "Elija otro nombre")
                }
                dialogUtils.showDialogWithTwoFunctionOnClose(binding.root.context,
                    "Ya existe una categoría de nombre $categoryName que se encuentra " +
                            "inactiva, desea restaurarla?\nTenga en cuenta que las personas " +
                            "que poseían dicha categoría no seran reactivadas.",
                    onYesFunction, onNoFunction)
                return@setOnClickListener
            }

            if (category != null) {
                val intent = Intent(root.context, ABMCategoryActivity::class.java)

                intent.putExtra("type", "modify")
                intent.putExtra("name", categoryName)
                intent.putExtra("temporary", category.isTemporary)
                intent.putExtra("institute", category.allowsInstitutes)

                startActivity(intent)
            }
        }

        binding.buttonDesactivarCategoria.setOnClickListener {
            val categoryName = stringUtils.normalizeAndSentenceCase(input.text.toString())

            if(categoryName.isEmpty()) {
                dialogUtils.showDialog(binding.root.context, "El campo no puede estar vacío")
                return@setOnClickListener
            }

            if(categoryName == "Seguridad" || categoryName == "Administrador" ||
                categoryName == "Admin" ||categoryName == "Jerarquico" || categoryName == "Rrhh" ||
                categoryName == "Rr.hh" || categoryName == "Recursos humanos") {
                dialogUtils.showDialog(binding.root.context,
                    "Las siguientes categorías no pueden ser desactivadas:\nAdministrador\n" +
                            "Seguridad\nJerarquico\nRRHH")
                return@setOnClickListener
            }

            val categories = categoriesUtils.getActiveCategories()

            if(!categories.contains(categoryName)) {
                dialogUtils.showDialog(binding.root.context, "No existe una categoría\n" +
                        "de nombre $categoryName")
                return@setOnClickListener
            }

            val onYesFunction = {
                categoriesUtils.deactivateCategory(categoryName)

                popUpUtils.showPopUp(binding.root.context, "La categoría $categoryName ha sido " +
                        "desactivada exitosamente", "Cerrar")
                input.text.clear()
            }

            val onNoFunction = {
                dialogUtils.showDialog(binding.root.context,
                    "Elija otro nombre")
            }

            dialogUtils.showDialogWithTwoFunctionOnClose(binding.root.context,
                "Esta seguro que desea desactivar la categoría $categoryName?\n" +
                        "Esta acción inactivará cualquier persona que posea dicha categoría",
                onYesFunction, onNoFunction)
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