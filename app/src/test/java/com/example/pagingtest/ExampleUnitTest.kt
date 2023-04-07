package com.example.pagingtest

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val byte: Byte = -1
        assert(byte.hexToInt().also { println("$it") } >= 0)
    }

    fun Byte.hexToInt(): Int = if (this >= 0)
        this.toInt()
    else
        Byte.MAX_VALUE - Byte.MIN_VALUE + this
}