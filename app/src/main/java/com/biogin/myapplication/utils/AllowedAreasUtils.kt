package com.biogin.myapplication.utils

import android.view.View
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AllowedAreasUtils(view: View) {
    private var root = view
    private var modulesAssociatedWithInstitutes = HashMap<String, ArrayList<String>>()

    @Serializable
    data class Institute(val name: String, val areas: ArrayList<String>)

    init {
//        modulesAssociatedWithInstitutes.set("IDEI", arrayListOf("Módulo 1", "Módulo 3", "Módulo 4","Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("ICO", arrayListOf("Módulo 1", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("IDH", arrayListOf("Módulo 1", "Módulo 2","Módulo 3", "Módulo 5", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("ICI", arrayListOf("Módulo 1", "Módulo 2", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))

        val institutesString: String =
            root.context.assets.open("areas").bufferedReader().use { it.readText() }

        val institutes = Json.decodeFromString<List<Institute>>(institutesString)

        for(institute in institutes) {
            modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
    }
    fun getAllowedAreas(institutes : ArrayList<String>) : MutableSet<String> {
        val allowedAreas = mutableSetOf<String>()
        for (institute in institutes) {
            allowedAreas.addAll(modulesAssociatedWithInstitutes.get(institute)!!)
        }

        return allowedAreas
    }

    fun getAllAreas(): ArrayList<String> {
        val allAreas = ArrayList<String>()

        for(institute in modulesAssociatedWithInstitutes) {
            for(area in institute.value) {
                if(!allAreas.contains(area)) {
                    allAreas.add(area)
                }
            }
        }

        allAreas.sort()

        return allAreas
    }
}