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
                    if(!document.data.containsKey("nombre modificado")) {
                        val name: String = document.data["nombre"] as String
                        val isTemporary = document.data["temporal"] as Boolean
                        val allowsInstitutes = document.data["permite institutos"] as Boolean
                        val active = document.data["activo"] as Boolean

                        val category = Category(name, isTemporary, allowsInstitutes, active)
                        categories[name] = category
                    }
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

    fun modifyCategory(oldName: String, newName: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRefOldName = db.collection("categorias").document(oldName)
        val docRefNewName = db.collection("categorias").document(newName)

        docRefOldName.get()
            .addOnSuccessListener { oldCategoryDocument ->
                if (oldCategoryDocument != null) {
                    val oldData = oldCategoryDocument.data
                    docRefOldName.update("nombre modificado", true)
                    docRefNewName.get()
                        .addOnSuccessListener {
                            val newNameCategory = hashMapOf(
                                "nombre" to newName,
                                "temporal" to oldData?.get("temporal") as Boolean,
                                "permite institutos" to oldData["permite institutos"] as Boolean,
                                "activo" to oldData["activo"] as Boolean
                            )

                            docRefNewName.set(newNameCategory)
                                .addOnSuccessListener {
                                    Log.d("Firebase", "Se modificó el nombre de la categoría " +
                                            "$oldName a $newName")
                                    callback(true)
                                }.addOnFailureListener { exception ->
                                    Log.d("Firebase", "No se pudo cambiar el nombre de" +
                                            "la categoría $oldName", exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "No se pudo actualizar la categoría $oldName",
                                exception)
                            callback(false)
                        }
                } else {
                    Log.d("Firebase", "No existe el documento")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento de la categoría $oldName", exception)
                callback(false)
            }
    }

    fun updateUserOnCategoryChange(oldName: String, newName: String) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("usuarios")

        documentReference.get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    if(document.data["categoria"] == oldName) {
                        println("encontre que ${document.data["dni"]} es $oldName")
                        documentReference.document(document.data["dni"].toString())
                            .update("categoria", newName)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Se modificó la categoría del usuario con" +
                                        "DNI ${document.data["dni"]} de $oldName a $newName")
                            }.addOnFailureListener { exception ->
                                Log.e("Firebase", "No se pudo modificar " +
                                        "la categoría del usuario con dni ${document.data["dni"]}",
                                    exception)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "No se pudo leer la colección Usuarios", e)
            }
    }
}