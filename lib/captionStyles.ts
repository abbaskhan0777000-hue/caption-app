import { CaptionStyleConfig, PresetTheme, CustomFontItem } from './types';

export const INITIAL_FONTS: CustomFontItem[] = [
  { name: 'Montserrat', label: 'Montserrat (CapCut Standard)', category: 'CapCut Classic' },
  { name: 'Anton', label: 'Anton (Hormozi Impact)', category: 'High Energy' },
  { name: 'Archivo Black', label: 'Archivo Black (MrBeast Heavy)', category: 'High Energy' },
  { name: 'Bebas Neue', label: 'Bebas Neue (Tall Poster)', category: 'Bold Display' },
  { name: 'Poppins', label: 'Poppins (Ali Abdaal Clean)', category: 'Creator Clean' },
  { name: 'Inter', label: 'Inter (Sleek Minimal)', category: 'Creator Clean' },
  { name: 'Fredoka', label: 'Fredoka (Playful Bubble)', category: 'Playful & Fun' },
  { name: 'Righteous', label: 'Righteous (Cyberpunk Retro)', category: 'Display / Sci-Fi' },
  { name: 'Cinzel', label: 'Cinzel (Iman Gadzhi Luxury)', category: 'Cinematic / Luxury' },
];

/**
 * Dynamically loads any Google Font by name and injects it into the DOM
 */
export function loadGoogleFontDynamically(fontName: string): Promise<boolean> {
  return new Promise((resolve) => {
    try {
      const formatted = fontName.trim().replace(/\s+/g, '+');
      const linkId = `gfont-${formatted.toLowerCase()}`;
      
      if (document.getElementById(linkId)) {
        resolve(true);
        return;
      }

      const link = document.createElement('link');
      link.id = linkId;
      link.rel = 'stylesheet';
      link.href = `https://fonts.googleapis.com/css2?family=${formatted}:wght@400;700;800;900&display=swap`;
      
      link.onload = () => resolve(true);
      link.onerror = () => resolve(false);

      document.head.appendChild(link);
    } catch {
      resolve(false);
    }
  });
}

/**
 * Loads a user-uploaded custom font file (.ttf, .otf, .woff, .woff2) into the browser via FontFace API
 */
export async function registerCustomFontFile(file: File): Promise<CustomFontItem> {
  const fontName = file.name.replace(/\.[^/.]+$/, '').replace(/[^a-zA-Z0-9_-]/g, '_');
  const buffer = await file.arrayBuffer();

  const fontFace = new FontFace(fontName, buffer);
  const loadedFace = await fontFace.load();
  document.fonts.add(loadedFace);

  return {
    name: fontName,
    label: `${file.name.replace(/\.[^/.]+$/, '')} (Custom File)`,
    category: 'Custom Uploaded',
    isCustom: true,
  };
}

export const DEFAULT_STYLE: CaptionStyleConfig = {
  fontFamily: 'Montserrat',
  fontSize: 52,
  textColor: '#FFFFFF',
  highlightColor: '#FFE600', // Vibrant Gold Yellow (CapCut #1 default)
  highlightTextColor: '#000000',
  highlightBgColor: '#FFE600',
  activeWordEffect: 'color',
  activeWordScale: 1.2,
  activeWordTilt: 0,
  letterSpacing: 1,
  lineHeight: 1.2,
  textTransform: 'uppercase',
  backgroundColor: 'transparent',
  backgroundPadding: 8,
  backgroundRounded: 8,
  strokeColor: '#000000',
  strokeWidth: 6,
  shadowStyle: 'soft',
  shadowColor: 'rgba(0, 0, 0, 0.85)',
  shadowBlur: 10,
  shadowOffsetX: 0,
  shadowOffsetY: 4,
  positionYPercent: 78,
  animationPreset: 'karaoke',
  wordsPerChunk: 3,
  glowEffect: false,
  glowColor: '#FFE600',
  gradientPreset: 'none',
};

export const STYLE_PRESETS: PresetTheme[] = [
  {
    id: 'capcut-signature-karaoke',
    name: 'CapCut Classic Karaoke',
    creator: 'CapCut Official',
    description: 'The #1 viral short-form caption in the world. Crisp white text, heavy black stroke, and glowing yellow active word.',
    thumbnailColor: '#FFE600',
    badge: '👑 #1 Trending',
    previewWord: 'KARAOKE',
    config: {
      fontFamily: 'Montserrat',
      fontSize: 54,
      textColor: '#FFFFFF',
      highlightColor: '#FFE600',
      activeWordEffect: 'color',
      activeWordScale: 1.15,
      activeWordTilt: 0,
      textTransform: 'uppercase',
      strokeColor: '#000000',
      strokeWidth: 6,
      shadowStyle: 'soft',
      shadowColor: 'rgba(0,0,0,0.85)',
      backgroundColor: 'transparent',
      animationPreset: 'karaoke',
      wordsPerChunk: 3,
      positionYPercent: 78,
    }
  },
  {
    id: 'hormozi-beast-pill',
    name: 'Alex Hormozi Punch',
    creator: 'Alex Hormozi',
    description: '1-2 words rapid-fire impact. Bold Anton typeface with neon green active words and black contrast pill badges.',
    thumbnailColor: '#00FF66',
    badge: '⚡ High Energy',
    previewWord: 'MONEY',
    config: {
      fontFamily: 'Anton',
      fontSize: 60,
      textColor: '#FFFFFF',
      highlightColor: '#00FF66',
      highlightTextColor: '#000000',
      highlightBgColor: '#00FF66',
      activeWordEffect: 'pill',
      activeWordScale: 1.35,
      activeWordTilt: -3,
      textTransform: 'uppercase',
      strokeColor: '#000000',
      strokeWidth: 8,
      shadowStyle: 'soft',
      shadowColor: '#000000',
      backgroundColor: 'rgba(0,0,0,0.7)',
      backgroundPadding: 8,
      backgroundRounded: 6,
      animationPreset: 'pop',
      wordsPerChunk: 2,
      positionYPercent: 75,
    }
  },
  {
    id: 'mrbeast-3d-impact',
    name: 'MrBeast 3D Impact',
    creator: 'MrBeast',
    description: 'Super punchy 3D stacked shadow with ultra-bold Archivo Black and electric cyan highlights.',
    thumbnailColor: '#00F0FF',
    badge: '💥 Viral Pop',
    previewWord: '$1,000,000',
    config: {
      fontFamily: 'Archivo Black',
      fontSize: 52,
      textColor: '#FFFFFF',
      highlightColor: '#00F0FF',
      activeWordEffect: 'scale-pop',
      activeWordScale: 1.3,
      activeWordTilt: 2,
      textTransform: 'uppercase',
      strokeColor: '#000000',
      strokeWidth: 8,
      shadowStyle: '3d-hard',
      shadowColor: '#000000',
      shadowOffsetX: 0,
      shadowOffsetY: 6,
      backgroundColor: 'transparent',
      animationPreset: 'pop',
      wordsPerChunk: 3,
      positionYPercent: 76,
    }
  },
  {
    id: 'ali-abdaal-highlighter',
    name: 'Ali Abdaal Marker',
    creator: 'Ali Abdaal',
    description: 'Aesthetic yellow marker highlighter pen streak behind spoken words on modern Poppins typography.',
    thumbnailColor: '#FFF275',
    badge: '🖍️ Notion Style',
    previewWord: 'Productive',
    config: {
      fontFamily: 'Poppins',
      fontSize: 46,
      textColor: '#FFFFFF',
      highlightColor: '#121212',
      highlightTextColor: '#121212',
      highlightBgColor: '#FFF275',
      activeWordEffect: 'marker',
      activeWordScale: 1.1,
      activeWordTilt: 0,
      textTransform: 'capitalize',
      strokeColor: '#000000',
      strokeWidth: 2,
      shadowStyle: 'soft',
      shadowColor: 'rgba(0,0,0,0.6)',
      backgroundColor: 'rgba(0,0,0,0.4)',
      backgroundPadding: 8,
      backgroundRounded: 8,
      animationPreset: 'karaoke',
      wordsPerChunk: 4,
      positionYPercent: 80,
    }
  },
  {
    id: 'iman-gadzhi-luxury',
    name: 'Iman Gadzhi Luxury',
    creator: 'Iman Gadzhi',
    description: 'High-ticket cinematic look with Champagne Gold Cinzel serif typography, spacious letter tracking, and soft glow.',
    thumbnailColor: '#D4AF37',
    badge: '👑 Luxury',
    previewWord: 'SUCCESS',
    config: {
      fontFamily: 'Cinzel',
      fontSize: 46,
      textColor: '#F5E6CA',
      highlightColor: '#FFD700',
      activeWordEffect: 'glow',
      activeWordScale: 1.12,
      activeWordTilt: 0,
      textTransform: 'uppercase',
      letterSpacing: 2,
      strokeColor: '#000000',
      strokeWidth: 3,
      shadowStyle: 'neon-glow',
      shadowColor: 'rgba(212, 175, 55, 0.6)',
      shadowBlur: 14,
      backgroundColor: 'rgba(15, 12, 5, 0.6)',
      backgroundPadding: 10,
      backgroundRounded: 4,
      animationPreset: 'wave',
      wordsPerChunk: 4,
      positionYPercent: 80,
      glowEffect: true,
      glowColor: '#FFD700',
    }
  },
  {
    id: 'cyber-neon-matrix',
    name: 'Cyberpunk Neon',
    creator: 'Sci-Fi / Gaming',
    description: 'Multi-layer electric cyan and magenta neon glow with Righteous display font.',
    thumbnailColor: '#00F0FF',
    badge: '🔮 Cyber Glow',
    previewWord: 'FUTURE',
    config: {
      fontFamily: 'Righteous',
      fontSize: 48,
      textColor: '#FFFFFF',
      highlightColor: '#00F0FF',
      activeWordEffect: 'glow',
      activeWordScale: 1.2,
      activeWordTilt: 0,
      textTransform: 'uppercase',
      strokeColor: '#090A0F',
      strokeWidth: 4,
      shadowStyle: 'neon-glow',
      shadowColor: '#00F0FF',
      shadowBlur: 18,
      backgroundColor: 'rgba(10, 15, 30, 0.85)',
      backgroundPadding: 10,
      backgroundRounded: 12,
      animationPreset: 'slide',
      wordsPerChunk: 3,
      positionYPercent: 78,
      glowEffect: true,
      glowColor: '#00F0FF',
    }
  },
  {
    id: 'fredoka-bubble-bounce',
    name: 'Playful Bubble Bounce',
    creator: 'Vlog & Lifestyle',
    description: 'Bouncy spring motion with bubble rounded letters and vibrant coral pink word pop.',
    thumbnailColor: '#FF5E7E',
    badge: '🎈 Fun & Playful',
    previewWord: 'Awesome!',
    config: {
      fontFamily: 'Fredoka',
      fontSize: 52,
      textColor: '#FFFFFF',
      highlightColor: '#FF5E7E',
      activeWordEffect: 'scale-pop',
      activeWordScale: 1.25,
      activeWordTilt: 3,
      textTransform: 'capitalize',
      strokeColor: '#1B1E2B',
      strokeWidth: 6,
      shadowStyle: 'soft',
      shadowColor: 'rgba(0,0,0,0.6)',
      backgroundColor: 'transparent',
      animationPreset: 'bounce',
      wordsPerChunk: 3,
      positionYPercent: 76,
    }
  },
  {
    id: 'bebas-streetwear-tilt',
    name: 'Streetwear Slanted',
    creator: 'Hype / Fitness',
    description: 'Aggressive -4° slanted tilt with Bebas Neue tall poster typography and fiery crimson red pop.',
    thumbnailColor: '#FF2E4D',
    badge: '🔥 Street Style',
    previewWord: 'RELENTLESS',
    config: {
      fontFamily: 'Bebas Neue',
      fontSize: 58,
      textColor: '#FFFFFF',
      highlightColor: '#FF2E4D',
      activeWordEffect: 'scale-pop',
      activeWordScale: 1.25,
      activeWordTilt: -4,
      textTransform: 'uppercase',
      strokeColor: '#000000',
      strokeWidth: 7,
      shadowStyle: 'soft',
      shadowColor: 'rgba(0,0,0,0.9)',
      backgroundColor: 'transparent',
      animationPreset: 'pop',
      wordsPerChunk: 3,
      positionYPercent: 77,
    }
  }
];
