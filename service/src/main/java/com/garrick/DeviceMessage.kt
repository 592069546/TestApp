package com.garrick

sealed class DeviceMessage private constructor(
    val type: Int
) {
    class ClipboardMessage(val text: String) : DeviceMessage(TYPE_CLIPBOARD)

    class AckClipboardMessage(val sequence: Long = 0) : DeviceMessage(TYPE_ACK_CLIPBOARD)

    companion object {
        const val TYPE_CLIPBOARD = 0
        const val TYPE_ACK_CLIPBOARD = 1
        const val SEQUENCE_INVALID = ControlMessage.SEQUENCE_INVALID

        @JvmStatic
        fun createClipboard(text: String): DeviceMessage = ClipboardMessage(text)

        @JvmStatic
        fun createAckClipboard(sequence: Long): DeviceMessage = AckClipboardMessage(sequence)
    }
}