

## Project Overview

Build a web application called **CaptionForge** that lets a user upload a video, automatically transcribes the speech with word-level timestamps, and lets them style the resulting captions with a library of fonts, colors, and animated presets (Pop, Bounce, Slide-in, Typewriter, Wave, Karaoke word-highlight) — similar to CapCut's caption feature. The user previews the styled captions live over their video, then exports a final rendered MP4 at 1080p, 30fps, with captions burned in.

## Tech Stack

- **Framework:** Next.js 14+ (App Router, TypeScript)
- **Styling:** Tailwind CSS
- **Transcription:** Groq API, `whisper-large-v3-turbo` model, via the `groq-sdk` npm package. Use `response_format: "verbose_json"` and `timestamp_granularities: ["word"]` to get per-word timestamps.
- **Video rendering/export:** FFmpeg, invoked server-side via `fluent-ffmpeg` (or direct `child_process` calls to a static ffmpeg binary via `ffmpeg-static`)
- **Video upload/storage:** Store uploaded videos temporarily on local disk (`/tmp` or a `storage/` dir) for the MVP — no cloud storage needed yet
- **State management:** React state/hooks; no external state library needed
- **Fonts:** Google Fonts, self-hosted or loaded via `next/font`

## Environment Variables

```
GROQ_API_KEY=your_key_here
```

## Core Features

### 1. Video Upload
- Drag-and-drop or file-picker upload (accept `.mp4`, `.mov`, `.webm`)
- Show upload progress
- After upload, load the video into an HTML5 `<video>` element for playback/preview
- Extract and store video duration, resolution, and fps on upload

### 2. Automatic Transcription
- On upload, extract the audio track server-side with FFmpeg (convert to 16kHz mono WAV — required for best Whisper accuracy)
- Send the audio to Groq's `/openai/v1/audio/transcriptions` endpoint using `whisper-large-v3-turbo`
- Request `timestamp_granularities: ["word"]` so every word has a `start` and `end` time
- Store the transcription result as an array of `{ word: string, start: number, end: number }` objects tied to the video
- Show a loading/processing state during transcription (this should take just a few seconds for typical clip lengths thanks to Groq's speed)
- After transcription completes, display the full transcript in an editable text panel — let the user click any word to fix misheard text (this is essential; ASR is never 100% accurate). Editing text should NOT require the user to manually re-time anything — keep the original timestamp, just replace the word string.

### 3. Caption Style Engine
Build a caption styling panel with the following, matching CapCut's flexibility:

**Fonts:** offer at least 8–10 Google Fonts across categories:
- Bold/Impact style (e.g., Anton, Bebas Neue, Archivo Black)
- Clean sans-serif (e.g., Inter, Montserrat, Poppins)
- Playful/rounded (e.g., Baloo 2, Fredoka)

**Text styling controls:**
- Font size, color, letter spacing, line height
- Text background (solid box, semi-transparent box, or none)
- Outline/stroke width and color
- Drop shadow toggle
- Text position on screen (draggable, or preset top/center/bottom)
- Uppercase toggle

**Animation presets** (apply per-word or per-line):
- **Pop** — word scales up briefly as it's spoken
- **Bounce** — word bounces in on a spring easing
- **Slide-in** — word slides in from a direction
- **Fade** — simple opacity fade in/out
- **Typewriter** — characters appear sequentially
- **Wave** — letters ripple up and down
- **Karaoke highlight** — the whole line is visible at once, but the currently-spoken word is highlighted in a different color/background as playback reaches it (this is the single most-used CapCut caption style — prioritize getting this one right)

**Presets:** bundle 5–6 combinations of the above (font + color + animation) as one-click "Style Presets" the user can pick, same as CapCut's top preset bar, then still customize further.

### 4. Live Preview
- Overlay the styled captions directly on top of the `<video>` element during playback, positioned/timed using the word timestamps
- Captions must stay in sync as the user scrubs/plays/pauses the video
- Use `requestAnimationFrame` synced to `video.currentTime` to trigger word-level animation timing accurately — do not rely on `setInterval`, which drifts

### 5. Export (1080p, 30fps, captions burned in)
This is the heaviest part — implement server-side:

- Generate an **ASS (Advanced SubStation Alpha) subtitle file** from the styled transcript. ASS format supports per-word karaoke timing (`\k` tags), font, color, position, and basic animation tags (`\fad`, `\move`, `\t` for scale/transforms) — this is the correct tool for reproducing CapCut-style animated captions, far more reliable than trying to hand-roll animations with FFmpeg `drawtext`.
- Map each caption style/animation choice in the UI to the corresponding ASS style + override tags.
- Run FFmpeg server-side with the `ass` filter (`-vf "ass=captions.ass"`) to burn the subtitles into the video.
- Output settings: `-s 1920x1080 -r 30 -c:v libx264 -preset medium -crf 18 -c:a aac`. If the source video isn't 1080p, scale it up/down to 1920x1080 (pad or crop to preserve aspect ratio — ask the user whether to letterbox or crop-fill if the source aspect ratio doesn't match 16:9 or 9:16).
- Show export progress (FFmpeg outputs progress info to stderr — parse it and pipe progress % to the frontend via a simple polling endpoint or Server-Sent Events)
- On completion, provide a download link for the final MP4

## Suggested Project Structure

```
/app
  /page.tsx                  → upload + main editor UI
  /api/upload/route.ts       → handles video upload, saves to storage
  /api/transcribe/route.ts   → extracts audio, calls Groq Whisper, returns word timestamps
  /api/export/route.ts       → generates ASS file, runs FFmpeg burn-in, returns job id
  /api/export/status/route.ts → polling endpoint for export progress
/components
  /VideoUploader.tsx
  /TranscriptEditor.tsx
  /CaptionStylePanel.tsx
  /CaptionPreviewOverlay.tsx
  /ExportPanel.tsx
/lib
  /groq.ts                   → Groq client + transcription helper
  /ffmpeg.ts                 → audio extraction + export helpers
  /assGenerator.ts           → converts style config + word timestamps → .ass file content
  /captionStyles.ts          → font/animation preset definitions shared between preview (CSS) and export (ASS tags)
/storage                     → temp video/audio/export files (gitignored)
```

## Important Implementation Notes

1. **Keep one source of truth for styles.** Define caption styles/animations as a single config object (`captionStyles.ts`) and write two renderers off of it: one that outputs CSS/JS for the live browser preview, one that outputs ASS tags for the FFmpeg export. This guarantees the exported video actually matches what the user previewed — the most common bug in apps like this is preview/export mismatch.
2. **Groq free tier limits:** 20 requests/minute, 2,000 requests/day, 28,800 audio seconds/day. Add basic error handling for HTTP 429 (rate limit) with a clear message to the user, and consider client-side debouncing so re-transcription isn't triggered accidentally.
3. **Long videos:** Groq recommends chunking audio into segments (~60s) for longer files. Implement chunked transcription with sequential timestamp offsetting for videos longer than a few minutes.
4. **File cleanup:** Since videos are stored temporarily on disk, add a cleanup job (cron or on-access TTL check) to delete files older than X hours.
5. **FFmpeg availability:** Confirm `ffmpeg-static` or system FFmpeg is available in whatever environment this gets deployed to — this is a common deployment gotcha (e.g., doesn't work out of the box on Vercel serverless; needs a Node server or container with FFmpeg installed).

## MVP Scope (build in this order)

1. Video upload + playback
2. Groq transcription integration + editable transcript panel
3. Karaoke-highlight style only (get one animation fully working end-to-end: preview + export) before building out the rest
4. Add remaining animation presets (Pop, Bounce, Slide, Fade, Typewriter, Wave)
5. Style presets bar
6. Full export pipeline with progress tracking and download

---

Deploy target: a Node.js server/container environment (not serverless-only) since FFmpeg processing needs to run for potentially long durations without execution time limits.
