package com.andreuzaitsev.datastorepersistentcookiejar.robolectric

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import com.andreuzaitsev.common.TestCookieCreator
import com.andreuzaitsev.persistentcookiejar.persistence.CoroutineCookiePersistor
import com.andreuzaitsev.persistentcookiejar.persistence.dataStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
class DataStoreCookiePersistorTest {

    private val dataStore: DataStore<Preferences> = ApplicationProvider.getApplicationContext<Context>().dataStore
    private val mockedDataStore: DataStore<Preferences> = mock()
    private val persistor: CoroutineCookiePersistor = CoroutineCookiePersistor.DataStoreImpl(dataStore)

    @After
    fun clearPersistor() = runTest {
        persistor.clear()
    }

    @Test
    fun saveAll_ShouldSaveCookies() = runTest {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistor.saveAll(listOf(cookie))
        val cookies = persistor.loadAll()
        Assert.assertEquals(cookie, cookies[0])
    }

    @Test
    fun removeAll_ShouldRemoveCookies() = runTest {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistor.saveAll(listOf(cookie))
        persistor.removeAll(listOf(cookie))
        Assert.assertTrue(persistor.loadAll().isEmpty())
    }

    @Test
    fun clear_ShouldClearAllCookies() = runTest {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        persistor.saveAll(listOf(cookie))
        persistor.clear()
        Assert.assertTrue(persistor.loadAll().isEmpty())
    }

    /**
     * Cookie equality used to update: same cookie-name, domain-value, and path-value.
     */
    @Test
    fun addAll_WithACookieEqualsToOneAlreadyPersisted_ShouldUpdatePersistedCookie() = runTest {
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
    fun saveAll_WithMultipleEqualCookies_LastOneShouldBePersisted() = runTest {
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
    fun loadAll_WithCorruptedCookie_ShouldSkipCookie() = runTest {
        val corruptedCookies = mutableMapOf<String, String>()
        corruptedCookies["key"] = "invalidCookie_"
        Mockito.doReturn(flowOf(corruptedCookies)).`when`(mockedDataStore).data
        Assert.assertTrue(persistor.loadAll().isEmpty())
    }
}
