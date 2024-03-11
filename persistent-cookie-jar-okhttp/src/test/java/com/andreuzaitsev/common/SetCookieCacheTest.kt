package com.andreuzaitsev.common

import com.andreuzaitsev.persistentcookiejar.cache.SetCookieCache
import org.junit.Assert
import org.junit.Test

class SetCookieCacheTest {

    @Test
    fun clear_ShouldClearAllCookies() {
        val cache = SetCookieCache()
        val cookie = TestCookieCreator.createPersistentCookie(false)
        cache.addAll(listOf(cookie))
        cache.clear()
        Assert.assertFalse(cache.iterator().hasNext())
    }

    /**
     * Cookie equality used to update: same cookie-name, domain-value, and path-value.
     */
    @Test
    fun addAll_WithACookieEqualsToOneAlreadyAdded_ShouldUpdateTheStoreCookie() {
        val cache = SetCookieCache()
        cache.addAll(setOf(TestCookieCreator.createNonPersistentCookie("name", "first")))
        val newCookie = TestCookieCreator.createNonPersistentCookie("name", "last")
        cache.addAll(setOf(newCookie))
        val addedCookie = cache.iterator().next()
        Assert.assertEquals(newCookie, addedCookie)
    }

    /**
     * This is not RFC Compliant but strange things happen in the real world and it is intended to maintain a common behavior between Cache and Persistor
     *
     *
     * Cookie equality used to update: same cookie-name, domain-value, and path-value.
     */
    @Test
    fun addAll_WithMultipleEqualCookies_LastOneShouldBeAdded() {
        val cache = SetCookieCache()
        val equalCookieThatShouldNotBeAdded = TestCookieCreator.createPersistentCookie("name", "first")
        val equalCookieThatShouldBeAdded = TestCookieCreator.createPersistentCookie("name", "last")
        cache.addAll(
            listOf(
                equalCookieThatShouldNotBeAdded,
                equalCookieThatShouldBeAdded
            )
        )
        val addedCookie = cache.iterator().next()
        Assert.assertEquals(equalCookieThatShouldBeAdded, addedCookie)
    }
}
