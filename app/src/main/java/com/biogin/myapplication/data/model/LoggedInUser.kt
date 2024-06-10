package com.biogin.myapplication.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    var dni: String,
    val name: String,
    val surname: String,
    val email: String,
    val category: String,
    val state: String,
    val institutes: ArrayList<String>,
    val areasAllowed: ArrayList<String>,

    ) {

    private var trabajaDesde: String? = null
    private var trabajaHasta: String? = null

    //constructor para temporales
    constructor(
        dni: String,
        name: String,
        surname: String,
        email: String,
        category: String,
        state: String,
        institutes: ArrayList<String>,
        areasAllowed: ArrayList<String>,
        trabajaDesde: String?,
        trabajaHasta: String?
    ) : this(dni, name, surname, email, category, state, institutes, areasAllowed){
        this.trabajaDesde = trabajaDesde
        this.trabajaHasta = trabajaHasta
    }

    fun getTrabajaDesde(): String? {
        return trabajaDesde
    }

    fun getTrabajaHasta(): String? {
        return trabajaHasta
    }

}