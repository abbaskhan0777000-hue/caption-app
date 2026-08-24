import { WordCaption, CaptionStyleConfig } from './types';

/**
 * Converts a standard RGB/RGBA or Hex color string into ASS subtitle color format:
 * &HAABBGGRR& (Note: ASS uses BGR order and 00=opaque, FF=transparent alpha)
 */
export function hexToAssColor(colorStr: string, defaultAlpha = '00'): string {
  if (!colorStr || colorStr === 'transparent') {
    return '&HFF000000&'; // fully transparent
  }

  // Handle rgba(r, g, b, a)
  const rgbaMatch = colorStr.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/i);
  if (rgbaMatch) {
    const r = parseInt(rgbaMatch[1], 10);
    const g = parseInt(rgbaMatch[2], 10);
    const b = parseInt(rgbaMatch[3], 10);
    const aVal = rgbaMatch[4] !== undefined ? parseFloat(rgbaMatch[4]) : 1;
    
    // In ASS, 00 is fully opaque, FF is fully transparent
    const assAlpha = Math.round((1 - aVal) * 255).toString(16).padStart(2, '0').toUpperCase();
    const bb = b.toString(16).padStart(2, '0').toUpperCase();
    const gg = g.toString(16).padStart(2, '0').toUpperCase();
    const rr = r.toString(16).padStart(2, '0').toUpperCase();

    return `&H${assAlpha}${bb}${gg}${rr}&`;
  }

  // Handle Hex #RGB, #RRGGBB, #RRGGBBAA
  let hex = colorStr.replace('#', '').trim();
  if (hex.length === 3) {
    hex = hex.split('').map(c => c + c).join('');
  }

  if (hex.length === 6) {
    const rr = hex.substring(0, 2).toUpperCase();
    const gg = hex.substring(2, 4).toUpperCase();
    const bb = hex.substring(4, 6).toUpperCase();
    return `&H${defaultAlpha}${bb}${gg}${rr}&`;
  }

  if (hex.length === 8) {
    const rr = hex.substring(0, 2).toUpperCase();
    const gg = hex.substring(2, 4).toUpperCase();
    const bb = hex.substring(4, 6).toUpperCase();
    const hexAlpha = parseInt(hex.substring(6, 8), 16);
    const assAlpha = (255 - hexAlpha).toString(16).padStart(2, '0').toUpperCase();
    return `&H${assAlpha}${bb}${gg}${rr}&`;
  }

  return '&H00FFFFFF&';
}

/**
 * Formats seconds into ASS timestamp format: H:MM:SS.cs (e.g. 0:01:23.45)
 */
export function formatAssTime(seconds: number): string {
  const totalCs = Math.max(0, Math.round(seconds * 100));
  const cs = totalCs % 100;
  const totalSec = Math.floor(totalCs / 100);
  const s = totalSec % 60;
  const totalMin = Math.floor(totalSec / 60);
  const m = totalMin % 60;
  const h = Math.floor(totalMin / 60);

  const csStr = cs.toString().padStart(2, '0');
  const sStr = s.toString().padStart(2, '0');
  const mStr = m.toString().padStart(2, '0');

  return `${h}:${mStr}:${sStr}.${csStr}`;
}

/**
 * Groups raw word timestamps into readable chunks (e.g. 3-5 words or on pauses)
 */
export interface CaptionChunk {
  words: WordCaption[];
  start: number;
  end: number;
}

export function chunkWords(words: WordCaption[], wordsPerChunk = 4, maxPauseSec = 0.6): CaptionChunk[] {
  if (!words || words.length === 0) return [];

  const chunks: CaptionChunk[] = [];
  let currentWords: WordCaption[] = [];

  for (let i = 0; i < words.length; i++) {
    const word = words[i];
    const prevWord = currentWords[currentWords.length - 1];

    // Check if we should break chunk: word count limit or significant silence pause
    const isPaused = prevWord && (word.start - prevWord.end > maxPauseSec);
    const isCountLimit = currentWords.length >= wordsPerChunk;

    if (currentWords.length > 0 && (isPaused || isCountLimit)) {
      chunks.push({
        words: [...currentWords],
        start: currentWords[0].start,
        end: currentWords[currentWords.length - 1].end,
      });
      currentWords = [];
    }

    currentWords.push(word);
  }

  if (currentWords.length > 0) {
    chunks.push({
      words: [...currentWords],
      start: currentWords[0].start,
      end: currentWords[currentWords.length - 1].end,
    });
  }

  return chunks;
}

/**
 * Generates a complete .ASS (Advanced SubStation Alpha v4.00+) subtitle file
 * matching the exact preview styles & CapCut animations.
 */
export function generateAssSubtitles(
  words: WordCaption[],
  style: CaptionStyleConfig,
  videoWidth = 1920,
  videoHeight = 1080
): string {
  const primaryColor = hexToAssColor(style.textColor);
  const secondaryColor = hexToAssColor(style.highlightColor); // Used for Karaoke \k highlight
  const outlineColor = hexToAssColor(style.strokeColor);
  const shadowColor = hexToAssColor(style.shadowColor);
  
  // Calculate margins based on position percent
  // 1080p reference:
  const posY = Math.round((style.positionYPercent / 100) * videoHeight);
  const marginV = Math.round(videoHeight - posY);

  // Border style: 1 = outline + drop shadow, 3 = opaque box
  const hasBgBox = style.backgroundColor && style.backgroundColor !== 'transparent';
  const borderStyle = hasBgBox ? 3 : 1;
  const backColor = hasBgBox ? hexToAssColor(style.backgroundColor) : shadowColor;

  const fontScale = Math.round(style.fontSize * 1.5); // Scale for 1080p display
  const outlineWidth = style.strokeWidth ? Math.round(style.strokeWidth * 1.2) : 0;
  const shadowDepth = style.hasShadow ? 3 : 0;

  const chunks = chunkWords(words, style.wordsPerChunk);

  let assHeader = `[Script Info]
; Script generated by CaptionForge
Title: CaptionForge Subtitles
ScriptType: v4.00+
PlayResX: ${videoWidth}
PlayResY: ${videoHeight}
WrapStyle: 0
ScaledBorderAndShadow: yes

[V4+ Styles]
Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
Style: CaptionDefault,${style.fontFamily},${fontScale},${primaryColor},${secondaryColor},${outlineColor},${backColor},1,0,0,0,100,100,${style.letterSpacing},0,${borderStyle},${outlineWidth},${shadowDepth},2,30,30,${marginV},1

[Events]
Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
`;

  let events = '';

  for (const chunk of chunks) {
    const startTime = formatAssTime(chunk.start);
    const endTime = formatAssTime(chunk.end + 0.15); // slight padding for smooth fade

    let lineText = '';

    if (style.animationPreset === 'karaoke') {
      // Per-word karaoke \k tags (duration in centiseconds)
      for (const w of chunk.words) {
        const durationCs = Math.max(5, Math.round((w.end - w.start) * 100));
        let displayWord = w.word;
        if (style.textTransform === 'uppercase') displayWord = displayWord.toUpperCase();
        lineText += `{\\k${durationCs}}${displayWord} `;
      }
      lineText = lineText.trim();
    } else if (style.animationPreset === 'pop') {
      // Pop scale effect
      for (const w of chunk.words) {
        let displayWord = w.word;
        if (style.textTransform === 'uppercase') displayWord = displayWord.toUpperCase();
        lineText += `{\\t(0,120,\\fscx118\\fscy118)\\t(120,250,\\fscx100\\fscy100)}${displayWord} `;
      }
      lineText = lineText.trim();
    } else if (style.animationPreset === 'bounce') {
      // Spring bounce effect
      for (const w of chunk.words) {
        let displayWord = w.word;
        if (style.textTransform === 'uppercase') displayWord = displayWord.toUpperCase();
        lineText += `{\\t(0,150,\\fscx122\\fscy122)\\t(150,300,\\fscx100\\fscy100)}${displayWord} `;
      }
      lineText = lineText.trim();
    } else if (style.animationPreset === 'fade') {
      // Smooth fade in & out
      let text = chunk.words.map(w => w.word).join(' ');
      if (style.textTransform === 'uppercase') text = text.toUpperCase();
      lineText = `{\\fad(120,120)}${text}`;
    } else if (style.animationPreset === 'typewriter') {
      let text = chunk.words.map(w => w.word).join(' ');
      if (style.textTransform === 'uppercase') text = text.toUpperCase();
      lineText = `{\\fad(60,60)}${text}`;
    } else {
      // Default / Wave / Clean
      let text = chunk.words.map(w => w.word).join(' ');
      if (style.textTransform === 'uppercase') text = text.toUpperCase();
      lineText = text;
    }

    events += `Dialogue: 0,${startTime},${endTime},CaptionDefault,,0,0,0,,${lineText}\n`;
  }

  return assHeader + events;
}

/**
 * Generates standard SRT subtitle file
 */
export function generateSrtSubtitles(words: WordCaption[], wordsPerChunk = 4): string {
  const chunks = chunkWords(words, wordsPerChunk);
  let srt = '';

  chunks.forEach((chunk, index) => {
    const start = formatSrtTime(chunk.start);
    const end = formatSrtTime(chunk.end);
    const text = chunk.words.map(w => w.word).join(' ');

    srt += `${index + 1}\n${start} --> ${end}\n${text}\n\n`;
  });

  return srt;
}

/**
 * Generates standard WebVTT subtitle file
 */
export function generateVttSubtitles(words: WordCaption[], wordsPerChunk = 4): string {
  const chunks = chunkWords(words, wordsPerChunk);
  let vtt = 'WEBVTT\n\n';

  chunks.forEach((chunk, index) => {
    const start = formatVttTime(chunk.start);
    const end = formatVttTime(chunk.end);
    const text = chunk.words.map(w => w.word).join(' ');

    vtt += `${index + 1}\n${start} --> ${end}\n${text}\n\n`;
  });

  return vtt;
}

function formatSrtTime(seconds: number): string {
  const totalMs = Math.max(0, Math.round(seconds * 1000));
  const ms = totalMs % 1000;
  const totalSec = Math.floor(totalMs / 1000);
  const s = totalSec % 60;
  const totalMin = Math.floor(totalSec / 60);
  const m = totalMin % 60;
  const h = Math.floor(totalMin / 60);

  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')},${ms.toString().padStart(3, '0')}`;
}

function formatVttTime(seconds: number): string {
  const totalMs = Math.max(0, Math.round(seconds * 1000));
  const ms = totalMs % 1000;
  const totalSec = Math.floor(totalMs / 1000);
  const s = totalSec % 60;
  const totalMin = Math.floor(totalSec / 60);
  const m = totalMin % 60;
  const h = Math.floor(totalMin / 60);

  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}.${ms.toString().padStart(3, '0')}`;
}
