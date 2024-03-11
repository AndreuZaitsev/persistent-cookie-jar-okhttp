package com.andreuzaitsev.common

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl

internal object TestCookieCreator {

    private const val DEFAULT_DOMAIN = "domain.com"
    private const val DEFAULT_PATH = "/"

    @JvmField
    val DEFAULT_URL = "https://$DEFAULT_DOMAIN$DEFAULT_PATH".toHttpUrl()

    @JvmField
    val OTHER_URL = "https://otherdomain.com/".toHttpUrl()

    @JvmStatic
    fun createPersistentCookie(hostOnlyDomain: Boolean): Cookie {
        val builder = Cookie.Builder()
            .path(DEFAULT_PATH)
            .name("name")
            .value("value")
            .expiresAt(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
            .httpOnly()
            .secure()
        if (hostOnlyDomain) {
            builder.hostOnlyDomain(DEFAULT_DOMAIN)
        } else {
            builder.domain(DEFAULT_DOMAIN)
        }
        return builder.build()
    }

    fun createPersistentCookie(name: String, value: String): Cookie {
        return Cookie.Builder()
            .domain(DEFAULT_DOMAIN)
            .path(DEFAULT_PATH)
            .name(name)
            .value(value)
            .expiresAt(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
            .httpOnly()
            .secure()
            .build()
    }

    @JvmStatic
    fun createNonPersistentCookie(): Cookie {
        return Cookie.Builder()
            .domain(DEFAULT_DOMAIN)
            .path(DEFAULT_PATH)
            .name("name")
            .value("value")
            .httpOnly()
            .secure()
            .build()
    }

    fun createNonPersistentCookie(name: String, value: String): Cookie {
        return Cookie.Builder()
            .domain(DEFAULT_DOMAIN)
            .path(DEFAULT_PATH)
            .name(name)
            .value(value)
            .httpOnly()
            .secure()
            .build()
    }

    @JvmStatic
    fun createExpiredCookie(): Cookie {
        return Cookie.Builder()
            .domain(DEFAULT_DOMAIN)
            .path(DEFAULT_PATH)
            .name("name")
            .value("value")
            .expiresAt(Long.MIN_VALUE)
            .httpOnly()
            .secure()
            .build()
    }
}
