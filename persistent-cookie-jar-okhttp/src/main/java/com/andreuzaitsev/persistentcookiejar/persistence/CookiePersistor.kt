/*
 * Copyright (C) 2016 Francisco José Montiel Navarro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.andreuzaitsev.persistentcookiejar.persistence

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import okhttp3.Cookie

const val COOKIES_PREFERENCES_NAME = "CookiePersistence"

fun createCookieKey(cookie: Cookie): String =
    "${if (cookie.secure) "https" else "http"}://${cookie.domain}${cookie.path}|${cookie.name}"

/**
 * A CookiePersistor handles the persistent cookie storage.
 */
interface CookiePersistor {

    fun loadAll(): List<Cookie>

    /**
     * Persist all cookies, existing cookies will be overwritten.
     *
     * @param cookies cookies persist
     */
    fun saveAll(cookies: Collection<Cookie>)

    /**
     * Removes indicated cookies from persistence.
     *
     * @param cookies cookies to remove from persistence
     */
    fun removeAll(cookies: Collection<Cookie>)

    /**
     * Clear all cookies from persistence.
     */
    fun clear()

    @SuppressLint("ApplySharedPref")
    class PrefsImpl(
        private val sharedPreferences: SharedPreferences
    ) : CookiePersistor {

        constructor(context: Context) : this(context.getSharedPreferences(COOKIES_PREFERENCES_NAME, MODE_PRIVATE))

        override fun loadAll(): List<Cookie> = sharedPreferences.all.values
            .filterIsInstance<String>()
            .mapNotNull { serializedCookie -> SerializableCookie().decode(serializedCookie) }

        override fun saveAll(cookies: Collection<Cookie>) {
            val editor = sharedPreferences.edit()
            for (cookie in cookies) {
                editor.putString(createCookieKey(cookie), SerializableCookie().encode(cookie))
            }
            editor.commit()
        }

        override fun removeAll(cookies: Collection<Cookie>) {
            val editor = sharedPreferences.edit()
            for (cookie in cookies) {
                editor.remove(createCookieKey(cookie))
            }
            editor.commit()
        }

        override fun clear() {
            sharedPreferences.edit().clear().commit()
        }
    }
}