package com.bfrachia.retrosimple

import android.content.Context
import android.os.Build
import java.util.*

class DataWrapper<T>(
    var data: T? = null,
    var result: Boolean = false,
    var needUpdate: Boolean = false,
    var errorCode: Int = StatusCode.SUCCESS.code,
    var message: String? = "",
    var showMessage: Map<String, String>? = null
) {
    fun getShowMessage(context: Context): String {
        showMessage?.let { showMessage ->
            val locale = getLocale(context).language.toUpperCase()
            if (showMessage.containsKey(locale)) {
                return showMessage[locale] ?: ""
            }
        }
        return ""
    }

    private fun getLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
    }
}