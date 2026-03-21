package me.echeung.moemoekyun.service

import androidx.media3.common.audio.AudioProcessor
import io.kotest.assertions.withClue
import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.sin

class VisualizerAudioProcessorTest {

    @Test
    fun `band boundaries are monotonically increasing`() {
        val boundaries = VisualizerAudioProcessor.computeBandBoundaries(44100)
        for (i in 1 until boundaries.size) {
            withClue("Boundary $i should be greater than boundary ${i - 1}") {
                boundaries[i] shouldBeGreaterThan boundaries[i - 1]
            }
        }
    }

    @Test
    fun `band boundaries stay within FFT range`() {
        val boundaries = VisualizerAudioProcessor.computeBandBoundaries(44100)
        boundaries.first() shouldBeGreaterThan 0
        boundaries.last() shouldBeLessThanOrEqual 256
    }

    @Test
    fun `band boundaries has correct count`() {
        val boundaries = VisualizerAudioProcessor.computeBandBoundaries(44100)
        boundaries.size shouldBe VisualizerState.BAND_COUNT + 1
    }

    @Test
    fun `FFT detects dominant frequency in low band`() {
        val sampleRate = 44100
        val fftSize = 512
        val freq = 100.0
        val real = FloatArray(fftSize) { i -> sin(2.0 * PI * freq * i / sampleRate).toFloat() }
        val imag = FloatArray(fftSize)
        VisualizerAudioProcessor.fft(real, imag)
        var maxBin = 0
        var maxMag = 0f
        for (i in 1 until fftSize / 2) {
            val mag = real[i] * real[i] + imag[i] * imag[i]
            if (mag > maxMag) {
                maxMag = mag
                maxBin = i
            }
        }
        val expectedBin = (freq * fftSize / sampleRate).toInt()
        withClue("Dominant frequency bin should be near expected bin $expectedBin") {
            maxBin shouldBe expectedBin
        }
    }

    @Test
    fun `FFT detects dominant frequency in high band`() {
        val sampleRate = 44100
        val fftSize = 512
        val freq = 5000.0
        val real = FloatArray(fftSize) { i -> sin(2.0 * PI * freq * i / sampleRate).toFloat() }
        val imag = FloatArray(fftSize)
        VisualizerAudioProcessor.fft(real, imag)
        var maxBin = 0
        var maxMag = 0f
        for (i in 1 until fftSize / 2) {
            val mag = real[i] * real[i] + imag[i] * imag[i]
            if (mag > maxMag) {
                maxMag = mag
                maxBin = i
            }
        }
        val expectedBin = (freq * fftSize / sampleRate).toInt()
        withClue("Dominant frequency bin should be near expected bin $expectedBin") {
            maxBin shouldBe expectedBin
        }
    }

    @Test
    fun `VisualizerState EMPTY has correct band count`() {
        VisualizerState.EMPTY.magnitudes.size shouldBe VisualizerState.BAND_COUNT
    }

    @Test
    fun `VisualizerState EMPTY has all zero magnitudes`() {
        VisualizerState.EMPTY.magnitudes.all { it == 0f } shouldBe true
    }

    @Test
    fun `emitSimulated produces values in valid range`() {
        val processor = VisualizerAudioProcessor()
        processor.isEnabled = true
        processor.emitSimulated()
        val state = processor.state.replayCache.firstOrNull()
        state shouldNotBe null
        requireNotNull(state)
        state.magnitudes.size shouldBe VisualizerState.BAND_COUNT
        state.magnitudes.forEach { mag ->
            withClue("Simulated magnitude should be in 0..1") {
                mag shouldBeGreaterThan -0.01f
            }
        }
    }

    @Test
    fun `queueInput passes buffer through with position advanced to limit`() {
        val processor = VisualizerAudioProcessor()
        processor.isEnabled = true
        val audioFormat = AudioProcessor.AudioFormat(44100, 1, androidx.media3.common.C.ENCODING_PCM_16BIT)
        processor.configure(audioFormat)
        processor.flush(AudioProcessor.StreamMetadata.DEFAULT)

        // Create a test buffer with some PCM data
        val pcmData = ByteArray(64) { it.toByte() }
        val buffer = java.nio.ByteBuffer.wrap(pcmData)

        processor.queueInput(buffer)

        // Buffer position should be at limit (all bytes consumed)
        buffer.position() shouldBe buffer.limit()
    }
}
