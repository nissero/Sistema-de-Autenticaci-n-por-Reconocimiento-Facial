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
import com.google.zxing.integration.android.IntentIntegrator
import java.time.LocalDate
import java.time.LocalTime

class OfflineDataBaseHelper(context: Context) : SQLiteOpenHelper(context, "OfflineDb", null, 1){
    override fun onCreate(db: SQLiteDatabase) {
        val createBarcodes = "CREATE TABLE IF NOT EXISTS Barcodes(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT)"
        val createUsers = "CREATE TABLE IF NOT EXISTS Users(dni TEXT PRIMARY KEY, apellido TEXT)"
        val createOfflineLogs = "CREATE TABLE IF NOT EXISTS OfflineLogs(id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, dni TEXT, apellido TEXT, fecha DATE, hora TIME, FOREIGN KEY(dni) REFERENCES Users(dni))"

        db.execSQL(createBarcodes)
        db.execSQL(createUsers)
        db.execSQL(createOfflineLogs)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun registerUser(dni: String, apellido: String){
        if (!checkInDatabase(dni)){
            saveDataToDatabase(dni, apellido)
            Log.d(TAG, "GUARDADO EN LA BASE DE DATOS EXITOSAMENTE")
        } else {
            Log.e(TAG, "ERROR AL GUARDAR EN LA BASE DE DATOS")
        }
    }

    @SuppressLint("Recycle")
    private fun checkInDatabase(dni: String): Boolean {
        val db = readableDatabase
        val result = db.rawQuery("SELECT 1 FROM Users WHERE dni='$dni'", null)
        val exists = result.moveToFirst()
        result.close()
        return exists
    }

    fun getApellido(dni: String){
        val db = readableDatabase
        val query = "SELECT apellido FROM Users WHERE dni = ?"
        val cursor = db.rawQuery(query, arrayOf(dni))

        var apellido: String? = null
        if (cursor.moveToFirst()) {
            apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido"))
        }
        cursor.close()

        Log.d(TAG, "$apellido")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerLog(type: String, dni: String, apellido: String) {
        val db = writableDatabase
        val sql = "INSERT INTO OfflineLogs(tipo, dni, apellido, fecha, hora) VALUES(?, ?, ?, ?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, type)
        statement.bindString(2, dni)
        statement.bindString(3, apellido)
        statement.bindString(4, currentDate().toString())
        statement.bindString(5, currentTime().toString())
        statement.executeInsert()
    }

    private fun saveDataToDatabase(dni: String, apellido: String) {
        val db = writableDatabase
        val sql = "INSERT INTO Users(dni, apellido) VALUES(?, ?)"
        val statement = db.compileStatement(sql)
        statement.bindString(1, dni)
        statement.bindString(2, apellido)
        statement.executeInsert()
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