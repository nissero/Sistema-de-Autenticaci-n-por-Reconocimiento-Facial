package com.biogin.myapplication.utils

import com.biogin.myapplication.data.Category

class CategoriesUtils {
    private val firebaseCategoryMethods = FirebaseCategoryMethods()
    private lateinit var categoriesList: HashMap<String, Category>
    
    init {
        firebaseCategoryMethods.getCategories {
            categories -> categoriesList = categories
        }
    }

    fun addCategory(name: String, isTemporary: Boolean, allowsInstitutes: Boolean,
                    active: Boolean) {
        val newCategory = Category(name, isTemporary, allowsInstitutes, active)
        val success = firebaseCategoryMethods.addCategory(newCategory)

        if(success)
            categoriesList.set(name, newCategory)
    }

    fun deactivateCategory(name: String) {
        val success = firebaseCategoryMethods.deactivateCategory(name)
        if(success) {
            categoriesList.get(name)?.active = false
        }
    }

    fun activateCategory(name: String) {
        val success = firebaseCategoryMethods.activateCategory(name)
        if(success) {
            categoriesList.get(name)?.active = true
        }
    }

    fun getActiveCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList.values) {
            if(category.active)
                cat.add(category.name)
        }

        return cat
    }

    fun getTemporaryCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList.values) {
            if(category.active && category.isTemporary)
                cat.add(category.name)
        }

        return cat
    }

    fun getNoInstitutesCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList.values) {
            if(category.active && !category.allowsInstitutes)
                cat.add(category.name)
        }

        return cat
    }

    fun getInactiveCategories(): ArrayList<String> {
        val cat = ArrayList<String>()

        for(category in categoriesList.values) {
            if(!category.active)
                cat.add(category.name)
        }

        return cat
    }

}