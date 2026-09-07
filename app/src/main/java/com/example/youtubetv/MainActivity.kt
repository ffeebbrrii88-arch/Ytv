package com.example.youtubetv

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo

class MainActivity : FragmentActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private val streamCandidates = mutableListOf<StreamCandidate>()
    private var currentCandidateIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)

        // Initialize Downloader
        val okHttpClient = OkHttpClient.Builder().build()
        NewPipe.init(DownloaderImpl.init(okHttpClient))

        setupPlayer()

        val youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        extractAndPrepareFallback(youtubeUrl)
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    tryNextCandidate()
                }
            })
        }
    }

    private fun extractAndPrepareFallback(youtubeUrl: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, youtubeUrl)

                streamCandidates.clear()
                currentCandidateIndex = 0

                // 1. HLS
                if (!streamInfo.hlsUrl.isNullOrEmpty()) {
                    streamCandidates.add(StreamCandidate(streamInfo.hlsUrl, C.CONTENT_TYPE_HLS))
                }

                // 2. DASH
                if (!streamInfo.dashUrl.isNullOrEmpty()) {
                    streamCandidates.add(StreamCandidate(streamInfo.dashUrl, C.CONTENT_TYPE_DASH))
                }

                // 3. Progressive MP4
                val progressiveStreams = streamInfo.videoStreams
                if (progressiveStreams.isNotEmpty()) {
                    val bestMp4Url = progressiveStreams.last().content
                    streamCandidates.add(StreamCandidate(bestMp4Url, C.CONTENT_TYPE_OTHER))
                }

                withContext(Dispatchers.Main) {
                    if (streamCandidates.isNotEmpty()) {
                        playCandidate(currentCandidateIndex)
                    } else {
                        Toast.makeText(this@MainActivity, "Tidak ada stream yang tersedia", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Gagal mengekstrak: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playCandidate(index: Int) {
        val candidate = streamCandidates.getOrNull(index) ?: return

        player?.let { exoPlayer ->
            val mediaItem = MediaItem.Builder()
                .setUri(candidate.url)
                .setMimeType(
                    when (candidate.type) {
                        C.CONTENT_TYPE_HLS -> "application/x-mpegURL"
                        C.CONTENT_TYPE_DASH -> "application/dash+xml"
                        else -> "video/mp4"
                    }
                )
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun tryNextCandidate() {
        currentCandidateIndex++
        if (currentCandidateIndex < streamCandidates.size) {
            val formatName = when (streamCandidates[currentCandidateIndex].type) {
                C.CONTENT_TYPE_HLS -> "HLS"
                C.CONTENT_TYPE_DASH -> "DASH"
                else -> "MP4 Progressive"
            }
            Toast.makeText(this, "Stream gagal. Beralih ke fallback: $formatName", Toast.LENGTH_SHORT).show()
            playCandidate(currentCandidateIndex)
        } else {
            Toast.makeText(this, "Semua pilihan stream gagal diputar.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
