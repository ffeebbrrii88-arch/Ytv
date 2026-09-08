dependencies {
    // AndroidX & TV Leanback
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // <-- Tambahkan baris ini

    // Media3 (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.2.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    // NewPipeExtractor
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.22.7")

    // OkHttp & Coroutines
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
