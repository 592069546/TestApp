package com.garrick.client.adb

data class Tunnel(
    val enabled: Boolean = false,
    val forward: Boolean = false,
    val serverSocket: Int = -1,     // use "adb forward" instead of "adb reverse"
    val localPort: Int = 0          // only used if !forward
) {
}