package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class VisualizerState(val magnitudes: FloatArray) {
    companion object {
        const val BAND_COUNT = 16
        val EMPTY = VisualizerState(FloatArray(BAND_COUNT))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VisualizerState) return false
        return magnitudes.contentEquals(other.magnitudes)
    }

    override fun hashCode(): Int = magnitudes.contentHashCode()
}

@OptIn(UnstableApi::class)
class VisualizerAudioProcessor : AudioProcessor {
    @Volatile
    var isEnabled: Boolean = false

    private val _state = MutableSharedFlow<VisualizerState>(replay = 1, extraBufferCapacity = 1)
    val state: SharedFlow<VisualizerState> = _state.asSharedFlow()

    private var inputFormat = AudioFormat.NOT_SET
    private var inputBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputEnded = false

    private val fftSize = 512
    private val sampleBuffer = FloatArray(fftSize)
    private var sampleBufferPos = 0
    private val smoothedMagnitudes = FloatArray(VisualizerState.BAND_COUNT)
    private var channelCount = 1
    private var bandBoundaries = computeBandBoundaries(44100)

    private var lastRealDataTimeMs = 0L
    private var simulatedPhase = 0.0

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        inputFormat = inputAudioFormat
        channelCount = inputAudioFormat.channelCount
        bandBoundaries = computeBandBoundaries(inputAudioFormat.sampleRate)
        return inputAudioFormat
    }

    override fun isActive(): Boolean = inputFormat != AudioFormat.NOT_SET

    override fun queueInput(buffer: ByteBuffer) {
        if (!isEnabled || !buffer.hasRemaining()) {
            inputBuffer = buffer
            outputBuffer = buffer
            return
        }

        // Use a duplicate for reading so we don't affect the original buffer position
        val analysisBuffer = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)

        while (analysisBuffer.remaining() >= 2 * channelCount) {
            var sample = 0f
            for (ch in 0 until channelCount) {
                sample += analysisBuffer.getShort().toFloat() / Short.MAX_VALUE
            }
            sample /= channelCount
            sampleBuffer[sampleBufferPos++] = sample
            if (sampleBufferPos >= fftSize) {
                performFft()
                sampleBufferPos = 0
                lastRealDataTimeMs = System.currentTimeMillis()
            }
        }

        // Advance original buffer to limit (signal all bytes consumed)
        buffer.position(buffer.limit())
        inputBuffer = buffer
        outputBuffer = buffer
    }

    override fun queueEndOfStream() {
        inputEnded = true
        outputBuffer = inputBuffer
    }

    override fun getOutput(): ByteBuffer {
        val output = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return output
    }

    override fun isEnded(): Boolean = inputEnded && outputBuffer === AudioProcessor.EMPTY_BUFFER

    override fun flush(streamMetadata: AudioProcessor.StreamMetadata) {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false
        sampleBufferPos = 0
    }

    override fun reset() {
        flush(AudioProcessor.StreamMetadata.DEFAULT)
        inputFormat = AudioFormat.NOT_SET
        sampleBuffer.fill(0f)
        smoothedMagnitudes.fill(0f)
    }

    fun emitSimulated() {
        if (!isEnabled) return
        if (System.currentTimeMillis() - lastRealDataTimeMs < 500) return
        simulatedPhase += 0.15
        val magnitudes = FloatArray(VisualizerState.BAND_COUNT) { i ->
            val freq = 0.3 + i * 0.2
            val value = 0.3f + 0.3f * sin(simulatedPhase * freq + i * 0.7).toFloat() +
                0.1f * cos(simulatedPhase * freq * 1.7 + i * 1.3).toFloat()
            value.coerceIn(0f, 1f)
        }
        _state.tryEmit(VisualizerState(magnitudes))
    }

    private fun performFft() {
        val real = FloatArray(fftSize)
        val imag = FloatArray(fftSize)
        for (i in 0 until fftSize) {
            val window = 0.5f * (1 - cos(2.0 * PI * i / (fftSize - 1))).toFloat()
            real[i] = sampleBuffer[i] * window
        }
        fft(real, imag)
        val magnitudes = FloatArray(VisualizerState.BAND_COUNT)
        for (band in 0 until VisualizerState.BAND_COUNT) {
            val lo = bandBoundaries[band]
            val hi = bandBoundaries[band + 1]
            var sum = 0f
            var count = 0
            for (bin in lo until hi) {
                sum += sqrt(real[bin] * real[bin] + imag[bin] * imag[bin])
                count++
            }
            if (count > 0) magnitudes[band] = sum / count
        }
        val maxMag = magnitudes.maxOrNull() ?: 0f
        if (maxMag > 0.001f) {
            for (i in magnitudes.indices) {
                magnitudes[i] = (magnitudes[i] / maxMag).coerceIn(0f, 1f)
            }
        }
        for (i in smoothedMagnitudes.indices) {
            smoothedMagnitudes[i] = smoothedMagnitudes[i] * 0.4f + magnitudes[i] * 0.6f
        }
        _state.tryEmit(VisualizerState(smoothedMagnitudes.copyOf()))
    }

    companion object {
        fun computeBandBoundaries(sampleRate: Int, fftSize: Int = 512): IntArray {
            val bandCount = VisualizerState.BAND_COUNT
            val nyquist = sampleRate / 2
            val minFreq = 20.0
            val maxFreq = nyquist.toDouble()
            val logMin = ln(minFreq)
            val logMax = ln(maxFreq)
            val boundaries = IntArray(bandCount + 1)
            for (i in 0..bandCount) {
                val freq = exp(logMin + (logMax - logMin) * i / bandCount)
                val bin = (freq * fftSize / sampleRate).toInt()
                boundaries[i] = min(max(bin, if (i > 0) boundaries[i - 1] + 1 else 1), fftSize / 2)
            }
            return boundaries
        }

        fun fft(real: FloatArray, imag: FloatArray) {
            val n = real.size
            var j = 0
            for (i in 1 until n) {
                var bit = n shr 1
                while (j and bit != 0) {
                    j = j xor bit
                    bit = bit shr 1
                }
                j = j xor bit
                if (i < j) {
                    real[i] = real[j].also { real[j] = real[i] }
                    imag[i] = imag[j].also { imag[j] = imag[i] }
                }
            }
            var len = 2
            while (len <= n) {
                val halfLen = len / 2
                val angle = -2.0 * PI / len
                for (i in 0 until n step len) {
                    for (k in 0 until halfLen) {
                        val theta = angle * k
                        val cosT = cos(theta).toFloat()
                        val sinT = sin(theta).toFloat()
                        val tReal = real[i + k + halfLen] * cosT - imag[i + k + halfLen] * sinT
                        val tImag = real[i + k + halfLen] * sinT + imag[i + k + halfLen] * cosT
                        real[i + k + halfLen] = real[i + k] - tReal
                        imag[i + k + halfLen] = imag[i + k] - tImag
                        real[i + k] += tReal
                        imag[i + k] += tImag
                    }
                }
                len = len shl 1
            }
        }
    }
}
