package com.garrick

data class CodecOption constructor(val key: String, val value: Any) {
    companion object {
        @JvmStatic
        fun parse(codecOptions: String?): List<CodecOption>? {
            if (codecOptions.isNullOrEmpty())
                return null

            val result: MutableList<CodecOption> = ArrayList()
            var escape = false
            val buf = StringBuilder()
            for (c in codecOptions.toCharArray()) {
                when (c) {
                    '\\' -> escape = if (escape) {
                        buf.append('\\')
                        false
                    } else {
                        true
                    }

                    ',' -> if (escape) {
                        buf.append(',')
                        escape = false
                    } else {
                        // This comma is a separator between codec options
                        val codecOption = buf.toString()
                        result.add(codecOption.parseOption())
                        // Clear buf
                        buf.setLength(0)
                    }

                    else -> buf.append(c)
                }
            }
            if (buf.isNotEmpty()) {
                val codecOption = buf.toString()
                result.add(codecOption.parseOption())
            }
            return result
        }

        private fun String.parseOption(): CodecOption {
            val equalSignIndex = indexOf('=')
            require(equalSignIndex != -1) { "'=' expected" }
            val keyAndType = substring(0, equalSignIndex)
            require(keyAndType.isNotEmpty()) { "Key may not be null" }
            val key: String
            val type: String
            val colonIndex = keyAndType.indexOf(':')
            if (colonIndex != -1) {
                key = keyAndType.substring(0, colonIndex)
                type = keyAndType.substring(colonIndex + 1)
            } else {
                key = keyAndType
                type = "int" // assume int by default
            }
            val valueString = substring(equalSignIndex + 1)
            val value = when (type) {
                "int" -> valueString.toInt()
                "long" -> valueString.toLong()
                "float" -> valueString.toFloat()
                "string" -> valueString
                else -> throw IllegalArgumentException("Invalid codec option type (int, long, float, str): $type")
            }
            return CodecOption(key, value)
        }
    }
}