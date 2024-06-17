package com.biogin.myapplication.ui.rrhh.logs

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.databinding.FragmentLogsRrhhBinding
import com.biogin.myapplication.utils.DatePickerDialog
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.util.Calendar

class LogsRRHHFragment : Fragment() {
    private val logsRepository : LogsRepository = LogsRepository()
    private var _binding: FragmentLogsRrhhBinding? = null
    private lateinit var datePickerDialog: DatePickerDialog
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    @Serializable
    data class Log(val fecha: String, val hora: String, val idAutenticador:
    Int, val idVisitante : Int, val lugarFisico: String)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(LogsRRHHViewModel::class.java)
        datePickerDialog = DatePickerDialog()
        _binding = FragmentLogsRrhhBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.tableDetailedLogsRrhhView.visibility = View.VISIBLE
        binding.nonDetailedLogsRrhhView.visibility = View.GONE

        val logDetailOptions = resources.getStringArray(R.array.rrhh_logs_detail_options)
        binding.spinnerFilterDetailOption.adapter = ArrayAdapter(root.context, R.layout.spinner_item_logs_rrhh, logDetailOptions.toList())

        setRecyclerView()
        binding.filterFechaDesdeLogsRrhh.setOnClickListener {
            datePickerDialog.showDatePickerDialog(binding.filterFechaDesdeLogsRrhh, null, binding.root.context){
                binding.filterFechaHastaLogsRrhh.setText("")
                binding.filterFechaHastaLogsRrhh.isEnabled = true
                binding.filterFechaHastaLogsRrhh.isClickable = true
            }
        }

        binding.filterFechaHastaLogsRrhh.setOnClickListener {
            datePickerDialog.showDatePickerDialog(binding.filterFechaHastaLogsRrhh, null, binding.filterFechaDesdeLogsRrhh.text.toString().toCalendarDate().timeInMillis, binding.root.context) {}
        }

        binding.btnFilterLogsRrhh.setOnClickListener {
            val detailOption = binding.spinnerFilterDetailOption.selectedItem.toString()
            if (detailOption == "Con detalle") {
                filterDetailedLogs(binding.filterDniUserAffected.text.toString(), binding.filterFechaDesdeLogsRrhh.text.toString(), binding.filterFechaHastaLogsRrhh.text.toString())
            } else if (detailOption == "Sin detalle") {
                filterNonDetailedLogs(binding.filterDniUserAffected.text.toString(), binding.filterFechaDesdeLogsRrhh.text.toString(), binding.filterFechaHastaLogsRrhh.text.toString())
            }
        }

        binding.btnShowAllLogsRrhh.setOnClickListener {
            showAllDetailedLogs()
        }

        binding.btnClearFiltersLogsRrhh.setOnClickListener {
            clearFilters()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        clearFilters()
        enableDetailedView()
        showAllDetailedLogs()
    }
    private fun String.toCalendarDate(): Calendar {
        val splitDate = split("/")
        val year = splitDate[2].toInt()
        val month = splitDate[1].toInt() - 1 // Month in Calendar is 0-based
        val day = splitDate[0].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar
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

    private fun showAllDetailedLogs() {
        enableDetailedView()
        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerViewLogs)
        var logs : List<com.biogin.myapplication.logs.Log> = ArrayList()

        runBlocking {
            logs = logsRepository.getAllLogs()
        }
        val adapter = LogsAdapter(binding.root.context, logs)
        recyclerView.adapter = adapter
        clearFilters()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterDetailedLogs(dniUser : String, dateFrom : String, dateTo : String) {
        enableDetailedView()
        lateinit var adapter : LogsAdapter
        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerViewLogs)
        var logs : List<com.biogin.myapplication.logs.Log>

        if (dniUser.isNotEmpty() && dateFrom.isNotEmpty() && dateTo.isNotEmpty()) {
            runBlocking {
                logs = logsRepository.getAllLogsFromUserByDate(dniUser, dateFrom, dateTo)
            }
        }
        else if (dniUser.isNotEmpty() && dateFrom.isEmpty() && dateTo.isEmpty()) {
            runBlocking {
                logs = logsRepository.getAllLogsFromUser(dniUser)
            }
        }
        else {
            logs =  ArrayList()
        }

        adapter = LogsAdapter(binding.root.context, logs)
        recyclerView.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterNonDetailedLogs(dniUser : String, dateFrom : String, dateTo : String) {
        enableNonDetailedView()
        var amountOfInSuccesfulAuths : Int
        var amountOfOutSuccesfulAuths : Int
        runBlocking {
            amountOfInSuccesfulAuths = logsRepository.getAmountOfSuccesfulAuthenticationsInOfUser(dniUser, dateFrom, dateTo)
            amountOfOutSuccesfulAuths = logsRepository.getAmountOfSuccesfulAuthenticationsOutOfUser(dniUser, dateFrom, dateTo)
        }

        binding.amountOfInSuccessfulAuthsUserRrhh.text = amountOfInSuccesfulAuths.toString()
        binding.amountOfOutSuccessfulAuthsUserRrhh.text = amountOfOutSuccesfulAuths.toString()
    }

    private fun enableDetailedView() {
        binding.tableDetailedLogsRrhhView.visibility = View.VISIBLE
        binding.nonDetailedLogsRrhhView.visibility = View.GONE
    }

    private fun enableNonDetailedView() {
        binding.tableDetailedLogsRrhhView.visibility = View.GONE
        binding.nonDetailedLogsRrhhView.visibility = View.VISIBLE
    }
    private fun clearFilters() {
        binding.filterFechaDesdeLogsRrhh.setText("")
        binding.filterFechaHastaLogsRrhh.setText("")
        binding.filterFechaHastaLogsRrhh.isEnabled = false
        binding.filterFechaHastaLogsRrhh.isClickable = false
        binding.filterDniUserAffected.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}