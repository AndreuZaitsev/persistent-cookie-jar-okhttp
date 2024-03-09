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
package com.andreuzaitsev.persistentcookiejar.persistence

import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serial
import java.io.Serializable
import okhttp3.Cookie

class SerializableCookie : Serializable {

    @Transient
    private var cookie: Cookie? = null

    fun encode(cookie: Cookie?): String? {
        this.cookie = cookie
        val byteArrayOutputStream = ByteArrayOutputStream()
        var objectOutputStream: ObjectOutputStream? = null
        try {
            objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(this)
        } catch (e: IOException) {
            Log.d(TAG, "IOException in encodeCookie", e)
            return null
        } finally {
            if (objectOutputStream != null) {
                try {
                    // Closing a ByteArrayOutputStream has no effect,
                    // it can be used later (and is used in the return statement)
                    objectOutputStream.close()
                } catch (e: IOException) {
                    Log.d(TAG, "Stream not closed in encodeCookie", e)
                }
            }
        }
        return byteArrayToHexString(byteArrayOutputStream.toByteArray())
    }

    fun decode(encodedCookie: String): Cookie? {
        val bytes = hexStringToByteArray(encodedCookie)
        val byteArrayInputStream = ByteArrayInputStream(
            bytes)
        var cookie: Cookie? = null
        var objectInputStream: ObjectInputStream? = null
        try {
            objectInputStream = ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as SerializableCookie).cookie
        } catch (e: IOException) {
            Log.d(TAG, "IOException in decodeCookie", e)
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "ClassNotFoundException in decodeCookie", e)
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close()
                } catch (e: IOException) {
                    Log.d(TAG, "Stream not closed in decodeCookie", e)
                }
            }
        }
        return cookie
    }

    @Serial @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        cookie?.run {
            out.writeObject(name)
            out.writeObject(value)
            out.writeLong(if (persistent) expiresAt else NON_VALID_EXPIRES_AT)
            out.writeObject(domain)
            out.writeObject(path)
            out.writeBoolean(secure)
            out.writeBoolean(httpOnly)
            out.writeBoolean(hostOnly)
        }
    }

    @Serial @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val builder = Cookie.Builder()
        builder.name(`in`.readObject() as String)
        builder.value(`in`.readObject() as String)
        val expiresAt = `in`.readLong()
        if (expiresAt != NON_VALID_EXPIRES_AT) {
            builder.expiresAt(expiresAt)
        }
        val domain = `in`.readObject() as String
        builder.domain(domain)
        builder.path(`in`.readObject() as String)
        if (`in`.readBoolean()) builder.secure()
        if (`in`.readBoolean()) builder.httpOnly()
        if (`in`.readBoolean()) builder.hostOnlyDomain(domain)
        cookie = builder.build()
    }

    companion object {

        private val TAG = SerializableCookie::class.java.getSimpleName()

        @Serial
        private val serialVersionUID = -8594045714036645534L

        /**
         * Using some super basic byte array &lt;-&gt; hex conversions so we don't
         * have to rely on any large Base64 libraries. Can be overridden if you
         * like!
         *
         * @param bytes byte array to be converted
         * @return string containing hex values
         */
        private fun byteArrayToHexString(bytes: ByteArray): String {
            val sb = StringBuilder(bytes.size * 2)
            for (element in bytes) {
                val v = element.toInt() and 0xff
                if (v < 16) {
                    sb.append('0')
                }
                sb.append(Integer.toHexString(v))
            }
            return sb.toString()
        }

        /**
         * Converts hex values from strings to byte array
         *
         * @param hexString string of hex-encoded values
         * @return decoded byte array
         */
        private fun hexStringToByteArray(hexString: String): ByteArray {
            val len = hexString.length
            val data = ByteArray(len / 2)
            for (i in 0 until len step 2) {
                val firstPart = Character.digit(hexString[i], 16) shl 4
                val secondPart = Character.digit(hexString[i + 1], 16)
                data[i / 2] = (firstPart + secondPart).toByte()
            }
            return data
        }

        private const val NON_VALID_EXPIRES_AT = -1L
    }
}