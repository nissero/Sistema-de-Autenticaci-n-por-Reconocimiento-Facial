package com.biogin.myapplication.ui.rrhh.abm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.FragmentAbmBinding
import com.biogin.myapplication.ui.login.RegisterActivity

class ABMFragment : Fragment() {

    private var _binding: FragmentAbmBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(ABMViewModel::class.java)

        _binding = FragmentAbmBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        val register = root.findViewById<Button>(R.id.button_register)
        register.setOnClickListener {
            val intent = Intent(root.context, RegisterActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}