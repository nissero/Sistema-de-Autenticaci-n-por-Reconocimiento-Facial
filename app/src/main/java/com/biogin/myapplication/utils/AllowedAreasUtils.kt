package com.biogin.myapplication.utils

import com.biogin.myapplication.FirebaseMethods

class AllowedAreasUtils() {
    private var firebaseMethods = FirebaseMethods()
    private var modulesAssociatedWithInstitutes = HashMap<String, ArrayList<String>>()

    init {
//        modulesAssociatedWithInstitutes.set("IDEI", arrayListOf("Módulo 1", "Módulo 3", "Módulo 4","Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("ICO", arrayListOf("Módulo 1", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("IDH", arrayListOf("Módulo 1", "Módulo 2","Módulo 3", "Módulo 5", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//        modulesAssociatedWithInstitutes.set("ICI", arrayListOf("Módulo 1", "Módulo 2", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
//
//        val institutesString: String =
//            root.context.assets.open("areas").bufferedReader().use { it.readText() }
//
//        val institutes = Json.decodeFromString<ArrayList<Institute>>(institutesString)

        firebaseMethods.readInstitutes("ICI") {
            institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseMethods.readInstitutes("ICO") {
            institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseMethods.readInstitutes("IDH") {
            institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseMethods.readInstitutes("IDEI") {
            institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
    }
    fun getAllowedAreas(institutes : ArrayList<String>) : MutableSet<String> {
        val allowedAreas = mutableSetOf<String>()
        for (institute in institutes) {
            allowedAreas.addAll(modulesAssociatedWithInstitutes.get(institute)!!)
        }

        return allowedAreas
    }

//    fun addAreaToInstitute(instituteName: String, area: String) {
//        if(modulesAssociatedWithInstitutes.containsKey(instituteName)) {
//            if(!modulesAssociatedWithInstitutes.get(instituteName)?.contains(area)!!) {
//                modulesAssociatedWithInstitutes.get(instituteName)!!.add(area)
//            }
//        }
//    }
//
//    fun getAllAreas(): ArrayList<String> {
//        val allAreas = ArrayList<String>()
//
//        for(institute in modulesAssociatedWithInstitutes) {
//            for(area in institute.value) {
//                if(!allAreas.contains(area)) {
//                    allAreas.add(area)
//                }
//            }
//        }
//
//        allAreas.sort()
//
//        return allAreas
//    }
//
//    private fun updateFile() {
//        val institutes = ArrayList<Institute>()
//        for(institute in modulesAssociatedWithInstitutes) {
//            institutes.add(Institute(institute.key, institute.value))
//        }
//        val json = Json.encodeToString(institutes)
//    }
}