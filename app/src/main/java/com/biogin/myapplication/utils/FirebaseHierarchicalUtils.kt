package com.biogin.myapplication.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHierarchicalUtils {
    fun getMailFromFirebase(callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("data jerarquico").document("info")

        documentReference.get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.exists()) {
                    val mail = snapshot.data?.get("mail") as String
                    Log.d("Firebase", "Se encontro el mail $mail")

                    callback(mail)
                } else {
                    Log.d("Firebase", "No se encontró ningun mail")
                    callback("")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "No se encontró el documento", e)
                callback("")
            }
    }

    fun getTrainingDaysFromFirebase(callback: (ArrayList<Boolean>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("data jerarquico").document("info")

        documentReference.get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.exists()) {
                    val trainingDays = snapshot.data?.get("dias de entrenamiento") as ArrayList<Boolean>

                    callback(trainingDays)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "No se encontró el documento", e)
                callback(ArrayList())
            }
    }

    fun setMail(mail: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("data jerarquico").document("info")

        documentReference.get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.exists()) {
                    documentReference.update("mail", mail)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Se actualizó el mail")
                            callback(true)
                    }.addOnFailureListener {e ->
                            Log.e("Firestore", "No se pudo actualizar el mail", e)
                        callback(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "No se encontró el documento", e)
                callback(false)
            }
    }

    fun setTrainingDays(trainingDays: ArrayList<Boolean>, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("data jerarquico").document("info")

        documentReference.get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.exists()) {
                    documentReference.update("dias de entrenamiento", trainingDays)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Se actualizaron los días de entrenamiento")
                            callback(true)
                        }.addOnFailureListener {e ->
                            Log.e("Firestore", "No se pudieron actualizar los " +
                                    "días de entrenamiento", e)
                            callback(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "No se encontró el documento", e)
                callback(false)
            }
    }
}