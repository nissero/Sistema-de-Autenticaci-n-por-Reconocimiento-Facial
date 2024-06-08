package com.biogin.myapplication


class Usuario (
    private val nombre: String = "",
    private val apellido: String = "",
    private val dni: String = "",
    private val email: String = "",
    private val area: String = "",
    private val categoria: String = "",
    private val estado: String = "",
    private val areasPermitidas: ArrayList<String> = ArrayList(),
    private val areasTemporales: ArrayList<String> = ArrayList(),
    private val accesoDesde: String = "",
    private val accesoHasta: String = ""
){
    fun getNombre(): String {
        return nombre
    }

    fun getApellido(): String {
        return apellido
    }

    fun getDni(): String {
        return dni
    }

    fun getEstado(): Boolean {
        return estado.lowercase() == "activo"
    }

    fun getEmail(): String {
        return email
    }

    fun getArea(): String {
        return area
    }

    fun getCategoria(): String{
        return categoria
    }

    fun getAreasPermitidas(): String{
        val areasPermitidas = areasPermitidas.joinToString()

        return areasPermitidas.ifEmpty {
            "Ninguna"
        }
    }

    fun getAreasTemporales(): String{
        val areasTemporales = areasTemporales.joinToString()

        return areasTemporales.ifEmpty {
            "Ninguna"
        }
    }

    fun hasAreasTemporales(): Boolean{
        return areasTemporales.isNotEmpty()
    }

    fun getAccesoDesde(): String{
        return accesoDesde
    }

    fun getAccesoHasta(): String{
        return accesoHasta
    }

    //MODIFICAR CUANDO SE MODIFIQUE LA BASE DE DATOS
}