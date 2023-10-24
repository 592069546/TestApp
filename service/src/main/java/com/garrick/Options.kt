package com.garrick

import android.graphics.Rect
import com.garrick.Ln.w

class Options {
    var logLevel = Ln.Level.DEBUG
        private set
    var scid = -1 // 31-bit non-negative value, or -1
        private set
    var video = true
        private set
    var audio = true
        private set
    var maxSize = 0
        private set
    var videoCodec = VideoCodec.H264
        private set
    var audioCodec = AudioCodec.OPUS
        private set
    var audioSource = AudioSource.OUTPUT
        private set
    var videoBitRate = 8000000
        private set
    var audioBitRate = 128000
        private set
    var maxFps = 0
        private set
    var lockVideoOrientation = -1
        private set
    var isTunnelForward = false
        private set
    var crop: Rect? = null
        private set
    var control = true
        private set
    var displayId = 0
        private set
    var showTouches = false
        private set
    var stayAwake = false
        private set
    var videoCodecOptions: List<CodecOption>? = null
        private set
    var audioCodecOptions: List<CodecOption>? = null
        private set
    var videoEncoder: String? = null
        private set
    var audioEncoder: String? = null
        private set
    var powerOffScreenOnClose = false
        private set
    var clipboardAutosync = true
        private set
    var downsizeOnError = true
        private set
    var cleanup = true
        private set
    var powerOn = true
        private set
    var listEncoders = false
        private set
    var listDisplays = false
        private set

    // Options not used by the scrcpy client, but useful to use scrcpy-server directly
    var sendDeviceMeta = true // send device name and size
        private set
    var sendFrameMeta = true // send PTS so that the client may record properly
        private set
    var sendDummyByte = true // write a byte on start to detect connection issues
        private set
    var sendCodecMeta = true // write the codec metadata before the stream
        private set

    companion object {
        @JvmStatic
        fun parse(vararg args: String): Options {
            require(args.isNotEmpty()) { "Missing client version" }
            val clientVersion = args[0]
            require(clientVersion == BuildConfig.VERSION_NAME) {
                "The server version (${BuildConfig.VERSION_NAME}) does not match the client (${clientVersion})"
            }
            val options = Options()
            for (i in 1 until args.size) {
                val arg = args[i]
                val equalIndex = arg.indexOf('=')
                require(equalIndex != -1) { "Invalid key=value pair: \"$arg\"" }
                val key = arg.substring(0, equalIndex)
                val value = arg.substring(equalIndex + 1)
                when (key) {
                    "scid" -> {
                        val scid = value.toInt(0x10)
                        require(scid >= -1) { "scid may not be negative (except -1 for 'none'): $scid" }
                        options.scid = scid
                    }

                    "log_level" -> options.logLevel = Ln.Level.valueOf(value.uppercase())
                    "video" -> options.video = value.toBoolean()
                    "audio" -> options.audio = value.toBoolean()
                    "video_codec" -> {
                        val videoCodec = VideoCodec.findByName(value) ?: throw IllegalArgumentException("Video codec $value not supported")
                        options.videoCodec = videoCodec
                    }

                    "audio_codec" -> {
                        val audioCodec = AudioCodec.findByName(value) ?: throw IllegalArgumentException("Audio codec $value not supported")
                        options.audioCodec = audioCodec
                    }

                    "audio_source" -> {
                        val audioSource = AudioSource.findByName(value) ?: throw IllegalArgumentException("Audio source $value not supported")
                        options.audioSource = audioSource
                    }

                    "max_size" -> options.maxSize = value.toInt() and 7.inv() // multiple of 8
                    "video_bit_rate" -> options.videoBitRate = value.toInt()
                    "audio_bit_rate" -> options.audioBitRate = value.toInt()
                    "max_fps" -> options.maxFps = value.toInt()
                    "lock_video_orientation" -> options.lockVideoOrientation = value.toInt()
                    "tunnel_forward" -> options.isTunnelForward = value.toBoolean()
                    "crop" -> options.crop = parseCrop(value)
                    "control" -> options.control = value.toBoolean()
                    "display_id" -> options.displayId = value.toInt()
                    "show_touches" -> options.showTouches = value.toBoolean()
                    "stay_awake" -> options.stayAwake = value.toBoolean()
                    "video_codec_options" -> options.videoCodecOptions = CodecOption.parse(value)
                    "audio_codec_options" -> options.audioCodecOptions = CodecOption.parse(value)
                    "video_encoder" -> if (value.isNotEmpty()) {
                        options.videoEncoder = value
                    }

                    "audio_encoder" -> {
                        if (value.isNotEmpty()) {
                            options.audioEncoder = value
                        }
                        options.powerOffScreenOnClose = value.toBoolean()
                    }

                    "power_off_on_close" -> options.powerOffScreenOnClose = value.toBoolean()
                    "clipboard_autosync" -> options.clipboardAutosync = value.toBoolean()
                    "downsize_on_error" -> options.downsizeOnError = value.toBoolean()
                    "cleanup" -> options.cleanup = value.toBoolean()
                    "power_on" -> options.powerOn = value.toBoolean()
                    "list_encoders" -> options.listEncoders = value.toBoolean()
                    "list_displays" -> options.listDisplays = value.toBoolean()
                    "send_device_meta" -> options.sendDeviceMeta = value.toBoolean()
                    "send_frame_meta" -> options.sendFrameMeta = value.toBoolean()
                    "send_dummy_byte" -> options.sendDummyByte = value.toBoolean()
                    "send_codec_meta" -> options.sendCodecMeta = value.toBoolean()
                    "raw_stream" -> {
                        val rawStream = value.toBoolean()
                        if (rawStream) {
                            options.sendDeviceMeta = false
                            options.sendFrameMeta = false
                            options.sendDummyByte = false
                            options.sendCodecMeta = false
                        }
                    }

                    else -> w("Unknown server option: $key")
                }
            }
            return options
        }

        private fun parseCrop(crop: String?): Rect? {
            if (crop.isNullOrEmpty()) {
                return null
            }
            // input format: "width:height:x:y"
            val tokens = crop.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            require(tokens.size == 4) { "Crop must contains 4 values separated by colons: \"$crop\"" }
            val width = tokens[0].toInt()
            val height = tokens[1].toInt()
            val x = tokens[2].toInt()
            val y = tokens[3].toInt()
            return Rect(x, y, x + width, y + height)
        }
    }
}