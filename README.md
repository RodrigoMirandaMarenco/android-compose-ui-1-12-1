# Compose RectList Reproducer

Standalone Android sample for investigating:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

The sample uses public AndroidX Compose APIs to exercise the alignment-placement
path implicated by the Compose UI `1.12.0` regression. It contains six cases:

- An upstream-minimal nested `Row`/`BasicText` baseline-alignment case.
- The same upstream tree with tracked aligned nodes and 60 Hz size/content
  replacement.
- A frame-driven `AnimatedContent` case that changes a numeric string between
  `1000.00` and `10000.00` while each character participates in baseline
  alignment.
- A targeted case combining `onGloballyPositioned`, a custom layout that reads
  and propagates baselines, `alignByBaseline()`, and rapid `AnimatedContent`
  subtree replacement.
- A `LazyColumn` stress case with a shared `LazyListState`, rapid
  suggestions/loading/results replacement, two independent `AnimatedContent`
  trees, `contentKey = { it.javaClass }`, unkeyed item reuse,
  baseline-aligned custom layouts, and continuous programmatic scrolling.
- A stable generic search flow that loads heterogeneous results once and waits
  for manual fast downward flings.

## Primary configuration

- Compose BOM: `2026.08.00`
- Expected AndroidX Compose UI: `1.12.0`
- Device: Android Emulator
- Android version: Android 15
- API level: 35
- Runtime network access: not required

The dependency graph must be verified rather than inferred from the BOM:

```sh
./gradlew :app:dependencyInsight \
  --dependency androidx.compose.ui:ui \
  --configuration debugRuntimeClasspath
```

No network access or external runtime dependency is required for these cases.
The standalone app intentionally does not include Coil so the Compose runtime
under test is unambiguous.

## Build and run

From a clean checkout with a configured Android SDK:

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch the app on an Android 15 API 35 emulator. The list contains generated
text and a local placeholder image; no network access is required.

## Manual reproduction

1. Force-stop the application.
2. Launch it and wait for the first frame.
3. Select `Minimal` and observe the baseline-aligned content.
4. Select `Exact stress` and leave it running for at least two minutes.
5. Select `Animated text` and leave it running while the value changes length.
6. Select `Positioned overlay` and leave it running through repeated
   suggestions/loading/results replacements.
7. Select `Lazy stress` and leave it running while the list is continuously
   replaced and scrolled.
8. Select `Stable search`, wait for `Results loaded`, then perform repeated fast
   downward flings through the results.
9. Repeat the complete sequence at least 20 times.
10. Capture logcat only around a failure.

The targeted cases intentionally register `onGloballyPositioned`. This matters
because the failing `RectManager` path only applies to nodes tracked in its
`RectList`. The AndroidX regression test also includes a `LazyColumn` scrolling
and reuse stress pattern, which is included here as a separate case.

The shortest reliable trigger in this sample is `Stable search`. It combines
the conditions that matter:

- A `LazyColumn` driven by real touch flings.
- A custom layout that reads `FirstBaseline`/`LastBaseline` and republishes
  them.
- A sibling `Row` child using `alignByBaseline()`.
- A nested custom `Modifier.layout` that changes constraints and placement.
- `onGloballyPositioned`, semantics, and heterogeneous unkeyed rows so nodes
  participate in `RectList` tracking and reuse.

On Compose UI `1.12.0`, the observed failure is:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

The stack traverses `RectManager.recalculateRectIfDirty()` from
`MeasurePassDelegate.markNodeAndSubtreeAsPlaced()` during
`AlignmentLines.recalculate()` while `LazyListState.onScroll()` is processing a
touch gesture. Keep the numeric layout-node identifier redacted in shared logs.

The expected failure signature, if reproduced, is:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

Keep the numeric layout-node identifier redacted as `<id>` in shared evidence.

## Scope

This repository is intended to isolate public Compose behavior. It contains no
proprietary application code, private dependencies, network endpoints,
analytics, crash reporting, or application-specific data.
