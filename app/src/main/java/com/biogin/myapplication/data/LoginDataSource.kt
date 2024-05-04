package com.biogin.myapplication.data

import android.util.Log
import com.biogin.myapplication.data.model.LoggedInUser
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.util.UUID

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(UUID.randomUUID().toString(), "Jane Doe")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun register(
        name: String,
        surname: String,
        dni: String,
        email: String,
        category: String,
        areasAllowed: MutableSet<String>,
        institutesSelected: ArrayList<String>
    ): Result<LoggedInUser> {
        try {
            var inserted: Boolean = true
            uploadUserToFirebase(name, surname, dni, email, category, areasAllowed, institutesSelected) { result ->
                inserted = result
            }
            if (!inserted) {
                return Result.Error(Exception("No se pudo insertar el documento para el dni $dni"))
            }
            // TODO: handle loggedInUser authentication
            val userCreated = LoggedInUser(dni, "$name $surname")
            return Result.Success(userCreated)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    private fun uploadUserToFirebase(
        name: String,
        surname: String,
        dni: String,
        email: String,
        category: String,
        areasAllowed: MutableSet<String>,
        institutesSelected: ArrayList<String>,
        callback: (Boolean) -> Unit
    ): Boolean {
        val newUser = hashMapOf(
            "nombre" to name,
            "apellido" to surname,
            "dni" to dni,
            "email" to email,
            "category" to category,
            "areasAllowed" to areasAllowed.toString(),
            "institutes" to institutesSelected.toString()
        )
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(dni)
                .set(newUser)
                .addOnSuccessListener {
                    Log.d("Firebase", "Documento añadido con ID $dni")
                    callback(true)
                }
                .addOnFailureListener {e ->
                    Log.w("Firebase", "Error al añadir el documento", e)
                    callback(false)
                }

        return true
    }
    fun logout() {
        // TODO: revoke authentication
    }
}