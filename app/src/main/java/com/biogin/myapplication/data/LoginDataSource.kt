package com.biogin.myapplication.data


import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.CategoriesUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
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
import java.util.Locale
import java.util.TimeZone
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

            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_CREATION,
                MasterUserDataSession.getDniUser(), dni, category)
        }
    }

    public suspend fun existsUserWithGivenEmail(email : String) : Boolean {
        val db = FirebaseFirestore.getInstance()

        return db.collection("usuarios")
            .whereEqualTo("email", email)
            .whereEqualTo("estado", "Activo")
            .get()
            .await()
            .size() != 0
    }

    public suspend fun existsUserWithGivenDni(dni : String) : Boolean {
        val db = FirebaseFirestore.getInstance()

        return db.collection("usuarios")
            .whereEqualTo("dni", dni)
            .whereEqualTo("estado", "Activo")
            .get()
            .await()
            .size() != 0
    }

    private suspend fun existsUserWithGivenEmailButDifferentGivenDni(currentDni: String, email : String) : Boolean {
        val db = FirebaseFirestore.getInstance()

        return db.collection("usuarios")
            .whereEqualTo("email", email)
            .whereEqualTo("estado", "Activo")
            .whereNotEqualTo("dni", currentDni)
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
        currentEmail: String,
        newEmail: String,
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
                    "El dni ingresado ya existe, intente nuevamente",
                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                )
            }

            if (currentEmail != newEmail) {
                runBlocking {
                    if (existsUserWithGivenEmailButDifferentGivenDni(oldDni, newEmail)) {
                        throw FirebaseFirestoreException(
                            "El email ingresado ya existe, intente nuevamente",
                            FirebaseFirestoreException.Code.ALREADY_EXISTS
                        )
                    }
                }
            }
            val docRefOldDni = db.collection("usuarios").document(oldDni)
            val transactionInstance = transaction.update(docRefOldDni, "estado", "Inactivo")

            if(!categoriesUtils.getTemporaryCategories().contains(category)) {
                val newUser = hashMapOf(
                    "nombre" to name,
                    "apellido" to surname,
                    "dni" to newDni,
                    "email" to newEmail,
                    "categoria" to category,
                    "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                    "institutos" to institutesSelected,
                    "estado" to state
                )

                transactionInstance.set(docRefNewDni, newUser)
            } else {
                val today = LocalDate.now()
                val trabajaDesdeDate = LocalDate.parse(fechaDesde, DateTimeFormatter.ofPattern("yyyy/MM/dd"))

                val fechaDesdeTimestamp = convertStringToTimestamp(fechaDesde)
                val fechaHastaTimestamp = convertStringToTimestamp(fechaHasta)
                if (trabajaDesdeDate.isAfter(today)){
                    val newUser = hashMapOf(
                        "nombre" to name,
                        "apellido" to surname,
                        "dni" to newDni,
                        "email" to newEmail,
                        "categoria" to category,
                        "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos" to institutesSelected,
                        "estado" to "Inactivo",
                        "trabajaDesde" to fechaDesdeTimestamp,
                        "trabajaHasta" to fechaHastaTimestamp
                    )

                    transaction.set(docRefNewDni, newUser)
                }
                if (trabajaDesdeDate.isEqual(today)){
                    val newUser = hashMapOf(
                        "nombre" to name,
                        "apellido" to surname,
                        "dni" to newDni,
                        "email" to newEmail,
                        "categoria" to category,
                        "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos" to institutesSelected,
                        "estado" to "Activo",
                        "trabajaDesde" to fechaDesdeTimestamp,
                        "trabajaHasta" to fechaHastaTimestamp
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
        currentEmail: String,
        newEmail: String,
        category: String,
        state: String,
        institutesSelected: ArrayList<String>,
        fechaDesde: String,
        fechaHasta: String
    ): Task<Transaction> {
        val db = FirebaseFirestore.getInstance()
        val docRefUserUpdated = db.collection("usuarios").document(dni)
        lateinit var tx: Task<Transaction>

        return db.runTransaction { transaction ->
            if (currentEmail != newEmail) {
                runBlocking {
                    if (existsUserWithGivenEmailButDifferentGivenDni(dni, newEmail)) {
                        throw FirebaseFirestoreException(
                            "El email ingresado ya existe, intente nuevamente",
                            FirebaseFirestoreException.Code.ALREADY_EXISTS
                        )
                    }
                }
            }

            if (!categoriesUtils.getTemporaryCategories().contains(category)) {

                val newUser = hashMapOf(
                    "nombre" to name,
                    "apellido" to surname,
                    "dni" to dni,
                    "email" to newEmail,
                    "categoria" to category,
                    "areasPermitidas" to allowedAreasUtils.getAllowedAreas(institutesSelected)
                        .toList(),
                    "institutos" to institutesSelected,
                    "estado" to state
                )

                transaction.set(docRefUserUpdated, newUser)

                logsRepository.logEventWithTransaction(
                    db,
                    transaction,
                    LogsApp.LogEventType.INFO,
                    LogsApp.LogEventName.USER_UPDATE,
                    MasterUserDataSession.getDniUser(),
                    dni,
                    category
                )

            } else {
                val today = LocalDate.now()
                val trabajaDesdeDate =
                    LocalDate.parse(fechaDesde, DateTimeFormatter.ofPattern("yyyy/MM/dd"))

                val fechaDesdeTimestamp = convertStringToTimestamp(fechaDesde)
                val fechaHastaTimestamp = convertStringToTimestamp(fechaHasta)
                if (trabajaDesdeDate.isAfter(today)) {
                    transaction.update(
                        docRefUserUpdated,
                        "nombre",
                        name,
                        "apellido",
                        surname,
                        "email",
                        newEmail,
                        "categoria",
                        category,
                        "estado",
                        "Inactivo",
                        "areasPermitidas",
                        allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos",
                        institutesSelected,
                        "trabajaDesde",
                        fechaDesdeTimestamp,
                        "trabajaHasta",
                        fechaHastaTimestamp
                    )
                }
                if (trabajaDesdeDate.isEqual(today)) {
                    transaction.update(
                        docRefUserUpdated,
                        "nombre",
                        name,
                        "apellido",
                        surname,
                        "email",
                        newEmail,
                        "categoria",
                        category,
                        "estado",
                        state,
                        "areasPermitidas",
                        allowedAreasUtils.getAllowedAreas(institutesSelected).toList(),
                        "institutos",
                        institutesSelected,
                        "trabajaDesde",
                        fechaDesdeTimestamp,
                        "trabajaHasta",
                        fechaHastaTimestamp
                    )
                }

                logsRepository.logEventWithTransaction(
                    db,
                    transaction,
                    LogsApp.LogEventType.INFO,
                    LogsApp.LogEventName.USER_UPDATE,
                    MasterUserDataSession.getDniUser(),
                    dni,
                    category
                )
            }
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
            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_INACTIVATION,MasterUserDataSession.getDniUser(), dni, dniDoc.data?.get("categoria").toString())
        }
    }
//
//    fun getDocument(dni: String): Task<DocumentSnapshot> {
//        val db = FirebaseFirestore.getInstance()
//        return db.collection("usuarios").document(dni).get()
//    }

    suspend fun getUserFromFirebase(dni: String): Result<LoggedInUser> {
        val db = FirebaseFirestore.getInstance()
        val document = db.collection("usuarios").document(dni).get().await()


        if (document.data != null) {
            val data = document.data
            val categoryDocument = db.collection("categorias").document(data?.get("categoria").toString()).get().await()

            val user: LoggedInUser

            if (categoryDocument.getBoolean("temporal") == true){
                val fechaDesdeTimeStamp = document.getTimestamp("trabajaDesde")
                val fechaHastaTimeStamp = document.getTimestamp("trabajaHasta")

                val fechaDesdeDate = fechaDesdeTimeStamp?.toDate()
                val fechaHastaDate = fechaHastaTimeStamp?.toDate()

                val fechaHastaFormateada = fechaHastaDate?.let { formatearFecha(it) }
                val fechaDesdeFormateada = fechaDesdeDate?.let { formatearFecha(it) }

                user = LoggedInUser(
                    data?.get("dni").toString(),
                    data?.get("nombre").toString(),
                    data?.get("apellido").toString(),
                    data?.get("email").toString(),
                    data?.get("categoria").toString(),
                    data?.get("estado").toString(),
                    data?.get("institutos") as ArrayList<String>,
                    data["areasPermitidas"] as ArrayList<String>,
                    fechaDesdeFormateada,
                    fechaHastaFormateada
                )
            } else{
                user = LoggedInUser(
                    data?.get("dni").toString(),
                    data?.get("nombre").toString(),
                    data?.get("apellido").toString(),
                    data?.get("email").toString(),
                    data?.get("categoria").toString(),
                    data?.get("estado").toString(),
                    data?.get("institutos") as ArrayList<String>,
                    data["areasPermitidas"] as ArrayList<String>,
                )
            }

            return Result.Success(user)
        }

        return Result.Error(Exception("Error al obtener el usuario con el dni ingresado"))
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

            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.USER_TEMPORAL_INACTIVATION,MasterUserDataSession.getDniUser(), dni, dniDoc.data?.get("categoria").toString())
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
        formatter.timeZone = TimeZone.getTimeZone("America/Argentina/Buenos_Aires")
        val parsedDate = formatter.parse(dateString) ?: return Timestamp.now() // Handle invalid format

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"))
        calendar.time = parsedDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Optional: Uncomment the next line if you still need to add a day
        // calendar.add(Calendar.DATE, 1)

        return Timestamp(calendar.timeInMillis / 1000, 0)
    }

    suspend fun getLugares(dniUser: String): Result<MutableList<String>> {
        val ret = mutableListOf<String>()
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("lugares")

        val resultUserData: Result<LoggedInUser> = getUserFromFirebase(dniUser)
        var userLugares = arrayListOf<String>()
        if (resultUserData is Result.Success){
            userLugares = resultUserData.data.areasAllowed
        }

        Log.d("getLugares", "LUGARES USUARIO: $userLugares")

        // Realizar una consulta a la colección "lugares"
        val task = collectionRef.get()
        val result = task.await() // Esperar a que la consulta se complete

        for (document in result) {
            val id = document.id
            if (userLugares.isNotEmpty()){
                if (!userLugares.contains(id)){
                    ret.add(id)
                }
            } else {
                ret.add(id)
            }
        }

        return Result.Success(ret)
    }

    fun addTemportalUserAccessToLugares(dni: String, fechaDesde: String, fechaHasta: String, placesSelected: List<String>): Task<Transaction> {
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
                "accesoDesde" to fechaDesdeTimeStamp,
                "accesoHasta" to fechaHastaTimeStamp,
                "areasTemporales" to placesSelected
            )
            val dataActualizada = data?.plus(nuevosAtributos)

            if (dataActualizada != null) {
                transaction.update(docRefDni, dataActualizada)
            }

            logsRepository.logEventWithTransaction(db, transaction, LogsApp.LogEventType.INFO, LogsApp.LogEventName.GRANT_TEMPORAL_ACCESS,MasterUserDataSession.getDniUser(), dni, dniDoc.data?.get("categoria").toString())
        }
    }

    private fun formatearFecha(fecha: Date): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone("America/Argentina/Buenos_Aires") // Zona horaria UTC-3
        return simpleDateFormat.format(fecha)
    }




}