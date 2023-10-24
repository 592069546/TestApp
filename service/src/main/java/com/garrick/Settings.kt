package com.garrick

import android.os.Build
import com.garrick.Command.exec
import com.garrick.Command.execReadLine
import com.garrick.Ln.w
import com.garrick.wrappers.ContentProvider
import com.garrick.wrappers.ServiceManager
import java.io.IOException

object Settings {
    const val TABLE_SYSTEM = ContentProvider.TABLE_SYSTEM
    const val TABLE_SECURE = ContentProvider.TABLE_SECURE
    const val TABLE_GLOBAL = ContentProvider.TABLE_GLOBAL

    @Throws(SettingsException::class)
    @JvmStatic
    private fun execSettingsPut(table: String, key: String, value: String) {
        try {
            exec("settings", "put", table, key, value)
        } catch (e: IOException) {
            throw SettingsException("put", table, key, value, e)
        } catch (e: InterruptedException) {
            throw SettingsException("put", table, key, value, e)
        }
    }

    @Throws(SettingsException::class)
    @JvmStatic
    private fun execSettingsGet(table: String, key: String): String {
        return try {
            execReadLine("settings", "get", table, key)
        } catch (e: IOException) {
            throw SettingsException("get", table, key, null, e)
        } catch (e: InterruptedException) {
            throw SettingsException("get", table, key, null, e)
        }
    }

    @Throws(SettingsException::class)
    @JvmStatic
    fun getValue(table: String, key: String): String {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            // on Android >= 12, it always fails: <https://github.com/Genymobile/scrcpy/issues/2788>
            try {
                return ServiceManager.getActivityManager().createSettingsProvider().use { provider ->
                    provider.getValue(table, key)
                }
            } catch (e: SettingsException) {
                w("Could not get settings value via ContentProvider, fallback to settings process", e)
            }
        }
        return execSettingsGet(table, key)
    }

    @Throws(SettingsException::class)
    @JvmStatic
    fun putValue(table: String, key: String, value: String) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            // on Android >= 12, it always fails: <https://github.com/Genymobile/scrcpy/issues/2788>
            try {
                ServiceManager.getActivityManager().createSettingsProvider().use { provider ->
                    provider.putValue(table, key, value)
                }
            } catch (e: SettingsException) {
                w("Could not put settings value via ContentProvider, fallback to settings process", e)
            }
        }
        execSettingsPut(table, key, value)
    }

    @Throws(SettingsException::class)
    @JvmStatic
    fun getAndPutValue(table: String, key: String, value: String): String {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            // on Android >= 12, it always fails: <https://github.com/Genymobile/scrcpy/issues/2788>
            try {
                ServiceManager.getActivityManager().createSettingsProvider().use { provider ->
                    val oldValue = provider.getValue(table, key)
                    if (value != oldValue) {
                        provider.putValue(table, key, value)
                    }
                    return oldValue
                }
            } catch (e: SettingsException) {
                w("Could not get and put settings value via ContentProvider, fallback to settings process", e)
            }
        }
        val oldValue = getValue(table, key)
        if (value != oldValue) {
            putValue(table, key, value)
        }
        return oldValue
    }
}