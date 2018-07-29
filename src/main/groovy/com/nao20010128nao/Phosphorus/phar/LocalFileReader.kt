package com.nao20010128nao.Phosphorus.phar

import org.codehaus.groovy.runtime.*

import java.io.*
import java.nio.*

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

    private val fis: DataInputStream = ResourceGroovyMethods.newDataInputStream(f)

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
