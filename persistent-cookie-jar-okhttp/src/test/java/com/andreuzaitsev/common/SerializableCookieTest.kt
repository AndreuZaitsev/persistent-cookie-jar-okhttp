package com.andreuzaitsev.common

import com.andreuzaitsev.persistentcookiejar.persistence.SerializableCookie
import org.junit.Assert
import org.junit.Test

class SerializableCookieTest {

    @Test
    fun cookieSerialization() {
        val cookie = TestCookieCreator.createPersistentCookie(false)
        val serializedCookie = SerializableCookie().encode(cookie)
        val deserializedCookie = SerializableCookie().decode(serializedCookie!!)
        Assert.assertEquals(cookie, deserializedCookie)
    }

    @Test
    fun hostOnlyDomainCookieSerialization() {
        val cookie = TestCookieCreator.createPersistentCookie(true)
        val serializedCookie = SerializableCookie().encode(cookie)
        val deserializedCookie = SerializableCookie().decode(serializedCookie!!)
        Assert.assertEquals(cookie, deserializedCookie)
    }

    @Test
    fun nonPersistentCookieSerialization() {
        val cookie = TestCookieCreator.createNonPersistentCookie()
        val serializedCookie = SerializableCookie().encode(cookie)
        val deserializedCookie = SerializableCookie().decode(serializedCookie!!)
        Assert.assertEquals(cookie, deserializedCookie)
    }
}