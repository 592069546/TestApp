package com.garrick.client.adb

data class AdbDevice(
    val serial: String? = null,
    val state: String? = null,
    val mode: String? = null,
    val selected: Boolean = false
) {
}

enum class DeviceType {
    SC_ADB_DEVICE_TYPE_USB,
    SC_ADB_DEVICE_TYPE_TCP_IP,
    SC_ADB_DEVICE_TYPE_EMULATOR
}

fun String?.getDeviceType(): DeviceType = if (isNullOrEmpty())
    DeviceType.SC_ADB_DEVICE_TYPE_USB
else if (startsWith("emulator-"))
    DeviceType.SC_ADB_DEVICE_TYPE_EMULATOR
else if (contains(":"))
    DeviceType.SC_ADB_DEVICE_TYPE_TCP_IP
else DeviceType.SC_ADB_DEVICE_TYPE_USB