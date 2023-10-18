package com.garrick

import android.annotation.TargetApi
import android.content.AttributionSource
import android.content.MutableContextWrapper
import android.os.Build
import android.os.Process

class FakeContext private constructor() : MutableContextWrapper(null) {
    override fun getPackageName(): String = PACKAGE_NAME

    override fun getOpPackageName(): String = PACKAGE_NAME

    @TargetApi(Build.VERSION_CODES.S)
    override fun getAttributionSource(): AttributionSource {
        val builder = AttributionSource.Builder(Process.SHELL_UID)
        builder.setPackageName(PACKAGE_NAME)
        return builder.build()
    }

    @get:Suppress("unused")
    val deviceId: Int
        // @Override to be added on SDK upgrade for Android 14
        get() = 0

    companion object {
        const val PACKAGE_NAME = "com.android.shell"
        const val ROOT_UID = 0 // Like android.os.Process.ROOT_UID, but before API 29
        private val INSTANCE = FakeContext()

        @JvmStatic
        fun get(): FakeContext {
            return INSTANCE
        }
    }
}