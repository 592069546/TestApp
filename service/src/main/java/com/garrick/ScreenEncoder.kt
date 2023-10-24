package com.garrick

import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.view.Surface
import com.garrick.AsyncProcessor.TerminationListener
import com.garrick.CodecUtils.setCodecOption
import com.garrick.Device.FoldListener
import com.garrick.Device.RotationListener
import com.garrick.IO.isBrokenPipe
import com.garrick.Ln.d
import com.garrick.Ln.e
import com.garrick.Ln.i
import com.garrick.LogUtils.buildVideoEncoderListMessage
import com.garrick.wrappers.SurfaceControl
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class ScreenEncoder(
    private val device: Device,
    private val streamer: Streamer,
    private val videoBitRate: Int,
    private val maxFps: Int,
    private val codecOptions: List<CodecOption>?,
    private val encoderName: String?,
    private val downsizeOnError: Boolean
) : RotationListener, FoldListener, AsyncProcessor {
    private val resetCapture = AtomicBoolean()
    private var firstFrameSent = false
    private var consecutiveErrors = 0
    private var thread: Thread? = null
    private val stopped = AtomicBoolean()

    override fun onFoldChanged(displayId: Int, folded: Boolean) {
        resetCapture.set(true)
    }

    override fun onRotationChanged(rotation: Int) {
        resetCapture.set(true)
    }

    private fun consumeResetCapture(): Boolean = resetCapture.getAndSet(false)

    @Throws(IOException::class, ConfigurationException::class)
    private fun streamScreen() {
        val codec = streamer.codec
        val mediaCodec = createMediaCodec(codec, encoderName)
        val format = createFormat(codec.mimeType, videoBitRate, maxFps, codecOptions)
        val display = createDisplay()
        device.setRotationListener(this)
        device.setFoldListener(this)
        streamer.writeVideoHeader(device.screenInfo.videoSize)
        var alive: Boolean
        try {
            do {
                val screenInfo = device.screenInfo
                val contentRect = screenInfo.contentRect

                // include the locked video orientation
                val videoRect = screenInfo.videoSize.toRect()
                format.setInteger(MediaFormat.KEY_WIDTH, videoRect.width())
                format.setInteger(MediaFormat.KEY_HEIGHT, videoRect.height())
                var surface: Surface? = null
                try {
                    mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                    surface = mediaCodec.createInputSurface()

                    // does not include the locked video orientation
                    val unlockedVideoRect = screenInfo.unlockedVideoSize.toRect()
                    val videoRotation = screenInfo.videoRotation
                    val layerStack = device.layerStack
                    setDisplaySurface(display, surface, videoRotation, contentRect, unlockedVideoRect, layerStack)
                    mediaCodec.start()
                    alive = encode(mediaCodec, streamer)
                    // do not call stop() on exception, it would trigger an IllegalStateException
                    mediaCodec.stop()
                } catch (e: IllegalStateException) {
                    e("Encoding error: ${e.javaClass.name}: ${e.message}")
                    if (!prepareRetry(device, screenInfo)) {
                        throw e
                    }
                    i("Retrying...")
                    alive = true
                } catch (e: IllegalArgumentException) {
                    e("Encoding error: " + e.javaClass.name + ": " + e.message)
                    if (!prepareRetry(device, screenInfo)) {
                        throw e
                    }
                    i("Retrying...")
                    alive = true
                } finally {
                    mediaCodec.reset()
                    surface?.release()
                }
            } while (alive)
        } finally {
            mediaCodec.release()
            device.setRotationListener(null)
            device.setFoldListener(null)
            SurfaceControl.destroyDisplay(display)
        }
    }

    private fun prepareRetry(device: Device, screenInfo: ScreenInfo): Boolean {
        if (firstFrameSent) {
            ++consecutiveErrors
            if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                // Definitively fail
                return false
            }

            // Wait a bit to increase the probability that retrying will fix the problem
            SystemClock.sleep(50)
            return true
        }
        if (!downsizeOnError) {
            // Must fail immediately
            return false
        }

        // Downsizing on error is only enabled if an encoding failure occurs before the first frame (downsizing later could be surprising)
        val newMaxSize = chooseMaxSizeFallback(screenInfo.videoSize)
        if (newMaxSize == 0) {
            // Must definitively fail
            return false
        }

        // Retry with a smaller device size
        i("Retrying with -m$newMaxSize...")
        device.setMaxSize(newMaxSize)
        return true
    }

    @Throws(IOException::class)
    private fun encode(codec: MediaCodec, streamer: Streamer): Boolean {
        var eof = false
        var alive = true
        val bufferInfo = MediaCodec.BufferInfo()
        while (!consumeResetCapture() && !eof) {
            if (stopped.get()) {
                alive = false
                break
            }
            val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, -1)
            try {
                if (consumeResetCapture()) {
                    // must restart encoding with new size
                    break
                }
                eof = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                if (outputBufferId >= 0) {
                    val codecBuffer = codec.getOutputBuffer(outputBufferId)
                    val isConfig = bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                    if (!isConfig) {
                        // If this is not a config packet, then it contains a frame
                        firstFrameSent = true
                        consecutiveErrors = 0
                    }
                    codecBuffer?.apply {
                        streamer.writePacket(this, bufferInfo)
                    }
                }
            } finally {
                if (outputBufferId >= 0) {
                    codec.releaseOutputBuffer(outputBufferId, false)
                }
            }
        }
        return !eof && alive
    }

    override fun start(listener: TerminationListener) {
        thread = thread(start = true, name = "video") {

            // Some devices (Meizu) deadlock if the video encoding thread has no Looper
            // <https://github.com/Genymobile/scrcpy/issues/4143>
            Looper.prepare()
            try {
                streamScreen()
            } catch (e: ConfigurationException) {
                // Do not print stack trace, a user-friendly error-message has already been logged
            } catch (e: IOException) {
                // Broken pipe is expected on close, because the socket is closed by the client
                if (!isBrokenPipe(e)) {
                    e("Video encoding error", e)
                }
            } finally {
                d("Screen streaming stopped")
                listener.onTerminated(true)
            }
        }
    }

    override fun stop() {
        if (thread != null) {
            stopped.set(true)
        }
    }

    @Throws(InterruptedException::class)
    override fun join() {
        thread?.join()
    }

    companion object {
        private const val DEFAULT_I_FRAME_INTERVAL = 10 // seconds
        private const val REPEAT_FRAME_DELAY_US = 100000 // repeat after 100ms
        private const val KEY_MAX_FPS_TO_ENCODER = "max-fps-to-encoder"

        // Keep the values in descending order
        private val MAX_SIZE_FALLBACK = intArrayOf(2560, 1920, 1600, 1280, 1024, 800)
        private const val MAX_CONSECUTIVE_ERRORS = 3

        private fun chooseMaxSizeFallback(failedSize: Size): Int {
            val currentMaxSize = failedSize.width.coerceAtLeast(failedSize.height)
            for (value in MAX_SIZE_FALLBACK) {
                if (value < currentMaxSize) {
                    // We found a smaller value to reduce the video size
                    return value
                }
            }
            // No fallback, fail definitively
            return 0
        }

        @Throws(IOException::class, ConfigurationException::class)
        private fun createMediaCodec(codec: Codec, encoderName: String?): MediaCodec {
            if (encoderName != null) {
                d("Creating encoder by name: '$encoderName'")
                return try {
                    MediaCodec.createByCodecName(encoderName)
                } catch (e: IllegalArgumentException) {
                    e("Video encoder '$encoderName' for ${codec.name} not found \n${buildVideoEncoderListMessage()}")
                    throw ConfigurationException("Unknown encoder: $encoderName")
                } catch (e: IOException) {
                    e("Could not create video encoder '$encoderName' for ${codec.name}\n${buildVideoEncoderListMessage()}")
                    throw e
                }
            }
            return try {
                val mediaCodec = MediaCodec.createEncoderByType(codec.mimeType)
                d("Using video encoder: '${mediaCodec.name}'")
                mediaCodec
            } catch (e: IOException) {
                e("Could not create default video encoder for ${codec.name}\n${buildVideoEncoderListMessage()}")
                throw e
            } catch (e: IllegalArgumentException) {
                e("Could not create default video encoder for ${codec.name}\n${buildVideoEncoderListMessage()}")
                throw e
            }
        }

        private fun createFormat(videoMimeType: String, bitRate: Int, maxFps: Int, codecOptions: List<CodecOption>?): MediaFormat {
            val format = MediaFormat()
            format.setString(MediaFormat.KEY_MIME, videoMimeType)
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            // must be present to configure the encoder, but does not impact the actual frame rate, which is variable
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 60)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_I_FRAME_INTERVAL)
            // display the very first frame, and recover from bad quality when no new frames
            format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, REPEAT_FRAME_DELAY_US.toLong()) // µs
            if (maxFps > 0) {
                // The key existed privately before Android 10:
                // <https://android.googlesource.com/platform/frameworks/base/+/625f0aad9f7a259b6881006ad8710adce57d1384%5E%21/>
                // <https://github.com/Genymobile/scrcpy/issues/488#issuecomment-567321437>
                format.setFloat(KEY_MAX_FPS_TO_ENCODER, maxFps.toFloat())
            }
            if (codecOptions != null) {
                for (codecOption in codecOptions) {
                    setCodecOption(format, codecOption.key, codecOption.value)
                    d("Video codec option set: ${codecOption.key} (${codecOption.value.javaClass.simpleName}) = ${codecOption.value}")
                }
            }
            return format
        }

        private fun createDisplay(): IBinder {
            // Since Android 12 (preview), secure displays could not be created with shell permissions anymore.
            // On Android 12 preview, SDK_INT is still R (not S), but CODENAME is "S".
            val secure = Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.R && "S" != Build.VERSION.CODENAME
            return SurfaceControl.createDisplay("scrcpy", secure)
        }

        private fun setDisplaySurface(display: IBinder, surface: Surface, orientation: Int, deviceRect: Rect, displayRect: Rect, layerStack: Int) {
            SurfaceControl.openTransaction()
            try {
                SurfaceControl.setDisplaySurface(display, surface)
                SurfaceControl.setDisplayProjection(display, orientation, deviceRect, displayRect)
                SurfaceControl.setDisplayLayerStack(display, layerStack)
            } finally {
                SurfaceControl.closeTransaction()
            }
        }
    }
}