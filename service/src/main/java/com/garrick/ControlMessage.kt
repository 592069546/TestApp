package com.garrick

/**
 * Union of all supported event types, identified by their `type`.
 */
sealed class ControlMessage(val type: Int = 0) {
    companion object {
        const val TYPE_INJECT_KEYCODE = 0
        const val TYPE_INJECT_TEXT = 1
        const val TYPE_INJECT_TOUCH_EVENT = 2
        const val TYPE_INJECT_SCROLL_EVENT = 3
        const val TYPE_BACK_OR_SCREEN_ON = 4
        const val TYPE_EXPAND_NOTIFICATION_PANEL = 5
        const val TYPE_EXPAND_SETTINGS_PANEL = 6
        const val TYPE_COLLAPSE_PANELS = 7
        const val TYPE_GET_CLIPBOARD = 8
        const val TYPE_SET_CLIPBOARD = 9
        const val TYPE_SET_SCREEN_POWER_MODE = 10
        const val TYPE_ROTATE_DEVICE = 11
        const val SEQUENCE_INVALID: Long = 0
        const val COPY_KEY_NONE = 0
        const val COPY_KEY_COPY = 1
        const val COPY_KEY_CUT = 2

        fun createInjectKeycode(action: Int, keycode: Int, repeat: Int, metaState: Int): ControlMessage {
            return InjectKeycode(action, keycode, repeat, metaState)
        }

        fun createInjectText(text: String?): ControlMessage {
            return InjectText(text)
        }

        fun createInjectTouchEvent(
            action: Int, pointerId: Long, position: Position?,
            pressure: Float, actionButton: Int, buttons: Int
        ): ControlMessage {
            return InjectTouchEvent(action, pointerId, position, pressure, actionButton, buttons)
        }

        fun createInjectScrollEvent(position: Position?, hScroll: Float, vScroll: Float, buttons: Int): ControlMessage {
            return InjectScrollEvent(position, hScroll, vScroll, buttons)
        }

        fun createBackOrScreenOn(action: Int): ControlMessage {
            return BackOrScreenOn(action)
        }

        fun createGetClipboard(copyKey: Int): ControlMessage {
            return GetClipboard(copyKey)
        }

        fun createSetClipboard(sequence: Long, text: String?, paste: Boolean): ControlMessage {
            return SetClipboard(sequence, text, paste)
        }

        /**
         * @param mode one of the `Device.SCREEN_POWER_MODE_*` constants
         */
        fun createSetScreenPowerMode(mode: Int): ControlMessage = SetScreenPowerMode(mode)

        fun createEmpty(type: Int): ControlMessage = Empty(type)
    }
}

class InjectKeycode(
    val action: Int = 0,    // KeyEvent.ACTION_* or MotionEvent.ACTION_* or POWER_MODE_*
    val keycode: Int = 0,   // KeyEvent.KEYCODE_*
    val repeat: Int = 0,
    val metaState: Int = 0  // KeyEvent.META_*
) : ControlMessage(TYPE_INJECT_KEYCODE)

class InjectText(val text: String? = null) : ControlMessage(TYPE_INJECT_TEXT)

class InjectTouchEvent(
    val action: Int = 0,
    val pointerId: Long = 0,
    val position: Position? = null,
    val pressure: Float = 0f,
    val actionButton: Int = 0,  // MotionEvent.BUTTON_*
    val buttons: Int = 0        // MotionEvent.BUTTON_*
) : ControlMessage(TYPE_INJECT_TOUCH_EVENT)

class InjectScrollEvent(
    val position: Position? = null,
    val hScroll: Float = 0f,
    val vScroll: Float = 0f,
    val buttons: Int = 0
) : ControlMessage(TYPE_INJECT_SCROLL_EVENT)

class BackOrScreenOn(val action: Int = 0) : ControlMessage(TYPE_BACK_OR_SCREEN_ON)

class GetClipboard(val copyKey: Int = 0) : ControlMessage(TYPE_GET_CLIPBOARD)

class SetClipboard(
    val sequence: Long = 0,
    val text: String? = null,
    val paste: Boolean = false
) : ControlMessage(TYPE_SET_CLIPBOARD)

/**
 * @param mode one of the `Device.SCREEN_POWER_MODE_*` constants
 */
class SetScreenPowerMode(val mode: Int = 0) : ControlMessage(TYPE_SET_SCREEN_POWER_MODE)

class Empty(type: Int) : ControlMessage(type)