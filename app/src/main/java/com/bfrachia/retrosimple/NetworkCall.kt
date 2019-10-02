package com.bfrachia.retrosimple

fun <RESPONSE: Any> makeNetworkCall(
    block: CallHandler<RESPONSE>.() -> Unit
): DataWrapper<RESPONSE> = CallHandler<RESPONSE>().apply(block).makeCall()

fun <DATA: Any> CallHandler<DATA>.onSuccess(block: CallHandler<DATA>.(DataWrapper<DATA>) -> Unit) {
    onSuccess = {
        block(this, it)
    }
}

fun <DATA: Any> CallHandler<DATA>.onFail(block: CallHandler<DATA>.(DataWrapper<DATA>) -> Unit) {
    onFailed = {
        block(this, it)
    }
}