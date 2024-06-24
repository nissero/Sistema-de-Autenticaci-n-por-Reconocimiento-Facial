package com.biogin.myapplication

import android.content.Context
import android.util.Log
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FirebaseSyncService(context: Context) {
    private val offlineDatabaseHelper = OfflineDataBaseHelper(context)
    private val usersReference = Firebase.firestore.collection("usuarios")

    init {
        listenForUsersChanges()
    }

    private fun listenForUsersChanges(){
        usersReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.e(TAG, "Error al sincronizar datos")
                return@addSnapshotListener
            }
            querySnapshot?.let{
                for(user in it){
                    val dni = user.get("dni").toString()
                    val estado = user.get("estado").toString()
                    val categoria = user.get("categoria").toString()

                    Log.d(TAG, "User: $dni, $estado, $categoria")

                    if (estado == "Activo"){
                        if (categoria == "Seguridad"){
                            if (!offlineDatabaseHelper.checkIfSecurity(dni)){
                                offlineDatabaseHelper.registerSecurity(dni)
                                Log.d(TAG, "User: $dni, $estado, $categoria REGISTRADO COMO SEGURIDAD")
                                if (offlineDatabaseHelper.checkInDatabase(dni)){
                                    Log.d(TAG, "User: $dni, $estado, $categoria ERA USUARIO")
                                    offlineDatabaseHelper.deleteUserByDni(dni)
                                }
                            }
                        } else {
                            if (!offlineDatabaseHelper.checkInDatabase(dni)){
                                offlineDatabaseHelper.registerUser(dni)
                                Log.d(TAG, "User: $dni, $estado, $categoria REGISTRADO COMO USUARIO")
                                if (offlineDatabaseHelper.checkIfSecurity(dni)){
                                    Log.d(TAG, "User: $dni, $estado, $categoria ERA SEGURIDAD")
                                    offlineDatabaseHelper.deleteSecurityByDni(dni)
                                }
                            }
                        }
                    }
                    else {
                        if (categoria == "Seguridad"){
                            if (offlineDatabaseHelper.checkIfSecurity(dni)){
                                offlineDatabaseHelper.deleteSecurityByDni(dni)
                                Log.d(TAG, "User: $dni, $estado, $categoria BORRADO DE SEGURIDAD")
                            }
                        } else {
                            if (offlineDatabaseHelper.checkInDatabase(dni)){
                                offlineDatabaseHelper.deleteUserByDni(dni)
                                Log.d(TAG, "User: $dni, $estado, $categoria BORRADO DE USUARIOS")
                            }
                        }
                    }
                }
            }
        }


    }


        companion object {
            private const val TAG = "FirebaseSyncManager"
        }
}


