package com.biogin.myapplication.ui.seguridad.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
        _binding = FragmentLogsSecurityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textViewAmountOfInSuccessfulAuths =
            root.findViewById<TextView>(R.id.amount_of_in_successful_auths)
        val textViewAmountOfOutSuccessfulAuths =
            root.findViewById<TextView>(R.id.amount_of_out_successful_auths)
        val textViewAmountOfUnsuccessfulAuths =
            root.findViewById<TextView>(R.id.amount_of_unsuccessful_auths)

        val dniSeguridadActual = binding.dniSeguridad
        dniSeguridadActual.text = this.activity?.intent?.getStringExtra("dniMaster") ?: ""

        logsRepository.getSuccessfulInAuthenticationsOfDay().addOnSuccessListener { queryResult ->
            android.util.Log.e("Firebase", "Ingresos exitosos del dia: ${queryResult.size()}")
            textViewAmountOfInSuccessfulAuths.text = queryResult.size().toString()
        }.addOnFailureListener { ex ->
            android.util.Log.e("Firebase", ex.toString())
        }

        logsRepository.getSuccessfulOutAuthenticationsOfDay().addOnSuccessListener { queryResult ->
            android.util.Log.e("Firebase", "Egresos exitosos del dia: ${queryResult.size()}")
            textViewAmountOfOutSuccessfulAuths.text = queryResult.size().toString()
        }.addOnFailureListener { ex ->
            android.util.Log.e("Firebase", ex.toString())
        }

        logsRepository.getUnsuccessfulAuthenticationsOfDay().addOnSuccessListener { queryResult ->
            android.util.Log.e("Firebase", "Autenticaciones fallidas del dia: ${queryResult.size()}")
            textViewAmountOfUnsuccessfulAuths.text = queryResult.size().toString()
        }.addOnFailureListener { ex ->
            android.util.Log.e("Firebase", ex.toString())
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}