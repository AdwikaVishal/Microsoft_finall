package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.model.AbilityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    private val authTokenKey = stringPreferencesKey("auth_token")
    private val abilityTypeKey = stringPreferencesKey("ability_type")
    private val languageKey = stringPreferencesKey("language")
    private val userNameKey = stringPreferencesKey("user_name")

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[authTokenKey]
        }

    val abilityType: Flow<AbilityType> = context.dataStore.data
        .map { preferences ->
            AbilityType.valueOf(preferences[abilityTypeKey] ?: AbilityType.NONE.name)
        }

    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[languageKey] ?: "en"
        }

    val userName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[userNameKey]
        }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit {
            it[authTokenKey] = token
        }
    }

    suspend fun saveAbilityType(abilityType: AbilityType) {
        context.dataStore.edit {
            it[abilityTypeKey] = abilityType.name
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit {
            it[languageKey] = language
        }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit {
            it[userNameKey] = name
        }
    }
}