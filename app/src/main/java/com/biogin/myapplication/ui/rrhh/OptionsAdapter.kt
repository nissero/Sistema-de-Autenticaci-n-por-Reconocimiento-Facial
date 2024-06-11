package com.biogin.myapplication.ui.rrhh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.biogin.myapplication.R

class OptionsAdapter (private val options: MutableList<Option>,
                      private val enableButton: () -> Unit) : RecyclerView.Adapter<OptionsAdapter.OptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = options[position]
        holder.checkBox.text = option.name
        holder.checkBox.isChecked = option.isSelected
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            option.isSelected = isChecked
            enableButton()
        }
    }

    override fun getItemCount(): Int = options.size
    fun isAnyOptionSelected(): Boolean {
        return options.any { it.isSelected }
    }

    class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.option_checkbox)
    }
}