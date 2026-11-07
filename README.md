# BLE Beacon Detection & Distance Estimation (Java / Android)

An Android application developed in Java for detecting nearby Bluetooth Low Energy (BLE) advertisers and estimating their distance using received signal strength (RSSI) and advertised transmission power (TxPower).

**This project is a reconstruction of a 2023 university Android prototype**, based on the author's original technical report. The original source code was unavailable, so the application was reimplemented from scratch.

**Build status:** Successfully built using Gradle 8.7, Android Gradle Plugin 8.5.2, and JDK 17. BLE scanning functionality has not yet been tested on a physical Android device or with a physical BLE beacon.

## Features

- Scan for nearby Bluetooth Low Energy (BLE) advertisers.
- Display detected device addresses and RSSI values.
- Read advertised TxPower when available.
- Estimate approximate distance using an RSSI-based path-loss model.
- Start and stop BLE scanning.
- Handle missing Bluetooth permissions and disabled Bluetooth.

## Distance Estimation

The application uses a simplified logarithmic path-loss model:

\[
d = 10^{\frac{TxPower - RSSI}{10 \times n}}
\]

Where:

- **d**: Estimated distance in meters.
- **TxPower**: Reference transmission power value in dBm, when available.
- **RSSI**: Received Signal Strength Indicator in dBm.
- **n**: Path-loss exponent, assumed to be 2.

This model provides an approximate distance estimate rather than an accurate physical measurement.

Actual results depend on environmental conditions, signal interference, obstacles, and device characteristics.

If TxPower is unavailable, the application cannot calculate a distance estimate using this model.

## Technologies

- **Language:** Java
- **Platform:** Android
- **API:** Android Bluetooth Low Energy (BLE)
- **Build system:** Gradle 8.7
- **Android Gradle Plugin:** 8.5.2
- **JDK:** 17
- **Compile SDK:** Android API 35
- **Minimum SDK:** Android API 23

## Build Instructions

### Prerequisites

- JDK 17
- Android SDK Platform 35
- Android SDK Build Tools
- Gradle Wrapper (included in the repository)

### Build from the command line

Clone the repository and navigate to its directory.

Configure the Android SDK location using the `ANDROID_HOME` environment variable or a local `local.properties` file.

Example `local.properties`:

```properties
sdk.dir=/path/to/your/Android/Sdk
```

Then run:

```bash
./gradlew assembleDebug
```

The project has successfully completed this build command on Linux.

The generated debug APK can be found at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Running the Application

1. Open the project in Android Studio.
2. Allow Gradle to synchronize the project.
3. Connect an Android device with Bluetooth Low Energy support.
4. Enable Bluetooth and grant the required permissions.
5. Launch the application.
6. Tap **SEARCH BEACON** to start scanning.
7. Tap **STOP CALCULATING** to stop scanning.

On Android 6–11, Location Services may also need to be enabled for BLE scanning.

**Note:** These are the intended usage steps. The application's runtime behavior has not yet been verified on physical hardware.

## Current Limitations

- The scanner detects BLE advertisers, not exclusively iBeacon devices.
- iBeacon manufacturer-data filtering has not been implemented.
- Many BLE devices do not advertise TxPower, making distance estimation unavailable.
- The distance calculation uses a fixed path-loss exponent of `n = 2`.
- RSSI readings fluctuate and can be affected by walls, obstacles, interference, and device orientation.
- Distance estimates are approximate and should not be used for safety-critical navigation.
- Audio guidance, route guidance, and tunnel deployment are not implemented.
- The project builds successfully but has not yet been tested on a physical Android device or BLE beacon.

## Project Background

The original project was developed in 2023 as part of a university project investigating navigation with the assistance of Bluetooth beacons.

Its technical report, titled *Πλοήγηση με τη βοήθεια beacons* (Navigation with the Help of Beacons), documented the original concept and experimental work.

Because the original source code was no longer available, this repository contains a new implementation based on that report.

The purpose of this reconstruction is to preserve and demonstrate the underlying Android, Java, Bluetooth Low Energy, and signal-processing concepts.

The BLE hardware used in the original experiments belonged to the university laboratory and is not included in this repository.

## Repository Notes

The following files are intentionally excluded from version control:

- Local Android SDK configuration (`local.properties`)
- Gradle caches and build outputs
- Generated APK and AAB files
- IDE-specific configuration
- Credentials, signing keys, and private laboratory files

## Future Improvements

- Test BLE scanning on physical Android devices.
- Validate distance estimation using real BLE beacons.
- Implement iBeacon-specific manufacturer-data parsing.
- Improve error handling and runtime permission management.
- Add automated tests.
- Investigate RSSI filtering to reduce fluctuations.
- Improve the user interface and device information display.

## Attribution

Reconstructed from the author's 2023 university technical report, *Πλοήγηση με τη βοήθεια beacons*.

The repository contains newly written source code and does not claim to reproduce or recover the original implementation.