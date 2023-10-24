package com.garrick

import com.garrick.IO.toString
import java.io.IOException
import java.util.Scanner

object Command {
    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun exec(vararg cmd: String) {
        val process = Runtime.getRuntime().exec(cmd)
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IOException("Command " + cmd.contentToString() + " returned with value " + exitCode)
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun execReadLine(vararg cmd: String): String {
        var result = ""
        val process = Runtime.getRuntime().exec(cmd)
        val scanner = Scanner(process.inputStream)
        if (scanner.hasNextLine()) {
            result = scanner.nextLine()
        }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IOException("Command " + cmd.contentToString() + " returned with value " + exitCode)
        }
        return result
    }

    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun execReadOutput(vararg cmd: String): String {
        val process = Runtime.getRuntime().exec(cmd)
        val output = toString(process.inputStream)
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IOException("Command " + cmd.contentToString() + " returned with value " + exitCode)
        }
        return output
    }
}