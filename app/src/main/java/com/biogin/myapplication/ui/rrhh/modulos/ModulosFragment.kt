package com.biogin.myapplication.ui.rrhh.modulos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.databinding.FragmentModulosBinding

class ModulosFragment : Fragment() {

    private var _binding: FragmentModulosBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(ModulosViewModel::class.java)

        _binding = FragmentModulosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonAdd.setOnClickListener {

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}