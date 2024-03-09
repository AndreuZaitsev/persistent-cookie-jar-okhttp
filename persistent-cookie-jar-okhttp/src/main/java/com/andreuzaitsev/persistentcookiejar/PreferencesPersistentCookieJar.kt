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

class PreferencesPersistentCookieJar(
    private val cookieCache: CookieCache,
    private val cookieStorage: CookiePersistor
) : ClearableCookieJar {

    init {
        cookieCache.addAll(cookieStorage.loadAll())
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieCache.addAll(cookies)
        cookieStorage.saveAll(cookies.filter { it.persistent })
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val expiredCookies: MutableList<Cookie> = mutableListOf()
        val validCookies: MutableList<Cookie> = mutableListOf()

        val cacheIterator = cookieCache.iterator()
        while (cacheIterator.hasNext()) {
            val currentCookie = cacheIterator.next()
            if (isCookieExpired(currentCookie)) {
                expiredCookies += currentCookie
                cacheIterator.remove()
            } else if (currentCookie.matches(url)) {
                validCookies += currentCookie
            }
        }
        cookieStorage.removeAll(expiredCookies)
        return validCookies
    }

    private fun isCookieExpired(cookie: Cookie): Boolean = cookie.expiresAt < System.currentTimeMillis()

    @Synchronized
    override fun clearSession() {
        cookieCache.clear()
        cookieCache.addAll(cookieStorage.loadAll())
    }

    @Synchronized
    override fun clear() {
        cookieCache.clear()
        cookieStorage.clear()
    }
}