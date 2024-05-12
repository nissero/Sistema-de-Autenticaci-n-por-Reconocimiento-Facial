package com.biogin.myapplication

class Usuario (
    private val nombre: String = "",
    private val apellido: String = "",
    private val dni: String = "",
    private val email: String = "",
    private val area: String = "",
    private val categoria: String = ""
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

    fun getEmail(): String {
        return email
    }

    fun getArea(): String {
        return area
    }

    fun getCategoria(): String{
        return categoria
    }

    //MODIFICAR CUANDO SE MODIFIQUE LA BASE DE DATOS
}