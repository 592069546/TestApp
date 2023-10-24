package com.garrick

interface Codec {
    enum class Type {
        VIDEO, AUDIO
    }

    val type: Type

    val id: Int

    val name: String

    val mimeType: String
}