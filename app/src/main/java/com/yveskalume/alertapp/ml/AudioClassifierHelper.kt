package com.yveskalume.alertapp.ml

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.SystemClock
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.audio.core.RunningMode
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.components.containers.AudioData.AudioDataFormat
import com.google.mediapipe.tasks.core.BaseOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioClassifierHelper(
    val context: Context,
    var onError: (error: Throwable) -> Unit,
    var onResult: (audiClassifierResult: AudioClassifierResult) -> Unit
) {

    private var recorder: AudioRecord? = null
    private var classifyJob: Job? = null
    private var audioClassifier: AudioClassifier? = null

    init {
        initClassifier()
    }

    private fun initClassifier() {
        val baseOptionsBuilder = BaseOptions.builder()

        baseOptionsBuilder.setModelAssetPath("yamnet.tflite")

        try {
            val baseOptions = baseOptionsBuilder.build()
            val optionsBuilder =
                AudioClassifier.AudioClassifierOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.AUDIO_STREAM)
                    .setScoreThreshold(0.3f)
                    .setResultListener(onResult)
                    .setErrorListener(onError)

            val options = optionsBuilder.build()

            audioClassifier = AudioClassifier.createFromOptions(context, options)

            recorder = audioClassifier!!.createAudioRecord(
                AudioFormat.CHANNEL_IN_DEFAULT,
                SAMPLING_RATE_IN_HZ,
                BUFFER_SIZE_IN_BYTES.toInt()
            )
        } catch (t: Throwable) {
            onError(t)
        }
    }

    suspend fun startAudioClassification() {
        if (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return
        }

        recorder?.startRecording()

        val lengthInMilliSeconds =
            ((REQUIRE_INPUT_BUFFER_SIZE * 1.0f) / SAMPLING_RATE_IN_HZ) * 1000

        val interval = (lengthInMilliSeconds * (1 - (DEFAULT_OVERLAP * 0.25))).toLong()

        classifyJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                recorder?.let { classifyAudioAsync(it) }
                delay(interval)
            }
        }

    }

    private fun classifyAudioAsync(audioRecord: AudioRecord) {
        val audioData = AudioData.create(
            AudioDataFormat.create(recorder!!.format), SAMPLING_RATE_IN_HZ
        )
        audioData.load(audioRecord)

        val inferenceTime = SystemClock.uptimeMillis()
        audioClassifier?.classifyAsync(audioData, inferenceTime)
    }

    fun stopAudioClassification() {
        classifyJob?.cancel()
        audioClassifier?.close()
        audioClassifier = null
        recorder?.stop()
    }

    fun isClosed(): Boolean {
        return audioClassifier == null
    }

    companion object {
        const val DEFAULT_OVERLAP = 2

        private const val SAMPLING_RATE_IN_HZ = 16000
        private const val BUFFER_SIZE_FACTOR: Int = 2
        const val EXPECTED_INPUT_LENGTH = 0.975F
        private const val REQUIRE_INPUT_BUFFER_SIZE =
            SAMPLING_RATE_IN_HZ * EXPECTED_INPUT_LENGTH

        private const val BUFFER_SIZE_IN_BYTES =
            REQUIRE_INPUT_BUFFER_SIZE * Float.SIZE_BYTES * BUFFER_SIZE_FACTOR
    }
}
