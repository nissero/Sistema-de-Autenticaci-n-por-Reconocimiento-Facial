package com.biogin.myapplication.ui.seguridad.autenticacion

import android.app.Activity
import android.app.AlertDialog
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.FaceRecognitionActivity
import com.biogin.myapplication.OfflineLogInActivity
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.FragmentAutenticacionBinding
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper
import com.biogin.myapplication.utils.ConnectionCheck
import java.net.ConnectException

class AutenticacionFragment : Fragment() {

    private var _binding: FragmentAutenticacionBinding? = null
    private var turnoIniciado = false
    private lateinit var autenticacionButton: Button
    private lateinit var turnoButton: Button
    private lateinit var mensaje: TextView
    private lateinit var dniMaster: String
    private lateinit var logsRepository : LogsRepository
    private lateinit var ConnectionCheck: ConnectionCheck


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(AutenticacionViewModel::class.java)

        _binding = FragmentAutenticacionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val bundle = arguments
        dniMaster = bundle?.getString("dniMaster").toString()
        Log.d("AUTENTICATIONFRAGMENT", dniMaster)
        logsRepository = LogsRepository()

        autenticacionButton = root.findViewById(R.id.button_visitantes)

        turnoButton = root.findViewById(R.id.button_turno)

        ConnectionCheck = ConnectionCheck(requireActivity())

        mensaje = root.findViewById(R.id.message_main_screen)
        if(turnoIniciado) {
            turnoButton.text = this.context?.getString(R.string.finalizar_turno)
            mensaje.text = this.context?.getString(R.string.mensaje_turno_iniciado)
            autenticacionButton.visibility = View.VISIBLE
        } else {
            turnoButton.text = this.context?.getString(R.string.iniciar_turno)
            mensaje.text = this.context?.getString(R.string.mansaje_turno_no_iniciado)
            autenticacionButton.visibility = View.INVISIBLE
        }

        val autenticacionOfflineButton = root.findViewById<Button>(R.id.button_visitantes_offline)
        autenticacionOfflineButton.visibility = View.INVISIBLE

        val turnoButton = root.findViewById<Button>(R.id.button_turno)

        val mensaje = root.findViewById<TextView>(R.id.message_main_screen)


        //metodo para crear una actividad nueva y obtener un resultado
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if(data?.getBooleanExtra("autenticado", false) == true) {
                    turnoIniciado = false
                    autenticacionButton.visibility = View.INVISIBLE
                    autenticacionOfflineButton.visibility = View.INVISIBLE
                    turnoButton.text = this.context?.getString(R.string.iniciar_turno)
                    mensaje.text = this.context?.getString(R.string.mansaje_turno_no_iniciado)
                }
            }
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

            if(!turnoIniciado) {
                val dialogClickListener =
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {DialogInterface.BUTTON_POSITIVE -> {
                                turnoIniciado = true
                                turnoButton.text = this.context?.getString(R.string.finalizar_turno)
                                autenticacionButton.visibility = View.VISIBLE
                                mensaje.text = this.context?.getString(R.string.mensaje_turno_iniciado)
                                autenticacionOfflineButton.visibility = View.VISIBLE

                                if (ConnectionCheck.isOnlineNet()){
                                    logsRepository.LogEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.START_OF_SHIFT,MasterUserDataSession.getDniUser(), "", MasterUserDataSession.getCategoryUser())
                                } else {
                                    val database = OfflineDataBaseHelper(requireActivity())
                                    database.startOfShift(dniMaster)
                                }
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {

                            }
                        }
                    }

                val builder = AlertDialog.Builder(context)
                builder.setMessage("Quiere iniciar el turno?").setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            } else {
                val dialogClickListener =
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                if (ConnectionCheck.isOnlineNet()){
                                    logsRepository.LogEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.END_OF_SHIFT,MasterUserDataSession.getDniUser(), "", MasterUserDataSession.getCategoryUser())
                                } else {
                                    val database = OfflineDataBaseHelper(requireActivity())
                                    database.endOfShift(dniMaster)
                                }

                                val intent = Intent(root.context, FaceRecognitionActivity::class.java)
                                intent.putExtra("authenticationType", "fin de turno")
                                resultLauncher.launch(intent)
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {

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