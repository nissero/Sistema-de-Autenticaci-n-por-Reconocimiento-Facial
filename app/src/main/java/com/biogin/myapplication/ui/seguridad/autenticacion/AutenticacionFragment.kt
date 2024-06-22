package com.biogin.myapplication.ui.seguridad.autenticacion

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.biogin.myapplication.FaceRecognitionActivity
import com.biogin.myapplication.OfflineLogInActivity
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.FragmentAutenticacionBinding
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper
import com.biogin.myapplication.utils.ConnectionCheck
import java.time.LocalDate
import com.biogin.myapplication.logs.Log as LogClass

class AutenticacionFragment : Fragment() {

    private var _binding: FragmentAutenticacionBinding? = null
    private lateinit var autenticacionButton: Button
    private lateinit var autenticacionOfflineButton: Button
    private lateinit var turnoButton: Button
    private lateinit var mensaje: TextView
    private lateinit var dniMaster: String
    private lateinit var logsRepository : LogsRepository
    private lateinit var connectionCheck: ConnectionCheck


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPref = this.activity?.getSharedPreferences("turno", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()

        _binding = FragmentAutenticacionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        dniMaster = this.activity?.intent?.getStringExtra("dniMaster") ?: ""
        Log.d("AUTENTICATIONFRAGMENT", dniMaster)
        logsRepository = LogsRepository()

        val dniSeguridadActual = binding.dniSeguridad
        dniSeguridadActual.text = dniMaster

        autenticacionButton = root.findViewById(R.id.button_visitantes)

        autenticacionOfflineButton = root.findViewById(R.id.button_visitantes_offline)

        turnoButton = root.findViewById(R.id.button_turno)

        connectionCheck = ConnectionCheck(requireActivity())

        mensaje = root.findViewById(R.id.message_main_screen)

        var turnoIniciado = sharedPref?.getBoolean("turnoIniciado", false)

        if(turnoIniciado == true) {
            turnoButton.text = this.context?.getString(R.string.finalizar_turno)
            mensaje.text = this.context?.getString(R.string.mensaje_turno_iniciado)
            autenticacionButton.visibility = View.VISIBLE
            autenticacionOfflineButton.visibility = View.VISIBLE
        } else {
            turnoButton.text = this.context?.getString(R.string.iniciar_turno)
            mensaje.text = this.context?.getString(R.string.mansaje_turno_no_iniciado)
            autenticacionButton.visibility = View.INVISIBLE
            autenticacionOfflineButton.visibility = View.INVISIBLE
        }

        autenticacionButton.setOnClickListener {
            val intent = Intent(root.context, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "visitante")
            startActivity(intent)
        }

        autenticacionOfflineButton.setOnClickListener{
            val intent = Intent(root.context, OfflineLogInActivity::class.java)
            intent.putExtra("authenticationType", "visitante")
            intent.putExtra("dniMaster", dniMaster)
            startActivity(intent)
        }

        turnoButton.setOnClickListener {
            if(turnoIniciado == false) {
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                editor?.apply {
                                    putBoolean("turnoIniciado", true)
                                    putString("fecha", LocalDate.now().toString())
                                    commit()
                                }
                                turnoButton.text = this.context?.getString(R.string.finalizar_turno)
                                autenticacionButton.visibility = View.VISIBLE
                                mensaje.text = this.context?.getString(R.string.mensaje_turno_iniciado)
                                autenticacionOfflineButton.visibility = View.VISIBLE

                                turnoIniciado = sharedPref?.getBoolean("turnoIniciado", false)

                                if (connectionCheck.isOnlineNet()){
                                    logsRepository.logEvent(LogClass.LogEventType.INFO, LogClass.LogEventName.START_OF_SHIFT, dniMaster, "", MasterUserDataSession.getCategoryUser())
                                } else {
                                    val database = OfflineDataBaseHelper(requireActivity())
                                    database.startOfShift(dniMaster)
                                }
                            }
                        }
                }

                val builder = AlertDialog.Builder(context)
                builder.setMessage("Quiere iniciar el turno?").setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            } else {
                val dialogClickListener =
                    DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                if (connectionCheck.isOnlineNet()){
                                    logsRepository.logEvent(LogClass.LogEventType.INFO, LogClass.LogEventName.END_OF_SHIFT, dniMaster, "", MasterUserDataSession.getCategoryUser())
                                } else {
                                    val database = OfflineDataBaseHelper(requireActivity())
                                    database.endOfShift(dniMaster)
                                }
                                editor?.apply {
                                    putBoolean("turnoIniciado", false)
                                    commit()
                                }
                                autenticacionButton.visibility = View.INVISIBLE
                                autenticacionOfflineButton.visibility = View.INVISIBLE
                                turnoButton.text = this.context?.getString(R.string.iniciar_turno)
                                mensaje.text = this.context?.getString(R.string.mansaje_turno_no_iniciado)

                                turnoIniciado = sharedPref?.getBoolean("turnoIniciado", false)
                            }
                        }
                    }

                val builder = AlertDialog.Builder(context)
                builder.setMessage("Quiere finalizar el turno?").setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}