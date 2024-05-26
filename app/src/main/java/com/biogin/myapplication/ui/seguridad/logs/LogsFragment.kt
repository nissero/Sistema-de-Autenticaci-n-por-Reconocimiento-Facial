package com.biogin.myapplication.ui.seguridad.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.databinding.FragmentLogsSecurityBinding
import kotlinx.serialization.Serializable

class LogsFragment : Fragment() {

    private var _binding: FragmentLogsSecurityBinding? = null
    private val logsRepository: LogsRepository = LogsRepository()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Serializable
    data class Log(
        val fecha: String, val hora: String, val idAutenticador:
        Int, val idVisitante: Int, val lugarFisico: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(LogsViewModel::class.java)

        _binding = FragmentLogsSecurityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textViewAmountOfSuccessfulAuths =
            root.findViewById<TextView>(R.id.amount_of_successful_auths)
        val textViewAmountOfUnsuccessfulAuths =
            root.findViewById<TextView>(R.id.amount_of_unsuccessful_auths)

        logsRepository.getSuccesfulAuthentications().addOnSuccessListener { queryResult ->
            textViewAmountOfSuccessfulAuths.text = queryResult.size().toString()
        }

        logsRepository.getUnsuccesfulAuthentications().addOnSuccessListener { queryResult ->
            textViewAmountOfUnsuccessfulAuths.text = queryResult.size().toString()
        }



        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}