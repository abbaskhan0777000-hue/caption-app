export interface WordCaption {
  id: string;
  word: string;
  start: number; // in seconds (e.g. 1.24)
  end: number;   // in seconds (e.g. 1.85)
  punctuation?: string;
}

export interface CaptionSegment {
  id: string;
  words: WordCaption[];
  start: number;
  end: number;
  text: string;
}

export type AnimationPreset = 
  | 'karaoke'
  | 'pop'
  | 'bounce'
  | 'tilt-punch'
  | 'slide'
  | 'fade'
  | 'typewriter'
  | 'wave';

export type ActiveWordEffect = 
  | 'color'          // Color switch only
  | 'marker'         // Marker highlighter streak behind active word
  | 'pill'           // Rounded pill badge with distinct background
  | 'glow'           // Intense neon/aurora glow
  | 'scale-pop'      // Punchy scale pop (1.25x - 1.4x)
  | 'gradient'       // Multi-tone gradient fill
  | 'box';           // Full solid badge

export type ShadowStyle = 'none' | 'soft' | '3d-hard' | 'neon-glow';

export interface CustomFontItem {
  name: string;
  label: string;
  category: string;
  isCustom?: boolean;
  fileUrl?: string;
}

export type FontChoice = string;

export interface CaptionStyleConfig {
  fontFamily: FontChoice;
  fontSize: number; // in px for 1080p reference, responsive in preview
  textColor: string;
  highlightColor: string; // Color of active spoken word
  highlightTextColor: string; // Text color inside active word badge/marker
  highlightBgColor: string; // Background of active word marker / pill
  activeWordEffect: ActiveWordEffect;
  activeWordScale: number; // 1.0 to 1.5
  activeWordTilt: number; // -8 to +8 degrees tilt
  letterSpacing: number; // px
  lineHeight: number;
  textTransform: 'uppercase' | 'none' | 'capitalize';
  backgroundColor: string; // Full line background
  backgroundPadding: number;
  backgroundRounded: number;
  strokeColor: string;
  strokeWidth: number; // 0 to 16
  shadowStyle: ShadowStyle;
  shadowColor: string;
  shadowBlur: number;
  shadowOffsetX: number;
  shadowOffsetY: number;
  positionYPercent: number; // 15 to 88
  animationPreset: AnimationPreset;
  wordsPerChunk: number; // 1 to 7 words per line
  glowEffect: boolean;
  glowColor: string;
  gradientPreset?: 'gold' | 'cyber' | 'fire' | 'sunset' | 'none';
}

export interface VideoMetadata {
  name: string;
  duration: number;
  width: number;
  height: number;
  size: number;
  type: string;
}

export interface PresetTheme {
  id: string;
  name: string;
  creator: string;
  description: string;
  thumbnailColor: string;
  badge: string;
  previewWord: string;
  config: Partial<CaptionStyleConfig>;
}

export type ExportResolution = '4k' | '1080p' | '720p' | 'original';
export type ExportFps = 60 | 30 | 24;
export type ExportBitrate = 'ultra' | 'high' | 'balanced' | 'compact';
export type ExportAspectRatio = 'original' | '9:16' | '16:9' | '1:1';
export type ExportSpeed = 'turbo' | 'standard';

export interface ExportSettings {
  resolution: ExportResolution;
  fps: ExportFps;
  bitrate: ExportBitrate;
  aspectRatio: ExportAspectRatio;
  speed?: ExportSpeed; // 'turbo' (2x-3x speed InShot style) vs 'standard'
}
