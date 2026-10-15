# Aperture - BlackBerry Edition

A customized, feature-rich camera app built on top of LineageOS Aperture and Android CameraX, redesigned with the iconic **BlackBerry Camera** UI/UX and **Material 3 Expressive** styling.

Specifically optimized and tested for keyboard-centric devices such as the **Zinwa Q25** (square 720×720 display with physical trackball) and the **BlackBerry KEY2** (Android 16 / LineageOS 24).


---

## 📸 Screenshots

### BlackBerry KEY2 (Portrait 3:2)

<p align="center">
  <img src="docs/screenshots/key2_viewfinder_modes.png" width="30%" alt="KEY2 Viewfinder & Mode Drawer" />
  <img src="docs/screenshots/key2_resolution_zsl.png" width="30%" alt="KEY2 Resolutions & Latency / ZSL" />
  <img src="docs/screenshots/key2_live_filters.png" width="30%" alt="KEY2 Live Filters Ribbon" />
</p>

### Zinwa Q25 (Square 1:1 / 720×720)

<p align="center">
  <img src="docs/screenshots/q25_viewfinder_grid.png" width="19%" alt="Q25 Viewfinder with Grid" />
  <img src="docs/screenshots/q25_modes_drawer.png" width="19%" alt="Q25 Mode Drawer" />
  <img src="docs/screenshots/q25_live_filters.png" width="19%" alt="Q25 Live Filters" />
  <img src="docs/screenshots/q25_resolution_latency.png" width="19%" alt="Q25 50MP Resolutions & Latency" />
  <img src="docs/screenshots/q25_grid_leveler.png" width="19%" alt="Q25 Grid & Leveler Menu" />
</p>

---

## ✨ Features

- **BlackBerry Camera Experience**:
  - Top & bottom gradient bars with iconic BlackBerry camera icons and styling.
  - Camera Mode shortcut drawer with flat Monet accents: **Photo**, **Video**, **Pro**, **Portrait**, **Panorama**, and **Document Scanner**.
  - Live Color Filters ribbon with rich thumbnail previews: *Normal*, *Portra 160*, *Ilford B&W*, *Agapantha*, *Infrared*, *Sepia*, *Freeze*, *Negative*, and more.
  - Document scanner mode with real-time green corner alignment brackets.

- **Pro / Manual Controls**:
  - Direct on-screen manual controls with large, touch-friendly dials for:
    - **EV** (Exposure Compensation)
    - **ISO** sensitivity
    - **Shutter Speed**
    - **White Balance (AWB)** presets
    - **Manual Focus (MF)** distance slider

- **Aspect Ratio & Resolution Engine**:
  - Full support for **4:3**, **16:9**, and **1:1 (Square)** aspect ratios.
  - Live preview decoupling from sensor capture resolution: allows square preview on 1:1 displays while capturing at full sensor resolution.
  - On-screen resolution picker for each format.

- **Capture Latency & Zero Shutter Lag**:
  - Two-tiered latency configuration:
    - **Min Latency** (Fast capture pipeline) with optional **ZSL (Zero Shutter Lag)** on/off toggle.
    - **Max Quality** (Multi-frame post-processing and maximum image quality).

- **Hardware Controls & Physical Keyboard Integration**:
  - **Enter Key** (`KEYCODE_ENTER`, `KEYCODE_NUMPAD_ENTER`, `KEYCODE_DPAD_CENTER`) triggers photo capture / video record toggle.
  - **Trackball & D-pad Scroll**: Roll or press Up/Down on the physical trackball or D-pad to smoothly adjust digital/optical zoom.
  - **Dual Vertical Tickers**:
    - Left side: Smooth optical zoom slider with tick marks (activates on scroll, pinch, or tapping zoom indicator).
    - Right side: Exposure / light bias slider (activates upon tap-to-focus).

- **Multilingual Support**:
  - Fully localized in **English**, **Spanish** (*Español*), and **Catalan** (*Català*).

---

## 📱 Supported Devices

- **Zinwa Q25** (LineageOS 24 / Android 15, 720×720 square screen)
- **BlackBerry KEY2** (Android 16 / LineageOS, physical QWERTY keyboard)
- Any modern Android device running Android 14+ (API 34+)

---

## 🚀 Building & Installing

### Build from source
```bash
./gradlew assembleDebug
```

The output APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

Based on [LineageOS Aperture](https://github.com/LineageOS/android_packages_apps_Aperture), licensed under the Apache License, Version 2.0.
