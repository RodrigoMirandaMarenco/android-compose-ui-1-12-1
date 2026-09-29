# Compose RectList Reproducer

Standalone Android sample for investigating:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

The app contains one focused reproducer. It opens directly on a lazy result.

## Primary Configuration

- Compose BOM: `2026.09.00`
- Expected AndroidX Compose UI: `1.12.1`
- Device: Pixel 5 AVD
- Android: API 37
- Runtime network access: not required

Verify the resolved dependency rather than inferring it from the BOM:

```sh
./gradlew :app:dependencyInsight \
  --dependency androidx.compose.ui:ui \
  --configuration debugRuntimeClasspath
```

The expected resolved artifact is:

```text
androidx.compose.ui:ui-android:1.12.1
```

## Build and Run

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch the app and wait for the result list to appear.

## Reproduction

The single result-list flow contains the smallest surviving combination found
in this repository:

- A touch-scrolled `LazyColumn` with unkeyed rows.
- One custom `Layout` per row.
- `FirstBaseline` and `LastBaseline` reads from the anchor placeable.
- Custom propagation of those alignment lines.
- A nested `Modifier.layout` that changes constraints and placement.

Manual sequence:

1. Install the application.
2. Launch it and wait for the result list.
3. Perform repeated fast downward flings through the list.
4. Repeat the sequence at least 20 times.
5. Capture logcat only around a failure.

The expected failure signature is:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

The observed stack enters:

```text
RectManager.indexInRectList()
RectManager.recalculateRectIfDirty()
MeasurePassDelegate.markNodeAndSubtreeAsPlaced()
LazyListState.onScroll()
```

This was reproduced on a Pixel 5 AVD running Android 17/API 37. Keep the
numeric layout-node identifier redacted as `<id>` in shared evidence.