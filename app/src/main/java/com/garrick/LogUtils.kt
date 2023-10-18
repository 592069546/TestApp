package com.garrick

import com.garrick.wrappers.ServiceManager

object LogUtils {

    @JvmStatic
    fun buildVideoEncoderListMessage(): String {
        val builder = StringBuilder("List of video encoders:")
        val videoEncoders = CodecUtils.listVideoEncoders()
        if (videoEncoders.isEmpty()) {
            builder.append("\n    (none)")
        } else {
            for (encoder in videoEncoders) {
                builder.append("\n    --video-codec=").append(encoder.codec.name)
                builder.append(" --video-encoder='").append(encoder.info.name).append("'")
            }
        }
        return builder.toString()
    }

    @JvmStatic
    fun buildAudioEncoderListMessage(): String {
        val builder = StringBuilder("List of audio encoders:")
        val audioEncoders = CodecUtils.listAudioEncoders()
        if (audioEncoders.isEmpty()) {
            builder.append("\n    (none)")
        } else {
            for (encoder in audioEncoders) {
                builder.append("\n    --audio-codec=").append(encoder.codec.name)
                builder.append(" --audio-encoder='").append(encoder.info.name).append("'")
            }
        }
        return builder.toString()
    }

    @JvmStatic
    fun buildDisplayListMessage(): String {
        val builder = StringBuilder("List of displays:")
        val displayManager = ServiceManager.getDisplayManager()
        val displayIds = displayManager.displayIds
        if (displayIds == null || displayIds.isEmpty()) {
            builder.append("\n    (none)")
        } else {
            for (id in displayIds) {
                builder.append("\n    --display=").append(id).append("    (")
                val displayInfo = displayManager.getDisplayInfo(id)
                if (displayInfo != null) {
                    val (width, height) = displayInfo.size
                    builder.append(width).append("x").append(height)
                } else {
                    builder.append("size unknown")
                }
                builder.append(")")
            }
        }
        return builder.toString()
    }
}