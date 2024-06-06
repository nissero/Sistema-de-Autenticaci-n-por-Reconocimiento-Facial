package com.biogin.myapplication

import android.util.Log
import com.biogin.myapplication.data.Category
import com.biogin.myapplication.data.Institute
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseMethods {

    //esto hay que modificarlo cuando se cambie la base de datos
    //y se agreguen los modulos de acceso para cada rol.
    fun readData(dni: String, callback: (Usuario) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("usuarios").document(dni)

        documentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val nombre = userData?.get("nombre") as? String ?: ""
                    val apellido = userData?.get("apellido") as? String ?: ""
                    val email = userData?.get("email") as? String ?: ""
                    val area = userData?.get("area") as? String ?: ""
                    val categoria = userData?.get("categoria") as? String ?: ""
                    val estado = userData?.get("estado") as? String ?: ""
                    val areasPermitidas = userData?.get("areasPermitidas") as? ArrayList<String> ?: arrayListOf()

                    val usuario = Usuario(nombre, apellido, dni, email, area, categoria, estado, areasPermitidas)

                    Log.d("LOGIN", "FIREBASE RESPONDIO")
                    callback(usuario)
                } else {
                    // Usuario no encontrado, devolver objeto Usuario vacío
                    callback(Usuario())
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener los datos del usuario con DNI $dni", e)
                // En caso de error, devolver objeto Usuario vacío
                callback(Usuario())
            }
    }

    fun getLogsFromDni(dni: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("logs").get()
            .addOnSuccessListener { documents ->
                var logsString = ""
                for (document in documents) {
                    if(document.get("dniMasterUser") == dni || document.get("dniUserAffected") == dni) {
                        logsString += "Timestamp: " + document.get("timestamp") + "\n"
                        logsString += "DNI usuario maestro: " + document.get("dniMasterUser") + "\n"
                        logsString += "DNI usuario afectado: " + document.get("dniUserAffected") + "\n"
                        logsString += "Evento: " + document.get("logEventName") + "\n\n"
                    }

                }
                callback(logsString)
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener los datos del usuario con  $dni", e)
            }
    }

//    fun readInstitutes(instituteName: String, callback: (Institute) -> Unit) {
//        val db = FirebaseFirestore.getInstance()
//        val documentReference = db.collection("institutos").document(instituteName)
//
//        documentReference.get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val userData = documentSnapshot.data
//                    val areas = userData?.get("areas") as? ArrayList<String> ?: arrayListOf()
//
//                    val institute = Institute(instituteName, areas)
//                    callback(institute)
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Error al obtener los datos del instituto con nombre $instituteName", e)
//            }
//    }
//
//    fun addAreaToInstitute(instituteName: String, newArea: String): Boolean {
//        var success = false
//        val db = FirebaseFirestore.getInstance()
//        val docRefInstitute = db.collection("institutos").document(instituteName)
//        docRefInstitute.get()
//            .addOnSuccessListener { instituteDocument ->
//                if (instituteDocument != null) {
//                    val instituteData = instituteDocument.data
//                    val areasArray = instituteData?.get("areas") as ArrayList<String>
//                    if(!areasArray.contains(newArea)) {
//                        areasArray.add(newArea)
//                        docRefInstitute.update("areas", areasArray)
//                        Log.d("Firebase", "Se agregó el area $newArea al instituto $instituteName")
//
//                        success = true
//                    } else {
//                        Log.d("Firebase", "Ya existe el area $newArea en el instituto $instituteName")
//                    }
//                } else {
//                    Log.d("Firebase", "No existe el documento")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d("Firebase", "Se fallo al obtener el documento del instituto $instituteName", exception)
//            }
//
//        return success
//    }
//    fun removeAreaFromInstitute(instituteName: String, areaToRemove: String): Boolean {
//        var success = false
//        val db = FirebaseFirestore.getInstance()
//        val docRefInstitute = db.collection("institutos").document(instituteName)
//        docRefInstitute.get()
//            .addOnSuccessListener { instituteDocument ->
//                if (instituteDocument != null) {
//                    val instituteData = instituteDocument.data
//                    val areasArray = instituteData?.get("areas") as ArrayList<String>
//                    if(areasArray.contains(areaToRemove)) {
//                        docRefInstitute.update("areas", FieldValue.arrayRemove(areaToRemove))
//                        Log.d("Firebase", "Se eliminó el area $areaToRemove del instituto $instituteName")
//
//                        success = true
//                    } else {
//                        Log.d("Firebase", "No existe el area $areaToRemove en el " +
//                                "instituto $instituteName")
//                    }
//                } else {
//                    Log.d("Firebase", "No existe el documento")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d("Firebase", "Se fallo al obtener el documento del instituto $instituteName", exception)
//            }
//
//        return success
//    }
}