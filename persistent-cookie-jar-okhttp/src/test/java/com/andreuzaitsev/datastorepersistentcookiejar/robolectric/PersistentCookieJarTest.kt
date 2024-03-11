package com.andreuzaitsev.datastorepersistentcookiejar.robolectric

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import com.andreuzaitsev.persistentcookiejar.DataStorePersistentCookieJar
import com.andreuzaitsev.common.TestCookieCreator
import com.andreuzaitsev.persistentcookiejar.cache.SetCookieCache
import com.andreuzaitsev.persistentcookiejar.persistence.CoroutineCookiePersistor
import com.andreuzaitsev.persistentcookiejar.persistence.dataStore
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PersistentCookieJarTest {

    private val dataStore: DataStore<Preferences> = ApplicationProvider.getApplicationContext<Context>().dataStore
    private val persistor: CoroutineCookiePersistor = CoroutineCookiePersistor.DataStoreImpl(dataStore)
    private val persistentCookieJar = DataStorePersistentCookieJar(
        persistor,
        SetCookieCache(),
    )

    private val url = "https://domain.com/".toHttpUrl()

    @Before
    fun setUp() = runTest {
        persistentCookieJar.clear()
    }

    @After
    fun tearDown() = runTest {
        persistentCookieJar.clear()
    }

    /**
     * Test that the cookie is stored and also loaded when the a matching url is given
     */
    @Test
    fun regularCookie() {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistentCookieJar.saveFromResponse(url, listOf(cookie))
        val storedCookies = persistentCookieJar.loadForRequest(url)
        Assert.assertEquals(cookie, storedCookies[0])
    }

    /**
     * Test that a stored cookie is not loaded for a non matching url.
     */
    @Test
    fun differentUrlRequest() {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistentCookieJar.saveFromResponse(url, listOf(cookie))
        val storedCookies = persistentCookieJar.loadForRequest("https://otherdomain.com".toHttpUrl())
        Assert.assertTrue(storedCookies.isEmpty())
    }

    /**
     * Test that when receiving a cookie equal(cookie-name, domain-value, and path-value) to one that is already
     * stored then the old cookie is overwritten by the new one.
     */
    @Test
    fun updateCookie() {
        persistentCookieJar
            .saveFromResponse(url, listOf(TestCookieCreator.createPersistentCookie("name", "first")))
        val newCookie = TestCookieCreator.createPersistentCookie("name", "last")
        persistentCookieJar.saveFromResponse(url, listOf(newCookie))
        val storedCookies = persistentCookieJar.loadForRequest(url)
        Assert.assertEquals(1, storedCookies.size.toLong())
        Assert.assertEquals(newCookie, storedCookies[0])
    }

    /**
     * Test that a expired cookie is not retrieved
     */
    @Test
    fun expiredCookie() {
        persistentCookieJar.saveFromResponse(url, listOf(TestCookieCreator.createExpiredCookie()))
        val cookies = persistentCookieJar.loadForRequest(url)
        Assert.assertTrue(cookies.isEmpty())
    }

    /**
     * Test that when receiving an expired cookie equal(cookie-name, domain-value, and path-value) to one that is
     * already stored then the old cookie is overwritten by the new one.
     */
    @Test
    fun removeCookieWithExpiredOne() {
        persistentCookieJar.saveFromResponse(url, listOf(TestCookieCreator.createPersistentCookie(false)))
        persistentCookieJar.saveFromResponse(url, listOf(TestCookieCreator.createExpiredCookie()))
        Assert.assertTrue(persistentCookieJar.loadForRequest(url).isEmpty())
    }

    /**
     * Test that the session cookies are cleared without affecting to the persisted cookies
     */
    @Test
    fun clearSessionCookies() = runTest {
        val persistentCookie = TestCookieCreator.createPersistentCookie(false)
        persistentCookieJar.saveFromResponse(url, listOf(persistentCookie))
        persistentCookieJar.saveFromResponse(url, listOf(TestCookieCreator.createNonPersistentCookie()))
        persistentCookieJar.clearSession()

        val cookies = persistentCookieJar.loadForRequest(url)
        Assert.assertEquals(1, cookies.size.toLong())
        Assert.assertEquals(cookies[0], persistentCookie)
    }
}