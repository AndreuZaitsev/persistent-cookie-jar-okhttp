package com.andreuzaitsev.persistentcookiejar.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.Cookie

val Context.dataStore by preferencesDataStore(
    name = COOKIES_PREFERENCES_NAME,
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, COOKIES_PREFERENCES_NAME)) }
)

interface CoroutineCookiePersistor {

    suspend fun loadAll(): List<Cookie>
    suspend fun saveAll(cookies: Collection<Cookie>)
    suspend fun removeAll(cookies: Collection<Cookie>)
    suspend fun clear()

    class DataStoreImpl(
        private val dataStore: DataStore<Preferences>
    ) : CoroutineCookiePersistor {

        constructor(context: Context) : this(context.dataStore)

        override suspend fun loadAll(): List<Cookie> = dataStore.data
            .firstOrNull()
            ?.asMap()
            .orEmpty()
            .values
            .filterIsInstance<String>()
            .mapNotNull(SerializableCookie()::decode)

        override suspend fun saveAll(cookies: Collection<Cookie>) {
            dataStore.edit { prefs ->
                cookies
                    .asSequence()
                    .map {
                        stringPreferencesKey(createCookieKey(it)) to SerializableCookie().encode(it).orEmpty()
                    }
                    .forEach(prefs::plusAssign)
            }
        }

        override suspend fun removeAll(cookies: Collection<Cookie>) {
            dataStore.edit { prefs ->
                cookies
                    .asSequence()
                    .map { stringPreferencesKey(createCookieKey(it)) }
                    .forEach(prefs::remove)
            }
        }

        override suspend fun clear() {
            dataStore.edit { it.clear() }
        }
    }
}