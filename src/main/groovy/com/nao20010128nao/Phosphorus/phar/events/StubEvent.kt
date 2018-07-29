package com.nao20010128nao.Phosphorus.phar.events

import java.nio.charset.StandardCharsets

/**
 * Created by nao on 2017/02/09.
 */
class StubEvent(val stub: ByteArray) : PharParserEvent {
    val stubString: String = stub.toString(StandardCharsets.UTF_8)

    override val id: Int = PharParserEvent.ID_STUB
}
