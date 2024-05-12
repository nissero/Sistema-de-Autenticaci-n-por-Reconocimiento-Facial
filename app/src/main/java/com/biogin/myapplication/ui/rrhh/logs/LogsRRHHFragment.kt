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
import com.biogin.myapplication.databinding.FragmentLogsRrhhBinding
import com.biogin.myapplication.ui.seguridad.logs.LogsFragment
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class LogsRRHHFragment : Fragment() {

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

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        var registrosString: String =
            root.context.assets.open("logs").bufferedReader().use { it.readText() }

        var listaRegistros = Json.decodeFromString<List<LogsFragment.Log>>(registrosString)

        var tablaRegistros = root.findViewById<TableLayout>(R.id.tablaRegistrosRRHH)

        for(i in 0..listaRegistros.size-1) {
            var fila = TableRow(root.context)

            var fecha = TextView(root.context)
            fecha.gravity = Gravity.CENTER_HORIZONTAL
            fecha.text = listaRegistros[i].fecha
            fila.addView(fecha)

            var hora = TextView(root.context)
            hora.text = listaRegistros[i].hora
            hora.gravity = Gravity.CENTER_HORIZONTAL
            fila.addView(hora)

            var idAut = TextView(root.context)
            idAut.text = listaRegistros[i].idAutenticador.toString()
            idAut.gravity = Gravity.CENTER_HORIZONTAL
            fila.addView(idAut)

            var idVis = TextView(root.context)
            idVis.text = listaRegistros[i].idVisitante.toString()
            idVis.gravity = Gravity.CENTER_HORIZONTAL
            fila.addView(idVis)

            var lugar = TextView(root.context)
            lugar.text = listaRegistros[i].lugarFisico
            lugar.gravity = Gravity.CENTER_HORIZONTAL
            fila.addView(lugar)

            tablaRegistros.addView(fila)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}