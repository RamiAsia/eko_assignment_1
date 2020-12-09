package com.ramiasia.ekoapp1.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteConversionUtils {
    companion object {
        fun fromLittleEndian(bytes: ByteArray): Int {
            return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
        }
    }
}