package com.garrick

import android.media.MediaCodec
import com.garrick.IO.writeFully
import java.io.FileDescriptor
import java.io.IOException
import java.nio.ByteBuffer

data class Streamer(
    private val fd: FileDescriptor,
    val codec: Codec,
    private val sendCodecMeta: Boolean,
    private val sendFrameMeta: Boolean
) {
    private val headerBuffer = ByteBuffer.allocate(12)

    @Throws(IOException::class)
    fun writeAudioHeader() {
        if (sendCodecMeta) {
            val buffer = ByteBuffer.allocate(4)
            buffer.putInt(codec.id)
            buffer.flip()
            writeFully(fd, buffer)
        }
    }

    @Throws(IOException::class)
    fun writeVideoHeader(videoSize: Size) {
        if (sendCodecMeta) {
            val buffer = ByteBuffer.allocate(12)
            buffer.putInt(codec.id)
            buffer.putInt(videoSize.width)
            buffer.putInt(videoSize.height)
            buffer.flip()
            writeFully(fd, buffer)
        }
    }

    @Throws(IOException::class)
    fun writeDisableStream(error: Boolean) {
        // Writing a specific code as codec-id means that the device disables the stream
        //   code 0: it explicitly disables the stream (because it could not capture audio), scrcpy should continue mirroring video only
        //   code 1: a configuration error occurred, scrcpy must be stopped
        val code = ByteArray(4)
        if (error) {
            code[3] = 1
        }
        writeFully(fd, code)
    }

    @Throws(IOException::class)
    fun writePacket(buffer: ByteBuffer, pts: Long, config: Boolean, keyFrame: Boolean) {
        if (config && codec === AudioCodec.OPUS) {
            fixOpusConfigPacket(buffer)
        }
        if (sendFrameMeta) {
            writeFrameMeta(fd, buffer.remaining(), pts, config, keyFrame)
        }
        writeFully(fd, buffer)
    }

    @Throws(IOException::class)
    fun writePacket(codecBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        val pts = bufferInfo.presentationTimeUs
        val config = bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
        val keyFrame = bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
        writePacket(codecBuffer, pts, config, keyFrame)
    }

    @Throws(IOException::class)
    private fun writeFrameMeta(fd: FileDescriptor, packetSize: Int, pts: Long, config: Boolean, keyFrame: Boolean) {
        headerBuffer.clear()
        val ptsAndFlags: Long = if (config) {
            PACKET_FLAG_CONFIG // non-media data packet
        } else {
            if (keyFrame) pts or PACKET_FLAG_KEY_FRAME
            else pts
        }
        headerBuffer.putLong(ptsAndFlags)
        headerBuffer.putInt(packetSize)
        headerBuffer.flip()
        writeFully(fd, headerBuffer)
    }

    companion object {
        private const val PACKET_FLAG_CONFIG = 1L shl 63
        private const val PACKET_FLAG_KEY_FRAME = 1L shl 62
        private const val AOPUSHDR = 0x5244485355504F41L // "AOPUSHDR" in ASCII (little-endian)

        @Throws(IOException::class)
        private fun fixOpusConfigPacket(buffer: ByteBuffer) {
            // Here is an example of the config packet received for an OPUS stream:
            //
            // 00000000  41 4f 50 55 53 48 44 52  13 00 00 00 00 00 00 00  |AOPUSHDR........|
            // -------------- BELOW IS THE PART WE MUST PUT AS EXTRADATA  -------------------
            // 00000010  4f 70 75 73 48 65 61 64  01 01 38 01 80 bb 00 00  |OpusHead..8.....|
            // 00000020  00 00 00                                          |...             |
            // ------------------------------------------------------------------------------
            // 00000020           41 4f 50 55 53  44 4c 59 08 00 00 00 00  |   AOPUSDLY.....|
            // 00000030  00 00 00 a0 2e 63 00 00  00 00 00 41 4f 50 55 53  |.....c.....AOPUS|
            // 00000040  50 52 4c 08 00 00 00 00  00 00 00 00 b4 c4 04 00  |PRL.............|
            // 00000050  00 00 00                                          |...|
            //
            // Each "section" is prefixed by a 64-bit ID and a 64-bit length.
            //
            // <https://developer.android.com/reference/android/media/MediaCodec#CSD>
            if (buffer.remaining() < 16) {
                throw IOException("Not enough data in OPUS config packet")
            }
            val id = buffer.long
            if (id != AOPUSHDR) {
                throw IOException("OPUS header not found")
            }
            val sizeLong = buffer.long
            if (sizeLong < 0 || sizeLong >= 0x7FFFFFFF) {
                throw IOException("Invalid block size in OPUS header: $sizeLong")
            }
            val size = sizeLong.toInt()
            if (buffer.remaining() < size) {
                throw IOException("Not enough data in OPUS header (invalid size: $size)")
            }

            // Set the buffer to point to the OPUS header slice
            buffer.limit(buffer.position() + size)
        }
    }
}