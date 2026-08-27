# CaptionForge Pro 🎬⚡
### AI Auto-Captions, Viral Subtitle Animations & Video Studio for Android

[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?style=flat&logo=android&logoColor=white)](https://github.com/abbaskhan0777000-hue/caption-app)
[![Release](https://img.shields.io/badge/Release-v1.5.0-blue.svg)](https://github.com/abbaskhan0777000-hue/caption-app/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/abbaskhan0777000-hue/caption-app/build-apk.yml?branch=main&label=APK%20Build)](https://github.com/abbaskhan0777000-hue/caption-app/actions)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**CaptionForge Pro** is a mobile video subtitle and caption studio for Android. Powered by **Groq Whisper Large v3 Turbo**, it automatically transcribes speech with millisecond word-level timestamps, styles viral animated captions (Hormozi, MrBeast, TikTok, Ali Abdaal, Cyberpunk), supports two-finger pinch-to-zoom resizing, and burns subtitles directly into videos using **Hardware GPU Acceleration (MediaCodec)** with an InShot-style real-time percentage progress bar.

---

## ✨ Features Overview

### 🎙️ 1. Instant AI Speech-to-Text (Whisper Large v3 Turbo)
- Extracts compact 16kHz audio streams locally in milliseconds.
- Transcribes speech word-by-word with exact start and end timestamps.
- **Zero Lag Sync**: Subtitle word highlighting matches spoken audio down to the exact millisecond.

### 🎨 2. 38 Unique Signature Templates (7 Categories)
- **Legacy (6 Presets):** Classic Karaoke, Navy White Pill, Yellow Highlight Box, Clean Minimal White, Royal Blue Pill, Dark Obsidian Pill.
- **Modern (6 Presets):** Ali Abdaal Aesthetic, Electric Cyan Clean, Velvet Violet, Emerald Growth, Sunset Fade Italic, Glassmorphism Cyan.
- **Viral (6 Presets):** Hormozi Fire (`$100K` yellow/red combo), MrBeast Impact, TikTok 1-Word Punch, Bebas Condensed Hook, Purple Beast Pill, GaryVee Hustle.
- **Bold (5 Presets):** Red Alert Strike, Cinematic Gold, Cyberpunk 2077, Heavyweight Boxer, Electric Voltage.
- **Minimal (5 Presets):** Minimalist Whisper, Lavender Breeze, Nordic Ice, Subtle Black Pill, Monochrome Studio.
- **Cool (6 Presets):** Neon Tokyo, Hot Pink Pop, Ice & Fire, Retro 80s Synthwave, Ocean Wave, Acid Lime Pop.
- **Split View (4 Presets):** Dynamic Dual 2-Line, Cyan Strike 2-Line, Minimal Dark Box 2-Line, Emerald Tech 2-Line.

### 🔤 3. 21 Pre-Built Viral Fonts & Custom `.ttf` / `.otf` Importer
- **21 Creator Fonts:** `Montserrat Black`, `Impact Heavy`, `Bebas Neue Condensed`, `Poppins Bold`, `Inter Medium`, `Anton Bold`, `Cinzel Bold`, `Righteous`, `Orbitron`, `Permanent Marker`, `Syne Heavy`, `Bangers Comic`, and more.
- **Custom Font Importer (`➕ Import Custom Font`):** Import any `.ttf` or `.otf` font file directly from device storage. Custom fonts render identically in both live preview and exported videos.

### 🖐️ 4. CapCut & InShot Touch Gestures
- **Two-Finger Pinch-to-Zoom:** Scale caption font sizes between `12sp` and `80sp` directly on the video canvas.
- **Single-Finger Dragging:** Reposition captions anywhere on the video screen.
- **Visual Bounding Box:** Dashed blue outline provides instant tactile feedback while moving or resizing.

### 💥 5. Dynamic Animation Engine
- **`pop` (Viral Punch Scale):** `1.15x` punch scale on active word onset with smooth settle.
- **`bounce` (Rhythmic Jump):** Rhythmic vertical jump (`-4.5dp`) timed to speaker syllables.
- **`glow` (Pulsing Neon Shadow):** Glowing `8dp` drop-shadow aura in accent colors.
- **`fade` (Transparency Lead):** Upcoming words sit at `60%` opacity while active words illuminate to `100%`.
- **`karaoke` / `clean`:** Millisecond-level color wave transitions.
- **Non-Overlapping Spacing:** Expanded inter-word clearance (`+14dp`) and isolated padding (`hPadX = 6dp`) prevent active-word background boxes from bleeding into neighboring words.

### ⏯️ 6. Video Preview Controls
- **Floating Center Play/Pause Button:** Semi-transparent `▶` overlay appears on pause.
- **Timeline Controls:** Interactive play/pause toggle beside the `00:00 - 00:00` timecode.
- **Tap-to-Toggle:** Single tap anywhere on the video canvas to play or pause.
- **Looping & Replay:** Auto-rewinds to `0:00` with replay prompt on video end.

### ⚡ 7. Hardware GPU Video Burner & InShot Progress Bar
- **Hardware Acceleration:** Encodes overlays with Android `h264_mediacodec` GPU pipeline with OpenH264 / MPEG4 fallbacks.
- **InShot-Style Progress UI:** Real-time **`0%` → `100%`** percentage indicator with frame-by-frame statistics streaming.
- **Direct Gallery Saving:** Saves rendered MP4 files directly to `Movies/CaptionForge/` in the Android Gallery.

---

## 🛠️ Architecture & Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Platform** | Android Native (Java 17, Min SDK 24, Target SDK 34) |
| **Media Player** | AndroidX Media3 ExoPlayer (`1.2.1`) |
| **Video Engine** | FFmpegKit Full GPL (`6.0-2.LTS`) |
| **Transcription AI** | Groq Cloud API (`whisper-large-v3-turbo`) |
| **UI Components** | Google Material Design 3, CardView, ConstraintLayout |
| **CI/CD** | GitHub Actions Automated APK Builder |

---

## 📂 Project Structure

```
caption-app/
├── caption-forge-android-native/
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/captionforge/nativeapp/
│   │   │   │   ├── api/
│   │   │   │   │   └── GroqTranscriber.java       # Whisper AI transcription client
│   │   │   │   ├── audio/
│   │   │   │   │   └── AudioExtractor.java        # 16kHz WAV audio extraction
│   │   │   │   ├── engine/
│   │   │   │   │   ├── AssGenerator.java          # Subtitle chunking & ASS formatting
│   │   │   │   │   ├── FontManager.java           # 21 fonts & custom .ttf/.otf importer
│   │   │   │   │   └── NativeVideoBurner.java     # Hardware GPU FFmpeg video encoder
│   │   │   │   ├── model/
│   │   │   │   │   ├── CaptionStyle.java          # Subtitle styling data model
│   │   │   │   │   └── WordCaption.java           # Word timestamp data model
│   │   │   │   └── ui/
│   │   │   │       ├── CaptionOverlayView.java    # 60 FPS gesture & animation canvas
│   │   │   │       ├── ChipSegmentAdapter.java    # Timeline segment chip adapter
│   │   │   │       ├── EditCaptionsActivity.java  # Word-by-word transcript editor
│   │   │   │       ├── MainActivity.java          # Studio interface & player lifecycle
│   │   │   │       ├── SegmentEditAdapter.java    # Segment list adapter
│   │   │   │       ├── SettingsActivity.java      # API key settings
│   │   │   │       ├── StylesBottomSheet.java     # Full typography & effects editor
│   │   │   │       ├── TemplateCardAdapter.java   # 38 templates categorized adapter
│   │   │   │       └── TemplatesBottomSheet.java  # 7 categories bottom sheet
│   │   │   ├── res/
│   │   │   │   ├── drawable/                      # Vector drawables & background shapes
│   │   │   │   ├── layout/                        # High-contrast UI layouts
│   │   │   │   └── values/                        # Colors, styles, strings
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle                           # Dependencies & NDK configuration
│   ├── gradle/
│   └── build.gradle
└── .github/
    └── workflows/
        └── build-apk.yml                          # Automated GitHub Actions APK build
```

---

## 🚀 Building & Running

### Option 1: Download Pre-Built APK
Download the latest APK artifact directly from the **[GitHub Actions Releases](https://github.com/abbaskhan0777000-hue/caption-app/actions)** tab.

### Option 2: Build with Android Studio
1. Clone the repository:
   ```bash
   git clone https://github.com/abbaskhan0777000-hue/caption-app.git
   ```
2. Open the `caption-forge-android-native` directory in **Android Studio Hedgehog or newer**.
3. Let Gradle sync project dependencies.
4. Connect an Android device (API 24+) or launch an emulator.
5. Click **Run (`Shift + F10`)** or run:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔑 Groq API Setup
To use auto-transcription:
1. Obtain a free API key from [Groq Console](https://console.groq.com/).
2. In the app, open **Settings (`⚙️`)** and paste your API key.
3. Your key is securely stored in encrypted app preferences.

---

## 📄 License
This project is open-source and licensed under the [MIT License](LICENSE).
