# Quick Http Test

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
