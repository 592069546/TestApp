package com.garrick

import com.garrick.DeviceMessage.Companion.createAckClipboard
import com.garrick.DeviceMessage.Companion.createClipboard
import com.garrick.Ln.d
import java.io.IOException
import kotlin.concurrent.thread

class DeviceMessageSender(private val connection: DesktopConnection) {
    private var thread: Thread? = null
    private var clipboardText: String? = null
    private var ack: Long = 0

    private val lock = Object()

    @Synchronized
    fun pushClipboardText(text: String?) {
        clipboardText = text
        lock.notify()
    }

    @Synchronized
    fun pushAckClipboard(sequence: Long) {
        ack = sequence
        lock.notify()
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun loop() {
        while (!Thread.currentThread().isInterrupted) {
            var text: String?
            var sequence: Long
            synchronized(this) {
                while (ack == DeviceMessage.SEQUENCE_INVALID && clipboardText == null) {
                    lock.wait()
                }
                text = clipboardText
                clipboardText = null
                sequence = ack
                ack = DeviceMessage.SEQUENCE_INVALID
            }
            if (sequence != DeviceMessage.SEQUENCE_INVALID) {
                val event = createAckClipboard(sequence)
                connection.sendDeviceMessage(event)
            }
            text?.apply {
                val event = createClipboard(this)
                connection.sendDeviceMessage(event)
            }
        }
    }

    fun start() {
        thread = thread(start = true, name = "control-send") {
            try {
                loop()
            } catch (e: IOException) {
                // this is expected on close
            } catch (e: InterruptedException) {
            } finally {
                d("Device message sender stopped")
            }
        }
    }

    fun stop() {
        thread?.interrupt()
    }

    @Throws(InterruptedException::class)
    fun join() {
        thread?.join()
    }
}