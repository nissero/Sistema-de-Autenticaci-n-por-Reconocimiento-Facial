package com.biogin.myapplication.ui.seguridad.autenticacion

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.FaceRecognitionActivity
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.FragmentAutenticacionBinding

class AutenticacionFragment : Fragment() {

    private var _binding: FragmentAutenticacionBinding? = null
    private var turnoIniciado = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(AutenticacionViewModel::class.java)

        _binding = FragmentAutenticacionBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        val autenticacionButton = root.findViewById<Button>(R.id.button_visitantes)
        autenticacionButton.visibility = View.INVISIBLE
        autenticacionButton.setOnClickListener {
            val intent = Intent(root.context, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "visitante")
            startActivity(intent)
        }

        val turnoButton = root.findViewById<Button>(R.id.button_turno)
        turnoButton.setOnClickListener {
            val mensaje = root.findViewById<TextView>(R.id.message_main_screen)
            if(!turnoIniciado) {
                val dialogClickListener =
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                turnoIniciado = true
                                turnoButton.text = this.context?.getString(R.string.finalizar_turno)
                                autenticacionButton.visibility = View.VISIBLE
//                                mensaje.text = this.context?.getString(R.string.mensaje_turno_iniciado)
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
                                turnoIniciado = false
                                autenticacionButton.visibility = View.INVISIBLE
                                turnoButton.text = this.context?.getString(R.string.iniciar_turno)
                                mensaje.text = this.context?.getString(R.string.mansaje_inicio_turno)
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