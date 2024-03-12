# persistent-cookie-jar-okhttp for OkHttp 3

[![Release](https://jitpack.io/v/thomas-bouvier/persistent-cookie-jar-okhttp.svg)](https://jitpack.io/#thomas-bouvier/persistent-cookie-jar-okhttp)

A persistent CookieJar implementation for OkHttp 3 based on DataStore.

## Download

Step 1. Add the JitPack repository in your root build.gradle at the end of repositories:

```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

Step 2. Add the dependency

```groovy
dependencies {
    implementation "com.github.andreu-zaitsev:persistent-cookie-jar:1.0.0"
}
```

## Usage

Create an instance of `PersistentCookieJar` passing a `CookieCache` and a `CookiePersistor`:

```java
ClearableCookieJar cookieJar = new PersistentCookieJar(context);
```

Then just add the CookieJar when building your OkHttp client:

```java
OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
```

## Features

This is a really simple library but here are some of the things that it provides:
* Possibility to clear the jar: `PersistentCookieJar` implements `ClearableCookieJar` interface that declares a `clear()` method for removing all cookies from the jar.

* Possibility to clear session cookies: `PersistentCookieJar` implements `ClearableCookieJar` interface that declares a `clearSession()` method for removing session cookies from the jar.

* Decoupled and extensible: `CookieCache` and `CookiePersistor` are interfaces so you can provide your own implementation for each one.
    * `CookieCache` represents an in-memory cookie storage. `SetCookieCache` is the provided implementation that uses a Set to store the Cookies.
    * `CoroutineCookiePersistor` represents a persistent storage.
    * `CoroutineCookiePersistor.DataStoreImpl` is the provided implementation that uses a DataStore to persist the Cookies.

* Thread-safe: `PersistentCookieJar` public methods are synchronized so there is no need to worry about threading if you need to implement a `CookieCache` or a `CookiePersistor`.

## License

    Copyright 2016 Francisco José Montiel Navarro
    Copyright 2024 Andreu Zaitsev

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
