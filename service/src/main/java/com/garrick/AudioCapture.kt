package com.garrick

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTimestamp
import android.media.MediaCodec
import android.os.Build
import android.os.SystemClock
import com.garrick.FakeContext.Companion.get
import com.garrick.Ln.d
import com.garrick.Ln.e
import com.garrick.Ln.w
import com.garrick.wrappers.ServiceManager
import java.nio.ByteBuffer

class AudioCapture(audioSource: AudioSource) {
    private val audioSource: Int = audioSource.value
    private var recorder: AudioRecord? = null
    private val timestamp = AudioTimestamp()
    private var previousPts: Long = 0
    private var nextPts: Long = 0

    @Throws(AudioCaptureForegroundException::class)
    private fun tryStartRecording(attempts: Int = 5, delayMs: Int = 1000) {
        var attemptsTime = attempts
        while (attemptsTime-- > 0) {
            // Wait for activity to start
            SystemClock.sleep(delayMs.toLong())
            try {
                startRecording()
                return  // it worked
            } catch (e: UnsupportedOperationException) {
                if (attemptsTime == 0) {
                    e("Failed to start audio capture")
                    e("On Android 11, audio capture must be started in the foreground, make sure that the device is unlocked when starting scrcpy.")
                    throw AudioCaptureForegroundException()
                } else {
                    d("Failed to start audio capture, retrying...")
                }
            }
        }
    }

    private fun startRecording() {
        recorder = try {
            createAudioRecord(audioSource)
        } catch (e: NullPointerException) {
            // Creating an AudioRecord using an AudioRecord.Builder does not work on Vivo phones:
            // - <https://github.com/Genymobile/scrcpy/issues/3805>
            // - <https://github.com/Genymobile/scrcpy/pull/3862>
            Workarounds.createAudioRecord(audioSource, SAMPLE_RATE, CHANNEL_CONFIG, CHANNELS, CHANNEL_MASK, ENCODING)
        }.apply {
            startRecording()
        }
    }

    @Throws(AudioCaptureForegroundException::class)
    fun start() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            startWorkaroundAndroid11()
            try {
                tryStartRecording()
            } finally {
                stopWorkaroundAndroid11()
            }
        } else {
            startRecording()
        }
    }

    fun stop() {
        // Will call .stop() if necessary, without throwing an IllegalStateException
        recorder?.release()
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun read(directBuffer: ByteBuffer, size: Int, outBufferInfo: MediaCodec.BufferInfo): Int {
        val r = recorder?.read(directBuffer, size) ?: -1
        if (r <= 0) {
            return r
        }
        val ret = recorder?.getTimestamp(timestamp, AudioTimestamp.TIMEBASE_MONOTONIC)
        var pts: Long = if (ret == AudioRecord.SUCCESS) {
            timestamp.nanoTime / 1000
        } else {
            if (nextPts == 0L) {
                w("Could not get any audio timestamp")
            }
            // compute from previous timestamp and packet size
            nextPts
        }
        val durationUs = (r * 1000000 / (CHANNELS * BYTES_PER_SAMPLE * SAMPLE_RATE)).toLong()
        nextPts = pts + durationUs
        if (previousPts != 0L && pts < previousPts) {
            // Audio PTS may come from two sources:
            //  - recorder.getTimestamp() if the call works;
            //  - an estimation from the previous PTS and the packet size as a fallback.
            //
            // Therefore, the property that PTS are monotonically increasing is no guaranteed in corner cases, so enforce it.
            pts = previousPts + 1
        }
        previousPts = pts
        outBufferInfo[0, r, pts] = 0
        return r
    }

    companion object {
        const val SAMPLE_RATE = 48000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        const val CHANNELS = 2
        const val CHANNEL_MASK = AudioFormat.CHANNEL_IN_LEFT or AudioFormat.CHANNEL_IN_RIGHT
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val BYTES_PER_SAMPLE = 2

        @JvmStatic
        fun millisToBytes(millis: Int): Int {
            return SAMPLE_RATE * CHANNELS * BYTES_PER_SAMPLE * millis / 1000
        }

        private fun createAudioFormat(): AudioFormat {
            val builder = AudioFormat.Builder()
            builder.setEncoding(ENCODING)
            builder.setSampleRate(SAMPLE_RATE)
            builder.setChannelMask(CHANNEL_CONFIG)
            return builder.build()
        }

        @TargetApi(Build.VERSION_CODES.M)
        @SuppressLint("WrongConstant", "MissingPermission")
        private fun createAudioRecord(audioSource: Int): AudioRecord {
            val builder = AudioRecord.Builder()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // On older APIs, Workarounds.fillAppInfo() must be called beforehand
                builder.setContext(get())
            }
            builder.setAudioSource(audioSource)
            builder.setAudioFormat(createAudioFormat())
            val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING)
            // This buffer size does not impact latency
            builder.setBufferSizeInBytes(8 * minBufferSize)
            return builder.build()
        }

        private fun startWorkaroundAndroid11() {
            // Android 11 requires Apps to be at foreground to record audio.
            // Normally, each App has its own user ID, so Android checks whether the requesting App has the user ID that's at the foreground.
            // But scrcpy server is NOT an App, it's a Java application started from Android shell, so it has the same user ID (2000) with Android
            // shell ("com.android.shell").
            // If there is an Activity from Android shell running at foreground, then the permission system will believe scrcpy is also in the
            // foreground.
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.component = ComponentName(FakeContext.PACKAGE_NAME, "com.android.shell.HeapDumpActivity")
            ServiceManager.getActivityManager().startActivityAsUserWithFeature(intent)
        }

        private fun stopWorkaroundAndroid11() {
            ServiceManager.getActivityManager().forceStopPackage(FakeContext.PACKAGE_NAME)
        }
    }
}