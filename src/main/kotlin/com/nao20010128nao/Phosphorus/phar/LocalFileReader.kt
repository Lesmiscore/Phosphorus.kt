package com.nao20010128nao.Phosphorus.phar

import java.io.Closeable
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by nao on 2017/02/09.
 */
class LocalFileReader(f: File) : Closeable {

    val int: Int
        get() {
            val buffer = ByteArray(4)
            get(buffer)
            return ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN).int
        }

    val leInt: Int
        get() {
            val buffer = ByteArray(4)
            get(buffer)
            return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).int
        }

    val short: Short
        get() {
            val buffer = ByteArray(2)
            get(buffer)
            return ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN).short
        }

    private val fis: DataInputStream = DataInputStream(f.inputStream())

    fun get(): Byte = fis.read().toByte()

    fun get(a: ByteArray) {
        fis.readFully(a)
    }

    fun skip(amount: Int) {
        fis.skipBytes(amount)
    }

    fun remaining(): Int = fis.available()

    @Throws(IOException::class)
    override fun close() {
        fis.close()
    }
}
