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
                    if(areaData[instituteName] == true &&
                        areaData["activo"] == true) {
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
                    if(areaData[instituteName] == true &&
                        areaData["activo"] == false) {
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
                idei: Boolean, idh: Boolean, callback: (Boolean) -> Unit) {
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
                callback(true)
            }.addOnFailureListener { e ->
                Log.w("Firebase", "Error al crear categoría", e)
                callback(false)
            }
    }

    fun deactivateArea(area: String, callback: (Boolean) -> Unit) {
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

                        callback(true)
                    } else {
                        Log.d("Firebase", "El lugar físico $area ya se encuentra inactivado")
                        callback(false)
                    }
                } else {
                    Log.d("Firebase", "No existe el documento")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento del lugar físico $area", exception)
                callback(false)
            }
    }

    fun activateArea(area: String, callback: (Boolean) -> Unit) {
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

                        callback(true)
                    } else {
                        Log.d("Firebase", "El lugar físico $area ya se encuentra activado")
                        callback(false)
                    }
                } else {
                    Log.d("Firebase", "No existe el documento")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento del lugar físico $area", exception)
            }
    }

    fun modifyArea(area: String, ici: Boolean, ico: Boolean,
                   idei: Boolean, idh: Boolean, callback: (Boolean) -> Unit) {
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

                    callback(true)
                } else {
                    Log.d("Firebase", "No existe el documento")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se falló al obtener el documento del lugar físico $area", exception)
                callback(false)
            }
    }
}