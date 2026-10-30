# Floating Lock Button (Pixel 6a)

A zero-bloat, ultra-low power native Android utility that adds a floating lock button to your screen. Tapping it instantly turns off and locks your display without needing the physical power button.

---

## 📥 Direct Download

* **Release APK (~26 KB)**: [Download app-release.apk](https://github.com/ramesh-madara/onscreen-home-btn/releases/download/v1.5.0/app-release.apk)
* **GitHub Releases**: [v1.5.0 Release Page](https://github.com/ramesh-madara/onscreen-home-btn/releases/tag/v1.5.0)

---

## ⚡ Zero-Bloat Specifications
* **CPU Consumption**: **0.0%** when idle. Fully event-driven; Android only wakes the touch listener when finger touches the button.
* **Power / Battery Consumption**: Virtually undetectable (**0%**). No background loops, no alarms, no wake-locks, no polling.
* **Biometric Support**: Uses `AccessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)`. Unlike Device Admin (`lockNow()`), this **does not disable fingerprint unlock** on Pixel phones.
* **Pixel Adaptive Vector Icon**: Custom crystal padlock vector icon with sharp keyhole and Material You Dynamic Color themed icon support.
* **Orientation Safe**: Scales proportionally when rotating between portrait and landscape; never disappears or jumps to center.
* **Strict Boundary Clamping**: 100% inside screen bounds at all times; cannot be dragged partially off-screen.
* **Clean Screenshots**: Triggering screenshot hides both the lock button and screenshot button before the image is captured.
* **Dependencies**: **0 external libraries** (no Jetpack, no AndroidX, no Kotlin stdlib).
* **APK Size**: **~26 KB** total.

---

## 📱 Features & Interactions
1. **Instant Screen Lock (Tap)**: Tap the floating button to immediately lock your device.
2. **Volume Slider (Tap & Hold)**: Tap and hold the button to bring up the native Android volume panel.
3. **Transient Screenshot Button (Drag & Drop)**:
   * When you move and release the lock button, a compact 38dp screenshot button appears right above it (or below it if near top border) for **1 second**.
   * Tap it to trigger an immediate native Android screenshot.
   * Both buttons **temporarily disappear during capture**, so neither icon ever shows up in your screenshots.
   * If not tapped, it automatically dismisses after 1 second.
4. **Draggable & Persistent**: Drag it anywhere along the screen edges; position is automatically saved in `SharedPreferences`.
5. **No Special Permissions Needed**: Leverages `AccessibilityService`'s native `TYPE_ACCESSIBILITY_OVERLAY`, so it doesn't even require `SYSTEM_ALERT_WINDOW` ("Display over other apps").

---

## 🚀 Setup on Pixel 6a
1. Download [app-release.apk](https://github.com/ramesh-madara/onscreen-home-btn/releases/download/v1.5.0/app-release.apk) onto your phone and install it.
2. Open **Floating Lock Button**.
3. Tap **Enable in Accessibility Settings**.
4. Turn on **Floating Lock Service**.
5. The lock button immediately appears on your screen. Tap it to lock!

---

## 📁 Project Structure
```
onscreen-home-btn/
├── .github/workflows/
│   └── build.yml                  # GitHub Actions CI build & release workflow
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

