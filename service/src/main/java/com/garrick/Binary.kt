package com.garrick

object Binary {

    @JvmStatic
    fun toUnsigned(value: Short): Int = value.toInt() and 0xffff

    @JvmStatic
    fun toUnsigned(value: Byte): Int = value.toInt() and 0xff

    /**
     * Convert unsigned 16-bit fixed-point to a float between 0 and 1
     *
     * @param value encoded value
     * @return Float value between 0 and 1
     */
    @JvmStatic
    fun u16FixedPointToFloat(value: Short): Float {
        val unsignedShort = toUnsigned(value)
        // 0x1p16f is 2^16 as float
        return if (unsignedShort == 0xffff) 1f else (unsignedShort / 0x10000).toFloat()
    }

    /**
     * Convert signed 16-bit fixed-point to a float between -1 and 1
     *
     * @param value encoded value
     * @return Float value between -1 and 1
     */
    @JvmStatic
    fun i16FixedPointToFloat(value: Short): Float {
        // 0x1p15f is 2^15 as float
        return if (value.toInt() == 0x7fff) 1f else (value / 0x10000).toFloat()
    }
}