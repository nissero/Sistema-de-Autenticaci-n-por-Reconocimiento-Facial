package com.biogin.myapplication.ui.rrhh.logs

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.databinding.FragmentLogsRrhhBinding
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

class LogsRRHHFragment : Fragment() {
    private val logsRepository : LogsRepository = LogsRepository()
    private var _binding: FragmentLogsRrhhBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Serializable
    data class Log(val fecha: String, val hora: String, val idAutenticador:
    Int, val idVisitante : Int, val lugarFisico: String)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(LogsRRHHViewModel::class.java)

        _binding = FragmentLogsRrhhBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var listaRegistros : List<com.biogin.myapplication.logs.Log>
        runBlocking {
            listaRegistros = logsRepository.GetAllLogs()
        }

        var tablaRegistros = root.findViewById<TableLayout>(R.id.tablaRegistrosRRHH)

        for(registro in listaRegistros) {
            var fila = TableRow(root.context)

            var fechaHora = TextView(root.context)
            fechaHora.gravity = Gravity.CENTER_HORIZONTAL
            fechaHora.text = registro.timestamp
            fechaHora.textSize = 11.0F
            fila.addView(fechaHora)

            var logType = TextView(root.context)
            logType.text = registro.logEventName.name
            logType.gravity = Gravity.CENTER_HORIZONTAL
            logType.textSize = 11.0F
            fila.addView(logType)

            var dniRRHH = TextView(root.context)
            dniRRHH.text = registro.dniMasterUser
            dniRRHH.gravity = Gravity.CENTER_HORIZONTAL
            dniRRHH.textSize = 11.0F
            fila.addView(dniRRHH)

            var dniUserAffected = TextView(root.context)
            dniUserAffected.text = registro.dniUserAffected
            dniUserAffected.textSize = 11.0F
            dniUserAffected.gravity = Gravity.CENTER_HORIZONTAL
            fila.addView(dniUserAffected)

            var category = TextView(root.context)
            category.text = registro.userCategory
            category.textSize = 11.0F
            category.gravity = Gravity.CENTER_HORIZONTAL
            fila.addView(category)

            tablaRegistros.addView(fila)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}