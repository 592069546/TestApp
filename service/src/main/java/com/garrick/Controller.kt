package com.garrick

import android.os.Build
import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import com.garrick.AsyncProcessor.TerminationListener
import com.garrick.KeyComposition.decompose
import com.garrick.Ln.d
import com.garrick.Ln.i
import com.garrick.Ln.w
import com.garrick.wrappers.InputManager
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class Controller(
    private val device: Device,
    private val connection: DesktopConnection,
    private val clipboardAutosync: Boolean,
    private val powerOn: Boolean
) :
    AsyncProcessor {
    private var thread: Thread? = null
    val sender: DeviceMessageSender = DeviceMessageSender(connection)
    private val charMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private var lastTouchDown: Long = 0
    private val pointersState = PointersState()
    private val pointerProperties = Array(PointersState.MAX_POINTERS) {
        PointerProperties().apply {
            toolType = MotionEvent.TOOL_TYPE_FINGER
        }
    }
    private val pointerCoords = Array(PointersState.MAX_POINTERS) {
        PointerCoords().apply {
            orientation = 0f
            size = 0f
        }
    }
    private var keepPowerModeOff = false

    @Throws(IOException::class)
    private fun control() {
        // on start, power on the device
        if (powerOn && !Device.isScreenOn()) {
            device.pressReleaseKeycode(KeyEvent.KEYCODE_POWER, Device.INJECT_MODE_ASYNC)

            // dirty hack
            // After POWER is injected, the device is powered on asynchronously.
            // To turn the device screen off while mirroring, the client will send a message that
            // would be handled before the device is actually powered on, so its effect would
            // be "canceled" once the device is turned back on.
            // Adding this delay prevents to handle the message before the device is actually
            // powered on.
            SystemClock.sleep(500)
        }
        while (!Thread.currentThread().isInterrupted) {
            handleEvent()
        }
    }

    override fun start(listener: TerminationListener) {
        thread = thread(start = true, name = "control-recv") {
            try {
                control()
            } catch (e: IOException) {
                // this is expected on close
            } finally {
                d("Controller stopped")
                listener.onTerminated(true)
            }
        }
        sender.start()
    }

    override fun stop() {
        thread?.interrupt()
        sender.stop()
    }

    @Throws(InterruptedException::class)
    override fun join() {
        thread?.join()
        sender.join()
    }

    @Throws(IOException::class)
    private fun handleEvent() {
        val msg = connection.receiveControlMessage()
        when {
            msg is InjectKeycode -> if (device.supportsInputEvents()) {
                msg.injectKeycode()
            }

            msg is InjectText -> if (device.supportsInputEvents()) {
                msg.injectText()
            }

            msg is InjectTouchEvent -> if (device.supportsInputEvents()) {
                msg.injectTouch()
            }

            msg is InjectScrollEvent -> if (device.supportsInputEvents()) {
                msg.injectScroll()
            }

            msg is BackOrScreenOn -> if (device.supportsInputEvents()) {
                msg.pressBackOrTurnScreenOn()
            }

            msg is GetClipboard -> msg.getClipboard()
            msg is SetClipboard -> msg.setClipboard()
            msg is SetScreenPowerMode -> if (device.supportsInputEvents()) {
                val mode: Int = msg.mode
                val setPowerModeOk = Device.setScreenPowerMode(mode)
                if (setPowerModeOk) {
                    keepPowerModeOff = mode == Device.POWER_MODE_OFF
                    i("Device screen turned " + if (mode == Device.POWER_MODE_OFF) "off" else "on")
                }
            }

            msg?.type == ControlMessage.TYPE_EXPAND_NOTIFICATION_PANEL -> Device.expandNotificationPanel()
            msg?.type == ControlMessage.TYPE_EXPAND_SETTINGS_PANEL -> Device.expandSettingsPanel()
            msg?.type == ControlMessage.TYPE_COLLAPSE_PANELS -> Device.collapsePanels()
            msg?.type == ControlMessage.TYPE_ROTATE_DEVICE -> Device.rotateDevice()
            else -> {}
        }
    }

    private fun InjectKeycode.injectKeycode(): Boolean {
        if (keepPowerModeOff && action == KeyEvent.ACTION_UP && (keycode == KeyEvent.KEYCODE_POWER || keycode == KeyEvent.KEYCODE_WAKEUP)) {
            schedulePowerModeOff()
        }
        return device.injectKeyEvent(action, keycode, repeat, metaState, Device.INJECT_MODE_ASYNC)
    }

    private fun Char.injectChar(): Boolean {
        val decomposed = decompose(this)
        val chars = decomposed?.toCharArray() ?: charArrayOf(this)
        val events = charMap.getEvents(chars) ?: return false
        for (event in events) {
            if (!device.injectEvent(event, Device.INJECT_MODE_ASYNC)) {
                return false
            }
        }
        return true
    }

    private fun InjectText.injectText(): Int = text?.toCharArray()?.filter {
        it.injectChar().apply {
            if (!this) w("Could not inject char u+" + String.format("%04x", it.code))
        }
    }?.size ?: 0

    private fun InjectTouchEvent.injectTouch(): Boolean {
        var action = action
        var buttons = buttons
        val now = SystemClock.uptimeMillis()
        val point = device.getPhysicalPoint(position)
        if (point == null) {
            w("Ignore touch event, it was generated for a different device size")
            return false
        }
        val pointerIndex = pointersState.getPointerIndex(pointerId)
        if (pointerIndex == -1) {
            w("Too many pointers for touch event")
            return false
        }
        val pointer = pointersState[pointerIndex]
        pointer.point = point
        pointer.pressure = pressure
        val source: Int
        if (pointerId == POINTER_ID_MOUSE.toLong() || pointerId == POINTER_ID_VIRTUAL_MOUSE.toLong()) {
            // real mouse event (forced by the client when --forward-on-click)
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_MOUSE
            source = InputDevice.SOURCE_MOUSE
            pointer.isUp = buttons == 0
        } else {
            // POINTER_ID_GENERIC_FINGER, POINTER_ID_VIRTUAL_FINGER or real touch from device
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_FINGER
            source = InputDevice.SOURCE_TOUCHSCREEN
            // Buttons must not be set for touch events
            buttons = 0
            pointer.isUp = action == MotionEvent.ACTION_UP
        }
        val pointerCount = pointersState.update(pointerProperties, pointerCoords)
        if (pointerCount == 1) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchDown = now
            }
        } else {
            // secondary pointers must use ACTION_POINTER_* ORed with the pointerIndex
            if (action == MotionEvent.ACTION_UP) {
                action = MotionEvent.ACTION_POINTER_UP or (pointerIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
            } else if (action == MotionEvent.ACTION_DOWN) {
                action = MotionEvent.ACTION_POINTER_DOWN or (pointerIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
            }
        }

        /* If the input device is a mouse (on API >= 23):
         *   - the first button pressed must first generate ACTION_DOWN;
         *   - all button pressed (including the first one) must generate ACTION_BUTTON_PRESS;
         *   - all button released (including the last one) must generate ACTION_BUTTON_RELEASE;
         *   - the last button released must in addition generate ACTION_UP.
         *
         * Otherwise, Chrome does not work properly: <https://github.com/Genymobile/scrcpy/issues/3635>
         */if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && source == InputDevice.SOURCE_MOUSE) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (actionButton == buttons) {
                    // First button pressed: ACTION_DOWN
                    val downEvent = MotionEvent.obtain(
                        lastTouchDown, now, MotionEvent.ACTION_DOWN, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0
                    )
                    if (!device.injectEvent(downEvent, Device.INJECT_MODE_ASYNC)) {
                        return false
                    }
                }

                // Any button pressed: ACTION_BUTTON_PRESS
                val pressEvent = MotionEvent.obtain(
                    lastTouchDown, now, MotionEvent.ACTION_BUTTON_PRESS, pointerCount, pointerProperties,
                    pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0
                )
                if (!InputManager.setActionButton(pressEvent, actionButton)) {
                    return false
                }
                return device.injectEvent(pressEvent, Device.INJECT_MODE_ASYNC)
            }
            if (action == MotionEvent.ACTION_UP) {
                // Any button released: ACTION_BUTTON_RELEASE
                val releaseEvent = MotionEvent.obtain(
                    lastTouchDown, now, MotionEvent.ACTION_BUTTON_RELEASE, pointerCount, pointerProperties,
                    pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0
                )
                if (!InputManager.setActionButton(releaseEvent, actionButton)) {
                    return false
                }
                if (!device.injectEvent(releaseEvent, Device.INJECT_MODE_ASYNC)) {
                    return false
                }
                if (buttons == 0) {
                    // Last button released: ACTION_UP
                    val upEvent = MotionEvent.obtain(
                        lastTouchDown, now, MotionEvent.ACTION_UP, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0
                    )
                    if (!device.injectEvent(upEvent, Device.INJECT_MODE_ASYNC)) {
                        return false
                    }
                }
                return true
            }
        }
        val event = MotionEvent.obtain(
            lastTouchDown, now, action, pointerCount, pointerProperties, pointerCoords, 0,
            buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0
        )
        return device.injectEvent(event, Device.INJECT_MODE_ASYNC)
    }

    private fun InjectScrollEvent.injectScroll(): Boolean {
        val now = SystemClock.uptimeMillis()
        val (x, y) = device.getPhysicalPoint(position)
            ?: // ignore event
            return false
        val props = pointerProperties[0]
        props.id = 0
        val coords = pointerCoords[0]
        coords.x = x.toFloat()
        coords.y = y.toFloat()
        coords.setAxisValue(MotionEvent.AXIS_HSCROLL, hScroll)
        coords.setAxisValue(MotionEvent.AXIS_VSCROLL, vScroll)
        val event = MotionEvent.obtain(
            lastTouchDown, now, MotionEvent.ACTION_SCROLL, 1, pointerProperties, pointerCoords, 0,
            buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, InputDevice.SOURCE_MOUSE, 0
        )
        return device.injectEvent(event, Device.INJECT_MODE_ASYNC)
    }

    private fun BackOrScreenOn.pressBackOrTurnScreenOn(): Boolean {
        if (Device.isScreenOn()) {
            return device.injectKeyEvent(action, KeyEvent.KEYCODE_BACK, 0, 0, Device.INJECT_MODE_ASYNC)
        }

        // Screen is off
        // Only press POWER on ACTION_DOWN
        if (action != KeyEvent.ACTION_DOWN) {
            // do nothing,
            return true
        }
        if (keepPowerModeOff) {
            schedulePowerModeOff()
        }
        return device.pressReleaseKeycode(KeyEvent.KEYCODE_POWER, Device.INJECT_MODE_ASYNC)
    }

    private fun GetClipboard.getClipboard() {
        // On Android >= 7, press the COPY or CUT key if requested
        if (copyKey != ControlMessage.COPY_KEY_NONE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && device.supportsInputEvents()) {
            val key = if (copyKey == ControlMessage.COPY_KEY_COPY) KeyEvent.KEYCODE_COPY else KeyEvent.KEYCODE_CUT
            // Wait until the event is finished, to ensure that the clipboard text we read just after is the correct one
            device.pressReleaseKeycode(key, Device.INJECT_MODE_WAIT_FOR_FINISH)
        }

        // If clipboard autosync is enabled, then the device clipboard is synchronized to the computer clipboard whenever it changes, in
        // particular when COPY or CUT are injected, so it should not be synchronized twice. On Android < 7, do not synchronize at all rather than
        // copying an old clipboard content.
        if (!clipboardAutosync) {
            val clipboardText = Device.getClipboardText()
            if (clipboardText != null) {
                sender.pushClipboardText(clipboardText)
            }
        }
    }

    private fun SetClipboard.setClipboard(): Boolean {
        val ok = device.setClipboardText(text)
        if (ok) {
            i("Device clipboard set")
        }

        // On Android >= 7, also press the PASTE key if requested
        if (paste && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && device.supportsInputEvents()) {
            device.pressReleaseKeycode(KeyEvent.KEYCODE_PASTE, Device.INJECT_MODE_ASYNC)
        }
        if (sequence != ControlMessage.SEQUENCE_INVALID) {
            // Acknowledgement requested
            sender.pushAckClipboard(sequence)
        }
        return ok
    }

    companion object {
        private const val DEFAULT_DEVICE_ID = 0

        // control_msg.h values of the pointerId field in inject_touch_event message
        private const val POINTER_ID_MOUSE = -1
        private const val POINTER_ID_VIRTUAL_MOUSE = -3
        private val EXECUTOR = Executors.newSingleThreadScheduledExecutor()

        /**
         * Schedule a call to set power mode to off after a small delay.
         */
        private fun schedulePowerModeOff() {
            EXECUTOR.schedule({
                i("Forcing screen off")
                Device.setScreenPowerMode(Device.POWER_MODE_OFF)
            }, 200, TimeUnit.MILLISECONDS)
        }
    }
}