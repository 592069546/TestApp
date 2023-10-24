package com.garrick.client.adb

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

fun executeCommand(command: String): String {
    val builder = StringBuilder()
    val process = Runtime.getRuntime().exec(command)
    try {
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            try {
                process.waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return builder.toString()
}

fun test() {
    //adb服务有可能（在Windows进程中可找到这个服务，该服务用来为模拟器或通过USB数据线连接的真机服务）会出现异常。这时需要重新对adb服务关闭和重启
    //TODO wifi 连接是否需要这一步
    executeCommand("adb start-server")
}