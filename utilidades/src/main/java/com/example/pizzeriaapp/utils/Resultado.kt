package com.example.pizzeriaapp.utils

// Manejo de errores explícito: quien llama decide cómo mostrar el motivo, sin excepciones para flujo normal
sealed interface Resultado<out T> {
    data class Exito<out T>(val valor: T) : Resultado<T>
    data class Error(val mensaje: String) : Resultado<Nothing>
}
