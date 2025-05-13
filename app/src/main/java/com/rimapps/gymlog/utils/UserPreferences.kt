package com.rimapps.gymlog.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val isUserSignedInKey = booleanPreferencesKey("is_user_signed_in")

    val isUserSignedIn: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            Log.e(TAG, "Error reading user signed in state", exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val isSignedIn = preferences[isUserSignedInKey] ?: false
            Log.d(TAG, "Reading user signed in state: $isSignedIn")
            isSignedIn
        }

    suspend fun setUserSignedIn(isSignedIn: Boolean) {
        try {
            Log.d(TAG, "Setting user signed in state to: $isSignedIn")
            context.dataStore.edit { preferences ->
                preferences[isUserSignedInKey] = isSignedIn
            }
            // Verify the change
            val newState = isUserSignedIn.first()
            Log.d(TAG, "Verified new signed in state: $newState")
        } catch (exception: Exception) {
            Log.e(TAG, "Error setting user signed in state", exception)
            throw exception
        }
    }

    suspend fun clearPreferences() {
        try {
            Log.d(TAG, "Clearing all preferences")
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            Log.d(TAG, "Successfully cleared preferences")
        } catch (exception: Exception) {
            Log.e(TAG, "Error clearing preferences", exception)
            throw exception
        }
    }

    companion object {
        private const val TAG = "UserPreferences"
    }
}