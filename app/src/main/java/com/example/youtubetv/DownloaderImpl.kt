package com.example.youtubetv

import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as ExtractorRequest
import org.schabi.newpipe.extractor.downloader.Response as ExtractorResponse
import java.io.IOException

class DownloaderImpl private constructor(private val client: OkHttpClient) : Downloader() {

    companion object {
        private var instance: DownloaderImpl? = null

        fun init(client: OkHttpClient): DownloaderImpl {
            if (instance == null) {
                instance = DownloaderImpl(client)
            }
            return instance!!
        }
    }

    @Throws(IOException::class)
    override fun execute(request: ExtractorRequest): ExtractorResponse {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = Request.Builder().url(url)
        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value.joinToString(","))
        }

        requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")

        when (httpMethod) {
            "GET" -> requestBuilder.get()
            "POST" -> {
                val body = okhttp3.RequestBody.create(null, dataToSend ?: ByteArray(0))
                requestBuilder.post(body)
            }
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string() ?: ""

        return ExtractorResponse(
            response.code,
            response.message,
            response.headers.toMultimap(),
            responseBody,
            url // <-- Diubah dari request.latestUrl ke url
        )
    }
}
