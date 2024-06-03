package com.biogin.myapplication.utils

import android.util.Log
import com.biogin.myapplication.data.Category
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseCategoryMethods {

    fun getCategories(callback: (HashMap<String, Category>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("categorias")

        documentReference.get()
            .addOnSuccessListener { documents ->
                val categories = HashMap<String, Category>()
                for(document in documents) {
                    val name: String = document.data.get("nombre") as String
                    val isTemporary = document.data.get("temporal") as Boolean
                    val allowsInstitutes = document.data.get("permite institutos") as Boolean
                    val active = document.data.get("activo") as Boolean

                    val category = Category(name, isTemporary, allowsInstitutes, active)
                    categories.set(name, category)
                }

                callback(categories)

            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "No se pudo leer la colección Categorias", e)
            }
    }

    fun addCategory(newCategory: Category): Boolean {
        var success = false
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("categorias").document(newCategory.name)

        val category = hashMapOf(
            "nombre" to newCategory.name,
            "temporal" to newCategory.isTemporary,
            "permite institutos" to newCategory.allowsInstitutes,
            "activo" to newCategory.active
        )

        docRef.set(category)
            .addOnSuccessListener {
                Log.d("Firebase", "Se ha creado la categoría $category.nombre correctamente")
                success = true
            }.addOnFailureListener {
                    e -> Log.w("Firebase", "Error al crear categoría", e)
            }

        return success
    }

    fun deactivateCategory(name: String): Boolean {
        var success = false

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("categorias").document(name)

        docRef.get()
            .addOnSuccessListener { categoryDocument ->
                if (categoryDocument != null) {
                    val categoryData = categoryDocument.data
                    val status = categoryData?.get("activo") as Boolean
                    if(status) {
                        docRef.update("activo", false)
                        Log.d("Firebase", "Se inactivó la categoría $name")

                        success = true
                    } else {
                        Log.d("Firebase", "La categoría $name ya se encuentra inactivada")
                    }
                } else {
                    Log.d("Firebase", "No existe el documento")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento de la categoría $name", exception)
            }

        return success
    }

    fun activateCategory(name: String): Boolean {
        var success = false

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("categorias").document(name)

        docRef.get()
            .addOnSuccessListener { categoryDocument ->
                if (categoryDocument != null) {
                    val categoryData = categoryDocument.data
                    val status = categoryData?.get("activo") as Boolean
                    if(!status) {
                        docRef.update("activo", true)
                        Log.d("Firebase", "Se activó la categoría $name")

                        success = true
                    } else {
                        Log.d("Firebase", "La categoría $name ya se encuentra activada")
                    }
                } else {
                    Log.d("Firebase", "No existe el documento")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento de la categoría $name", exception)
            }

        return success
    }
}