package com.example.ruvo_app.domain.util

sealed class AuthError : Exception() {
    object InvalidEmail : AuthError()
    object InvalidPassword : AuthError()
    object InvalidUsername : AuthError()
    object EmailAlreadyInUse : AuthError()
    object WeakPassword : AuthError()
    object UserNotFound : AuthError()
    object WrongPassword : AuthError()
    object NetworkError : AuthError()
    object UnknownError : AuthError()
    
    override val message: String?
        get() = when (this) {
            InvalidEmail -> "El formato del correo electrónico no es válido."
            InvalidPassword -> "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número."
            InvalidUsername -> "El nombre de usuario no es válido o es muy corto."
            EmailAlreadyInUse -> "Este correo electrónico ya está registrado."
            WeakPassword -> "La contraseña es muy débil."
            UserNotFound -> "No existe una cuenta asociada a este correo."
            WrongPassword -> "La contraseña es incorrecta."
            NetworkError -> "Error de red. Verifica tu conexión."
            UnknownError -> "Ha ocurrido un error inesperado. Inténtalo de nuevo."
        }
}