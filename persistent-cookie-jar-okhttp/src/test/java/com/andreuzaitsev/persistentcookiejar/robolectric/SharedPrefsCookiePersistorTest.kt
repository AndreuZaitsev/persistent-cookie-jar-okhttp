package com.andreuzaitsev.persistentcookiejar.robolectric

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.andreuzaitsev.common.TestCookieCreator
import com.andreuzaitsev.persistentcookiejar.persistence.COOKIES_PREFERENCES_NAME
import com.andreuzaitsev.persistentcookiejar.persistence.CookiePersistor
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SharedPrefsCookiePersistorTest {

    private val sharedPreferences: SharedPreferences =
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences(COOKIES_PREFERENCES_NAME, MODE_PRIVATE)

    private val mockedSharedPreferences = mock<SharedPreferences>()
    private val persistor: CookiePersistor = CookiePersistor.PrefsImpl(sharedPreferences)

    @After
    fun clearPersistor() {
        persistor.clear()
    }

    @Test
    fun saveAll_ShouldSaveCookies() {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistor.saveAll(listOf(cookie))
        val cookies = persistor.loadAll()
        Assert.assertEquals(cookie, cookies[0])
    }

    @Test
    fun removeAll_ShouldRemoveCookies() {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistor.saveAll(listOf(cookie))
        persistor.removeAll(listOf(cookie))
        Assert.assertTrue(persistor.loadAll().isEmpty())
    }

    @Test
    fun clear_ShouldClearAllCookies() {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistor.saveAll(listOf(cookie))
        persistor.clear()
        Assert.assertTrue(persistor.loadAll().isEmpty())
    }

    /**
     * Cookie equality used to update: same cookie-name, domain-value, and path-value.
     */
    @Test
    fun addAll_WithACookieEqualsToOneAlreadyPersisted_ShouldUpdatePersistedCookie() {
        persistor.saveAll(listOf(TestCookieCreator.createPersistentCookie("name", "first")))
        val lastCookieThatShouldBeSaved = TestCookieCreator.createPersistentCookie("name", "last")
        persistor.saveAll(listOf(lastCookieThatShouldBeSaved))
        val addedCookie = persistor.loadAll()[0]
        Assert.assertEquals(lastCookieThatShouldBeSaved, addedCookie)
    }

    /**
     * This is not RFC compliant but strange things happen in the real world and it is intended to maintain a common
     * behavior between Cache and Persistor
     *
     *
     * Cookie equality used to update: same cookie-name, domain-value, and path-value.
     */
    @Test
    fun saveAll_WithMultipleEqualCookies_LastOneShouldBePersisted() {
        val equalCookieThatShouldNotBeAdded = TestCookieCreator.createPersistentCookie("name", "first")
        val equalCookieThatShouldBeAdded = TestCookieCreator.createPersistentCookie("name", "last")
        persistor.saveAll(listOf(
            equalCookieThatShouldNotBeAdded,
            equalCookieThatShouldBeAdded
        ))
        val addedCookie = persistor.loadAll()[0]
        Assert.assertEquals(equalCookieThatShouldBeAdded, addedCookie)
    }

    @Test
    fun loadAll_WithCorruptedCookie_ShouldSkipCookie() {
        val corruptedCookies = mutableMapOf<String, String>()
        corruptedCookies["key"] = "invalidCookie_"
        Mockito.doReturn(corruptedCookies).`when`(mockedSharedPreferences).all
        Assert.assertTrue(persistor.loadAll().isEmpty())
    }
}
