package com.biogin.myapplication.data


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.CategoriesUtils
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
    private val categoriesUtils = CategoriesUtils()

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

            if (categoriesUtils.getTemporaryCategories().contains(category)) {
                val today = LocalDate.now()
                val trabajaDesdeDate = LocalDate.parse(fechaDesde, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
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
                        "trabajaDesde" to fechaDesde,
                        "trabajaHasta" to fechaHasta
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
                        "trabajaDesde" to fechaDesde,
                        "trabajaHasta" to fechaHasta
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

            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_CREATION,
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun duplicateUserInFirebase(
        name: String,
        surname: String,
        oldDni: String,
        newDni: String,
        email: String,
        category: String,
        state: String,
        institutesSelected: ArrayList<String>,
        fechaDesde: String,
        fechaHasta: String
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
            val transactionInstance = transaction.update(docRefOldDni, "estado", "Inactivo")

            if(!categoriesUtils.getTemporaryCategories().contains(category)) {
                val newUser = hashMapOf(
                    "nombre" to name,
                    "apellido" to surname,
                    "dni" to newDni,
                    "email" to email,
                    "categoria" to category,
                    "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                    "institutos" to institutesSelected,
                    "estado" to state
                )

                transactionInstance.set(docRefNewDni, newUser)
            } else {
                val today = LocalDate.now()
                val trabajaDesdeDate = LocalDate.parse(fechaDesde, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                if (trabajaDesdeDate.isAfter(today)){
                    val newUser = hashMapOf(
                        "nombre" to name,
                        "apellido" to surname,
                        "dni" to newDni,
                        "email" to email,
                        "categoria" to category,
                        "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos" to institutesSelected,
                        "estado" to "Inactivo",
                        "trabajaDesde" to fechaDesde,
                        "trabajaHasta" to fechaHasta
                    )

                    transaction.set(docRefNewDni, newUser)
                }
                if (trabajaDesdeDate.isEqual(today)){
                    val newUser = hashMapOf(
                        "nombre" to name,
                        "apellido" to surname,
                        "dni" to newDni,
                        "email" to email,
                        "categoria" to category,
                        "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos" to institutesSelected,
                        "estado" to "Activo",
                        "trabajaDesde" to fechaDesde,
                        "trabajaHasta" to fechaHasta
                    )
                    transaction.set(docRefNewDni, newUser)
                }
            }

            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_DNI_UPDATE,MasterUserDataSession.getDniUser(), "$oldDni a $newDni", category)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun modifyUserFirebase(
        name: String,
        surname: String,
        dni: String,
        email: String,
        category: String,
        state: String,
        institutesSelected: ArrayList<String>,
        fechaDesde: String,
        fechaHasta: String
    ): Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        val docRefUserUpdated = db.collection("usuarios").document(dni)
        lateinit var tx: Task<Transaction>
        if(!categoriesUtils.getTemporaryCategories().contains(category)) {
            tx = db.runTransaction { transaction ->

                val newUser = hashMapOf(
                    "nombre" to name,
                    "apellido" to surname,
                    "dni" to dni,
                    "email" to email,
                    "categoria" to category,
                    "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                    "institutos" to institutesSelected,
                    "estado" to state
                )

                transaction.set(docRefUserUpdated, newUser)

                logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_UPDATE,MasterUserDataSession.getDniUser(), dni, category)
            }
        } else {
            tx = db.runTransaction { transaction ->
                val today = LocalDate.now()
                val trabajaDesdeDate = LocalDate.parse(fechaDesde, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                if (trabajaDesdeDate.isAfter(today)){
                    transaction.update(docRefUserUpdated,
                        "nombre", name,
                        "apellido", surname,
                        "email", email,
                        "categoria", category,
                        "estado", "Inactivo",
                        "areasPermitidas", allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos", institutesSelected,
                        "trabajaDesde", fechaDesde,
                        "trabajaHasta", fechaHasta)
                }
                if (trabajaDesdeDate.isEqual(today)) {
                    transaction.update(docRefUserUpdated,
                        "nombre", name,
                        "apellido", surname,
                        "email", email,
                        "categoria", category,
                        "estado", state,
                        "areasPermitidas", allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos", institutesSelected,
                        "trabajaDesde", fechaDesde,
                        "trabajaHasta", fechaHasta)
                }

                logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_UPDATE,MasterUserDataSession.getDniUser(), dni, category)
            }
        }
        return tx
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
            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_INACTIVATION,MasterUserDataSession.getDniUser(), dni, dniDoc.data?.get("categoria").toString())
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
                data["areasPermitidas"] as ArrayList<String>,
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

            val data = dniDoc.data
            val nuevosAtributos = mapOf(
                "suspendidoDesde" to fechaDesde,
                "suspendidoHasta" to fechaHasta
            )
            val dataActualizada = data?.plus(nuevosAtributos)

            if (dataActualizada != null) {
                transaction.update(docRefDni, dataActualizada)
            }

            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_INACTIVATION,MasterUserDataSession.getDniUser(), dni, dniDoc.data?.get("categoria").toString())
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

}