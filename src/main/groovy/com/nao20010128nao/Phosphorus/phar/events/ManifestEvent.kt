package com.nao20010128nao.Phosphorus.phar.events

import java.nio.charset.StandardCharsets

/**
 * Created by nao on 2017/02/09.
 */
class ManifestEvent(
        val manifestTotal: Int,
        val filesInsidePhar: Int,
        val pharApiVersion: Short,
        val globalFlags: Int,
        val pharAliasesLength: Int,
        val pharAliases: ByteArray,
        val pharMetadataLength: Int,
        pharMetadata: ByteArray
) : PharParserEvent {
    val pharAliasesString: String = pharAliases.toString(StandardCharsets.UTF_8)

    override val id: Int = PharParserEvent.ID_MANIFEST

    val pharMetadata: String = pharMetadata.toString(StandardCharsets.UTF_8)
}
