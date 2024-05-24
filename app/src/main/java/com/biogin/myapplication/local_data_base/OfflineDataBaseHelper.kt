package com.biogin.myapplication.local_data_base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.toLowerCase
import com.google.zxing.integration.android.IntentIntegrator
import java.time.LocalDate
import java.time.LocalTime

class OfflineDataBaseHelper(context: Context) : SQLiteOpenHelper(context, "OfflineDb", null, 1){
    override fun onCreate(db: SQLiteDatabase) {
        val createBarcodes = "CREATE TABLE IF NOT EXISTS Barcodes(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT)"
        val createUsers = "CREATE TABLE IF NOT EXISTS Users(dni TEXT PRIMARY KEY)"
        val createSecurityMember = "CREATE TABLE IF NOT EXISTS SecurityMember(dni TEXT PRIMARY KEY)"
        val createOfflineLogs = "CREATE TABLE IF NOT EXISTS OfflineLogs(id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, dniMaster TEXT, dni TEXT, fecha DATE, hora TIME, FOREIGN KEY (dni) REFERENCES Users(dni), FOREIGN KEY(dniMaster) REFERENCES SecurityMember(dni))"

        db.execSQL(createBarcodes)
        Log.d(TAG, "Table Barcodes created")
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
        if (!checkInDatabase(dni)){
            saveUserDataToDatabase(dni)
            Log.d(TAG, "GUARDADO EN LA BASE DE DATOS EXITOSAMENTE")
            return true
        } else {
            Log.e(TAG, "ERROR AL GUARDAR EN LA BASE DE DATOS")
            return false
        }
    }

    fun registerSecurity(dni: String): Boolean{
        if (!checkIfSecurity(dni)){
            saveUserDataToSecurity(dni)
            Log.d(TAG, "GUARDADO EN LA BASE DE DATOS COMO SEGURIDAD EXITOSAMENTE")
            return true
        } else {
            Log.e(TAG, "ERROR AL GUARDAR EN SEGURIDAD")
            return false
        }
    }

    @SuppressLint("Recycle")
    fun checkInDatabase(dni: String): Boolean {
        val db = readableDatabase
        val result = db.rawQuery("SELECT 1 FROM Users WHERE dni='$dni'", null)
        val exists = result.moveToFirst()
        result.close()
        return exists
    }

    fun checkIfSecurity(dni: String): Boolean {
        val db = readableDatabase
        val result = db.rawQuery("SELECT 1 FROM SecurityMember WHERE dni='$dni'", null)
        val exists = result.moveToFirst()
        result.close()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun registerLog(tipo: String, dni: String, dniMaster: String): Boolean {
        if (checkInDatabase(dni) && checkIfSecurity(dniMaster)){
            val db = writableDatabase
            val sql = "INSERT INTO OfflineLogs(tipo, dniMaster, dni, fecha, hora) VALUES(?, ?, ?, ?, ?)"
            val statement = db.compileStatement(sql)
            statement.bindString(1, tipo)
            statement.bindString(2, dniMaster)
            statement.bindString(3, dni)
            statement.bindString(4, currentDate().toString())
            statement.bindString(5, currentTime().toString())
            statement.executeInsert()
            Log.d(TAG, "LOG REGISTRADO CORRECTAMENTE")
            return true
        } else{
            Log.e(TAG, "ERROR AL REGISTRAR EL LOG")
            return false
        }
    }

    private fun saveUserDataToDatabase(dni: String) {
        val db = writableDatabase
        val sql = "INSERT INTO Users(dni) VALUES(?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, dni)
        statement.executeInsert()
    }

    private fun saveUserDataToSecurity(dni: String) {
        val db = writableDatabase
        val sql = "INSERT INTO SecurityMember(dni) VALUES(?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, dni)
        statement.executeInsert()
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
                val fecha = cursor.getString(cursor.getColumnIndex("fecha"))
                val hora = cursor.getString(cursor.getColumnIndex("hora"))

                logs.append("Tipo: $tipo, Dni Maestro: $dniMaster, Dni: $dni, Fecha: $fecha, Hora: $hora\n")
                cursor.moveToNext()
            }
        } else {
            logs.append("No hay registros en la tabla OfflineLogs")
        }
        cursor.close()
        return logs.toString()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun currentDate(): LocalDate {
        return LocalDate.now()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun currentTime(): LocalTime {
        return LocalTime.now()
    }

    companion object {
        private const val TAG = "OfflineDataBaseHelper"
    }


}