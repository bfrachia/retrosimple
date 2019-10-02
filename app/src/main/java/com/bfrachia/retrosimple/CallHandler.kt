package com.bfrachia.retrosimple

import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException

class CallHandler<DATA: Any> {
    private var queryParameters = mutableMapOf<String, Any>()
    lateinit var apiCall: suspend () -> DataWrapper<DATA>
    lateinit var onSuccess: (DataWrapper<DATA>) -> Unit
    lateinit var onFailed: (DataWrapper<DATA>) -> Unit

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
                            showMessage = serviceResponse.showMessage
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
            catch (exception: HttpException) {
                val serviceResponse = Gson().fromJson<DataWrapper<DATA>>(
                    exception.response()?.errorBody()?.charStream(),
                    DataWrapper::class.java
                )

                withContext(Dispatchers.Main) {
                    onFailed(
                        DataWrapper(
                            errorCode = StatusCode.GENERIC_ERROR.code,
                            showMessage = serviceResponse.showMessage
                        )
                    )
                }
            }
        }
        return result
    }
}

class QueryParameters<String, Any>: HashMap<String, Any>() {
    fun queryParameter(pair: Pair<String, Any>) {
        this[pair.first] = pair.second
    }
}
