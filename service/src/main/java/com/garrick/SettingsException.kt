package com.garrick

class SettingsException(method: String?, table: String?, key: String?, value: String?, cause: Throwable?) :
    Exception(createMessage(method, table, key, value), cause) {
    companion object {
        private fun createMessage(method: String?, table: String?, key: String?, value: String?): String =
            "Could not access settings: ${method.tip("method")} ${table.tip("table")} ${key.tip("key")} ${value.tip("value")}"

        private fun String?.tip(tip: String): String = "$tip: " + if (isNullOrEmpty()) "" else " $this"
    }
}