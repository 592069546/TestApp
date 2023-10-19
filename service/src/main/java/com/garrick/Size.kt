package com.garrick

import android.graphics.Rect

data class Size constructor(val width: Int, val height: Int) {

    fun rotate(): Size = Size(height, width)

    fun toRect(): Rect = Rect(0, 0, width, height)
}