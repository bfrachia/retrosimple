package com.bfrachia.retrosimple

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

//fun <RESPONSE: Any> makeNetworkCall(
//    block: CallHandler<RESPONSE>.() -> Unit
//): DataWrapper<RESPONSE> = CallHandler<RESPONSE>().apply(block).makeCall()

fun <RESPONSE: Any> makeNetworkCall(
    init: CallHandlerBuilder<DataWrapper<RESPONSE>>.() -> CallHandler<DataWrapper<RESPONSE>>
): CallHandler<DataWrapper<RESPONSE>> {
    return CallHandlerBuilder<DataWrapper<RESPONSE>>().init()
}

fun <DATA: Any> CallHandler<DATA>.onSuccess(block: CallHandler<DATA>.(DataWrapper<DATA>) -> Unit) {
    onSuccess = {
        block(this, it)
    }
}

@ExperimentalContracts
fun <DATA: Any> CallHandlerBuilder<DATA>.onFail(block: CallHandlerBuilder<DATA>.(DataWrapper<DATA>) -> Unit) {
    contract {
        returns() implies (this@onFail is CallHandlerBuilder.OnFailedDefined)
    }
    _onFailed = {
        block(this, it)
    }
}

// Extension property for <PersonBuilder & Named>
val <S> S.onFailed where
        S : CallHandlerBuilder<Any>,
        S : CallHandlerBuilder.OnFailedDefined
    get() = _onFailed!!

// This method can be called only if the builder has been named
fun <S> S.build(): CallHandler<Any> where
        S : CallHandlerBuilder<Any>,
        S : CallHandlerBuilder.OnFailedDefined = CallHandler(onFailed = onFailed)