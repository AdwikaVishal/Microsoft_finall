package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

class AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 16000
    private val config = AudioFormat.CHANNEL_IN_MONO
    private val format = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, config, format)

    // Silence detection parameters
    private val silenceThreshold = 200 // Amplitude threshold
    private val silenceDurationMs = 1500L // 1.5 seconds of silence to stop
    private val maxDurationMs = 10000L // 10 seconds max

    @SuppressLint("MissingPermission")
    suspend fun recordAudio(): ByteArray? = withContext(Dispatchers.IO) {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                config,
                format,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext null
            }

            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(bufferSize)
            audioRecord?.startRecording()
            isRecording = true

            val startTime = System.currentTimeMillis()
            var lastSoundTime = System.currentTimeMillis()

            while (isRecording && isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    outputStream.write(buffer, 0, read)
                    
                    // Simple amplitude check
                    var sum = 0L
                    for (i in 0 until read step 2) {
                        val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                        sum += sample * sample
                    }
                    val amplitude = sqrt(sum.toDouble() / (read / 2))

                    if (amplitude > silenceThreshold) {
                        lastSoundTime = System.currentTimeMillis()
                    }

                    // Check for silence or max duration
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - startTime > maxDurationMs) {
                        break
                    }
                    if (currentTime - lastSoundTime > silenceDurationMs && (currentTime - startTime) > 2000) {
                         // Stop if silence detected after initial 2 seconds
                        break
                    }
                }
            }

            stopRecording()
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
            null
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioRecord = null
    }
}
