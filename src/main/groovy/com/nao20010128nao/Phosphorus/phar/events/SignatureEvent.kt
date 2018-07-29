package com.nao20010128nao.Phosphorus.phar.events

/**
 * Created by nao on 2017/02/09.
 */
class SignatureEvent(val hashSize: Int, val hash: ByteArray, val sigFlags: Int) : PharParserEvent {

    val isMd5: Boolean = sigFlags and 0x0001 != 0

    val isSha1: Boolean = sigFlags and 0x0002 != 0

    val isSha256: Boolean = sigFlags and 0x0004 != 0

    val isSha512: Boolean = sigFlags and 0x0008 != 0

    val isValidMd5: Boolean = isMd5 && hash.size == 16

    val isValidSha1: Boolean = isSha1 && hash.size == 20

    val isValidSha256: Boolean = isSha256 && hash.size == 32

    val isValidSha512: Boolean = isSha512 && hash.size == 64

    val hashNameFromFlag: String? = when {
        isMd5 -> "md5"
        isSha1 -> "sha-1"
        isSha256 -> "sha-256"
        isSha512 -> "sha-512"
        else -> null
    }

    override val id: Int = PharParserEvent.ID_SIGNATURE
}
