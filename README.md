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

- Compose BOM: `2026.08.00`
- Expected AndroidX Compose UI: `1.12.0`
- Coil Compose: `3.6.1`
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

The sample uses Coil only with a local drawable. It does not require network
access. The same `ConstraintsSizeResolver` is used as a modifier on each
custom-layout row and as the size resolver for the Coil image request.

The screen also models a search-result transition: the initial `sample` query
shows suggestion rows, then replaces them with heterogeneous result rows inside
`AnimatedContent`. The result list intentionally uses unkeyed items so content
replacement can recreate the node reuse and placement conditions involved in
the reported failure.

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
3. Confirm the query displays `sample`.
4. While suggestions are visible, wait for the transition to results or tap the header.
5. Perform repeated fast downward flings through the result list.
6. Repeat the query/content transition while the list is moving.
7. Repeat the complete sequence at least 20 times.
8. Capture logcat only around a failure.

The expected failure signature, if reproduced, is:

```text
java.lang.IllegalArgumentException: LayoutNode <id> not found in RectList
```

Keep the numeric layout-node identifier redacted as `<id>` in shared evidence.

## Scope

This repository is intended to isolate public Compose behavior. It contains no
proprietary application code, private dependencies, network endpoints,
analytics, crash reporting, or application-specific data.
