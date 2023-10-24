package com.garrick

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaCodec.CodecException
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.garrick.AsyncProcessor.TerminationListener
import com.garrick.AudioCapture.Companion.millisToBytes
import com.garrick.CodecUtils.setCodecOption
import com.garrick.IO.isBrokenPipe
import com.garrick.Ln.d
import com.garrick.Ln.e
import com.garrick.Ln.w
import com.garrick.LogUtils.buildAudioEncoderListMessage
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

class AudioEncoder(
    private val capture: AudioCapture,
    private val streamer: Streamer,
    private val bitRate: Int,
    private val codecOptions: List<CodecOption>?,
    private val encoderName: String?
) :
    AsyncProcessor {
    private class InputTask(val index: Int)
    private class OutputTask(val index: Int, val bufferInfo: MediaCodec.BufferInfo)

    // Capacity of 64 is in practice "infinite" (it is limited by the number of available MediaCodec buffers, typically 4).
    // So many pending tasks would lead to an unacceptable delay anyway.
    private val inputTasks: BlockingQueue<InputTask> = ArrayBlockingQueue(64)
    private val outputTasks: BlockingQueue<OutputTask> = ArrayBlockingQueue(64)

    private var thread: Thread? = null
    private var mediaCodecThread: HandlerThread? = null
    private var inputThread: Thread? = null
    private var outputThread: Thread? = null

    private var ended = false
    private val lock = Object()

    @TargetApi(Build.VERSION_CODES.N)
    @Throws(IOException::class, InterruptedException::class)
    private fun inputThread(mediaCodec: MediaCodec, capture: AudioCapture) {
        val bufferInfo = MediaCodec.BufferInfo()
        while (!Thread.currentThread().isInterrupted) {
            val task = inputTasks.take()
            val buffer = mediaCodec.getInputBuffer(task.index)
            val r = buffer?.run {
                capture.read(this, READ_SIZE, bufferInfo)
            } ?: -1
            if (r <= 0) {
                throw IOException("Could not read audio: $r")
            }
            mediaCodec.queueInputBuffer(task.index, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags)
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun outputThread(mediaCodec: MediaCodec) {
        streamer.writeAudioHeader()
        while (!Thread.currentThread().isInterrupted) {
            val task = outputTasks.take()
            val buffer = mediaCodec.getOutputBuffer(task.index)
            try {
                buffer?.apply {
                    streamer.writePacket(this, task.bufferInfo)
                }
            } finally {
                mediaCodec.releaseOutputBuffer(task.index, false)
            }
        }
    }

    override fun start(listener: TerminationListener) {
        thread = thread(start = true, name = "audio-encoder") {
            var fatalError = false
            try {
                encode()
            } catch (e: ConfigurationException) {
                // Do not print stack trace, a user-friendly error-message has already been logged
                fatalError = true
            } catch (e: AudioCaptureForegroundException) {
                // Do not print stack trace, a user-friendly error-message has already been logged
            } catch (e: IOException) {
                e("Audio encoding error", e)
                fatalError = true
            } finally {
                d("Audio encoder stopped")
                listener.onTerminated(fatalError)
            }
        }
    }

    override fun stop() {
        if (thread != null) {
            // Just wake up the blocking wait from the thread, so that it properly releases all its resources and terminates
            end()
        }
    }

    @Throws(InterruptedException::class)
    override fun join() {
        thread?.join()
    }

    @Synchronized
    private fun end() {
        ended = true
        lock.notify()
    }

    @Synchronized
    private fun waitEnded() {
        try {
            while (!ended) {
                lock.wait()
            }
        } catch (e: InterruptedException) {
            // ignore
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Throws(IOException::class, ConfigurationException::class, AudioCaptureForegroundException::class)
    fun encode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            w("Audio disabled: it is not supported before Android 11")
            streamer.writeDisableStream(false)
            return
        }
        var mediaCodec: MediaCodec? = null
        var mediaCodecStarted = false
        try {
            val codec = streamer.codec
            mediaCodec = createMediaCodec(codec, encoderName)
            val format = createFormat(codec.mimeType, bitRate, codecOptions)
            mediaCodecThread = HandlerThread("media-codec").apply {
                start()
                mediaCodec.setCallback(EncoderCallback(), Handler(looper))
            }
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            capture.start()
            val mediaCodecRef: MediaCodec = mediaCodec
            inputThread = thread(start = true, name = "audio-in") {
                try {
                    inputThread(mediaCodecRef, capture)
                } catch (e: IOException) {
                    e("Audio capture error", e)
                } catch (e: InterruptedException) {
                    e("Audio capture error", e)
                } finally {
                    end()
                }
            }
            outputThread = thread(start = true, name = "audio-out") {
                try {
                    outputThread(mediaCodecRef)
                } catch (e: InterruptedException) {
                    // this is expected on close
                } catch (e: IOException) {
                    // Broken pipe is expected on close, because the socket is closed by the client
                    if (!isBrokenPipe(e)) {
                        e("Audio encoding error", e)
                    }
                } finally {
                    end()
                }
            }
            mediaCodec.start()
            mediaCodecStarted = true
            waitEnded()
        } catch (e: ConfigurationException) {
            // Notify the error to make scrcpy exit
            streamer.writeDisableStream(true)
            throw e
        } catch (e: Throwable) {
            // Notify the client that the audio could not be captured
            streamer.writeDisableStream(false)
            throw e
        } finally {
            // Cleanup everything (either at the end or on error at any step of the initialization)
            mediaCodecThread?.looper?.quitSafely()
            inputThread?.interrupt()
            outputThread?.interrupt()
            try {
                mediaCodecThread?.join()
                inputThread?.join()
                outputThread?.join()
            } catch (e: InterruptedException) {
                // Should never happen
                throw AssertionError(e)
            }
            mediaCodec?.apply {
                if (mediaCodecStarted) {
                    stop()
                }
                release()
            }
            capture.stop()
        }
    }

    private inner class EncoderCallback : MediaCodec.Callback() {
        @TargetApi(Build.VERSION_CODES.N)
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            try {
                inputTasks.put(InputTask(index))
            } catch (e: InterruptedException) {
                end()
            }
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, bufferInfo: MediaCodec.BufferInfo) {
            try {
                outputTasks.put(OutputTask(index, bufferInfo))
            } catch (e: InterruptedException) {
                end()
            }
        }

        override fun onError(codec: MediaCodec, e: CodecException) {
            e("MediaCodec error", e)
            end()
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            // ignore
        }
    }

    companion object {
        private const val SAMPLE_RATE = AudioCapture.SAMPLE_RATE
        private const val CHANNELS = AudioCapture.CHANNELS
        private const val READ_MS = 5 // milliseconds
        private val READ_SIZE = millisToBytes(READ_MS)

        private fun createFormat(mimeType: String, bitRate: Int, codecOptions: List<CodecOption>?): MediaFormat {
            val format = MediaFormat()
            format.setString(MediaFormat.KEY_MIME, mimeType)
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNELS)
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE)
            if (codecOptions != null) {
                for (codecOption in codecOptions) {
                    setCodecOption(format, codecOption.key, codecOption.value)
                    d("Audio codec option set: ${codecOption.key} (${codecOption.value.javaClass.simpleName}) = ${codecOption.value}")
                }
            }
            return format
        }

        @Throws(IOException::class, ConfigurationException::class)
        private fun createMediaCodec(codec: Codec, encoderName: String?): MediaCodec = if (encoderName != null) {
            d("Creating audio encoder by name: '$encoderName'")
            try {
                MediaCodec.createByCodecName(encoderName)
            } catch (e: IllegalArgumentException) {
                e("Audio encoder '$encoderName' for ${codec.name} not found\n${buildAudioEncoderListMessage()}")
                throw ConfigurationException("Unknown encoder: $encoderName")
            } catch (e: IOException) {
                e("Could not create audio encoder '$encoderName' for ${codec.name}\n${buildAudioEncoderListMessage()}")
                throw e
            }
        } else {
            try {
                MediaCodec.createEncoderByType(codec.mimeType).also {
                    d("Using audio encoder: '${it.name}'")
                }
            } catch (e: IOException) {
                e("Could not create default audio encoder for ${codec.name}\n${buildAudioEncoderListMessage()}")
                throw e
            } catch (e: IllegalArgumentException) {
                e("Could not create default audio encoder for ${codec.name}\n${buildAudioEncoderListMessage()}")
                throw e
            }
        }
    }
}