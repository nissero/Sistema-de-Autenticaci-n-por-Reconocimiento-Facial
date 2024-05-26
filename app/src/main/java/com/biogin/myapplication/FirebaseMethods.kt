package com.biogin.myapplication

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseMethods {

    //todo esto hay que modificarlo cuando se cambie la base de datos
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
}