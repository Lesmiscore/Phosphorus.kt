package com.nao20010128nao.Phosphorus.phar

import com.google.common.primitives.Bytes
import com.nao20010128nao.Phosphorus.phar.events.*
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

/**
 * Created by nao on 2017/02/09.
 */
class PharParser(private val f: File) {

    fun startReading(listener: OnEventListener) {
        listener.onEvent(BeginEvent())
        //find stub
        var stubEnd = -1
        RandomAccessFile(f, "r").use { channel ->
            val tmp = ByteArray(STUB_FINAL_C.size + 20)
            for (i in 0 until channel.length() - STUB_FINAL_C.size.toLong() - 20) {
                channel.seek(i)
                channel.read(tmp)

                stubEnd = Bytes.indexOf(tmp, STUB_FINAL_A)
                if (stubEnd != -1) {
                    stubEnd = (stubEnd.toLong() + i + STUB_FINAL_A.size.toLong()).toInt()
                    break
                }

                stubEnd = Bytes.indexOf(tmp, STUB_FINAL_B)
                if (stubEnd != -1) {
                    stubEnd = (stubEnd.toLong() + i + STUB_FINAL_B.size.toLong()).toInt()
                    break
                }

                stubEnd = Bytes.indexOf(tmp, STUB_FINAL_C)
                if (stubEnd != -1) {
                    stubEnd = (stubEnd.toLong() + i + STUB_FINAL_C.size.toLong()).toInt()
                }
            }

            channel.seek(stubEnd.toLong())
            channel.read(tmp, 0, 2)
            val tmpStr = String(tmp, 0, 2)
            when {
                tmpStr.startsWith("\r\n") -> stubEnd += 2
                tmpStr.startsWith("\r") -> stubEnd++
                tmpStr.startsWith("\n") -> stubEnd++
            }
            Unit
        }


        val gbmbBuf = ByteArray(4)
        var sig: SignatureEvent? = null
        LocalFileReader(f).use { buffer ->
            if (stubEnd != -1) {
                val readBuf = ByteArray(stubEnd)
                buffer.get(readBuf)
                listener.onEvent(StubEvent(readBuf))
            }

            //read manifest in LITTLE ENDIAN
            val manifestTotal = buffer.leInt
            val filesInsidePhar = buffer.leInt
            val pharApiVersion = buffer.short
            val globalFlags = buffer.leInt
            val pharAliasesLength = buffer.leInt
            val pharAliases = ByteArray(pharAliasesLength)
            buffer.get(pharAliases)
            val pharMetadataLength = buffer.leInt
            val pharMetadata = ByteArray(pharMetadataLength)
            buffer.get(pharMetadata)
            listener.onEvent(ManifestEvent(manifestTotal, filesInsidePhar, pharApiVersion, globalFlags, pharAliasesLength, pharAliases, pharMetadataLength, pharMetadata))
            //read file manifest
            val files: MutableList<FileManifestEvent> = ArrayList()
            (1..filesInsidePhar).forEach {
                val fileNameLength = buffer.leInt
                val fileName = ByteArray(fileNameLength)
                buffer.get(fileName)
                val nonCompressedFileSize = buffer.leInt
                val unixFileTimeStamp = buffer.leInt
                val compressedFileSize = buffer.leInt
                val crc32Checksum = buffer.leInt
                val bitFlags = buffer.leInt
                val fileMetadataLength = buffer.leInt
                val fileMetadata = ByteArray(fileMetadataLength)
                buffer.get(fileMetadata)
                val event = FileManifestEvent(fileNameLength, fileName, nonCompressedFileSize, unixFileTimeStamp, compressedFileSize, crc32Checksum, bitFlags, fileMetadataLength, fileMetadata)
                listener.onEvent(event)
                files += event
            }
            //read files
            files.forEach { event ->
                val fileBuffer = ByteArray(event.compressedFileSize)
                buffer.get(fileBuffer)
                listener.onEvent(RawFileEvent(fileBuffer, event))
            }
            //read signature
            val remain = buffer.remaining()
            val hashSize = remain - (4 + 4)
            if (hashSize != 16 && hashSize != 20) {
                listener.onEvent(ErrorEvent(IllegalStateException("Wrong hash size: " + hashSize.toString())))
                return
            }

            val hash = ByteArray(hashSize)
            buffer.get(hash)
            val sigFlags = buffer.leInt
            sig = SignatureEvent(hashSize, hash, sigFlags)
            listener.onEvent(sig!!)
            //check GBMB
            buffer.get(gbmbBuf)
        }

        if (String(gbmbBuf) == "GBMB") {
            listener.onEvent(EofEvent())
            //validate signature
            RandomAccessFile(f, "r").use { channel ->
                var readRemaining = channel.length() - (sig!!.hash.size + 4 + 4)
                val tmp = ByteArray(1024)
                val digest = MessageDigest.getInstance(sig!!.hashNameFromFlag)
                while (readRemaining != 0L) {
                    val toRead = min(readRemaining, tmp.size.toLong()).toInt()
                    val actualRead = channel.read(tmp, 0, toRead)
                    readRemaining -= actualRead.toLong()
                    digest.update(tmp, 0, actualRead)
                }

                listener.onEvent(SignatureValidatedEvent(sig!!, Arrays.equals(digest.digest(), sig!!.hash)))
            }
        } else {
            listener.onEvent(ErrorEvent(IllegalStateException("GBMB not detected: hex: " + Hex.encodeHexString(gbmbBuf))))
        }

    }

    interface OnEventListener {
        fun onEvent(event: PharParserEvent)
    }

    companion object {
        val STUB_FINAL_A: ByteArray = "?>".toByteArray()
        val STUB_FINAL_B: ByteArray = "__HALT_COMPILER();".toByteArray()
        val STUB_FINAL_C: ByteArray = "__halt_compiler();".toByteArray()
    }
}
