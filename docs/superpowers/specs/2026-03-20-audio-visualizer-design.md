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
- Enabled only when all three conditions are true: setting is on, player is playing, sheet is expanded.
- FFT size: 512 samples (good balance of frequency resolution and latency).
- FFT implementation: Cooley-Tukey radix-2, ~50 lines of Kotlin, no external library.
- 512 FFT bins grouped into logarithmically-spaced bands matching the bar count (adapts to available width, up to 16 bars).
- Magnitudes normalized to 0f..1f with smoothing decay to reduce visual jitter.
- Publishes results every ~16ms (~60fps).
- Provided as a Hilt singleton so both the ExoPlayer builder and the UI can access it.

**`SimulatedVisualizerSource`** — Fallback that generates synthetic bar values using sine waves at different frequencies with random phase offsets. Produces a convincing visualizer look without actual audio data. Same `VisualizerState` output type. Used when the AudioProcessor can't produce data (e.g. stream format issues).

**`AudioVisualizer` composable** — A Compose `Canvas` that renders bars behind the player controls.

## FFT & Audio Processing

- AudioProcessor implements Media3's `AudioProcessor` interface (`@UnstableApi`, already used in the project).
- In `queueInput()`: when enabled, copies PCM samples into a ring buffer, runs FFT every ~16ms, publishes results.
- The 512 FFT bins are grouped into logarithmically-spaced bands (bass gets fewer bins, treble gets more, matching human hearing).
- Bar count adapts to available width: `min(16, (canvasWidth / minBarWidth).toInt())` where `minBarWidth` is ~12dp. The FFT band grouping adjusts accordingly.

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

- `VisualizerAudioProcessor` is created as a Hilt singleton.
- Passed to `ExoPlayer.Builder` via `.setAudioProcessorsFactory()`.
- The processor is a passive tap — reads PCM data and passes it through unmodified.

### HomeScreen (`HomeScreen.kt`)

- Collects the visualizer flow from the Hilt-provided `VisualizerAudioProcessor` singleton.
- Passes `VisualizerState` down through `PlayerScaffold` → `ExpandedPlayerContent` → portrait/landscape composables.

### PlaybackService

- Observes the settings preference and sheet expanded state.
- Sets `VisualizerAudioProcessor.isEnabled` accordingly.

## Lazy Efficiency

The visualizer is designed to have near-zero cost when not in use:

| Condition | Processor FFT | Composable | Cost |
|-----------|--------------|------------|------|
| Setting off | Skipped (boolean check) | Not in composition tree | ~0 |
| Setting on, sheet collapsed | Skipped (boolean check) | Not composed | ~0 |
| Setting on, sheet expanded, paused | Skipped (no audio flowing) | Bars at zero, idle | ~0 |
| Setting on, sheet expanded, playing | Active | Rendering | Normal |

## Settings

- New `visualizerEnabled` boolean preference in existing preferences.
- Default: `false` (opt-in).
- Exposed as `Flow<Boolean>`.
- Settings UI: toggle row labeled "Audio visualizer" with subtitle "Show frequency bars in the player".

## Files to Create

- `app/src/main/kotlin/me/echeung/moemoekyun/ui/common/AudioVisualizer.kt` — Canvas composable
- `app/src/main/kotlin/me/echeung/moemoekyun/service/VisualizerAudioProcessor.kt` — AudioProcessor + FFT

## Files to Modify

- `MediaModule.kt` — provide VisualizerAudioProcessor, wire into ExoPlayer builder
- `Player.kt` — Box wrapper around SongInfo, add AudioVisualizer composable
- `HomeScreen.kt` — collect visualizer state, pass down
- `PlaybackService.kt` — observe setting + sheet state, set processor enabled flag
- Preferences file — add visualizerEnabled preference
- Settings screen — add toggle row
