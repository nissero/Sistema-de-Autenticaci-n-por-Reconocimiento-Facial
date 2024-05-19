package com.biogin.myapplication.data

import android.util.Log
import android.view.View
import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource() {
    private var allowedAreasUtils = AllowedAreasUtils()

    public fun uploadUserToFirebase(
        name: String,
        surname: String,
        dni: String,
        email: String,
        category: String,
        institutesSelected: ArrayList<String>,
    ): Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        val docRefDni = db.collection("usuarios").document(dni)
        return db.runTransaction { transaction ->
            if(transaction.get(docRefDni).exists()) {
                throw FirebaseFirestoreException(
                    "El usuario ingresado ya existe, compruebe el dni ingresado",
                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                )
            }

            val newUser = hashMapOf(
                "nombre" to name,
                "apellido" to surname,
                "dni" to dni,
                "email" to email,
                "categoria" to category,
                "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                "institutos" to institutesSelected,
                "estado" to "Activo"
            )

            transaction.set(docRefDni, newUser)
        }
    }

    public fun duplicateUserInFirebase(
        name: String,
        surname: String,
        oldDni: String,
        newDni: String,
        email: String,
        category: String,
        institutesSelected: ArrayList<String>,
    ): Task<Transaction> {

        val db = FirebaseFirestore.getInstance()
        val docRefNewDni = db.collection("usuarios").document(newDni)
        return db.runTransaction { transaction ->
            val newDniDoc = transaction.get(docRefNewDni)
            if (newDniDoc.exists()) {
                throw FirebaseFirestoreException(
                    "El dni ingresado ya existe",
                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                )
            }

            val docRefOldDni = db.collection("usuarios").document(oldDni)
            var transactionInstance = transaction.update(docRefOldDni, "estado", "Inactivo")

            val newUser = hashMapOf(
                "nombre" to name,
                "apellido" to surname,
                "dni" to newDni,
                "email" to email,
                "categoria" to category,
                "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                "institutos" to institutesSelected,
                "estado" to "Activo"
            )

            transactionInstance.set(docRefNewDni, newUser)
        }
    }

    fun modifyUserFirebase(
        name: String,
        surname: String,
        dni: String,
        email: String,
        category: String,
        state: String,
        institutesSelected: ArrayList<String>
    ): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("usuarios").document(dni).update(
            "nombre", name,
            "apellido", surname,
            "email", email,
            "categoria", category,
            "estado", state,
            "areasPermitidas", allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
            "institutos", institutesSelected
        )

    }

    fun deactivateUserFirebase(dni: String) : Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        val docRefDni = db.collection("usuarios").document(dni)
        return db.runTransaction { transaction ->
            val dniDoc = transaction.get(docRefDni)
            if (!dniDoc.exists()) {
                throw FirebaseFirestoreException(
                    "El dni ingresado no existe",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )
            }

            val estado = dniDoc.data?.get("estado")
            if (estado != null) {
                if (estado == "Inactivo") {
                    throw FirebaseFirestoreException(
                        "El usuario ya fue eliminado",
                        FirebaseFirestoreException.Code.INVALID_ARGUMENT
                    )
                }
            }
            transaction.update(docRefDni, "estado", "Inactivo")
        }
    }

    fun getDocument(dni: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("usuarios").document(dni).get()
    }

    suspend fun getUserFromFirebase(dni: String): Result<LoggedInUser> {
        val db = FirebaseFirestore.getInstance()
        val document = db.collection("usuarios").document(dni).get().await()


        if (document.data != null) {
            val data = document.data
            val user = LoggedInUser(
                data?.get("dni").toString(),
                data?.get("nombre").toString(),
                data?.get("apellido").toString(),
                data?.get("email").toString(),
                data?.get("categoria").toString(),
                data?.get("estado").toString(),
                data?.get("institutos") as ArrayList<String>,
                data?.get("areasPermitidas") as ArrayList<String>,
            )

            return Result.Success(user)
        }

        return Result.Error(Exception("Error al obtener el usuario con el dni ingresado"))
    }

    fun logout() {
        // TODO: revoke authentication
    }
}