package com.garrick

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.garrick.IO.writeFully
import com.garrick.StringUtils.getUtf8TruncationIndex
import java.io.Closeable
import java.io.FileDescriptor
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class DesktopConnection private constructor(
    private val videoSocket: LocalSocket?,
    private val audioSocket: LocalSocket?,
    private val controlSocket: LocalSocket?
) : Closeable {
    val videoFd: FileDescriptor? = videoSocket?.fileDescriptor
    val audioFd: FileDescriptor? = audioSocket?.fileDescriptor
    private val controlInputStream: InputStream? = controlSocket?.inputStream
    private val controlOutputStream: OutputStream? = controlSocket?.outputStream
    private val reader = ControlMessageReader()
    private val writer = DeviceMessageWriter()

    private val firstSocket: LocalSocket?
        get() = videoSocket ?: (audioSocket ?: controlSocket)

    @Throws(IOException::class)
    override fun close() {
        videoSocket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        audioSocket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        controlSocket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
    }

    @Throws(IOException::class)
    fun sendDeviceMeta(deviceName: String) {
        val buffer = ByteArray(DEVICE_NAME_FIELD_LENGTH)
        val deviceNameBytes = deviceName.toByteArray(StandardCharsets.UTF_8)
        val len = getUtf8TruncationIndex(deviceNameBytes, DEVICE_NAME_FIELD_LENGTH - 1)
        System.arraycopy(deviceNameBytes, 0, buffer, 0, len)
        // byte[] are always 0-initialized in java, no need to set '\0' explicitly
        firstSocket?.fileDescriptor?.apply {
            writeFully(this, buffer)
        }
    }

    @Throws(IOException::class)
    fun receiveControlMessage(): ControlMessage? {
        controlInputStream ?: return null
        var msg = reader.next()
        while (msg == null) {
            reader.readFrom(controlInputStream)
            msg = reader.next()
        }
        return msg
    }

    @Throws(IOException::class)
    fun sendDeviceMessage(msg: DeviceMessage) {
        controlOutputStream?.apply {
            writer.writeTo(msg, this)
        }
    }

    companion object {
        private const val DEVICE_NAME_FIELD_LENGTH = 64
        private const val SOCKET_NAME_PREFIX = "scrcpy"

        @Throws(IOException::class)
        private fun connect(abstractName: String): LocalSocket = LocalSocket().apply {
            connect(LocalSocketAddress(abstractName))
        }

        private fun getSocketName(scid: Int): String {
            return if (scid == -1) {
                // If no SCID is set, use "scrcpy" to simplify using scrcpy-server alone
                SOCKET_NAME_PREFIX
            } else SOCKET_NAME_PREFIX + String.format("_%08x", scid)
        }

        @Throws(IOException::class)
        @JvmStatic
        fun open(
            scid: Int,
            tunnelForward: Boolean,
            video: Boolean,
            audio: Boolean,
            control: Boolean,
            sendDummyByte: Boolean
        ): DesktopConnection {
            var sendDummyB = sendDummyByte
            val socketName = getSocketName(scid)
            var videoSocket: LocalSocket? = null
            var audioSocket: LocalSocket? = null
            var controlSocket: LocalSocket? = null
            try {
                if (tunnelForward) {
                    LocalServerSocket(socketName).using { localServerSocket ->
                        if (video) {
                            videoSocket = localServerSocket.accept().apply {
                                if (sendDummyB) {
                                    // send one byte so the client may read() to detect a connection error
                                    outputStream.write(0)
                                    sendDummyB = false
                                }
                            }
                        }
                        if (audio) {
                            audioSocket = localServerSocket.accept().apply {
                                if (sendDummyB) {
                                    // send one byte so the client may read() to detect a connection error
                                    outputStream.write(0)
                                    sendDummyB = false
                                }
                            }
                        }
                        if (control) {
                            controlSocket = localServerSocket.accept().apply {
                                if (sendDummyB) {
                                    // send one byte so the client may read() to detect a connection error
                                    outputStream.write(0)
                                    sendDummyB = false
                                }
                            }
                        }
                    }
                } else {
                    if (video)
                        videoSocket = connect(socketName)
                    if (audio)
                        audioSocket = connect(socketName)
                    if (control)
                        controlSocket = connect(socketName)
                }
            } catch (e: IOException) {
                videoSocket?.close()
                audioSocket?.close()
                controlSocket?.close()
                throw e
            } catch (e: RuntimeException) {
                videoSocket?.close()
                audioSocket?.close()
                controlSocket?.close()
                throw e
            }
            return DesktopConnection(videoSocket, audioSocket, controlSocket)
        }

        private fun LocalServerSocket.using(block: (LocalServerSocket) -> Unit) {
            use(block)
        }
    }
}