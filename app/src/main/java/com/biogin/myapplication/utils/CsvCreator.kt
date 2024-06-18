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
    private val timestampDateUtil = TimestampDateUtil()
    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSaveCsvFileToExportLogs(context : Context, logData: List<Log>) {
        if (logData.isEmpty()) {
            throw Exception("No se puede exportar un CSV vacío")
        }
        Thread {
            val fileName =
                "${logData[0].dniUserAffected}_${timestampDateUtil.nowAsStringFileFormat()}.csv"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            android.util.Log.e(
                "CSV Creator",
                "Ubicacion CSV ${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}"
            )
            android.util.Log.e("CSV Creator", "File name CSV export $fileName")

            try {
                FileWriter(file).use { writer ->
                    writer.append("Fecha,Tipo log,Nombre Log,Dni usuario afectado,Dni usuario maestro,Categoria usuario afectado\n")
                    for (log in logData) {
                        writer.append("${timestampDateUtil.utcTimestampToLocalString(log.timestamp)},${log.logEventType},${log.logEventName.value},${log.dniUserAffected},${log.dniMasterUser},${log.userCategory}\n")
                    }
                }
                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.addCompletedDownload(
                    fileName,
                    "Reporte logs",
                    true,
                    "application/json",
                    file.absolutePath,
                    file.length(),
                    true
                )
                android.util.Log.e("CSV Creator", "CSV exportado exitosamente")

            } catch (e: Exception) {
                android.util.Log.e("CSV Creator", "Error al exportar el CSV de logs: $e")
            }
        }.start()
    }

}


