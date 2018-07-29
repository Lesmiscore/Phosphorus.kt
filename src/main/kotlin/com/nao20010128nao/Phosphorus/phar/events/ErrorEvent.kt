package com.nao20010128nao.Phosphorus.phar.events

/**
 * Created by nao on 2017/02/10.
 */
class ErrorEvent(cause: Throwable) : Throwable(cause), PharParserEvent {
    override val id: Int = PharParserEvent.ID_UNKNOWN
}
