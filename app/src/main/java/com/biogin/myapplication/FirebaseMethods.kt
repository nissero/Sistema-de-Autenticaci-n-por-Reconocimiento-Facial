package com.biogin.myapplication

import android.icu.text.SimpleDateFormat
import android.util.Log
import com.google.firebase.Timestamp
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

                    var areasTemporales: ArrayList<String> = arrayListOf()

                    val accesoDesde: Timestamp?
                    val accesoHasta: Timestamp?

                    var accesoDesdeString = ""
                    var accesoHastaString = ""

                    val hasAreasTemporales = userData?.containsKey("areasTemporales") ?: false
                    val hasAccesoDesde = userData?.containsKey("accesoDesde") ?: false
                    val hasAccesoHasta = userData?.containsKey("accesoHasta") ?: false

                    if (hasAreasTemporales && hasAccesoDesde && hasAccesoHasta){
                        areasTemporales = userData?.get("areasTemporales") as? ArrayList<String> ?: arrayListOf()
                        accesoDesde = documentSnapshot.getTimestamp("accesoDesde")
                        accesoHasta = documentSnapshot.getTimestamp("accesoHasta")

                        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
                        accesoDesdeString = simpleDateFormat.format(accesoDesde?.toDate())
                        accesoHastaString = simpleDateFormat.format(accesoHasta?.toDate())
                    }

                    Log.d("LOGIN", accesoDesdeString)
                    Log.d("LOGIN", accesoHastaString)

                    val usuario = Usuario(nombre, apellido, dni, email, area, categoria, estado, areasPermitidas, areasTemporales, accesoDesdeString, accesoHastaString)

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
}