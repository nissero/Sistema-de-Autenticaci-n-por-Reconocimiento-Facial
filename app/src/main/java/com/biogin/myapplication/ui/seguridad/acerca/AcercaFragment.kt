package com.biogin.myapplication.ui.seguridad.acerca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biogin.myapplication.databinding.FragmentAcercaBinding

class AcercaFragment : Fragment() {

    private var _binding: FragmentAcercaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcercaBinding.inflate(inflater, container, false)

        val dniSeguridadActual = binding.dniSeguridad
        dniSeguridadActual.text = this.activity?.intent?.getStringExtra("dniMaster") ?: ""

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}