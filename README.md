# CaptionForge 🎬⚡

**CaptionForge** is a web application that lets you upload videos, automatically transcribes speech with word-level timestamps using **Groq Whisper Large v3 Turbo**, styles animated captions (Karaoke Word Highlight, Pop, Bounce, Slide-in, Typewriter, Wave), and exports a final rendered 1080p 30fps video with burned-in captions or downloadable subtitle files (`.ass`, `.srt`, `.vtt`).

---

## ✨ Features

- **⚡ Client-Side Zero-Upload Video Engine:** Loads videos of any size (even GBs) instantly in the browser without uploading heavy video files to a server.
- **🎙️ Fast Transcription via Groq:** Browser extracts compact 16kHz WAV audio (<3MB) and transcribes word-by-word with Groq's `whisper-large-v3-turbo` in seconds.
- **🎤 CapCut-Style Karaoke Word Highlight:** Highlights spoken words dynamically in real time with custom colors and backgrounds.
- **💥 Animated Caption Presets:**
  - **Karaoke Highlight** (CapCut style)
  - **Pop-In** (Hormozi style)
  - **Spring Bounce**
  - **Slide-In**
  - **Sequential Typewriter**
  - **Wave Ripple**
  - **Smooth Fade**
- **🎨 Deep Typography & Styling:**
  - 10 Google Fonts across Bold/Impact, Clean Sans, Playful, Display, and Cinematic.
  - Text colors, stroke width & stroke color, background boxes, drop shadow, text transform (UPPERCASE/Capitalize/Normal).
  - Draggable & preset screen placement (Top, Center, Bottom).
- **📝 Interactive Word-by-Word Transcript Editor:**
  - Click any word to jump video playback to that exact timestamp.
  - Edit misheard words without losing timestamp synchronization.
  - Add/delete words and perform global Find & Replace.
- **🎬 High-Bitrate 1080p Burn-In Export:**
  - In-browser rendering pipeline directly onto canvas + audio stream.
  - Direct subtitle file export (`.ass` with karaoke tags, `.srt`, `.vtt`).

---

## 🚀 Getting Started

### 1. Environment Setup
Copy `.env.local.example` to `.env.local`:
```bash
GROQ_API_KEY=your_groq_api_key_here
```
*(You can also set the Groq API key directly in the web app UI).*

### 2. Run the Development Server
```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

---

## 🛠️ Tech Stack
- **Framework:** Next.js 14 (App Router, TypeScript)
- **Styling:** Tailwind CSS + Lucide Icons + Google Fonts
- **Transcription:** Groq SDK (`whisper-large-v3-turbo`)
- **Subtitle Engine:** ASS v4.00+ generator with `\k` karaoke timing tags
- **Video Rendering:** Canvas 2D frame compositor + MediaRecorder 1080p stream
