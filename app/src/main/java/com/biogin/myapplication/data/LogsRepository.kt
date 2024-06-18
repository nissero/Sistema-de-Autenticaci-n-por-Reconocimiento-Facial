package com.biogin.myapplication.data

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper
import com.biogin.myapplication.logs.Log
import com.biogin.myapplication.utils.TimestampDateUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar


class LogsRepository {
    private val LOGS_COLLECTION_NAME = "logs"
    private val timestampUtil = TimestampDateUtil()

    suspend fun getAllLogs() : List<Log> {
        val db = FirebaseFirestore.getInstance()
        val logs : ArrayList<Log> = ArrayList()

        val collectionRef = db.collection(LOGS_COLLECTION_NAME)
        val logsObtained = collectionRef.
        orderBy("timestamp", Query.Direction.DESCENDING).
            get().
            await()
            .documents

        for (logDocument in logsObtained) {
            val document = logDocument.data
            if (document != null) {
                logs.add(
                    Log(
                    Log.LogEventType.valueOf(document.get("logEventType").toString()),
                        Log.getLogEventNameFromValue(document.get("logEventName").toString()),
                    document.get("dniMasterUser").toString(),
                    document.get("dniUserAffected").toString(),
                    document.get("category").toString(),
                        timestampUtil.utcTimestampToLocalString(document.get("timestamp")!!))
                )
            }
        }

        return logs
    }

    suspend fun getAllLogsFromUser(dniUser : String) : List<Log> {
        if (dniUser.isEmpty()) {
            return ArrayList()
        }

        val db = FirebaseFirestore.getInstance()
        val logs : ArrayList<Log> = ArrayList()

        val collectionRef = db.collection(LOGS_COLLECTION_NAME)
        val logsObtained = collectionRef.
        whereEqualTo("dniUserAffected", dniUser).
        orderBy("timestamp", Query.Direction.DESCENDING).
        get().
        await()
            .documents

        for (logDocument in logsObtained) {
            val document = logDocument.data
            if (document != null) {
                logs.add(
                    Log(
                        Log.LogEventType.valueOf(document.get("logEventType").toString()),
                        Log.getLogEventNameFromValue(document.get("logEventName").toString()),
                        document.get("dniMasterUser").toString(),
                        document.get("dniUserAffected").toString(),
                        document.get("category").toString(),
                        timestampUtil.utcTimestampToLocalString(document.get("timestamp")!!))
                )
            }
        }

        return logs
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllLogsFromUserByDate(dniUser : String, dateFrom : String, dateTo : String) : List<Log> {
        if (dateFrom.isEmpty() || dateTo.isEmpty() || dniUser.isEmpty()) {
            return ArrayList()
        }

        val db = FirebaseFirestore.getInstance()
        val logs : ArrayList<Log> = ArrayList()

        val formatedDateFrom = timestampUtil.stringDateToLocalDate(dateFrom)
        val startOfDayOfFromDate = formatedDateFrom.atStartOfDay()

        val formatedDateTo = timestampUtil.stringDateToLocalDate(dateTo)
        val endOfDayOfToDate = formatedDateTo.atTime(23, 59, 59)

        val collectionRef = db.collection(LOGS_COLLECTION_NAME)
        val logsObtained = collectionRef.
            whereEqualTo("dniUserAffected", dniUser).
            whereGreaterThanOrEqualTo("timestamp", Timestamp(timestampUtil.asDate(startOfDayOfFromDate))).
            whereLessThanOrEqualTo("timestamp", Timestamp(timestampUtil.asDate(endOfDayOfToDate))).
            orderBy("timestamp", Query.Direction.DESCENDING).
        get().
        await()
            .documents

        for (logDocument in logsObtained) {
            val document = logDocument.data
            if (document != null) {
                logs.add(
                    Log(
                        Log.LogEventType.valueOf(document.get("logEventType").toString()),
                        Log.getLogEventNameFromValue(document.get("logEventName").toString()),
                        document.get("dniMasterUser").toString(),
                        document.get("dniUserAffected").toString(),
                        document.get("category").toString(),
                        timestampUtil.utcTimestampToLocalString(document.get("timestamp")!!))
                )
            }
        }

        return logs
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAmountOfSuccesfulAuthenticationsInOfUser(dniUser: String, dateFrom: String, dateTo: String) : Int {
        if (dateFrom.isEmpty() || dateTo.isEmpty() || dniUser.isEmpty()) {
            return 0
        }

        val db = FirebaseFirestore.getInstance()

        val formatedDateFrom = timestampUtil.stringDateToLocalDate(dateFrom)
        val startOfDayOfFromDate = formatedDateFrom.atStartOfDay()

        val formatedDateTo = timestampUtil.stringDateToLocalDate(dateTo)
        val endOfDayOfToDate = formatedDateTo.atTime(23, 59, 59)

        val collectionRef = db.collection(LOGS_COLLECTION_NAME)

        return collectionRef.
        whereEqualTo("dniUserAffected", dniUser).
        whereEqualTo("logEventName", "USER SUCCESSFUL AUTHENTICATION IN").
        whereGreaterThanOrEqualTo("timestamp", Timestamp(timestampUtil.asDate(startOfDayOfFromDate))).
        whereLessThanOrEqualTo("timestamp", Timestamp(timestampUtil.asDate(endOfDayOfToDate))).
        get().
        await()
            .count()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAmountOfSuccesfulAuthenticationsOutOfUser(dniUser: String, dateFrom: String, dateTo: String) : Int {
        if (dateFrom.isEmpty() || dateTo.isEmpty() || dniUser.isEmpty()) {
            return 0
        }

        val db = FirebaseFirestore.getInstance()

        val formatedDateFrom = timestampUtil.stringDateToLocalDate(dateFrom)
        val startOfDayOfFromDate = formatedDateFrom.atStartOfDay()

        val formatedDateTo = timestampUtil.stringDateToLocalDate(dateTo)
        val endOfDayOfToDate = formatedDateTo.atTime(23, 59, 59)

        val collectionRef = db.collection(LOGS_COLLECTION_NAME)

        return collectionRef.
        whereEqualTo("dniUserAffected", dniUser).
        whereEqualTo("logEventName", "USER SUCCESSFUL AUTHENTICATION OUT").
        whereGreaterThanOrEqualTo("timestamp", Timestamp(timestampUtil.asDate(startOfDayOfFromDate))).
        whereLessThanOrEqualTo("timestamp", Timestamp(timestampUtil.asDate(endOfDayOfToDate))).
        get().
        await()
            .count()
    }
    fun syncLogsOfflineWithOnline(sqlDb : OfflineDataBaseHelper) {
        val logsToUpload = ArrayList<HashMap<String, Any>>()
        val db = FirebaseFirestore.getInstance()
        val offlineLogs = sqlDb.getListOfLogs()
        for (offlineLog in offlineLogs) {
            val log = createHashmapOfflineLog(
                Log(
                    Log.LogEventType.INFO,
                    Log.LogEventName.valueOf(replaceWhitespacesWithUnderscores(offlineLog.getTipo())),
                    offlineLog.getDniMaster(),
                    offlineLog.getDni(),
                    "",
                    offlineLog.getTimestamp()
                )
            )
            logsToUpload.add(log)
        }
        val colRef = db.collection(LOGS_COLLECTION_NAME)
        var numberOfLogsSyncronized = 0
        db.runTransaction {
            for(log in logsToUpload) {
                val newDocRef = colRef.document()
                newDocRef.set(log)
                newDocRef.update("timestamp", log.get("timestamp"))
                numberOfLogsSyncronized++
            }
        }.addOnSuccessListener {
            sqlDb.deleteAllLogs()
            android.util.Log.e("FIREBASE", "Logs offline sincronizados correctamente! Cantidad de logs sincronizados : $numberOfLogsSyncronized")
        }.addOnFailureListener {
            android.util.Log.e("FIREBASE", "Hubo un error al sincronizar los logs offline")
        }

    }

    private fun replaceWhitespacesWithUnderscores(s : String) : String {
        return s.replace("\\s+".toRegex(), "_")
    }

    fun getSuccessfulAuthenticationsOfDay() : Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection(LOGS_COLLECTION_NAME).
            whereEqualTo("logEventName", "USER SUCCESSFUL AUTHENTICATION").
            whereGreaterThanOrEqualTo("timestamp", Timestamp(timestampUtil.getStartOfDay().time)).
            whereLessThanOrEqualTo("timestamp", Timestamp(timestampUtil.getEndOfDay().time))
                .get()

    }

    fun getSuccessfulInAuthenticationsOfDay() : Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection(LOGS_COLLECTION_NAME).
        whereEqualTo("logEventName", "USER SUCCESSFUL AUTHENTICATION IN").
        whereGreaterThanOrEqualTo("timestamp", Timestamp(timestampUtil.getStartOfDay().time)).
        whereLessThanOrEqualTo("timestamp", Timestamp(timestampUtil.getEndOfDay().time))
            .get()

    }

    fun getSuccessfulOutAuthenticationsOfDay() : Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection(LOGS_COLLECTION_NAME).
        whereEqualTo("logEventName", "USER SUCCESSFUL AUTHENTICATION OUT").
        whereGreaterThanOrEqualTo("timestamp", Timestamp(timestampUtil.getStartOfDay().time)).
        whereLessThanOrEqualTo("timestamp", Timestamp(timestampUtil.getEndOfDay().time))
            .get()

    }

    fun getUnsuccessfulAuthenticationsOfDay() : Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection(LOGS_COLLECTION_NAME).
            whereEqualTo("logEventName", "USER UNSUCCESSFUL AUTHENTICATION").
            whereGreaterThanOrEqualTo("timestamp", Timestamp(timestampUtil.getStartOfDay().time)).
            whereLessThanOrEqualTo("timestamp", Timestamp(timestampUtil.getEndOfDay().time))
            .get()
    }

    fun logEventWithTransaction(db : FirebaseFirestore, transaction : Transaction, logEventType : Log.LogEventType, logEventName: Log.LogEventName, dniRRHH : String, dniNewUser : String, categoryNewUser : String) :  Transaction {
        val log = createHashmapLog(
            Log(
                logEventType,
                logEventName,
                dniRRHH,
                dniNewUser,
                categoryNewUser,
                ""
            )
        )

        val newLogDocRef = db.collection("logs").document()
        return transaction.set(newLogDocRef, log)
    }

    fun logEvent(logEventType : Log.LogEventType, logEventName: Log.LogEventName, dniMasterUser : String, dniNewUser : String, categoryNewUser : String) {
        val db = FirebaseFirestore.getInstance()

        val log = createHashmapLog(
            Log(
                logEventType,
                logEventName,
                dniMasterUser,
                dniNewUser,
                categoryNewUser,
                ""
            )
        )

        db.collection(LOGS_COLLECTION_NAME).add(log)
    }

    private fun createHashmapLog(log : Log): HashMap<String, Any> {
        return hashMapOf(
            "logEventType" to log.logEventType,
            "logEventName" to log.logEventName.value,
            "dniMasterUser" to log.dniMasterUser,
            "dniUserAffected" to log.dniUserAffected,
            "timestamp" to Timestamp(Calendar.getInstance().time).toDate(),
            "category" to log.userCategory
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun createHashmapOfflineLog(log : Log): HashMap<String, Any> {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date =  Timestamp(formatter.parse(log.timestamp)!!).toDate()

        return hashMapOf(
            "logEventType" to log.logEventType,
            "logEventName" to log.logEventName.value,
            "dniMasterUser" to log.dniMasterUser,
            "dniUserAffected" to log.dniUserAffected,
            "timestamp" to date,
            "category" to log.userCategory
        )
    }

}