package com.biogin.myapplication.utils

import android.util.Log
import com.biogin.myapplication.FirebaseMethods
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction

class AllowedAreasUtils() {
    private var firebaseMethods = FirebaseMethods()
    private var modulesAssociatedWithInstitutes = HashMap<String, ArrayList<String>>()

    init {
//        modulesAssociatedWithInstitutes.set("IDEI", arrayListOf("Módulo 1", "Módulo 3", "Módulo 4","Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("ICO", arrayListOf("Módulo 1", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("IDH", arrayListOf("Módulo 1", "Módulo 2","Módulo 3", "Módulo 5", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("ICI", arrayListOf("Módulo 1", "Módulo 2", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//
//        val institutesString: String =
//            root.context.assets.open("areas").bufferedReader().use { it.readText() }
//
//        val institutes = Json.decodeFromString<ArrayList<Institute>>(institutesString)
        updateMap()
    }
    fun getAllowedAreas(institutes : ArrayList<String>) : MutableSet<String> {
        updateMap()
        val allowedAreas = mutableSetOf<String>()
        for (institute in institutes) {
            allowedAreas.addAll(modulesAssociatedWithInstitutes.get(institute)!!)
        }

        return allowedAreas
    }

    fun addAreaToInstitute(instituteName: String, newArea: String) {
        updateMap()
        val db = FirebaseFirestore.getInstance()
        val docRefInstitute = db.collection("institutos").document(instituteName)
        docRefInstitute.get()
            .addOnSuccessListener { instituteDocument ->
                if (instituteDocument != null) {
                    val instituteData = instituteDocument.data
                    val areasArray = instituteData?.get("areas") as ArrayList<String>
                    if(!areasArray.contains(newArea)) {
                        areasArray.add(newArea)
                        docRefInstitute.update("areas", areasArray)
                    } else {
                        Log.d("Firebase", "Ya existe el area $newArea en el instituto $instituteName")
                    }
                } else {
                    Log.d("Firebase", "No existe el documento")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Se fallo al obtener el documento del instituto $instituteName", exception)
            }
    }

    private fun updateMap() {
        firebaseMethods.readInstitutes("ICI") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseMethods.readInstitutes("ICO") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseMethods.readInstitutes("IDH") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseMethods.readInstitutes("IDEI") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
    }

//    fun addAreaToInstitute2(instituteName: String, area: String): Task<Transaction> {
//        val db = FirebaseFirestore.getInstance()
//        val docRefInstitute = db.collection("institutos").document(instituteName)
//        return db.runTransaction { transaction ->
//            if(!transaction.get(docRefInstitute).exists()) {
//                throw FirebaseFirestoreException(
//                    "El instituto ingresado no existe, compruebe el nombre",
//                    FirebaseFirestoreException.Code.NOT_FOUND
//                )
//            }
//
//            val newArea = hashMapOf(
//                "areas" to area
//            )
//
//            transaction.set(docRefInstitute, newArea)
//        }
//    }
//
//    fun getAllAreas(): ArrayList<String> {
//        val allAreas = ArrayList<String>()
//
//        for(institute in modulesAssociatedWithInstitutes) {
//            for(area in institute.value) {
//                if(!allAreas.contains(area)) {
//                    allAreas.add(area)
//                }
//            }
//        }
//
//        allAreas.sort()
//
//        return allAreas
//    }

}