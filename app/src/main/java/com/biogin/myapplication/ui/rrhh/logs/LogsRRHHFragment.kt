package com.biogin.myapplication.ui.rrhh.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        _binding = FragmentLogsRrhhBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setRecyclerView()

        return root
    }


    private fun setRecyclerView() {
        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerViewLogs)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(binding.root.context)

        var listaLogs : List<com.biogin.myapplication.logs.Log>
        runBlocking {
            listaLogs = logsRepository.getAllLogs()
        }
        val adapter = LogsAdapter(binding.root.context, listaLogs)
        recyclerView.adapter = adapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}