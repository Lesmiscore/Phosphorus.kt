package com.nao20010128nao.Phosphorus.phar.events

import java.nio.charset.StandardCharsets

/**
 * Created by nao on 2017/02/09.
 */
class FileManifestEvent(
        val fileNameLength: Int,
        val fileName: ByteArray,
        val nonCompressedFileSize: Int,
        val unixFileTimeStamp: Int,
        val compressedFileSize: Int,
        val crc32Checksum: Int,
        val bitFlags: Int,
        val fileMetadataLength: Int,
        val fileMetadata: ByteArray
) : PharParserEvent {

    val fileNameString: String = fileName.toString(StandardCharsets.UTF_8)

    val fileMetadataString: String = fileMetadata.toString(StandardCharsets.UTF_8)

    val filePermission: Int = bitFlags and 0x000001FF


    val isZlibCompressed: Boolean = bitFlags and 0x00001000 != 0

    val isBzipCompressed: Boolean = bitFlags and 0x00002000 != 0

    val isNotCompressed: Boolean = !(isZlibCompressed || isBzipCompressed)

    override val id: Int = PharParserEvent.ID_FILE_MANIFEST
}
