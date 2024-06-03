package com.biogin.myapplication.utils

import android.util.Log
import com.biogin.myapplication.data.Institute
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAreaMethods {

    fun readActiveAreas(instituteName: String, callback: (Institute) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("lugares")

        documentReference.get()
            .addOnSuccessListener { documents ->
                val areas = ArrayList<String>()
                for(document in documents) {
                    val areaData = document.data
                    if(areaData.get(instituteName) == true &&
                        areaData.get("activo") == true) {
                        areas.add(document.id)
                    }
                }

                val institute = Institute(instituteName, areas)
                callback(institute)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener los datos del instituto con nombre $instituteName", e)
            }
    }

    fun readInactiveAreas(instituteName: String, callback: (Institute) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("lugares")

        documentReference.get()
            .addOnSuccessListener { documents ->
                val areas = ArrayList<String>()
                for(document in documents) {
                    val areaData = document.data
                    if(areaData.get(instituteName) == true &&
                        areaData.get("activo") == false) {
                        areas.add(document.id)
                    }
                }

                val institute = Institute(instituteName, areas)
                callback(institute)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener los datos del instituto con nombre $instituteName", e)
            }
    }

    fun addArea(newArea: String, ici: Boolean, ico: Boolean,
                idei: Boolean, idh: Boolean): Boolean {
        var success = false
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("lugares").document(newArea)

        val area = hashMapOf(
            "ICI" to ici,
            "ICO" to ico,
            "IDEI" to idei,
            "IDH" to idh,
            "activo" to true
        )

        docRef.set(area)
            .addOnSuccessListener {
                Log.d("Firebase", "Se ha creado el lugar físico $newArea correctamente")
                success = true
            }.addOnFailureListener {
                    e -> Log.w("Firebase", "Error al crear categoría", e)
            }

        return success
    }

    fun deactivateArea(area: String): Boolean {
        var success = false
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("lugares").document(area)

        docRef.get()
            .addOnSuccessListener { areaDocument ->
                if (areaDocument != null) {
                    val areaData = areaDocument.data
                    val status = areaData?.get("activo") as Boolean
                    if(status) {
                        docRef.update("activo", false)
                        Log.d("Firebase", "Se inactivó el lugar físico $area")

                        success = true
                    } else {
                        Log.d("Firebase", "El lugar físico $area ya se encuentra inactivado")
                    }
                } else {
                    Log.d("Firebase", "No existe el documento")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento del lugar físico $area", exception)
            }

        return success
    }

    fun activateArea(area: String): Boolean {
        var success = false
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("lugares").document(area)

        docRef.get()
            .addOnSuccessListener { areaDocument ->
                if (areaDocument != null) {
                    val areaData = areaDocument.data
                    val status = areaData?.get("activo") as Boolean
                    if(!status) {
                        docRef.update("activo", true)
                        Log.d("Firebase", "Se activó el lugar físico $area")

                        success = true
                    } else {
                        Log.d("Firebase", "El lugar físico $area ya se encuentra activado")
                    }
                } else {
                    Log.d("Firebase", "No existe el documento")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento del lugar físico $area", exception)
            }

        return success
    }

    fun modifyArea(area: String, ici: Boolean, ico: Boolean,
                   idei: Boolean, idh: Boolean): Boolean {
        var success = false
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("lugares").document(area)

        docRef.get()
            .addOnSuccessListener { areaDocument ->
                if (areaDocument != null) {
                    docRef.update("ICI", ici)
                    docRef.update("ICO", ico)
                    docRef.update("IDEI", idei)
                    docRef.update("IDH", idh)
                    Log.d("Firebase", "Se modificó el lugar físico $area")

                    success = true
                } else {
                    Log.d("Firebase", "No existe el documento")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento del lugar físico $area", exception)
            }

        return success
    }
}