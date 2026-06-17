package com.example.ruvo_app.data.mapper

import com.example.ruvo_app.domain.util.AuthError
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

object AuthErrorMapper {
    fun map(exception: Exception): AuthError {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AuthError.WrongPassword
            is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
            is FirebaseAuthUserCollisionException -> AuthError.EmailAlreadyInUse
            is FirebaseAuthWeakPasswordException -> AuthError.WeakPassword
            is FirebaseNetworkException -> AuthError.NetworkError
            else -> AuthError.UnknownError
        }
    }
}