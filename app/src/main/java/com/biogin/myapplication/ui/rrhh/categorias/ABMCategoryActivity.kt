package com.biogin.myapplication.ui.rrhh.categorias

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.ActivityAbmCategoryBinding
import com.biogin.myapplication.utils.CategoriesUtils
import com.biogin.myapplication.utils.DialogUtils

class ABMCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbmCategoryBinding
    private val categoriesUtils = CategoriesUtils()
    private val dialogUtils = DialogUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbmCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var initialInstructions = binding.instrucciones
        val input = binding.nombreInput
        val checkboxTemporary = binding.checkboxTemporal
        val checkboxInstitute = binding.checkboxInstituto
        val button = binding.agregarCategoria

//        if(intent.getStringExtra("type") == "modify") {
//            initialInstructions.text = binding.root.context.getString(R.string.texto_ayuda__modificacion_categorias)
//            input.setText(intent.getStringExtra("name"))
//            if(intent.getBooleanExtra("temporary", false)) {
//                checkboxTemporary.isChecked = true
//            }
//            if(intent.getBooleanExtra("institute", false)) {
//                checkboxInstitute.isChecked = true
//            }
//            button.setText("Modificar categoría")
//        }

        button.setOnClickListener {
            val categoryName = input.text.toString().lowercase().replaceFirstChar(Char::titlecase)
            if(categoryName.isEmpty()) {
                dialogUtils.showDialog(binding.root.context,
                    "El campo de nombre no puede estar vacío")
                return@setOnClickListener
            }

            if(intent.getStringExtra("type") == "add") {
                if(checkIfCategoryExists(categoryName)) {
                    dialogUtils.showDialog(binding.root.context,
                        "Ya existe una categoría de nombre $categoryName, para modificarla\n" +
                                "debe ir a la sección previa y seguir las instrucciones")
                    return@setOnClickListener
                }

                if(checkIfCategoryIsInactive(categoryName)) {
                    val onYesFunction = {
                        categoriesUtils.activateCategory(categoryName)
                        dialogUtils.showDialogWithFunctionOnClose(binding.root.context,
                            "Categoría $categoryName restaurada") {
                            finish()
                        }
                    }
                    val onNoFunction = {
                        dialogUtils.showDialog(binding.root.context,
                            "Elija otro nombre")
                    }
                    dialogUtils.showDialogWithTwoFunctionOnClose(binding.root.context,
                        "Ya existe una categoría de nombre $categoryName que se encuentra " +
                                "inactiva, desea restaurarla?", onYesFunction, onNoFunction)
                    return@setOnClickListener
                }

                categoriesUtils.addCategory(categoryName, checkboxTemporary.isChecked,
                    checkboxInstitute.isChecked, true)

                dialogUtils.showDialogWithFunctionOnClose(binding.root.context,
                    "Se ha creado correctamente la" +
                        "categoría $categoryName") {
                    finish()
                }
            }
        }
    }

    private fun checkIfCategoryExists(name: String): Boolean {
        return categoriesUtils.getActiveCategories().contains(name)
    }

    private fun checkIfCategoryIsInactive(name: String): Boolean {
        return categoriesUtils.getInactiveCategories().contains(name)
    }
}