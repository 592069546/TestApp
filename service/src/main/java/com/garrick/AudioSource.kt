package com.garrick

import android.media.MediaRecorder

enum class AudioSource(
    private val sourceName: String,
    val value: Int
) {
    OUTPUT("output", MediaRecorder.AudioSource.REMOTE_SUBMIX),
    MIC("mic", MediaRecorder.AudioSource.MIC);

    companion object {
        fun findByName(name: String): AudioSource? {
            for (audioSource in values()) {
                if (name == audioSource.sourceName) {
                    return audioSource
                }
            }
            return null
        }
    }
}