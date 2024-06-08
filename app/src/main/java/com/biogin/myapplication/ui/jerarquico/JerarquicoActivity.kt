package com.biogin.myapplication.ui.jerarquico

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.ActivityAbmAreaBinding
import com.biogin.myapplication.databinding.ActivityJerarquicoBinding
import com.biogin.myapplication.utils.DialogUtils
import com.biogin.myapplication.utils.HierarchicalUtils
import com.biogin.myapplication.utils.PopUpUtils

class JerarquicoActivity : AppCompatActivity() {
    private var hierarchicalUtils = HierarchicalUtils()
    private lateinit var binding: ActivityJerarquicoBinding
    private val dialogUtils = DialogUtils()
    private val popUpUtils = PopUpUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJerarquicoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mailInput = binding.textInputMail
//        val currentMail = hierarchicalUtils.getMail()
//        if(currentMail.isNotEmpty())
//            mailInput.setText(currentMail)

        val buttonMail = binding.buttonMail

        val checkBoxMonday = binding.checkBoxMonday
        val checkBoxTuesday = binding.checkBoxTuesday
        val checkBoxWednesday = binding.checkBoxWednesday
        val checkBoxThursday = binding.checkBoxThursday
        val checkBoxFriday = binding.checkBoxFriday
        val checkBoxSaturday = binding.checkBoxSaturday
        val checkBoxSunday = binding.checkBoxSunday

//        if(currentTrainingDays.isNotEmpty()) {
//            checkBoxMonday.isChecked = currentTrainingDays[0]
//            checkBoxTuesday.isChecked = currentTrainingDays[1]
//            checkBoxWednesday.isChecked = currentTrainingDays[2]
//            checkBoxThursday.isChecked = currentTrainingDays[3]
//            checkBoxFriday.isChecked = currentTrainingDays[4]
//            checkBoxSaturday.isChecked = currentTrainingDays[5]
//            checkBoxSunday.isChecked = currentTrainingDays[6]
//        }

        val buttonTrainingDays = binding.buttonTrainingDays

        buttonMail.setOnClickListener {
            val currentMail = hierarchicalUtils.getMail()
            val newMail = mailInput.text.toString()
            if(currentMail != newMail) {
                hierarchicalUtils.setMail(newMail)
                popUpUtils.showPopUp(binding.root.context,
                    "Se configuró el mail a $newMail", "Ok")
                Log.d("Jerarquico", "Se configuró el mail a $newMail")
                mailInput.text.clear()
            } else {
                dialogUtils.showDialog(binding.root.context,
                    "El mail indicado es el mismo al actual")
                Log.d("Jerarquico", "El mail indicado es el mismo al actual")
            }
        }

        buttonTrainingDays.setOnClickListener {
            val currentTrainingDays = hierarchicalUtils.getTrainingDays()
            val newTrainingDays = ArrayList<Boolean>()
            newTrainingDays.add(checkBoxMonday.isChecked)
            newTrainingDays.add((checkBoxTuesday.isChecked))
            newTrainingDays.add((checkBoxWednesday.isChecked))
            newTrainingDays.add((checkBoxThursday.isChecked))
            newTrainingDays.add((checkBoxFriday.isChecked))
            newTrainingDays.add((checkBoxSaturday.isChecked))
            newTrainingDays.add((checkBoxSunday.isChecked))

            if(newTrainingDays != currentTrainingDays) {
                hierarchicalUtils.setTrainingDays(newTrainingDays)
                popUpUtils.showPopUp(binding.root.context,
                    "Se configuraron los nuevos días de entrenamiento", "Ok")
                Log.d("Jerarquico", "Se configuraron los nuevos días de entrenamiento")
                checkBoxMonday.isChecked = false
                checkBoxTuesday.isChecked = false
                checkBoxWednesday.isChecked = false
                checkBoxThursday.isChecked = false
                checkBoxFriday.isChecked = false
                checkBoxSaturday.isChecked = false
                checkBoxSunday.isChecked = false
            } else {
                dialogUtils.showDialog(binding.root.context,
                    "Los nuevos días de entrenamiento son los mismos que los actuales")
                Log.d("Jerarquico", "Los nuevos días de entrenamiento son los mismos " +
                        "que los actuales")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hierarchicalUtils = HierarchicalUtils()
    }
}