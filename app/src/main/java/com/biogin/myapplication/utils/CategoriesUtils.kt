package com.biogin.myapplication.utils

import androidx.compose.ui.text.capitalize
import com.biogin.myapplication.FirebaseMethods
import com.biogin.myapplication.data.Category
import java.util.Locale

class CategoriesUtils {
    private val firebaseMethods = FirebaseMethods()
    private val categoriesList = ArrayList<Category>()
    
    init {
        firebaseMethods.getCategories { 
            categories -> categoriesList.addAll(categories)
        }
    }

    fun addCategory(name: String, isTemporary: Boolean, allowsInstitutes: Boolean,
                    active: Boolean) {
        val actualName = name.lowercase().replaceFirstChar(Char::titlecase)
        val newCategory = Category(actualName, isTemporary, allowsInstitutes, active)
        val success = firebaseMethods.addCategory(newCategory)

        if(success)
            categoriesList.add(newCategory)
    }

    fun getCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList) {
            if(category.active)
                cat.add(category.name)
        }

        return cat
    }

    fun getTemporaryCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList) {
            if(category.active && category.isTemporary)
                cat.add(category.name)
        }

        return cat
    }

    fun getNoInstitutesCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList) {
            if(category.active && !category.allowsInstitutes)
                cat.add(category.name)
        }

        return cat
    }

    fun getInactiveCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList) {
            if(!category.active)
                cat.add(category.name)
        }

        return cat
    }

}