package com.example.intermediate_submission_awal.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.intermediate_submission_awal.data.response.LoginResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("User")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveUser(userName: String, userId: String, userToken: String) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = userName
            preferences[USERID_KEY] = userId
            preferences[TOKEN_KEY] = userToken
        }
    }

    fun getUser(): Flow<LoginResult> {
        return dataStore.data.map { preferences ->
            LoginResult(
                preferences[NAME_KEY] ?:"",
                preferences[USERID_KEY] ?:"",
                preferences[TOKEN_KEY] ?:"",
            )
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = ""
            preferences[USERID_KEY] = ""
            preferences[TOKEN_KEY] = ""
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val NAME_KEY = stringPreferencesKey("name")
        private val USERID_KEY = stringPreferencesKey("userId")
        private val TOKEN_KEY = stringPreferencesKey("token")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

}