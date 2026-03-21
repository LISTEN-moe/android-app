# Audio Visualizer Design

## Overview

Add an audio frequency visualizer to the expanded player screen. The visualizer renders as a subtle background layer behind the controls — bars growing upward from the bottom edge at low opacity, using the album art accent color. The existing layout is unchanged.

## Architecture

### Data Flow

```
ExoPlayer audio pipeline
  → VisualizerAudioProcessor (FFT on audio thread, when enabled)
  → SharedFlow<VisualizerState> (marshals to main thread)
  → AudioVisualizer composable (Canvas drawing)
```

### Components

**`VisualizerState`** — Data class holding a `FloatArray` of bar magnitudes (0f..1f). Both the real and simulated sources emit this same type.

**`VisualizerAudioProcessor`** — A Media3 `AudioProcessor` inserted into ExoPlayer's audio pipeline via `setAudioProcessorsFactory()`. Reads raw PCM samples, performs FFT, and publishes frequency magnitudes via `SharedFlow<VisualizerState>`.

- Has an `isEnabled` flag. When disabled, `queueInput()` passes audio through without FFT work (one boolean check per buffer).
- Enabled only when all three conditions are true: setting is on, player is playing, sheet is expanded. The enable/disable logic lives in the UI layer (composable side-effects), not in `PlaybackService`, since sheet expanded state is local Compose state.
- FFT size: 512 samples (good balance of frequency resolution and latency).
- FFT implementation: Cooley-Tukey radix-2, ~50 lines of Kotlin, no external library.
- 512 FFT bins grouped into logarithmically-spaced bands matching the bar count (adapts to available width, up to 16 bars).
- Magnitudes normalized to 0f..1f with smoothing decay to reduce visual jitter.
- Publishes results every ~16ms (~60fps).
- Provided via Hilt from `SingletonComponent` (not `ServiceComponent`) so both the service and UI layers can access it. A separate Hilt module (e.g. `VisualizerModule`) provides it at singleton scope.
- When no real FFT data is available (e.g. stream format issues, audio pipeline not yet started), the processor itself emits simulated bar values using sine waves at different frequencies with random phase offsets. This is internal to the processor — consumers always see `SharedFlow<VisualizerState>` regardless of source. The processor tracks whether it has received real audio data recently and switches to simulated output after a short timeout (~500ms).

**`AudioVisualizer` composable** — A Compose `Canvas` that renders bars behind the player controls.

## FFT & Audio Processing

- AudioProcessor implements Media3's `AudioProcessor` interface (`@UnstableApi`, already used in the project).
- In `configure()`: reads the input `AudioFormat` to determine channel count and sample rate. Supports mono and stereo PCM (stereo is downmixed to mono before FFT). Returns the input format unchanged (passive tap).
- In `queueInput()`: when enabled, copies PCM samples into a ring buffer, runs FFT every ~16ms, publishes results.
- The 512 FFT bins are grouped into logarithmically-spaced bands (bass gets fewer bins, treble gets more, matching human hearing).
- The processor always groups into 16 bands. The composable decides how many bars to render based on available width: `min(16, (canvasWidth / minBarWidth).toInt())` where `minBarWidth` is ~12dp. If fewer bars are needed, adjacent bands are merged.

## Compose Rendering

- `Canvas` fills parent width, fixed max height ~120dp.
- Each bar drawn as `drawRoundRect` with 2dp rounded top corners.
- Bar width: `(canvasWidth - totalGapWidth) / barCount`, with 2dp gaps.
- Bar height: `magnitude * maxHeight`, animated with `animateFloatAsState` using spring spec.
- Color: accent color from album art at ~12-15% alpha.
- When paused: bars animate down to zero, then stop recomposing.

## Placement & Integration

### Player UI (`Player.kt`)

- In `PortraitExpandedPlayerContent`: wrap `SongInfo` in a `Box`. `AudioVisualizer` is the first child (background), `SongInfo` is the second child (foreground). Both aligned to `BottomCenter`.
- In `LandscapeExpandedPlayerContent`: same `Box` pattern around `SongInfo` in the right column.
- The visualizer sits flush against the bottom edge — no padding or margin.
- Zero layout change to the existing UI. The visualizer is purely an overlay.

### ExoPlayer Setup (`MediaModule.kt`)

- `VisualizerAudioProcessor` is injected into `MediaModule` from `SingletonComponent` (provided by a new `VisualizerModule`).
- Passed to `ExoPlayer.Builder` via `.setAudioProcessorsFactory()`.
- The processor is a passive tap — reads PCM data and passes it through unmodified.

### HomeScreen (`HomeScreen.kt`)

- Collects the visualizer flow from the Hilt-provided `VisualizerAudioProcessor` singleton.
- Passes `VisualizerState` down through `PlayerScaffold` → `ExpandedPlayerContent` → portrait/landscape composables.

### Enable/Disable Logic (UI layer)

- The `isEnabled` flag on `VisualizerAudioProcessor` is controlled from the UI layer via `LaunchedEffect` in `PlayerScaffold` or `ExpandedPlayerContent`.
- It combines three signals: settings preference (from `PreferenceUtil`), player playing state (from `MediaController`), and sheet expanded state (from `scaffoldState.bottomSheetState`).
- All three are available in the composable scope. When any becomes false, `isEnabled` is set to false.

## Lazy Efficiency

The visualizer is designed to have near-zero cost when not in use:

| Condition | Processor FFT | Composable | Cost |
|-----------|--------------|------------|------|
| Setting off | Skipped (boolean check) | Not in composition tree | ~0 |
| Setting on, sheet collapsed | Skipped (boolean check) | Not composed | ~0 |
| Setting on, sheet expanded, paused | Skipped (boolean check) | Bars at zero, idle | ~0 |
| Setting on, sheet expanded, playing | Active | Rendering | Normal |

## Settings

- New `visualizerEnabled` boolean preference in existing preferences.
- Default: `false` (opt-in).
- Exposed as `Flow<Boolean>`.
- Settings UI: toggle row labeled "Audio visualizer" with subtitle "Show frequency bars in the player".

## Files to Create

- `app/src/main/kotlin/me/echeung/moemoekyun/ui/common/AudioVisualizer.kt` — Canvas composable
- `app/src/main/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessor.kt` — AudioProcessor + FFT + simulated fallback
- `app/src/main/kotlin/me/echeung/moemoekyun/di/VisualizerModule.kt` — Hilt module in `SingletonComponent` providing the processor

## Files to Modify

- `app/src/main/kotlin/me/echeung/moemoekyun/di/MediaModule.kt` — inject VisualizerAudioProcessor, wire into ExoPlayer builder via `setAudioProcessorsFactory()`
- `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/Player.kt` — Box wrapper around SongInfo in portrait/landscape, add AudioVisualizer composable, LaunchedEffect for enable/disable logic
- `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/home/HomeScreen.kt` — collect visualizer state, pass down
- `app/src/main/kotlin/me/echeung/moemoekyun/util/PreferenceUtil.kt` — add `visualizerEnabled` boolean preference
- `app/src/main/kotlin/me/echeung/moemoekyun/ui/screen/settings/SettingsScreen.kt` — add toggle row for visualizer
