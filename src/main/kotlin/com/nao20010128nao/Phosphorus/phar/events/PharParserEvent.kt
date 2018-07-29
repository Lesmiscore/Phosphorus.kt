package com.nao20010128nao.Phosphorus.phar.events

/**
 * Created by nao on 2017/02/09.
 */
interface PharParserEvent {
    val id: Int

    companion object {
        const val ID_UNKNOWN = -1
        const val ID_BEGIN = 0
        const val ID_STUB = 1
        const val ID_MANIFEST = 2
        const val ID_FILE_MANIFEST = 3
        const val ID_RAW_FILE = 4
        const val ID_SIGNATURE = 5
        const val ID_SIGNATURE_VALIDATED = 6
        const val ID_EOF = 7
    }
}
