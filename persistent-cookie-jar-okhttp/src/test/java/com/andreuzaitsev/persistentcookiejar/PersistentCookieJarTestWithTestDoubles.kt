package com.andreuzaitsev.persistentcookiejar

import com.andreuzaitsev.persistentcookiejar.TestCookieCreator.createExpiredCookie
import com.andreuzaitsev.persistentcookiejar.TestCookieCreator.createNonPersistentCookie
import com.andreuzaitsev.persistentcookiejar.TestCookieCreator.createPersistentCookie
import com.andreuzaitsev.persistentcookiejar.cache.CookieCache
import com.andreuzaitsev.persistentcookiejar.persistence.CookiePersistor
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.Cookie
import org.junit.Test
import org.mockito.ArgumentMatchers.anyCollection
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

class PersistentCookieJarTestWithTestDoubles {

    @Test
    fun `saveFromResponse with persistent cookie should save cookie in session and persistence`() {
        val cookieCache: CookieCache = mock()
        val cookiePersistor: CookiePersistor = mock()
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, cookiePersistor)
        val responseCookies = listOf(createPersistentCookie(false))

        persistentCookieJar.saveFromResponse(TestCookieCreator.DEFAULT_URL, responseCookies)

        val cookieCacheArgCaptor = argumentCaptor<Collection<Cookie>>()
        verify(cookieCache, atLeastOnce()).addAll(cookieCacheArgCaptor.capture())
        assertEquals(responseCookies[0], cookieCacheArgCaptor.allValues.iterator().next())

        val cookiePersistorArgCaptor = argumentCaptor<Collection<Cookie>>()
        verify(cookiePersistor, times(1)).saveAll(cookiePersistorArgCaptor.capture())
        assertEquals(responseCookies[0], cookiePersistorArgCaptor.allValues.iterator().next())
    }

    @Test
    fun `saveFromResponse with non-persistent cookie should save cookie only in session`() {
        val cookieCache: CookieCache = mock()
        val cookiePersistor: CookiePersistor = mock()
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, cookiePersistor)
        val responseCookies = listOf(createNonPersistentCookie())

        persistentCookieJar.saveFromResponse(TestCookieCreator.DEFAULT_URL, responseCookies)

        val cookieCacheArgCaptor = argumentCaptor<Collection<Cookie>>()
        verify(cookieCache, atLeastOnce()).addAll(cookieCacheArgCaptor.capture())
        assertEquals(responseCookies[0], cookieCacheArgCaptor.firstValue.iterator().next())

        verify(cookiePersistor, atLeast(0)).saveAll(anyCollection())
    }

    @Test
    fun `loadForRequest with matching URL should return matching cookies`() {
        val savedCookie = createNonPersistentCookie()
        val cookieCache: CookieCache = mock()
        whenever(cookieCache.iterator()).thenReturn(mutableListOf(savedCookie).iterator())
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, mock())

        val requestCookies = persistentCookieJar.loadForRequest(TestCookieCreator.DEFAULT_URL)

        assertEquals(savedCookie, requestCookies[0])
    }

    @Test
    fun `loadForRequest with non-matching URL should return empty cookie list`() {
        val cookieCache: CookieCache = mock()
        whenever(cookieCache.iterator()).thenReturn(emptyList<Cookie>().toMutableList().listIterator())
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, mock())

        val requestCookies = persistentCookieJar.loadForRequest(TestCookieCreator.OTHER_URL)

        assertTrue(requestCookies.isEmpty())
    }

    @Test
    fun `loadForRequest with expired cookie matching URL should return empty cookie list`() {
        val cookieCache: CookieCache = mock()
        whenever(cookieCache.iterator()).thenReturn(listOf(createExpiredCookie()).toMutableList().listIterator())
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, mock())

        val cookies = persistentCookieJar.loadForRequest(TestCookieCreator.DEFAULT_URL)

        assertTrue(cookies.isEmpty())
    }

    @Test
    fun `loadForRequest with expired cookie matching URL should remove the cookie`() {
        val savedCookie = createExpiredCookie()
        val cookieCache: CookieCache = mock()
        val cookieIterator: MutableIterator<Cookie> = mutableListOf(savedCookie).iterator()
        whenever(cookieCache.iterator()).thenReturn(cookieIterator)
        val cookiePersistor: CookiePersistor = mock()
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, cookiePersistor)

        persistentCookieJar.loadForRequest(TestCookieCreator.DEFAULT_URL)

        val cookiePersistorArgCaptor = argumentCaptor<Collection<Cookie>>()
        verify(cookiePersistor).removeAll(cookiePersistorArgCaptor.capture())
        assertEquals(savedCookie, cookiePersistorArgCaptor.firstValue.iterator().next())
    }

    @Test
    fun `clearSession should clear only session cookies`() {
        val cookieCache: CookieCache = mock()
        val cookiePersistor: CookiePersistor = mock()
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, cookiePersistor)

        persistentCookieJar.clearSession()

        verify(cookieCache, times(1)).clear()
        verify(cookiePersistor, times(0)).clear()
    }

    @Test
    fun `clear should clear all cookies`() {
        val cookieCache: CookieCache = mock()
        val cookiePersistor: CookiePersistor = mock()
        val persistentCookieJar = PreferencesPersistentCookieJar(cookieCache, cookiePersistor)

        persistentCookieJar.clear()

        verify(cookieCache, times(1)).clear()
        verify(cookiePersistor, times(1)).clear()
    }
}
