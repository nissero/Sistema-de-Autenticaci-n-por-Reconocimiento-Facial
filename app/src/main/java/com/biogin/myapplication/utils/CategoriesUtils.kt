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
            categoriesList[name] = newCategory
    }

    fun deactivateCategory(name: String) {
        val success = firebaseCategoryMethods.deactivateCategory(name)
        if(success) {
            categoriesList[name]?.active = false
        }
    }

    fun activateCategory(name: String) {
        val success = firebaseCategoryMethods.activateCategory(name)
        if(success) {
            categoriesList[name]?.active = true
        }
    }

    fun modifyCategory(oldName: String, newName: String) {
        firebaseCategoryMethods.modifyCategory(oldName, newName) {
            success ->
            run {
                if (success) {
                    firebaseCategoryMethods.updateUserOnCategoryChange(oldName, newName)

                    val isTemporaryOldName = categoriesList[oldName]?.isTemporary
                    val allowsInstitutesOldName = categoriesList[oldName]?.allowsInstitutes
                    val isActiveOldName = categoriesList[oldName]?.active
                    lateinit var newCategory: Category

                    if (isTemporaryOldName != null && allowsInstitutesOldName != null &&
                        isActiveOldName != null
                    ) {
                        newCategory = Category(
                            newName, isTemporaryOldName, allowsInstitutesOldName,
                            isActiveOldName
                        )
                    }

                    categoriesList.remove(oldName)
                    categoriesList[newName] = newCategory
                }
            }
        }
    }

    fun getCategoryFromName(name: String): Category? {
        val category = categoriesList[name]

        if(category != null) {
            if (category.active) return category
        }

        return null
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