package com.garrick

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat

object CodecUtils {

    @JvmStatic
    fun setCodecOption(format: MediaFormat, key: String, value: Any?) {
        value ?: return
        if (value is Int) {
            format.setInteger(key, value)
        } else if (value is Long) {
            format.setLong(key, value)
        } else if (value is Float) {
            format.setFloat(key, value)
        } else if (value is String) {
            format.setString(key, value)
        }
    }

    fun listVideoEncoders(): List<DeviceEncoder> {
        val encoders = ArrayList<DeviceEncoder>()
        val codecs = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (codec in VideoCodec.values()) {
            for (info in codecs.getEncoders(codec.mimeType)) {
                encoders.add(DeviceEncoder(codec, info))
            }
        }
        return encoders
    }

    fun listAudioEncoders(): List<DeviceEncoder> {
        val encoders = ArrayList<DeviceEncoder>()
        val codecs = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (codec in AudioCodec.values()) {
            for (info in codecs.getEncoders(codec.mimeType)) {
                encoders.add(DeviceEncoder(codec, info))
            }
        }
        return encoders
    }

    private fun MediaCodecList.getEncoders(mimeType: String): List<MediaCodecInfo> = codecInfos.filter {
        it.isEncoder && it.supportedTypes.contains(mimeType)
    }

    class DeviceEncoder internal constructor(val codec: Codec, val info: MediaCodecInfo)
}