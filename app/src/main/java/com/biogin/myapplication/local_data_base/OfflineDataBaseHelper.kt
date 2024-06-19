package com.biogin.myapplication.local_data_base

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.biogin.myapplication.Registro
import java.sql.SQLException


import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OfflineDataBaseHelper(context: Context) : SQLiteOpenHelper(context, "OfflineDb", null, 1){
    override fun onCreate(db: SQLiteDatabase) {
        val createUsers = "CREATE TABLE IF NOT EXISTS Users(dni TEXT PRIMARY KEY)"
        val createSecurityMember = "CREATE TABLE IF NOT EXISTS SecurityMember(dni TEXT PRIMARY KEY)"
        val createOfflineLogs = "CREATE TABLE IF NOT EXISTS OfflineLogs(id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, dniMaster TEXT, dni TEXT, timestamp TIMESTAMP, FOREIGN KEY (dni) REFERENCES Users(dni), FOREIGN KEY(dniMaster) REFERENCES SecurityMember(dni))"

        db.execSQL(createUsers)
        Log.d(TAG, "Table Users created")
        db.execSQL(createSecurityMember)
        Log.d(TAG, "Table SecurityMember created")
        db.execSQL(createOfflineLogs)
        Log.d(TAG, "Table OfflineLogs created")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun registerUser(dni: String): Boolean{
        return if (!checkInDatabase(dni)){
            saveUserDataToDatabase(dni)
            Log.d(TAG, "GUARDADO EN LA BASE DE DATOS EXITOSAMENTE")
            true
        } else {
            Log.e(TAG, "ERROR AL GUARDAR EN LA BASE DE DATOS")
            false
        }
    }

    fun registerSecurity(dni: String): Boolean{
        return if (!checkIfSecurity(dni)){
            saveUserDataToSecurity(dni)
            Log.d(TAG, "GUARDADO EN LA BASE DE DATOS COMO SEGURIDAD EXITOSAMENTE")
            true
        } else {
            Log.e(TAG, "ERROR AL GUARDAR EN SEGURIDAD")
            false
        }
    }

    @SuppressLint("Recycle")
    fun checkInDatabase(dni: String): Boolean {
        val db = readableDatabase
        val result = db.rawQuery("SELECT 1 FROM Users WHERE dni='$dni'", null)
        val exists = result.moveToFirst()
        db.close()
        return exists
    }

    fun checkIfSecurity(dni: String): Boolean {
        val db = readableDatabase
        val result = db.rawQuery("SELECT 1 FROM SecurityMember WHERE dni='$dni'", null)
        val exists = result.moveToFirst()
        result.close()
        db.close()
        return exists
    }

    fun deleteUserByDni(dni: String) {
        val db = writableDatabase
        val selection = "dni = ?"
        val selectionArgs = arrayOf(dni)
        val deletedRows = db.delete("Users", selection, selectionArgs)
        db.close()

        if (deletedRows > 0) {
            Log.d(TAG, "Usuario con DNI $dni eliminado exitosamente.")
        } else {
            Log.e(TAG, "No se encontró el usuario con DNI $dni.")
        }
    }

    fun deleteSecurityByDni(dni: String) {
        val db = writableDatabase
        val selection = "dni = ?"
        val selectionArgs = arrayOf(dni)
        val deletedRows = db.delete("SecurityMember", selection, selectionArgs)
        db.close()

        if (deletedRows > 0) {
            Log.d(TAG, "Usuario con DNI $dni eliminado exitosamente.")
        } else {
            Log.e(TAG, "No se encontró el usuario con DNI $dni.")
        }
    }

    @SuppressLint("Range")
    fun getAllUsers(): String {
        val users = StringBuilder()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Users", null)

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val dni = cursor.getString(cursor.getColumnIndex("dni"))

                users.append("Dni: $dni \n")
                cursor.moveToNext()
            }
        } else {
            users.append("No hay registros en la tabla Users")
        }
        cursor.close()
        return users.toString()
    }

    @SuppressLint("Range")
    fun getAllSecurity(): String {
        val security = StringBuilder()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM SecurityMember", null)

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val dni = cursor.getString(cursor.getColumnIndex("dni"))

                security.append("Dni: $dni \n")
                cursor.moveToNext()
            }
        } else {
            security.append("No hay registros en la tabla Security")
        }
        cursor.close()
        return security.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registerAuthenticationLog(dni: String, dniMaster: String): Boolean {
        return if (checkInDatabase(dni)) {
            val db = writableDatabase
            val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
            val statement = db.compileStatement(sql)
            statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.USER_SUCCESSFUL_AUTHENTICATION.value)
            statement.bindString(2, dniMaster)
            statement.bindString(3, dni)
            statement.bindString(4, currentTimeStamp())
            statement.executeInsert()
            Log.d(TAG, "DNI USER: $dni")
            Log.d(TAG, "LOG REGISTRADO CORRECTAMENTE")
            db.close()
            true
        } else {
            val db = writableDatabase
            val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
            val statement = db.compileStatement(sql)
            statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.USER_UNSUCCESSFUL_AUTHENTICATION.value)
            statement.bindString(2, dniMaster)
            statement.bindString(3, dni)
            statement.bindString(4, currentTimeStamp())
            statement.executeInsert()
            Log.e(TAG, "ERROR AL REGISTRAR EL LOG")
            db.close()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registerSecurityAuthenticationLog(dni: String): Boolean {
        if (checkIfSecurity(dni)){
            val db = writableDatabase
            val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
            val statement = db.compileStatement(sql)
            statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.SECURITY_SUCCESSFUL_LOGIN.value)
            statement.bindString(2, dni)
            statement.bindString(3, "")
            statement.bindString(4, currentTimeStamp())
            statement.executeInsert()
            Log.d(TAG, "DNI USER: $dni")
            Log.d(TAG, "LOG SEGURIDAD REGISTRADO CORRECTAMENTE")
            db.close()
            return true
        } else {
            val db = writableDatabase
            val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
            val statement = db.compileStatement(sql)
            statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.SECURITY_UNSUCCESSFUL_LOGIN.value)
            statement.bindString(2, "")
            statement.bindString(3, "")
            statement.bindString(4, currentTimeStamp())
            statement.executeInsert()
            Log.d(TAG, "DNI USER: $dni")
            Log.d(TAG, "LOG SEGURIDAD REGISTRADO CORRECTAMENTE")
            db.close()
            return false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun endOfShift(dniMaster: String) {
        val db = writableDatabase
        val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.END_OF_SHIFT.value)
        statement.bindString(2, dniMaster)
        statement.bindString(3, "")
        statement.bindString(4, currentTimeStamp())
        statement.executeInsert()

        db.close()

        Log.d(TAG, "LOG SEGURIDAD REGISTRADO CORRECTAMENTE")
        Log.d(TAG, "TODOS LOS LOGS: ${getAllLogs()}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startOfShift(dniMaster: String) {
        val db = writableDatabase
        val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.START_OF_SHIFT.value)
        statement.bindString(2, dniMaster)
        statement.bindString(3, "")
        statement.bindString(4, currentTimeStamp())
        statement.executeInsert()

        db.close()

        Log.d(TAG, "LOG SEGURIDAD REGISTRADO CORRECTAMENTE")
        Log.d(TAG, "TODOS LOS LOGS: ${getAllLogs()}")
    }

    @SuppressLint("Range", "Recycle")
    fun getListOfLogs(): List<Registro>{
        val logs = mutableListOf<Registro>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM OfflineLogs", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                val log: Registro
                val tipo = cursor.getString(cursor.getColumnIndex("tipo"))
                val dniMaster = cursor.getString(cursor.getColumnIndex("dniMaster"))
                val dni = cursor.getString(cursor.getColumnIndex("dni"))
                val timestamp = cursor.getString(cursor.getColumnIndex("timestamp"))

                log = Registro(tipo, dniMaster, dni, timestamp)

                logs.add(log)
                cursor.moveToNext()
            }
        }
        db.close()
        return logs
    }

    private fun saveUserDataToDatabase(dni: String) {
        val db = writableDatabase
        val sql = "INSERT INTO Users(dni) VALUES(?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, dni)
        statement.executeInsert()
        db.close()
    }

    private fun saveUserDataToSecurity(dni: String) {
        val db = writableDatabase
        val sql = "INSERT INTO SecurityMember(dni) VALUES(?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, dni)
        statement.executeInsert()
        db.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registerLogForInAuthentication(dni: String, dniMaster: String) {
        val db = writableDatabase
        val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.USER_SUCCESSFUL_AUTHENTICATION_IN.value)
        statement.bindString(2, dniMaster)
        statement.bindString(3, dni)
        statement.bindString(4, currentTimeStamp())
        statement.executeInsert()
        Log.d(TAG, "LOG REGISTRADO CORRECTAMENTE")
        db.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registerLogForOutAuthentication(dni: String, dniMaster: String) {
        val db = writableDatabase
        val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, timestamp) VALUES(?, ?, ?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, com.biogin.myapplication.logs.Log.LogEventName.USER_SUCCESSFUL_AUTHENTICATION_OUT.value)
        statement.bindString(2, dniMaster)
        statement.bindString(3, dni)
        statement.bindString(4, currentTimeStamp())
        statement.executeInsert()
        Log.d(TAG, "LOG REGISTRADO CORRECTAMENTE")
        db.close()
    }

    @SuppressLint("Range")
    fun getAllLogs(): String {
        val logs = StringBuilder()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM OfflineLogs", null)

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val tipo = cursor.getString(cursor.getColumnIndex("tipo"))
                val dniMaster = cursor.getString(cursor.getColumnIndex("dniMaster"))
                val dni = cursor.getString(cursor.getColumnIndex("dni"))
                val timestamp = cursor.getString(cursor.getColumnIndex("timestamp"))

                logs.append("Tipo: $tipo, Dni Maestro: $dniMaster, Dni: $dni, Timestamp: $timestamp")
                cursor.moveToNext()
            }
        } else {
            logs.append("No hay registros en la tabla OfflineLogs")
        }

        cursor.close()
        db.close()
        return logs.toString()
    }

    fun deleteAllLogs() {
        val db = writableDatabase
        val sql = "DELETE FROM OfflineLogs"
        try {
            val statement = db.compileStatement(sql)
            val rowsDeleted = statement.executeUpdateDelete()
            Log.i("SQLite", "Se borraron exitosamente todos los registros de la tabala OfflineLogs | Registros borrados en total: $rowsDeleted")
            db.close()
        } catch (e : SQLException) {
            Log.e("SQLite", "Error al borrar todos los registros de la tabla OfflineLogs")
            db.close()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun currentTimeStamp(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        return current.format(formatter)
    }

    companion object {
        private const val TAG = "OfflineDataBaseHelper"
    }


}