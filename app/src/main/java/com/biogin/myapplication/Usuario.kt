package com.biogin.myapplication

import androidx.compose.ui.text.toLowerCase
import org.checkerframework.checker.units.qual.A

class Usuario (
    private val nombre: String = "",
    private val apellido: String = "",
    private val dni: String = "",
    private val email: String = "",
    private val area: String = "",
    private val categoria: String = "",
    private val estado: String = "",
    private val areasPermitidas: ArrayList<String> = ArrayList()
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
        return estado.toLowerCase() == "activo"
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

    //MODIFICAR CUANDO SE MODIFIQUE LA BASE DE DATOS
}