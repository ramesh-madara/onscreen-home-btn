# Floating Lock Button (Pixel 6a)

A zero-bloat, ultra-low power native Android utility that adds a floating lock button to your screen. Tapping it instantly turns off and locks your display without needing the physical power button.

---

## ⚡ Zero-Bloat Specifications
* **CPU Consumption**: **0.0%** when idle. Fully event-driven; Android only wakes the touch listener when finger touches the button.
* **Power / Battery Consumption**: Virtually undetectable (**0%**). No background loops, no alarms, no wake-locks, no polling.
* **Biometric Support**: Uses `AccessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)`. Unlike Device Admin (`lockNow()`), this **does not disable fingerprint unlock** on Pixel phones.
* **Dependencies**: **0 external libraries** (no Jetpack, no AndroidX, no Kotlin stdlib).
* **Estimated APK Size**: **~20 to 30 KB** total.

---

## 📱 Features
1. **Instant Screen Lock**: Tap the floating circle to lock immediately.
2. **Draggable & Persistent**: Drag it anywhere along the screen edges; position is automatically saved in `SharedPreferences`.
3. **No Special Permissions Needed**: Leverages `AccessibilityService`'s native `TYPE_ACCESSIBILITY_OVERLAY`, so it doesn't even require `SYSTEM_ALERT_WINDOW` ("Display over other apps").

---

## 📁 Project Structure
```
onscreen-home-btn/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ramesh/lockbutton/
│       │   ├── FloatingLockService.java   # Native service managing the button
│       │   └── MainActivity.java          # Clean 1-button setup screen
│       └── res/
│           ├── drawable/                  # Vector lock and launcher icons
│           ├── layout/                    # Setup activity layout
│           ├── values/                    # Strings
│           └── xml/                       # Accessibility service config
├── build.gradle
└── settings.gradle
```

