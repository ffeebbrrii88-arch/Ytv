package com.example.youtubetv

import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as ExtractorRequest
import org.schabi.newpipe.extractor.downloader.Response as ExtractorResponse
import java.io.IOException

class DownloaderImpl private constructor(
    private val client: OkHttpClient
) : Downloader() {

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

        val builder = Request.Builder()
            .url(request.url())

            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
            )

            .header(
                "Accept-Language",
                "en-US,en;q=0.9"
            )

        request.headers().forEach { (key, values) ->
            builder.header(key, values.joinToString(","))
        }

        when(request.httpMethod()) {
            "POST" -> {
                val body = okhttp3.RequestBody.create(
                    null,
                    request.dataToSend() ?: ByteArray(0)
                )
                builder.post(body)
            }

            else -> {
                builder.get()
            }
        }


        val response = client
            .newCall(builder.build())
            .execute()


        val body = response.body?.string() ?: ""


        return ExtractorResponse(
            response.code,
            response.message,
            response.headers.toMultimap(),
            body,
            response.request.url.toString()
        )
    }
}
