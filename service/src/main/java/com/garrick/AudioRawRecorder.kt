package com.garrick

import android.media.MediaCodec
import android.os.Build
import com.garrick.AsyncProcessor.TerminationListener
import com.garrick.AudioCapture.Companion.millisToBytes
import com.garrick.Ln.d
import com.garrick.Ln.e
import com.garrick.Ln.w
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class AudioRawRecorder(private val capture: AudioCapture, private val streamer: Streamer) : AsyncProcessor {
    private var thread: Thread? = null

    @Throws(IOException::class, AudioCaptureForegroundException::class)
    private fun record() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            w("Audio disabled: it is not supported before Android 11")
            streamer.writeDisableStream(false)
            return
        }
        val buffer = ByteBuffer.allocateDirect(READ_SIZE)
        val bufferInfo = MediaCodec.BufferInfo()
        try {
            capture.start()
            streamer.writeAudioHeader()
            while (!Thread.currentThread().isInterrupted) {
                buffer.position(0)
                val r = capture.read(buffer, READ_SIZE, bufferInfo)
                if (r < 0) {
                    throw IOException("Could not read audio: $r")
                }
                buffer.limit(r)
                streamer.writePacket(buffer, bufferInfo)
            }
        } catch (e: Throwable) {
            // Notify the client that the audio could not be captured
            streamer.writeDisableStream(false)
            throw e
        } finally {
            capture.stop()
        }
    }

    override fun start(listener: TerminationListener) {
        thread = thread(start = true, name = "audio-raw") {
            var fatalError = false
            try {
                record()
            } catch (e: AudioCaptureForegroundException) {
                // Do not print stack trace, a user-friendly error-message has already been logged
            } catch (e: IOException) {
                e("Audio recording error", e)
                fatalError = true
            } finally {
                d("Audio recorder stopped")
                listener.onTerminated(fatalError)
            }
        }
    }

    override fun stop() {
        thread?.interrupt()
    }

    @Throws(InterruptedException::class)
    override fun join() {
        thread?.join()
    }

    companion object {
        private const val READ_MS = 5 // milliseconds
        private val READ_SIZE = millisToBytes(READ_MS)
    }
}