# Compose RectList Reproducer

Standalone Android sample for investigating:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

The sample contains three cases:

- `Minimal`: upstream-minimal nested `Row`/`BasicText` baseline alignment.
- `Exact stress`: the same alignment tree with tracked nodes and rapid content
  replacement.
- `Stable search`: the reduced lazy-list reproducer.

## Primary Configuration

- Compose BOM: `2026.09.00`
- Expected AndroidX Compose UI: `1.12.1`
- Device: Pixel 5 AVD
- Android: API 37
- Runtime network access: not required

Verify the resolved dependency:

```sh
./gradlew :app:dependencyInsight \
  --dependency androidx.compose.ui:ui \
  --configuration debugRuntimeClasspath
```

## Build and Run

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch the app and select `Stable search`.

## Reduced Reproduction

The `Stable search` case contains the smallest surviving combination found in
this repository:

- A touch-scrolled `LazyColumn`.
- One custom `Layout` per unkeyed lazy-list row.
- The custom layout reads `FirstBaseline` and `LastBaseline` from its anchor.
- The custom layout republishes those alignment lines.
- A nested `Modifier.layout` changes constraints and placement.

The following conditions were removed without preventing the crash:

- `AnimatedContent` and state replacement.
- Heterogeneous row variants and outer sibling text.
- Coordinate reads from `onGloballyPositioned`.
- Semantics and pointer-input modifiers.
- The second overlay marker child.
- Explicit outer `Modifier.alignByBaseline()` on the custom layout.

Manual sequence:

1. Force-stop the application.
2. Launch it and wait for the first frame.
3. Leave `Stable search` selected.
4. Perform repeated fast downward flings through the list.
5. Repeat the sequence at least 20 times.
6. Capture logcat only around a failure.

The expected failure signature is:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

The observed stack enters:

```text
RectManager.recalculateRectIfDirty()
MeasurePassDelegate.markNodeAndSubtreeAsPlaced()
LazyListState.onScroll()
```

Keep the numeric layout-node identifier redacted as `<id>` in shared evidence.

## Comparison Cases

`Minimal` isolates direct baseline alignment without lazy scrolling or custom
alignment-line propagation.

`Exact stress` adds tracked nodes and rapid replacement to the upstream tree.

## Scope

This repository contains only public AndroidX Compose APIs. It has no
application-specific code, private dependencies, network endpoints, analytics,
or production data.
