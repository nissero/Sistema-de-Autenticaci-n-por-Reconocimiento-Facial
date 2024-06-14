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
import com.biogin.myapplication.utils.StringUtils

class ABMCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbmCategoryBinding
    private val categoriesUtils = CategoriesUtils()
    private val dialogUtils = DialogUtils()
    private val stringUtils = StringUtils()
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

        val initialInstructions = binding.instrucciones
        val input = binding.nombreInput
        val checkboxTemporary = binding.checkboxTemporal
        val checkboxInstitute = binding.checkboxInstituto
        val button = binding.agregarCategoria

        if(intent.getStringExtra("type") == "modify") {
            initialInstructions.text = binding.root.context.getString(R.string.texto_ayuda_modificacion_categorias)
            input.setText(intent.getStringExtra("name"))

            checkboxTemporary.isChecked = intent.getBooleanExtra("temporary", false)
            checkboxInstitute.isChecked = intent.getBooleanExtra("institute", false)

            checkboxTemporary.isEnabled = false
            checkboxInstitute.isEnabled = false

            button.text = binding.root.context.getString(R.string.modificar_categoria)
        }

        button.setOnClickListener {
            val categoryName = stringUtils.normalizeAndSentenceCase(input.text.toString())
            if(categoryName.isEmpty()) {
                dialogUtils.showDialog(binding.root.context,
                    "El campo de nombre no puede estar vacío")
                return@setOnClickListener
            }

            if(categoryName == "Seguridad" || categoryName == "Administrador" ||
                categoryName == "Admin" ||categoryName == "Jerarquico" || categoryName == "Rrhh" ||
                categoryName == "Jerárquico" || categoryName == "Rr.hh" ||
                categoryName == "Recursos humanos") {
                dialogUtils.showDialog(binding.root.context,
                    "No es posible crear categorías con los siguientes nombres" +
                            ":\nAdministrador\nSeguridad\nJerarquico\nRRHH")
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
                    inactiveCategoryNameFound(categoryName)
                    return@setOnClickListener
                }

                categoriesUtils.addCategory(categoryName, checkboxTemporary.isChecked,
                    checkboxInstitute.isChecked, true)

                dialogUtils.showDialogWithFunctionOnClose(binding.root.context,
                    "Se ha creado correctamente la categoría $categoryName") {
                    finish()
                }
            } else if(intent.getStringExtra("type") == "modify") {
                val oldName = intent.getStringExtra("name")

                if(checkIfCategoryExists(categoryName) && oldName != categoryName) {
                    dialogUtils.showDialog(binding.root.context,
                        "Ya existe una categoría de nombre $categoryName, para modificarla\n" +
                                "debe ir a la sección previa y seguir las instrucciones")
                    return@setOnClickListener
                }

                if(checkIfCategoryIsInactive(categoryName) && oldName != categoryName) {
                    inactiveCategoryNameFound(categoryName)
                    return@setOnClickListener
                }

                if(oldName == categoryName) {
                    dialogUtils.showDialog(binding.root.context,
                        "El nombre de la categoría ingresado en el campo de texto es el mismo " +
                                "al nombre previo")
                    return@setOnClickListener
                }

                if (oldName != null) {
                    categoriesUtils.modifyCategory(oldName, categoryName)
                    dialogUtils.showDialogWithFunctionOnClose(binding.root.context,
                        "Se ha modificado el nombre de la categoría $oldName a " +
                                categoryName
                    ) {
                        finish()
                    }
                }
            }
        }
    }

    private fun checkIfCategoryExists(name: String): Boolean {
        return categoriesUtils.getActiveCategories().contains(name)
    }

    private fun inactiveCategoryNameFound(categoryName: String) {
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
    }

    private fun checkIfCategoryIsInactive(name: String): Boolean {
        return categoriesUtils.getInactiveCategories().contains(name)
    }
}