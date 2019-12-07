package com.bfrachia.retrosimple

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream


class ProgressRequestBody(
    private val contentType: String,
    private val file: File,
    private val onProgressUpdated: (Int) -> Unit
) : RequestBody() {

    private val DEFAULT_BUFFER_SIZE = 2048

    override fun contentType(): MediaType? {
        return "${contentType}/*".toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return file.length()
    }

    override fun writeTo(sink: BufferedSink) {
        val fileLength: Long = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inputStream = FileInputStream(file)
        var uploaded: Long = 0

        inputStream.use { inputStream ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (inputStream.read(buffer).also { read = it } != -1) { // update progress on UI thread
                handler.post(ProgressUpdater(uploaded, fileLength))
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        }
    }

    inner class ProgressUpdater(
        private val uploaded: Long,
        private val total: Long
    ): Runnable {
        override fun run() {
            onProgressUpdated((100*uploaded/total).toInt())
        }
    }
}