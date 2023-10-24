package com.garrick.client.adb

data class DeviceSelector(
    val type: DeviceSelectorType,
    val serial: String? = null
) {
}

enum class DeviceSelectorType {
    SC_ADB_DEVICE_SELECT_ALL,
    SC_ADB_DEVICE_SELECT_SERIAL,
    SC_ADB_DEVICE_SELECT_USB,
    SC_ADB_DEVICE_SELECT_TCP_IP
}