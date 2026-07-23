# Quick Http Test

[![Android CI](https://github.com/jpcottin/Quickhttptest/actions/workflows/android-ci.yml/badge.svg?branch=main)](https://github.com/jpcottin/Quickhttptest/actions/workflows/android-ci.yml)

<details>
<summary><b>CI details</b> — emulator matrix, API 30 → 37.1, plus an Android CLI leg</summary>

| Legs | Image | Emulator channel | GPU | Gating |
|---|---|---|---|---|
| API 30, 33, 36 | `google_apis` x86_64 | stable | swiftshader / auto | ✅ blocking |
| API 37.0 | `google_apis_ps16k` (16 KB page size) | stable | lavapipe | non-blocking |
| API 37.0 | `google_apis_ps16k` | canary (`--channel=3`) | lavapipe, auto | non-blocking |
| API 37.1 | `google_apis_ps16k` | canary | lavapipe, auto | non-blocking |
| Android CLI experiment | `google_apis_ps16k` 37.0 | canary | emulator default | non-blocking |

The Android CLI leg drives the whole flow with the [`android` CLI](https://d.android.com/tools/agents/android-cli) (`android sdk install --canary`, `android emulator create/start/stop`) instead of `sdkmanager`/`avdmanager` and the emulator-runner action.

All emulator-runner legs use full diagnostics (`-verbose -show-kernel -debug-metrics -metrics-collection`) and a `cmdline-tools;latest` update so `avdmanager` writes a valid `target=android-37.x` (the runner's preinstalled version writes `android-0`, which the emulator clamps to API 3, disabling the Vulkan/GLDirectMem auto-enable the ps16k images need).

</details>

A simple Android application to test HTTP connections, either to a local development server or a remote URL. It can perform a specified number of consecutive requests and display the read payload and total elapsed time.

## Features
- Test connectivity to distant or local URLs.
- Configurable number of request loops.
- Configurable stream reading buffer size (from 1024 to 1048576 bytes).
- Displays HTTP response payload chunks directly in the app.
- Measures the total execution time for all requests.
- Built with Jetpack Compose.

## How to use
1. Launch the app on an emulator or device.
2. Select **Distant URL** or **Local URL**.
3. Enter the number of loops.
4. Tap **Start Test** and see the logs populate.

## Development

- **Unit Tests**: Check `HttpTestTest.kt` for core logic tests.
- **UI Tests**: Check `MainScreenTest.kt` for Compose UI tests.
