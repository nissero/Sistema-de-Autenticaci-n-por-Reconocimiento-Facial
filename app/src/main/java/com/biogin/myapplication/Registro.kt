package com.biogin.myapplication

import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalTime

class Registro(private val tipo: String, private val dniMaster: String, private val dni: String, private val timestamp:  String) {

        fun getTipo(): String {
                return tipo
        }

        fun getDniMaster(): String {
                return dniMaster
        }

        fun getDni(): String {
                return dni
        }

        fun getTimestamp(): String {
                return timestamp
        }
}