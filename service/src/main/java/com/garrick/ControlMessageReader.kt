package com.garrick

import com.garrick.Binary.i16FixedPointToFloat
import com.garrick.Binary.toUnsigned
import com.garrick.Binary.u16FixedPointToFloat
import com.garrick.Ln.w
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class ControlMessageReader {
    private val rawBuffer = ByteArray(MESSAGE_MAX_SIZE)
    private val buffer = ByteBuffer.wrap(rawBuffer).apply {
        // invariant: the buffer is always in "get" mode
        limit(0)
    }

    private val isFull: Boolean
        get() = buffer.remaining() == rawBuffer.size

    @Throws(IOException::class)
    fun readFrom(input: InputStream) {
        check(!isFull) { "Buffer full, call next() to consume" }
        buffer.compact()
        val head = buffer.position()
        val r = input.read(rawBuffer, head, rawBuffer.size - head)
        if (r == -1) {
            throw EOFException("Controller socket closed")
        }
        buffer.position(head + r)
        buffer.flip()
    }

    operator fun next(): ControlMessage? {
        if (!buffer.hasRemaining()) {
            return null
        }
        val savedPosition = buffer.position()
        val msg: ControlMessage? = when (val type = buffer.get().toInt()) {
            ControlMessage.TYPE_INJECT_KEYCODE -> parseInjectKeycode()
            ControlMessage.TYPE_INJECT_TEXT -> parseInjectText()
            ControlMessage.TYPE_INJECT_TOUCH_EVENT -> parseInjectTouchEvent()
            ControlMessage.TYPE_INJECT_SCROLL_EVENT -> parseInjectScrollEvent()
            ControlMessage.TYPE_BACK_OR_SCREEN_ON -> parseBackOrScreenOnEvent()
            ControlMessage.TYPE_GET_CLIPBOARD -> parseGetClipboard()
            ControlMessage.TYPE_SET_CLIPBOARD -> parseSetClipboard()
            ControlMessage.TYPE_SET_SCREEN_POWER_MODE -> parseSetScreenPowerMode()
            ControlMessage.TYPE_EXPAND_NOTIFICATION_PANEL,
            ControlMessage.TYPE_EXPAND_SETTINGS_PANEL,
            ControlMessage.TYPE_COLLAPSE_PANELS,
            ControlMessage.TYPE_ROTATE_DEVICE -> ControlMessage.createEmpty(type)

            else -> {
                w("Unknown event type: $type")
                null
            }
        }
        if (msg == null) {
            // failure, reset savedPosition
            buffer.position(savedPosition)
        }
        return msg
    }

    private fun parseInjectKeycode(): ControlMessage? {
        if (buffer.remaining() < INJECT_KEYCODE_PAYLOAD_LENGTH) {
            return null
        }
        val action = toUnsigned(buffer.get())
        val keycode = buffer.int
        val repeat = buffer.int
        val metaState = buffer.int
        return ControlMessage.createInjectKeycode(action, keycode, repeat, metaState)
    }

    private fun parseString(): String? {
        if (buffer.remaining() < 4) {
            return null
        }
        val len = buffer.int
        if (buffer.remaining() < len) {
            return null
        }
        val position = buffer.position()
        // Move the buffer position to consume the text
        buffer.position(position + len)
        return String(rawBuffer, position, len, StandardCharsets.UTF_8)
    }

    private fun parseInjectText(): ControlMessage? {
        val text = parseString() ?: return null
        return ControlMessage.createInjectText(text)
    }

    private fun parseInjectTouchEvent(): ControlMessage? {
        if (buffer.remaining() < INJECT_TOUCH_EVENT_PAYLOAD_LENGTH) {
            return null
        }
        val action = toUnsigned(buffer.get())
        val pointerId = buffer.long
        val position = buffer.readPosition()
        val pressure = u16FixedPointToFloat(buffer.short)
        val actionButton = buffer.int
        val buttons = buffer.int
        return ControlMessage.createInjectTouchEvent(action, pointerId, position, pressure, actionButton, buttons)
    }

    private fun parseInjectScrollEvent(): ControlMessage? {
        if (buffer.remaining() < INJECT_SCROLL_EVENT_PAYLOAD_LENGTH) {
            return null
        }
        val position = buffer.readPosition()
        val hScroll = i16FixedPointToFloat(buffer.short)
        val vScroll = i16FixedPointToFloat(buffer.short)
        val buttons = buffer.int
        return ControlMessage.createInjectScrollEvent(position, hScroll, vScroll, buttons)
    }

    private fun parseBackOrScreenOnEvent(): ControlMessage? {
        if (buffer.remaining() < BACK_OR_SCREEN_ON_LENGTH) {
            return null
        }
        val action = toUnsigned(buffer.get())
        return ControlMessage.createBackOrScreenOn(action)
    }

    private fun parseGetClipboard(): ControlMessage? {
        if (buffer.remaining() < GET_CLIPBOARD_LENGTH) {
            return null
        }
        val copyKey = toUnsigned(buffer.get())
        return ControlMessage.createGetClipboard(copyKey)
    }

    private fun parseSetClipboard(): ControlMessage? {
        if (buffer.remaining() < SET_CLIPBOARD_FIXED_PAYLOAD_LENGTH) {
            return null
        }
        val sequence = buffer.long
        val paste = buffer.get().toInt() != 0
        val text = parseString() ?: return null
        return ControlMessage.createSetClipboard(sequence, text, paste)
    }

    private fun parseSetScreenPowerMode(): ControlMessage? {
        if (buffer.remaining() < SET_SCREEN_POWER_MODE_PAYLOAD_LENGTH) {
            return null
        }
        val mode = buffer.get().toInt()
        return ControlMessage.createSetScreenPowerMode(mode)
    }

    private fun ByteBuffer.readPosition(): Position {
        val x = int
        val y = int
        val screenWidth = toUnsigned(short)
        val screenHeight = toUnsigned(short)
        return Position(x, y, screenWidth, screenHeight)
    }

    companion object {
        const val INJECT_KEYCODE_PAYLOAD_LENGTH = 13
        const val INJECT_TOUCH_EVENT_PAYLOAD_LENGTH = 31
        const val INJECT_SCROLL_EVENT_PAYLOAD_LENGTH = 20
        const val BACK_OR_SCREEN_ON_LENGTH = 1
        const val SET_SCREEN_POWER_MODE_PAYLOAD_LENGTH = 1
        const val GET_CLIPBOARD_LENGTH = 1
        const val SET_CLIPBOARD_FIXED_PAYLOAD_LENGTH = 9
        private const val MESSAGE_MAX_SIZE = 1 shl 18 // 256k
        const val CLIPBOARD_TEXT_MAX_LENGTH = MESSAGE_MAX_SIZE - 14 // type: 1 byte; sequence: 8 bytes; paste flag: 1 byte; length: 4 bytes
        const val INJECT_TEXT_MAX_LENGTH = 300
    }
}