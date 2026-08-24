import type { Metadata, Viewport } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'CaptionForge — CapCut-Style Animated Captions with Groq Whisper',
  description:
    'Upload videos, transcribe audio in seconds with Groq Whisper Turbo, style animated captions (Karaoke, Pop, Bounce, Slide, Typewriter), and export 1080p video with burned-in subtitles.',
  manifest: '/manifest.json',
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="overflow-x-hidden w-full max-w-[100vw]">
      <body className="bg-background text-foreground antialiased selection:bg-primary-500 selection:text-white min-h-screen w-full max-w-[100vw] overflow-x-hidden">
        {children}
      </body>
    </html>
  );
}
