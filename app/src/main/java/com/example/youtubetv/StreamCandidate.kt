package com.example.youtubetv

import androidx.media3.common.C

data class StreamCandidate(
    val url: String,
    @C.ContentType val type: Int
)
