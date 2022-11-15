# Android BLE Framework for Peripheral Plugins

This repo contains the common Android BLE framework (shared library) that abstracts away the various complexities of interacting with the `android.bluetooth` APIs directly.

Information on the public APIs can be found documented inline in source, but a more comprehensive write-up is available in the form of the "Peripheral Framework and Interface Design" document.

## Publishing to Nexus

```shell
./gradlew -Pversion={{VERSION_NUMBER_HERE}} publishAllPublicationsToSnapshotsRepository
```

## Framework Use

### Requirements

Pursuant to Google Play's latest [guidance on the target API level requirement](https://developer.android.com/google/play/requirements/target-sdk), the framework has the following requirements:

* `targetSdkVersion`: `31` (Android 12).
* `compileSdkVersion`: `31` (Android 12).

### Adding as a Gradle Dependency

Under the project's `repositories` declaration in `build.gradle`, make sure the following is added:

```
maven {
    url "https://nexus.snap40.com/repository/maven-public/"
    credentials {
        username System.getenv('nexus_username') ?: "$nexus_username"
        password System.getenv('nexus_password') ?: "$nexus_password"
    }
}
```

Under your module's `dependencies`, add the following (replacing the `0.1.0-SNAPSHOT` with whatever the latest version is):

```
implementation "PeripheralFramework:peripheralframework:0.1.0-SNAPSHOT"
```

### API Usage Notes

Before instantiating either framework class that accesses Android's BLE API (i.e., `ScanManager` or `BluetoothManager`), the following needs to be called with a `Context` instance as an argument — the provided `Context` is typically an `Activity` or `Application` subclass.

```
PeripheralFramework.initialize(this)
```

This passes a copy of the context into the framework which the framework will use for creating instances of classes that access the BLE API (BluetoothPeripheral)

```
ActivityCompat.requestPermissions(
    this,
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
    requestCode
)
```
    
This handles requesting Android for the location permissions necessary for accessing BLE. Accessing BLE while not having been granted the correct permissions will cause the app to crash. `Activity.onRequestPermissionsResult` can be overridden to handle the result of the permission request in app. handler may look something like:

```
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    val resultsDescriptions = grantResults.map {
        when (it) {
            PackageManager.PERMISSION_DENIED -> "Denied"
            PackageManager.PERMISSION_GRANTED -> "Granted"
            else -> "Unknown"
        }
    }
    println("Permissions ${permissions.toList()}, grant results $resultsDescriptions")

    val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
        it.second == PackageManager.PERMISSION_DENIED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(this, it.first)
    }
    val containsDenial = grantResults.any { it == PackageManager.PERMISSION_DENIED }
    val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

    /* You may start making PeripheralFramework calls here, or anytime after this 
    point has been reached and permission has been granted */
}
```

## Logging

Optionally, `PeripheralFramework.logger = LogcatDefaultLogger()` can be called at any point to enable log output from the Peripheral Framework.
