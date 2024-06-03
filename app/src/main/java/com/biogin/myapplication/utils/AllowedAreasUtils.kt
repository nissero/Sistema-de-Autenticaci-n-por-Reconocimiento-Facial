package com.biogin.myapplication.utils

class AllowedAreasUtils() {
    private var firebaseAreaMethods = FirebaseAreaMethods()
    private var modulesAssociatedWithInstitutes = HashMap<String, ArrayList<String>>()
    private var inactiveModulesAssociatedWithInstitutes = HashMap<String, ArrayList<String>>()

    init {
        updateMaps()
    }
    fun getAllowedAreas(institutes : ArrayList<String>) : MutableSet<String> {
        val allowedAreas = mutableSetOf<String>()
        for (institute in institutes) {
            allowedAreas.addAll(modulesAssociatedWithInstitutes.get(institute)!!)
        }

        return allowedAreas
    }

    fun addArea(newArea: String, ici: Boolean, ico: Boolean,
                idei: Boolean, idh: Boolean) {
        val success = firebaseAreaMethods.addArea(newArea, ici, ico, idei, idh)

        if(success) {
            if (ici) modulesAssociatedWithInstitutes.get("ICI")?.add(newArea)
            if (ico) modulesAssociatedWithInstitutes.get("ICO")?.add(newArea)
            if (idei) modulesAssociatedWithInstitutes.get("IDEI")?.add(newArea)
            if (idh) modulesAssociatedWithInstitutes.get("IDH")?.add(newArea)
        }
    }

    fun deactivateArea(areaToRemove: String) {
        val success = firebaseAreaMethods.deactivateArea(areaToRemove)

        if(success) {
            if(modulesAssociatedWithInstitutes["ICI"]?.contains(areaToRemove) == true) {
                modulesAssociatedWithInstitutes["ICI"]?.remove(areaToRemove)
                inactiveModulesAssociatedWithInstitutes["ICI"]?.add(areaToRemove)
            }
            if(modulesAssociatedWithInstitutes["ICO"]?.contains(areaToRemove) == true) {
                modulesAssociatedWithInstitutes["ICO"]?.remove(areaToRemove)
                inactiveModulesAssociatedWithInstitutes["ICO"]?.add(areaToRemove)
            }
            if(modulesAssociatedWithInstitutes["IDH"]?.contains(areaToRemove) == true) {
                modulesAssociatedWithInstitutes["IDH"]?.remove(areaToRemove)
                inactiveModulesAssociatedWithInstitutes["IDH"]?.add(areaToRemove)
            }
            if(modulesAssociatedWithInstitutes["IDEI"]?.contains(areaToRemove) == true) {
                modulesAssociatedWithInstitutes["IDEI"]?.remove(areaToRemove)
                inactiveModulesAssociatedWithInstitutes["IDEI"]?.add(areaToRemove)
            }
        }
    }

    fun activateArea(area: String) {
        val success = firebaseAreaMethods.activateArea(area)

        if(success) {
            if(inactiveModulesAssociatedWithInstitutes["ICI"]?.contains(area) == true) {
                inactiveModulesAssociatedWithInstitutes["ICI"]?.remove(area)
                modulesAssociatedWithInstitutes["ICI"]?.add(area)
            }
            if(inactiveModulesAssociatedWithInstitutes["ICO"]?.contains(area) == true) {
                inactiveModulesAssociatedWithInstitutes["ICO"]?.remove(area)
                modulesAssociatedWithInstitutes["ICO"]?.add(area)
            }
            if(inactiveModulesAssociatedWithInstitutes["IDH"]?.contains(area) == true) {
                inactiveModulesAssociatedWithInstitutes["IDH"]?.remove(area)
                modulesAssociatedWithInstitutes["IDH"]?.add(area)
            }
            if(inactiveModulesAssociatedWithInstitutes["IDEI"]?.contains(area) == true) {
                inactiveModulesAssociatedWithInstitutes["IDEI"]?.remove(area)
                modulesAssociatedWithInstitutes["IDEI"]?.add(area)
            }
        }
    }

    fun modifyArea(area: String, ici: Boolean, ico: Boolean,
                   idei: Boolean, idh: Boolean) {
        val success = firebaseAreaMethods.modifyArea(area, ici, ico, idei, idh)

        if(success) {
            if(modulesAssociatedWithInstitutes["ICI"]?.contains(area) == true &&
                !ici) {
                modulesAssociatedWithInstitutes["ICI"]?.remove(area)
            } else if(modulesAssociatedWithInstitutes["ICI"]?.contains(area) == false &&
                ici) {
                modulesAssociatedWithInstitutes["ICI"]?.add(area)
            }

            if(modulesAssociatedWithInstitutes["ICO"]?.contains(area) == true &&
                !ico) {
                modulesAssociatedWithInstitutes["ICO"]?.remove(area)
            } else if(modulesAssociatedWithInstitutes["ICO"]?.contains(area) == false &&
                ico) {
                modulesAssociatedWithInstitutes["ICO"]?.add(area)
            }

            if(modulesAssociatedWithInstitutes["IDEI"]?.contains(area) == true &&
                !idei) {
                modulesAssociatedWithInstitutes["IDEI"]?.remove(area)
            } else if(modulesAssociatedWithInstitutes["IDEI"]?.contains(area) == false &&
                idei) {
                modulesAssociatedWithInstitutes["IDEI"]?.add(area)
            }

            if(modulesAssociatedWithInstitutes["IDH"]?.contains(area) == true &&
                !idh) {
                modulesAssociatedWithInstitutes["IDH"]?.remove(area)
            } else if(modulesAssociatedWithInstitutes["IDH"]?.contains(area) == false &&
                idh) {
                modulesAssociatedWithInstitutes["IDH"]?.add(area)
            }
        }
    }

    fun getInstitutesFromActiveArea(area: String): ArrayList<String> {
        val institutes = ArrayList<String>()

        for(institute in modulesAssociatedWithInstitutes.keys) {
            if(modulesAssociatedWithInstitutes[institute]?.contains(area) == true) {
                institutes.add(institute)
            }
        }

        return institutes
    }

    fun getAllActiveAreas(): ArrayList<String> {
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

    fun getAllInactiveAreas(): ArrayList<String> {
        val allAreas = ArrayList<String>()

        for(institute in inactiveModulesAssociatedWithInstitutes) {
            for(area in institute.value) {
                if(!allAreas.contains(area)) {
                    allAreas.add(area)
                }
            }
        }

        allAreas.sort()

        return allAreas
    }

    private fun updateMaps() {
        firebaseAreaMethods.readActiveAreas("ICI") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseAreaMethods.readActiveAreas("ICO") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseAreaMethods.readActiveAreas("IDH") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseAreaMethods.readActiveAreas("IDEI") {
                institute -> modulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }

        firebaseAreaMethods.readInactiveAreas("ICI") {
                institute -> inactiveModulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseAreaMethods.readInactiveAreas("ICO") {
                institute -> inactiveModulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseAreaMethods.readInactiveAreas("IDH") {
                institute -> inactiveModulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
        firebaseAreaMethods.readInactiveAreas("IDEI") {
                institute -> inactiveModulesAssociatedWithInstitutes.set(institute.name, institute.areas)
        }
    }

//    fun addAreaToInstitute(instituteName: String, newArea: String) {
//        val success = firebaseMethods.addAreaToInstitute(instituteName, newArea)
//
//        if(success)
//            modulesAssociatedWithInstitutes.get(instituteName)?.add(newArea)
//    }
//
//    fun removeAreaFromInstitute(instituteName: String, areaToRemove: String) {
//        val success = firebaseMethods.removeAreaFromInstitute(instituteName, areaToRemove)
//
//        if(success)
//            modulesAssociatedWithInstitutes.get(instituteName)?.remove(areaToRemove)
//    }

}