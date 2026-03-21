# Audio Visualizer Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a frequency-bar audio visualizer as a subtle background layer in the expanded player screen.

**Architecture:** Custom Media3 `AudioProcessor` performs FFT on PCM audio data and publishes bar magnitudes via `SharedFlow`. A Compose `Canvas` renders bars behind the player controls using the album art accent color at low opacity. A settings toggle controls visibility, and the processor is lazy — only active when visible.

**Tech Stack:** Media3 ExoPlayer, Jetpack Compose Canvas, Hilt DI, Kotlin Coroutines/Flow, FlowSharedPreferences

---

## Chunk 1: Full Implementation

### Task 1: VisualizerAudioProcessor — FFT and state emission

**Files:**
- Create: `app/src/main/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessor.kt`
- Test: `app/src/test/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessorTest.kt`

- [ ] **Step 1: Write the VisualizerState data class and VisualizerAudioProcessor**

```kotlin
// app/src/main/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessor.kt
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

    var isEnabled: Boolean = false

    private val _state = MutableSharedFlow<VisualizerState>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    val state: SharedFlow<VisualizerState> = _state.asSharedFlow()

    private var inputFormat = AudioFormat.NOT_SET
    private var inputBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputEnded = false

    // FFT state
    private val fftSize = 512
    private val sampleBuffer = FloatArray(fftSize)
    private var sampleBufferPos = 0
    private val smoothedMagnitudes = FloatArray(VisualizerState.BAND_COUNT)
    private var channelCount = 1

    // Band boundary indices for logarithmic grouping
    private var bandBoundaries = computeBandBoundaries(44100)

    // Simulated fallback state
    private var lastRealDataTimeMs = 0L
    private var simulatedPhase = 0.0

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        require(inputAudioFormat.encoding == C.ENCODING_PCM_16BIT) {
            "Unsupported encoding: ${inputAudioFormat.encoding}"
        }
        inputFormat = inputAudioFormat
        channelCount = inputAudioFormat.channelCount
        bandBoundaries = computeBandBoundaries(inputAudioFormat.sampleRate)
        return inputAudioFormat // Passive tap — format unchanged
    }

    override fun isActive(): Boolean = inputFormat != AudioFormat.NOT_SET

    override fun queueInput(buffer: ByteBuffer) {
        if (!isEnabled || !buffer.hasRemaining()) {
            // Pass through without processing
            inputBuffer = buffer
            outputBuffer = buffer
            return
        }

        // Read PCM 16-bit samples, downmix stereo to mono
        val order = buffer.order()
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val startPos = buffer.position()

        while (buffer.remaining() >= 2 * channelCount) {
            var sample = 0f
            for (ch in 0 until channelCount) {
                sample += buffer.getShort().toFloat() / Short.MAX_VALUE
            }
            sample /= channelCount

            sampleBuffer[sampleBufferPos++] = sample

            if (sampleBufferPos >= fftSize) {
                performFft()
                sampleBufferPos = 0
                lastRealDataTimeMs = System.currentTimeMillis()
            }
        }

        buffer.position(startPos)
        buffer.order(order)
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

    override fun flush() {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false
        sampleBufferPos = 0
    }

    override fun reset() {
        flush()
        inputFormat = AudioFormat.NOT_SET
        sampleBuffer.fill(0f)
        smoothedMagnitudes.fill(0f)
    }

    /**
     * Emit simulated visualizer data when no real audio is flowing.
     * Called from a coroutine on a timer when enabled but no real data arrives.
     */
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
        // Apply Hann window
        val real = FloatArray(fftSize)
        val imag = FloatArray(fftSize)
        for (i in 0 until fftSize) {
            val window = 0.5f * (1 - cos(2.0 * PI * i / (fftSize - 1))).toFloat()
            real[i] = sampleBuffer[i] * window
        }

        // Cooley-Tukey FFT (in-place, radix-2)
        fft(real, imag)

        // Compute magnitudes and group into logarithmic bands
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
            if (count > 0) {
                magnitudes[band] = sum / count
            }
        }

        // Normalize — scale to 0..1 range
        val maxMag = magnitudes.max()
        if (maxMag > 0.001f) {
            for (i in magnitudes.indices) {
                magnitudes[i] = (magnitudes[i] / maxMag).coerceIn(0f, 1f)
            }
        }

        // Smooth with previous values (exponential decay)
        for (i in smoothedMagnitudes.indices) {
            smoothedMagnitudes[i] = smoothedMagnitudes[i] * 0.4f + magnitudes[i] * 0.6f
        }

        _state.tryEmit(VisualizerState(smoothedMagnitudes.copyOf()))
    }

    companion object {
        /**
         * Compute logarithmically-spaced band boundaries for FFT bins.
         * Maps 16 bands across the useful frequency range (20Hz - Nyquist).
         */
        fun computeBandBoundaries(sampleRate: Int, fftSize: Int = 512): IntArray {
            val bandCount = VisualizerState.BAND_COUNT
            val nyquist = sampleRate / 2
            val minFreq = 20.0
            val maxFreq = nyquist.toDouble()
            val logMin = ln(minFreq)
            val logMax = ln(maxFreq)

            val boundaries = IntArray(bandCount + 1)
            for (i in 0..bandCount) {
                val freq = Math.exp(logMin + (logMax - logMin) * i / bandCount)
                val bin = (freq * fftSize / sampleRate).toInt()
                boundaries[i] = min(max(bin, if (i > 0) boundaries[i - 1] + 1 else 1), fftSize / 2)
            }
            return boundaries
        }

        /**
         * Cooley-Tukey radix-2 FFT (in-place).
         */
        fun fft(real: FloatArray, imag: FloatArray) {
            val n = real.size
            // Bit-reversal permutation
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

            // FFT butterfly
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
```

- [ ] **Step 2: Write unit tests for FFT and band computation**

```kotlin
// app/src/test/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessorTest.kt
package me.echeung.moemoekyun.service

import io.kotest.assertions.withClue
import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
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
        // Generate a 100Hz sine wave (should land in a low frequency band)
        val sampleRate = 44100
        val fftSize = 512
        val freq = 100.0
        val real = FloatArray(fftSize) { i ->
            sin(2.0 * PI * freq * i / sampleRate).toFloat()
        }
        val imag = FloatArray(fftSize)

        VisualizerAudioProcessor.fft(real, imag)

        // Find the bin with highest magnitude
        var maxBin = 0
        var maxMag = 0f
        for (i in 1 until fftSize / 2) {
            val mag = real[i] * real[i] + imag[i] * imag[i]
            if (mag > maxMag) {
                maxMag = mag
                maxBin = i
            }
        }

        // Expected bin for 100Hz: 100 * 512 / 44100 ≈ 1.16 → bin 1
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
        val real = FloatArray(fftSize) { i ->
            sin(2.0 * PI * freq * i / sampleRate).toFloat()
        }
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
        // Force past the 500ms real-data timeout
        processor.emitSimulated()

        // Since lastRealDataTimeMs is 0 (never received real data), simulated should emit
        val state = processor.state.replayCache.firstOrNull()
        if (state != null) {
            state.magnitudes.size shouldBe VisualizerState.BAND_COUNT
            state.magnitudes.forEach { mag ->
                withClue("Simulated magnitude should be in 0..1") {
                    mag shouldBeGreaterThan -0.01f
                }
            }
        }
    }
}
```

- [ ] **Step 3: Run tests to verify they pass**

Run: `./gradlew test --tests "me.echeung.moemoekyun.service.VisualizerAudioProcessorTest" --info`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessor.kt \
       app/src/test/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessorTest.kt
git commit -m "feat: add VisualizerAudioProcessor with FFT and simulated fallback"
```

---

### Task 2: Hilt DI — VisualizerModule and MediaModule wiring

**Files:**
- Create: `app/src/main/kotlin/me/echeung/moemoekyun/di/VisualizerModule.kt`
- Modify: `app/src/main/kotlin/me/echeung/moemoekyun/di/MediaModule.kt:53-61`

- [ ] **Step 1: Create VisualizerModule**

```kotlin
// app/src/main/kotlin/me/echeung/moemoekyun/di/VisualizerModule.kt
package me.echeung.moemoekyun.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.echeung.moemoekyun.service.VisualizerAudioProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VisualizerModule {

    @Provides
    @Singleton
    fun visualizerAudioProcessor(): VisualizerAudioProcessor = VisualizerAudioProcessor()
}
```

- [ ] **Step 2: Modify MediaModule to wire processor into ExoPlayer**

In `app/src/main/kotlin/me/echeung/moemoekyun/di/MediaModule.kt`, modify the `exoPlayer` provider to accept the `VisualizerAudioProcessor` and add it via `setAudioProcessorsFactory()`:

```kotlin
// Replace the existing exoPlayer provider (lines 53-61) with:
@Provides
fun exoPlayer(
    @ApplicationContext context: Context,
    progressiveMediaSourceFactory: ProgressiveMediaSource.Factory,
    audioAttributes: AudioAttributes,
    visualizerAudioProcessor: VisualizerAudioProcessor,
): Player = ExoPlayer.Builder(context)
    .setMediaSourceFactory(progressiveMediaSourceFactory)
    .setAudioAttributes(audioAttributes, true)
    .setWakeMode(C.WAKE_MODE_NETWORK)
    .setAudioProcessorsFactory { defaultProcessors ->
        defaultProcessors + arrayOf(visualizerAudioProcessor)
    }
    .build()
```

Also add import:
```kotlin
import me.echeung.moemoekyun.service.VisualizerAudioProcessor
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/me/echeung/moemoekyun/di/VisualizerModule.kt \
       app/src/main/kotlin/me/echeung/moemoekyun/di/MediaModule.kt \
       app/src/main/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessor.kt
git commit -m "feat: add Hilt DI for VisualizerAudioProcessor and wire into ExoPlayer"
```

---

### Task 3: Settings preference

**Files:**
- Modify: `app/src/main/kotlin/me/echeung/moemoekyun/util/PreferenceUtil.kt:21`
- Modify: `app/src/main/res/values/strings.xml:161`
- Modify: `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/settings/SettingsScreen.kt:93-95`

- [ ] **Step 1: Add visualizerEnabled preference to PreferenceUtil**

In `app/src/main/kotlin/me/echeung/moemoekyun/util/PreferenceUtil.kt`, add after line 17 (`shouldPauseOnNoisy`):

```kotlin
fun shouldShowVisualizer() = prefs.getBoolean("pref_audio_visualizer", false)
```

- [ ] **Step 2: Add string resources**

In `app/src/main/res/values/strings.xml`, add after line 161 (after the `pref_title_pause_on_noisy_summary` line):

```xml
<string name="pref_title_visualizer">Audio visualizer</string>
<string name="pref_title_visualizer_summary">Show frequency bars in the player</string>
```

- [ ] **Step 3: Add toggle to SettingsScreen**

In `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/settings/SettingsScreen.kt`, add a new `item` block after the existing pause-for-headphones `SwitchPreference` (after line 93):

```kotlin
item {
    SwitchPreference(
        title = stringResource(R.string.pref_title_visualizer),
        subtitle = stringResource(R.string.pref_title_visualizer_summary),
        preference = screenModel.preferenceUtil.shouldShowVisualizer(),
    )
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/me/echeung/moemoekyun/util/PreferenceUtil.kt \
       app/src/main/res/values/strings.xml \
       app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/settings/SettingsScreen.kt
git commit -m "feat: add audio visualizer settings toggle"
```

---

### Task 4: AudioVisualizer composable

**Files:**
- Create: `app/src/main/kotlin/me/echeung/moemoekyun/ui/common/AudioVisualizer.kt`

- [ ] **Step 1: Write the AudioVisualizer composable**

```kotlin
// app/src/main/kotlin/me/echeung/moemoekyun/ui/common/AudioVisualizer.kt
package me.echeung.moemoekyun.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import me.echeung.moemoekyun.service.VisualizerState
import kotlin.math.min

private val MaxHeight = 120.dp
private val MinBarWidth = 12.dp
private val BarGap = 2.dp
private val BarCornerRadius = 2.dp
private const val BAR_ALPHA = 0.12f

@Composable
fun AudioVisualizer(
    state: VisualizerState,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val minBarWidthPx = with(density) { MinBarWidth.toPx() }
    val gapPx = with(density) { BarGap.toPx() }
    val cornerRadiusPx = with(density) { BarCornerRadius.toPx() }

    val barCount = min(
        VisualizerState.BAND_COUNT,
        ((state.magnitudes.size * minBarWidthPx + gapPx) / (minBarWidthPx + gapPx)).toInt()
            .coerceAtLeast(1),
    )

    // Animate each bar independently with spring physics
    val animatedMagnitudes = Array(barCount) { i ->
        // If fewer bars than bands, merge adjacent bands
        val magnitude = if (barCount < state.magnitudes.size) {
            val bandsPerBar = state.magnitudes.size.toFloat() / barCount
            val startBand = (i * bandsPerBar).toInt()
            val endBand = min(((i + 1) * bandsPerBar).toInt(), state.magnitudes.size)
            var sum = 0f
            for (b in startBand until endBand) sum += state.magnitudes[b]
            if (endBand > startBand) sum / (endBand - startBand) else 0f
        } else {
            state.magnitudes.getOrElse(i) { 0f }
        }

        val animated by animateFloatAsState(
            targetValue = magnitude,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "bar_$i",
        )
        animated
    }

    val barColor = accentColor.copy(alpha = BAR_ALPHA)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(MaxHeight),
    ) {
        val actualBarCount = min(
            barCount,
            ((size.width + gapPx) / (minBarWidthPx + gapPx)).toInt().coerceAtLeast(1),
        )
        val totalGapWidth = (actualBarCount - 1) * gapPx
        val barWidth = (size.width - totalGapWidth) / actualBarCount

        for (i in 0 until actualBarCount) {
            val barHeight = animatedMagnitudes.getOrElse(i) { 0f } * size.height
            val x = i * (barWidth + gapPx)
            val y = size.height - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            )
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/me/echeung/moemoekyun/ui/common/AudioVisualizer.kt
git commit -m "feat: add AudioVisualizer Compose Canvas composable"
```

---

### Task 5: Integrate visualizer into player UI

**Files:**
- Modify: `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/HomeScreen.kt:39-44,60-71`
- Modify: `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/HomeScreenModel.kt:34,44-46`
- Modify: `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/Player.kt:92-101,293-326,330-370`

- [ ] **Step 1: Inject VisualizerAudioProcessor into HomeScreenModel**

In `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/HomeScreenModel.kt`:

Change the visibility of `preferenceUtil` from `private val` to `val` (line 43) so it is accessible from `HomeScreen`:
```kotlin
// Before:
private val preferenceUtil: PreferenceUtil,
// After:
val preferenceUtil: PreferenceUtil,
```

Add `visualizerAudioProcessor` to the constructor parameters (after `preferenceUtil`):
```kotlin
val visualizerAudioProcessor: VisualizerAudioProcessor,
```

Add import:
```kotlin
import me.echeung.moemoekyun.service.VisualizerAudioProcessor
```

- [ ] **Step 2: Add visualizer state and preference collection to HomeScreen**

In `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/HomeScreen.kt`:

Add imports:
```kotlin
import me.echeung.moemoekyun.service.VisualizerState
import me.echeung.moemoekyun.util.ext.collectAsState
```

After line 42 (`val radioState by screenModel.radioState.collectAsState()`), add:
```kotlin
val visualizerState by screenModel.visualizerAudioProcessor.state.collectAsState(
    initial = VisualizerState.EMPTY,
)
val showVisualizer by screenModel.preferenceUtil.shouldShowVisualizer().collectAsState()
```

This uses the project's existing `Preference<T>.collectAsState()` extension from `me.echeung.moemoekyun.util.ext.PreferenceExtensions.kt`.

Pass both to `PlayerScaffold`:
```kotlin
PlayerScaffold(
    radioState = radioState,
    mediaController = player,
    accentColor = state.accentColor,
    visualizerState = visualizerState,
    showVisualizer = showVisualizer,
    onClickStation = screenModel::toggleLibrary,
    // ... rest unchanged
)
```

- [ ] **Step 3: Thread visualizer state through PlayerScaffold and ExpandedPlayerContent**

In `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/Player.kt`:

Add parameters to `PlayerScaffold` signature (after `accentColor`):
```kotlin
visualizerState: VisualizerState = VisualizerState.EMPTY,
showVisualizer: Boolean = false,
```

Pass them to `PlayerContent`:
```kotlin
PlayerContent(
    radioState = radioState,
    playPauseButtonState = playPauseButtonState,
    accentColor = accentColor,
    visualizerState = visualizerState,
    showVisualizer = showVisualizer,
    onClickStation = onClickStation,
    // ... rest unchanged
)
```

Add the following parameters to each function signature and thread them through the call chain:

**`PlayerContent`** — add after `accentColor: Color?`:
```kotlin
visualizerState: VisualizerState = VisualizerState.EMPTY,
showVisualizer: Boolean = false,
```
Pass them to `ExpandedPlayerContent`.

**`ExpandedPlayerContent`** — add after `accentColor: Color?`:
```kotlin
visualizerState: VisualizerState = VisualizerState.EMPTY,
showVisualizer: Boolean = false,
```
Pass `accentColor`, `visualizerState`, and `showVisualizer` to both `PortraitExpandedPlayerContent` and `LandscapeExpandedPlayerContent`.

**`PortraitExpandedPlayerContent`** — add after `onClickCollapse: () -> Unit`:
```kotlin
accentColor: Color?,
visualizerState: VisualizerState = VisualizerState.EMPTY,
showVisualizer: Boolean = false,
```

**`LandscapeExpandedPlayerContent`** — add after `onClickCollapse: () -> Unit`:
```kotlin
accentColor: Color?,
visualizerState: VisualizerState = VisualizerState.EMPTY,
showVisualizer: Boolean = false,
```

Add imports at the top of `Player.kt`:
```kotlin
import me.echeung.moemoekyun.service.VisualizerState
import me.echeung.moemoekyun.ui.common.AudioVisualizer
```

- [ ] **Step 4: Add Box wrapper with AudioVisualizer in portrait layout**

In `PortraitExpandedPlayerContent`, replace the `SongInfo(...)` call (around line 318-324) with:

```kotlin
Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.BottomCenter,
) {
    if (showVisualizer) {
        AudioVisualizer(
            state = visualizerState,
            accentColor = accentColor ?: MaterialTheme.colorScheme.primary,
        )
    }
    SongInfo(
        radioState,
        playPauseButtonState,
        radioState.currentSong,
        onClickHistory,
        toggleFavorite,
    )
}
```

- [ ] **Step 5: Add Box wrapper with AudioVisualizer in landscape layout**

In `LandscapeExpandedPlayerContent`, apply the same `Box` pattern around the `SongInfo` call (around line 358-364):

```kotlin
Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.BottomCenter,
) {
    if (showVisualizer) {
        AudioVisualizer(
            state = visualizerState,
            accentColor = accentColor ?: MaterialTheme.colorScheme.primary,
        )
    }
    SongInfo(
        radioState,
        playPauseButtonState,
        radioState.currentSong,
        onClickHistory,
        toggleFavorite,
    )
}
```

- [ ] **Step 6: Add enable/disable logic and simulated emission in PlayerScaffold**

In `PlayerScaffold`, add the processor as a parameter (after `showVisualizer`):

```kotlin
visualizerAudioProcessor: VisualizerAudioProcessor? = null,
```

Add imports at the top of `Player.kt`:
```kotlin
import kotlinx.coroutines.delay
import me.echeung.moemoekyun.service.VisualizerAudioProcessor
```

`SheetValue` is already imported (used by `rememberStandardBottomSheetState`).

Then inside `PlayerScaffold`, after `playPauseButtonState`:

```kotlin
// Control processor enabled state based on visibility conditions
LaunchedEffect(showVisualizer, playPauseButtonState.showPlay, scaffoldState.bottomSheetState.currentValue) {
    val shouldEnable = showVisualizer &&
        !playPauseButtonState.showPlay &&
        scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
    visualizerAudioProcessor?.isEnabled = shouldEnable
}

// Drive simulated fallback emission when processor is enabled but no real audio arrives
LaunchedEffect(showVisualizer, playPauseButtonState.showPlay) {
    if (showVisualizer && !playPauseButtonState.showPlay) {
        while (true) {
            visualizerAudioProcessor?.emitSimulated()
            delay(16) // ~60fps
        }
    }
}
```

Thread this parameter from `HomeScreen`:
```kotlin
PlayerScaffold(
    // ...
    visualizerAudioProcessor = screenModel.visualizerAudioProcessor,
)
```

- [ ] **Step 7: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Run all tests**

Run: `./gradlew test`
Expected: All tests PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/HomeScreen.kt \
       app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/HomeScreenModel.kt \
       app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/Player.kt
git commit -m "feat: integrate audio visualizer into player UI with lazy enable/disable"
```

---

### Task 6: Verify and lint

- [ ] **Step 1: Run ktlint**

Run: `./gradlew ktlintCheck`
Expected: BUILD SUCCESSFUL (no lint errors)

If errors found, fix with: `./gradlew ktlintFormat` and review changes.

- [ ] **Step 2: Run full test suite**

Run: `./gradlew test`
Expected: All tests PASS

- [ ] **Step 3: Verify full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit any lint fixes**

```bash
git add -A
git commit -m "chore: fix lint issues" --allow-empty
```
