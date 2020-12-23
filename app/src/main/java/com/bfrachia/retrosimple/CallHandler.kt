package com.bfrachia.retrosimple

import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.lang.Exception

open class CallHandlerBuilder<DATA: Any> {
    var _onFailed: ((DataWrapper<DATA>) -> Unit)? = null

    interface OnFailedDefined

    private class Impl : CallHandlerBuilder<Any>(), OnFailedDefined

    companion object {
        operator fun invoke(): CallHandlerBuilder<Any> = Impl()
    }
}

class CallHandler<DATA: Any>(
    var onFailed: (DataWrapper<DATA>) -> Unit
) {
    private var queryParameters = mutableMapOf<String, Any>()
    lateinit var apiCall: suspend () -> DataWrapper<DATA>
    lateinit var onSuccess: (DataWrapper<DATA>) -> Unit

    fun withApiCall(body: suspend () -> DataWrapper<DATA>) {
        this.apiCall = body
    }

    fun withQueryParameters(block: QueryParameters<String, Any>.() -> Unit) {
        queryParameters.putAll(QueryParameters<String, Any>().apply(block))
    }

    fun makeCall(body: suspend () -> DATA): DataWrapper<DATA> {
        val result = DataWrapper<DATA>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = DataWrapper(body())
                withContext(Dispatchers.Main) {
                    onSuccess(response)
                }
            }
            catch (exception: HttpException) {
                val serviceResponse = Gson().fromJson<DataWrapper<DATA>>(
                    exception.response()?.errorBody()?.charStream(),
                    DataWrapper::class.java
                )

                withContext(Dispatchers.Main) {
                    onFailed(
                        DataWrapper(
                            errorCode = StatusCode.GENERIC_ERROR.code,
                            showMessage = serviceResponse?.showMessage
                        )
                    )
                }
            }
        }
        return result
    }

    fun makeCall(): DataWrapper<DATA> {
        val result = DataWrapper<DATA>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiCall()
                withContext(Dispatchers.Main) {
                    onSuccess(response)
                }
            }
            catch (exception: Exception) {
                when(exception) {
                    is HttpException -> {
                        try {
                            val serviceResponse = Gson().fromJson<DataWrapper<DATA>>(
                                exception.response()?.errorBody()?.charStream(),
                                DataWrapper::class.java
                            )

                            withContext(Dispatchers.Main) {
                                onFailed(
                                    DataWrapper(
                                        errorCode = StatusCode.GENERIC_ERROR.code,
                                        showMessage = serviceResponse?.showMessage
                                    )
                                )
                            }
                        }
                        catch (exception: JsonParseException) {
                            respondWithGenericError()
                        }
                    }
                    else -> {
                        respondWithGenericError()
                    }
                }
            }
        }
        return result
    }

    private suspend fun respondWithGenericError() {
        withContext(Dispatchers.Main) {
            onFailed(
                DataWrapper(
                    errorCode = StatusCode.GENERIC_ERROR.code,
                    showMessage = mapOf(
                        "EN" to "Connection error",
                        "ES" to "Error de conexión"
                    )
                )
            )
        }
    }
}

class QueryParameters<String, Any>: HashMap<String, Any>() {
    fun queryParameter(pair: Pair<String, Any>) {
        this[pair.first] = pair.second
    }
}
