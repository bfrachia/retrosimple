package com.bfrachia.retrosimple

/**
 * Created by Bruno Frachia on 2019-08-08.
 */
enum class StatusCode(val code: Int) {
    SUCCESS(0),
    GENERIC_ERROR(1),
    NETWORK_ERROR(2)
}