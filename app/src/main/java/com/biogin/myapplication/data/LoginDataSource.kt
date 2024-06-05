package com.biogin.myapplication.data


import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import com.biogin.myapplication.logs.Log as LogsApp

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */

class LoginDataSource {
    private var allowedAreasUtils: AllowedAreasUtils = AllowedAreasUtils()
    private var logsRepository: LogsRepository = LogsRepository()
    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadUserToFirebase(
        name: String,
        surname: String,
        dni: String,
        email: String,
        category: String,
        institutesSelected: ArrayList<String>,
        fechaDesde: String,
        fechaHasta: String
    ): Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        val colRef = db.collection("usuarios")
        val docRefDni = colRef.document(dni)
        return db.runTransaction { transaction ->

            if (transaction.get(docRefDni).exists()) {
                throw FirebaseFirestoreException(
                    "El usuario ingresado ya existe, compruebe el dni ingresado",
                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                )
            }

            runBlocking {
                if (existsUserWithGivenEmail(email)) {
                    throw FirebaseFirestoreException(
                        "El email ingresado ya existe, compruebe el email ingresado",
                        FirebaseFirestoreException.Code.ALREADY_EXISTS
                    )
                }
            }

            if (category == "Externo" || category == "Temporal"){
                val today = LocalDate.now()
                val trabajaDesdeDate = LocalDate.parse(fechaDesde, DateTimeFormatter.ofPattern("yyyy/MM/dd"))

                val fechaDesdeTimestamp = convertStringToTimestamp(fechaDesde)
                val fechaHastaTimestamp = convertStringToTimestamp(fechaHasta)

                if (trabajaDesdeDate.isAfter(today)){
                    val newUser = hashMapOf(
                        "nombre" to name,
                        "apellido" to surname,
                        "dni" to dni,
                        "email" to email,
                        "categoria" to category,
                        "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos" to institutesSelected,
                        "estado" to "Inactivo",
                        "trabajaDesde" to fechaDesdeTimestamp,
                        "trabajaHasta" to fechaHastaTimestamp
                    )

                    transaction.set(docRefDni, newUser)
                }
                if (trabajaDesdeDate.isEqual(today)){
                    val newUser = hashMapOf(
                        "nombre" to name,
                        "apellido" to surname,
                        "dni" to dni,
                        "email" to email,
                        "categoria" to category,
                        "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos" to institutesSelected,
                        "estado" to "Activo",
                        "trabajaDesde" to fechaDesdeTimestamp,
                        "trabajaHasta" to fechaHastaTimestamp
                    )
                    transaction.set(docRefDni, newUser)
                }
            } else{
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

            logsRepository.LogEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_CREATION,
                MasterUserDataSession.getDniUser(), dni, category)
        }
    }

    private suspend fun existsUserWithGivenEmail(email : String) : Boolean {
        val db = FirebaseFirestore.getInstance()

        return db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .await()
            .size() != 0
    }
    fun duplicateUserInFirebase(
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
            logsRepository.LogEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_DNI_UPDATE,MasterUserDataSession.getDniUser(), "$oldDni a $newDni", category)
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
    ): Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        val docRefUserUpdated = db.collection("usuarios").document(dni)
        return db.runTransaction { transaction ->
            transaction.update(docRefUserUpdated,
                "nombre", name,
                "apellido", surname,
                "email", email,
                "categoria", category,
                "estado", state,
                "areasPermitidas", allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                "institutos", institutesSelected)

            logsRepository.LogEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_UPDATE,MasterUserDataSession.getDniUser(), dni, category)
        }
    }

    fun deactivateUserFirebase(dni: String): Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        return db.runTransaction { transaction ->
            if (dni.isEmpty()) {
                throw FirebaseFirestoreException(
                    "El dni ingresado no existe",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )
            }

            val docRefDni = db.collection("usuarios").document(dni)
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
            logsRepository.LogEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_INACTIVATION,MasterUserDataSession.getDniUser(), dni, dniDoc.data?.get("categoria").toString())
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

    fun disableForSomeTime(dni: String, fechaDesde: String, fechaHasta: String): Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        return db.runTransaction { transaction ->
            if (dni.isEmpty()) {
                throw FirebaseFirestoreException(
                    "El dni ingresado no existe",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )
            }

            val docRefDni = db.collection("usuarios").document(dni)
            val dniDoc = transaction.get(docRefDni)

            if (!dniDoc.exists()) {
                throw FirebaseFirestoreException(
                    "El dni ingresado no existe",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )
            }

            val fechaDesdeTimeStamp = convertStringToTimestamp(fechaDesde)
            val fechaHastaTimeStamp = convertStringToTimestamp(fechaHasta)

            val data = dniDoc.data
            val nuevosAtributos = mapOf(
                "suspendidoDesde" to fechaDesdeTimeStamp,
                "suspendidoHasta" to fechaHastaTimeStamp
            )
            val dataActualizada = data?.plus(nuevosAtributos)

            if (dataActualizada != null) {
                transaction.update(docRefDni, dataActualizada)
            }

            logsRepository.LogEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_INACTIVATION,MasterUserDataSession.getDniUser(), dni, dniDoc.data?.get("categoria").toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ifSuspensionDateEqualsToday(fechaDesde: String, dni: String): Boolean {
        val today = LocalDate.now()
        val suspensionDesde = LocalDate.parse(fechaDesde, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        if (suspensionDesde.isEqual(today)) {
            Log.d("LOGINDATASOURCE", "FECHA HOY")
            deactivateUserFirebase(dni)
            return true
        }
        return false
    }


    @SuppressLint("SimpleDateFormat")
    private fun convertStringToTimestamp(dateString: String): Timestamp {
        val formatter = SimpleDateFormat("yyyy/MM/dd")
        val parsedDate = formatter.parse(dateString) ?: return Timestamp.now() // Handle invalid format

        val calendar = Calendar.getInstance()
        calendar.time = parsedDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.add(Calendar.DATE, 1)

        return Timestamp(calendar.timeInMillis / 1000, 0)
    }

}