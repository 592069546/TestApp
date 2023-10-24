package com.garrick

import com.garrick.StringUtils.getUtf8TruncationIndex
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class DeviceMessageWriter {
    private val rawBuffer = ByteArray(MESSAGE_MAX_SIZE)
    private val buffer = ByteBuffer.wrap(rawBuffer)

    @Throws(IOException::class)
    fun writeTo(msg: DeviceMessage, output: OutputStream) {
        buffer.clear()
        buffer.put(msg.type.toByte())
        when (msg) {
            is DeviceMessage.ClipboardMessage -> {
                val raw = msg.text.toByteArray(StandardCharsets.UTF_8)
                val len = getUtf8TruncationIndex(raw, CLIPBOARD_TEXT_MAX_LENGTH)
                buffer.putInt(len)
                buffer.put(raw, 0, len)
                output.write(rawBuffer, 0, buffer.position())
            }

            is DeviceMessage.AckClipboardMessage -> {
                buffer.putLong(msg.sequence)
                output.write(rawBuffer, 0, buffer.position())
            }
        }
    }

    companion object {
        private const val MESSAGE_MAX_SIZE = 1 shl 18 // 256k
        const val CLIPBOARD_TEXT_MAX_LENGTH = MESSAGE_MAX_SIZE - 5 // type: 1 byte; length: 4 bytes
    }
}