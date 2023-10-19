package com.garrick

import android.util.Log

/**
 * Log both to Android logger (so that logs are visible in "adb logcat") and standard output/error (so that they are visible in the terminal
 * directly).
 */
object Ln {
    private const val TAG = "scrcpy"
    private const val PREFIX = "[server] "
    private var threshold = Level.INFO

    /**
     * Initialize the log level.
     *
     *
     * Must be called before starting any new thread.
     *
     * @param level the log level
     */
    @JvmStatic
    fun initLogLevel(level: Level) {
        threshold = level
    }

    @JvmStatic
    fun isEnabled(level: Level): Boolean {
        return level.ordinal >= threshold.ordinal
    }

    @JvmStatic
    fun v(message: String) {
        if (isEnabled(Level.VERBOSE)) {
            Log.v(TAG, message)
            println("${PREFIX}VERBOSE: $message")
        }
    }

    @JvmStatic
    fun d(message: String) {
        if (isEnabled(Level.DEBUG)) {
            Log.d(TAG, message)
            println("${PREFIX}DEBUG: $message")
        }
    }

    @JvmStatic
    fun i(message: String) {
        if (isEnabled(Level.INFO)) {
            Log.i(TAG, message)
            println("${PREFIX}INFO: $message")
        }
    }

    @JvmStatic
    fun w(message: String, throwable: Throwable? = null) {
        if (isEnabled(Level.WARN)) {
            Log.w(TAG, message, throwable)
            System.err.println("${PREFIX}WARN: $message")
            throwable?.printStackTrace()
        }
    }

    @JvmStatic
    fun w(message: String) = w(message, null)

    @JvmStatic
    fun e(message: String, throwable: Throwable? = null) {
        if (isEnabled(Level.ERROR)) {
            Log.e(TAG, message, throwable)
            System.err.println("${PREFIX}ERROR: $message")
            throwable?.printStackTrace()
        }
    }

    @JvmStatic
    fun e(message: String) = e(message, null)

    enum class Level {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }
}