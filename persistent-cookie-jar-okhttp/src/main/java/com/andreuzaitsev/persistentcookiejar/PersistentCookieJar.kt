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
package com.andreuzaitsev.persistentcookiejar

import com.andreuzaitsev.persistentcookiejar.cache.CookieCache
import com.andreuzaitsev.persistentcookiejar.persistence.CookiePersistor
import okhttp3.Cookie
import okhttp3.HttpUrl

class PersistentCookieJar(
    private val cache: CookieCache,
    private val persistor: CookiePersistor
) : ClearableCookieJar {

    init {
        cache.addAll(persistor.loadAll())
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cache.addAll(cookies)
        persistor.saveAll(filterPersistentCookies(cookies))
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesToRemove = cache.filter { isCookieExpired(it) }
        val validCookies = cache.filter { !isCookieExpired(it) && it.matches(url) }
        // Remove all expired cookies
        cache.removeAll(cookiesToRemove)
        // Persist the removal of the expired cookies
        persistor.removeAll(cookiesToRemove)
        return validCookies
    }

    @Synchronized
    override fun clearSession() {
        cache.clear()
        cache.addAll(persistor.loadAll())
    }

    @Synchronized
    override fun clear() {
        cache.clear()
        persistor.clear()
    }

    companion object {

        private fun filterPersistentCookies(cookies: List<Cookie>): List<Cookie> = cookies.filter { it.persistent }

        private fun isCookieExpired(cookie: Cookie): Boolean = cookie.expiresAt < System.currentTimeMillis()
    }
}
