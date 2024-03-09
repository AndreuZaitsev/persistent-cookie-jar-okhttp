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
package com.andreuzaitsev.persistentcookiejar.cache

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import okhttp3.Cookie

internal class SetCookieCache : CookieCache {

    private val cookies: MutableSet<IdentifiableCookie> = Collections.newSetFromMap(ConcurrentHashMap())

    override fun addAll(cookies: Collection<Cookie>) {
        IdentifiableCookie.decorateAll(cookies).forEach { cookie ->
            this.cookies.remove(cookie)
            this.cookies.add(cookie)
        }
    }

    override fun clear() {
        cookies.clear()
    }

    override fun iterator(): MutableIterator<Cookie> = SetCookieCacheIterator()

    private inner class SetCookieCacheIterator : MutableIterator<Cookie> {

        private val iterator: MutableIterator<IdentifiableCookie> = cookies.iterator()
        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): Cookie = iterator.next().cookie
        override fun remove() {
            iterator.remove()
        }
    }
}
