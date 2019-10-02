package com.bfrachia.retrosimple

interface DataResponse<T> {
    fun retrieveData(): T
}