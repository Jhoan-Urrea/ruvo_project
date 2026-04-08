package com.example.ruvo_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val uid: String?,
    val email: String?,
    val role: String?
)

class UserPreferencesManager(private val context: Context) {
    companion object {
        private val USER_UID = stringPreferencesKey("user_uid")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_ROLE = stringPreferencesKey("user_role")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences: Preferences ->
            UserPreferences(
                uid = preferences[USER_UID],
                email = preferences[USER_EMAIL],
                role = preferences[USER_ROLE]
            )
        }

    suspend fun saveUserPreferences(uid: String, email: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_UID] = uid
            preferences[USER_EMAIL] = email
            preferences[USER_ROLE] = role
        }
    }

    suspend fun clearUserPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}