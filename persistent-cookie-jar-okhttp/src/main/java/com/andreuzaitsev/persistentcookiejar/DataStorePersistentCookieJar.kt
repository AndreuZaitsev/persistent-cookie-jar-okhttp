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

import android.content.Context
import com.andreuzaitsev.persistentcookiejar.cache.CookieCache
import com.andreuzaitsev.persistentcookiejar.cache.SetCookieCache
import com.andreuzaitsev.persistentcookiejar.persistence.CoroutineCookiePersistor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import okhttp3.HttpUrl

class DataStorePersistentCookieJar(
    private val cookieStorage: CoroutineCookiePersistor,
    private val cookieCache: CookieCache = SetCookieCache(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineClearableCookieJar {

    constructor(context: Context) : this(CoroutineCookiePersistor.DataStoreImpl(context))

    private val mutex = Mutex()
    private var initJob: Job? = null

    init {
        initJob = CoroutineScope(dispatcher).launch {
            mutex.withLock {
                cookieCache.addAll(cookieStorage.loadAll())
            }
        }
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        runBlocking { initJob?.join() }
        cookieCache.addAll(cookies)
        runBlocking { cookieStorage.saveAll(cookies.filter { it.persistent }) }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        runBlocking { initJob?.join() }

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
        runBlocking { cookieStorage.removeAll(expiredCookies) }
        return validCookies
    }

    private fun isCookieExpired(cookie: Cookie): Boolean = cookie.expiresAt < System.currentTimeMillis()

    override suspend fun clearSession() {
        mutex.withLock {
            cookieCache.clear()
            cookieCache.addAll(cookieStorage.loadAll())
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            cookieCache.clear()
            cookieStorage.clear()
        }
    }
}