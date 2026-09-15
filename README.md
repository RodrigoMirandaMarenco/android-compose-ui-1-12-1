# Compose RectList Reproducer

Standalone Android sample for investigating:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

The sample uses only public AndroidX Compose APIs. It contains a scrolling
`LazyColumn` whose rows use a generic custom `Layout` to measure a text-bearing
anchor, read and propagate `FirstBaseline` and `LastBaseline`, and place a
second marker child relative to the anchor.

## Primary configuration

- Compose BOM: `2026.09.00`
- Expected AndroidX Compose UI: `1.12.1`
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

The initial sample intentionally does not include image loading or other
external runtime dependencies.

## Build and run

From a clean checkout with a configured Android SDK:

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch the app on an Android 15 API 35 emulator. The list contains generated
text-only content and does not require network access.

## Manual reproduction

1. Force-stop the application.
2. Launch it and wait for the first frame.
3. Perform five slow upward scrolls.
4. Perform five fast flings from the middle of the list.
5. Scroll back through the list and repeat the sequence.
6. Repeat the complete sequence at least 20 times.
7. Capture logcat only around a failure.

The expected failure signature, if reproduced, is:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

Keep the numeric layout-node identifier redacted as `<id>` in shared evidence.

## Scope

This repository is intended to isolate public Compose behavior. It contains no
proprietary application code, private dependencies, network endpoints,
analytics, crash reporting, or application-specific data.
