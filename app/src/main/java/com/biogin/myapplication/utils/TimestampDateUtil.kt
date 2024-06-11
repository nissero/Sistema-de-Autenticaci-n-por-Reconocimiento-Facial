package com.biogin.myapplication.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat

class TimestampDateUtil {

    fun timestampToString(timestamp : Any) : String{
        val threeHoursInMiliseconds = 10800000
        if (timestamp is Timestamp) {
            val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            return sfd.format(timestamp.seconds*1000 - threeHoursInMiliseconds)
        }

        return timestamp.toString()
    }
}