package com.biogin.myapplication.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.biogin.myapplication.logs.Log
import java.io.File
import java.io.FileWriter

class CsvCreator {
    val timestampDateUtil = TimestampDateUtil()
    @RequiresApi(Build.VERSION_CODES.O)
    public fun createAndSaveCsvFileDetailedLogs(context : Context, logData: List<Log>) {
        if (logData.isEmpty()) {
            throw Exception("No se puede exportar un CSV vacío")
        }
        Thread(Runnable {
            val fileName = "reporte_con_detalle_${timestampDateUtil.nowAsStringFileFormat()}.csv"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            android.util.Log.e("CSV Creator", "Ubicacion CSV ${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}")
            android.util.Log.e("CSV Creator", "File name CSV export $fileName")

            try {
                FileWriter(file).use { writer ->
                    writer.append("Fecha,Tipo log,Nombre Log,Dni usuario afectado,Dni usuario maestro,Categoria usuario afectado\n")
                    for (log in logData) {
                        writer.append("${log.timestamp},${log.logEventType},${log.logEventName.value},${log.dniUserAffected},${log.dniMasterUser},${log.userCategory}\n")
                    }
                }
                val downloadManager =   context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.addCompletedDownload(fileName, "Reporte logs", true, "application/json", file.getAbsolutePath(),file.length(),true)
                android.util.Log.e("CSV Creator", "CSV exportado exitosamente")

            } catch (e: Exception) {
                android.util.Log.e("CSV Creator", "Error al exportar el CSV de logs: $e")
            }
        }).start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun createAndSaveCsvFileNonDetailedLogs(context : Context, dniUserAffected : String, dniMasterUser : String, categoryUserAffected : String, dateFrom : String, dateTo : String, amountOfSuccessfulInAuths : String, amountOfSuccessfulOutAuths : String) {
        if (amountOfSuccessfulInAuths == "0" && amountOfSuccessfulOutAuths == "0") {
            throw Exception("No se puede exportar el CSV sin datos")
        }
        val fechaActual = timestampDateUtil.nowAsStringFileFormat()
        Thread(Runnable {
            val fileName = "${dniUserAffected}_reporte_sin_detalle_${fechaActual}.csv"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            android.util.Log.e("CSV Creator", "Ubicacion CSV ${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}")
            android.util.Log.e("CSV Creator", "File name CSV export $fileName")

            try {
                FileWriter(file).use { writer ->
                    writer.append("Fecha reporte,Dni user afectado,Dni user maestro,Categoria user afectado,Desde,Hasta\n")
                    writer.append("${fechaActual},${dniUserAffected},${dniMasterUser},${categoryUserAffected},${dateFrom},${dateTo}\n")
                    writer.append("\n")
                    writer.append("Cantidad ingresos exitosos,Cantidad egresos exitosos\n")
                    writer.append("${amountOfSuccessfulInAuths},${amountOfSuccessfulOutAuths}\n")
                }
                val downloadManager =   context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.addCompletedDownload(fileName, "Reporte logs", true, "application/json", file.getAbsolutePath(),file.length(),true)
                android.util.Log.e("CSV Creator", "CSV exportado exitosamente")

            } catch (e: Exception) {
                android.util.Log.e("CSV Creator", "Error al exportar el CSV de logs: $e")
            }
        }).start()
    }


}


