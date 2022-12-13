package com.dhy.compatbundle

import java.util.*

inline fun <reified S> loadAutoService(): S? {
    return try {
        ServiceLoader.load(S::class.java).firstOrNull()
    } catch (e: ServiceConfigurationError) {
        e.printStackTrace()
        null
    }
}

