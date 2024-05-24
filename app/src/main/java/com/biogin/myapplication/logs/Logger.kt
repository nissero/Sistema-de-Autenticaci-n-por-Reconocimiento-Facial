package com.biogin.myapplication.logs

import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Logger {
    fun createHashmapLog(log : Log): HashMap<String, Any> {
        val date = getCurrentDateTime()
        val dateInString = date.toString("yyyy/MM/dd HH:mm:ss")

        return hashMapOf(
            "logEventType" to log.logEventType,
            "logEventName" to log.logEventName,
            "dniMasterUser" to log.dniMasterUser,
            "dniUserAffected" to log.dniUserAffected,
            "timestamp" to dateInString,
            "category" to log.userCategory
        )
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance(TimeZone.getDefault()).time
    }



}