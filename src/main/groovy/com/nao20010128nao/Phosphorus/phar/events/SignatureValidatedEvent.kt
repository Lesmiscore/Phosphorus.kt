package com.nao20010128nao.Phosphorus.phar.events

/**
 * Created by nao on 2017/02/09.
 */
class SignatureValidatedEvent(val signature: SignatureEvent, val correct: Boolean) : PharParserEvent {
    override val id: Int = PharParserEvent.ID_SIGNATURE_VALIDATED
}
