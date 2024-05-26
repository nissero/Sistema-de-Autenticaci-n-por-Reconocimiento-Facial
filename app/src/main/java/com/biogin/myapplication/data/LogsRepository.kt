package com.biogin.myapplication.data

import com.biogin.myapplication.logs.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LogsRepository {
    val LOGS_COLLECTION_NAME = "logs"
    suspend fun GetAllLogs() : List<Log> {
        val db = FirebaseFirestore.getInstance()
        val logs : ArrayList<Log> = ArrayList()

        val collectionRef = db.collection(LOGS_COLLECTION_NAME)
        var logsObtained = collectionRef.
            get().
            await()
            .documents

        for (logDocument in logsObtained) {
            var document = logDocument.data
            if (document != null) {
                logs.add(
                    Log(
                    Log.LogEventType.valueOf(document.get("logEventType").toString()),
                        Log.getLogEventNameFromValue(document.get("logEventName").toString()),
                    document.get("dniMasterUser").toString(),
                    document.get("dniUserAffected").toString(),
                    document.get("category").toString(),
                    document.get("timestamp").toString())
                )
            }
        }

        return logs
    }

    fun getSuccesfulAuthentications() : Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection(LOGS_COLLECTION_NAME).
             whereEqualTo("logEventName", "USER SUCCESSFUL AUTHENTICATION")
            .get()

    }

    fun getUnsuccesfulAuthentications() : Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection(LOGS_COLLECTION_NAME).
        whereEqualTo("logEventName", "USER UNSUCCESSFUL AUTHENTICATION")
            .get()
    }

    fun LogEventWithTransaction(db : FirebaseFirestore, transaction : Transaction, logEventType : Log.LogEventType, logEventName: Log.LogEventName, dniRRHH : String, dniNewUser : String, categoryNewUser : String) :  Transaction {
        val log = createHashmapLog(
            Log(
                logEventType,
                logEventName,
                dniRRHH,
                dniNewUser,
                categoryNewUser,
                Calendar.getInstance().time.toString()
            )
        )

        val newLogDocRef = db.collection("logs").document()
        return transaction.set(newLogDocRef, log)
    }

    fun LogEvent(logEventType : Log.LogEventType, logEventName: Log.LogEventName, dniMasterUser : String, dniNewUser : String, categoryNewUser : String) {
        val db = FirebaseFirestore.getInstance()

        val log = createHashmapLog(
            Log(
                logEventType,
                logEventName,
                dniMasterUser,
                dniNewUser,
                categoryNewUser,
                Calendar.getInstance().time.toString()
            )
        )

        db.collection(LOGS_COLLECTION_NAME).add(log)
    }

    private fun createHashmapLog(log : Log): HashMap<String, Any> {
        val date = getCurrentDateTime()
        val dateInString = date.toString("yyyy/MM/dd HH:mm:ss")

        return hashMapOf(
            "logEventType" to log.logEventType,
            "logEventName" to log.logEventName.value,
            "dniMasterUser" to log.dniMasterUser,
            "dniUserAffected" to log.dniUserAffected,
            "timestamp" to dateInString,
            "category" to log.userCategory
        )
    }

    private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    private fun getCurrentDateTime(): Date {
        return android.icu.util.Calendar.getInstance().time
    }

}