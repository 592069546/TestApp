package com.garrick

class DeviceMessage private constructor(
    val type: Int,
    val text: String? = null,
    val sequence: Long = 0
) {
    companion object {
        const val TYPE_CLIPBOARD = 0
        const val TYPE_ACK_CLIPBOARD = 1
        const val SEQUENCE_INVALID = ControlMessage.SEQUENCE_INVALID

        @JvmStatic
        fun createClipboard(text: String?): DeviceMessage =
            DeviceMessage(TYPE_CLIPBOARD, text)

        @JvmStatic
        fun createAckClipboard(sequence: Long): DeviceMessage =
            DeviceMessage(TYPE_ACK_CLIPBOARD, sequence = sequence)
    }
}