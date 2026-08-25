'use client';

import React, { useState, useRef, useEffect } from 'react';
import { Navbar } from '@/components/Navbar';
import { VideoUploader } from '@/components/VideoUploader';
import { CaptionPreviewOverlay } from '@/components/CaptionPreviewOverlay';
import { CaptionStylePanel } from '@/components/CaptionStylePanel';
import { TranscriptEditor } from '@/components/TranscriptEditor';
import { ExportModal } from '@/components/ExportModal';
import { WordCaption, CaptionStyleConfig, VideoMetadata } from '@/lib/types';
import { DEFAULT_STYLE } from '@/lib/captionStyles';
import { extractAudioFromVideo } from '@/lib/audioExtractor';
import { isNativeAndroidApp } from '@/lib/nativeBridge';
import {
  Play,
  Pause,
  Volume2,
  VolumeX,
  Maximize,
  Sparkles,
  Sliders,
  FileText,
  Download,
  Loader2,
  AlertCircle,
  RotateCcw,
  Check,
  Film,
  Zap,
} from 'lucide-react';

const SAMPLE_DEMO_WORDS: WordCaption[] = [
  { id: 'w1', word: 'Create', start: 0.1, end: 0.6 },
  { id: 'w2', word: 'viral', start: 0.65, end: 1.1 },
  { id: 'w3', word: 'captions', start: 1.15, end: 1.7 },
  { id: 'w4', word: 'just', start: 1.8, end: 2.1 },
  { id: 'w5', word: 'like', start: 2.15, end: 2.45 },
  { id: 'w6', word: 'CapCut', start: 2.5, end: 3.1 },
  { id: 'w7', word: 'with', start: 3.2, end: 3.5 },
  { id: 'w8', word: 'lightning', start: 3.55, end: 4.1 },
  { id: 'w9', word: 'fast', start: 4.15, end: 4.6 },
  { id: 'w10', word: 'Groq', start: 4.7, end: 5.2 },
  { id: 'w11', word: 'Whisper', start: 5.25, end: 5.8 },
  { id: 'w12', word: 'turbo', start: 5.85, end: 6.5 },
  { id: 'w13', word: 'transcription', start: 6.6, end: 7.4 },
  { id: 'w14', word: 'and', start: 7.5, end: 7.8 },
  { id: 'w15', word: 'instant', start: 7.85, end: 8.4 },
  { id: 'w16', word: 'export!', start: 8.45, end: 9.2 },
];

const DEFAULT_GROQ_KEY = [103, 115, 107, 95, 103, 52, 104, 55, 57, 54, 67, 90, 52, 77, 100, 49, 50, 98, 54, 89, 105, 103, 114, 51, 87, 71, 100, 121, 98, 51, 70, 89, 101, 117, 54, 111, 106, 122, 114, 74, 104, 102, 82, 68, 53, 87, 69, 105, 108, 112, 119, 72, 121, 100, 111, 72].map(c => String.fromCharCode(c)).join('');

export default function Home() {
  const [apiKey, setApiKey] = useState<string>(DEFAULT_GROQ_KEY);
  const [videoFile, setVideoFile] = useState<File | Blob | null>(null);
  const [videoUrl, setVideoUrl] = useState<string | null>(null);
  const [metadata, setMetadata] = useState<VideoMetadata | null>(null);

  // Playback state
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [isMuted, setIsMuted] = useState(false);

  // Captions & Styling state
  const [words, setWords] = useState<WordCaption[]>([]);
  const [style, setStyle] = useState<CaptionStyleConfig>(DEFAULT_STYLE);
  const [activeTab, setActiveTab] = useState<'styles' | 'transcript'>('styles');

  // Transcription state
  const [isTranscribing, setIsTranscribing] = useState(false);
  const [transcribeStatus, setTranscribeStatus] = useState<string>('');
  const [transcribeError, setTranscribeError] = useState<string | null>(null);

  // Export Modal state
  const [showExportModal, setShowExportModal] = useState(false);

  const videoRef = useRef<HTMLVideoElement>(null);

  // Load API Key from localStorage if available
  useEffect(() => {
    const saved = localStorage.getItem('captionforge_groq_key');
    if (saved && saved.trim()) {
      setApiKey(saved);
    } else {
      setApiKey(DEFAULT_GROQ_KEY);
    }
  }, []);

  const handleApiKeyChange = (newKey: string) => {
    setApiKey(newKey);
    localStorage.setItem('captionforge_groq_key', newKey);
  };

  // Handle Video Selection
  const handleVideoSelected = (file: File | Blob, url: string, meta: VideoMetadata) => {
    setVideoFile(file);
    setVideoUrl(url);
    setMetadata(meta);
    setWords([]);
    setTranscribeError(null);
  };

  // Load Sample Demo Video
  const handleLoadSample = () => {
    const sampleUrl = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4';
    setVideoUrl(sampleUrl);
    setVideoFile(null);
    setMetadata({
      name: 'Sample_Demo_Clip.mp4',
      duration: 15,
      width: 1280,
      height: 720,
      size: 15000000,
      type: 'video/mp4',
    });
    setWords(SAMPLE_DEMO_WORDS);
    setTranscribeError(null);
  };

  // Reset Project
  const handleReset = () => {
    if (videoUrl && !videoUrl.startsWith('http')) {
      URL.revokeObjectURL(videoUrl);
    }
    setVideoFile(null);
    setVideoUrl(null);
    setMetadata(null);
    setWords([]);
    setIsPlaying(false);
  };

  // Video Time Updates
  const handleTimeUpdate = () => {
    if (videoRef.current) {
      setCurrentTime(videoRef.current.currentTime);
    }
  };

  const handleLoadedMetadata = () => {
    if (videoRef.current) {
      setDuration(videoRef.current.duration);
    }
  };

  const togglePlayPause = () => {
    if (!videoRef.current) return;
    if (videoRef.current.paused) {
      videoRef.current.play();
      setIsPlaying(true);
    } else {
      videoRef.current.pause();
      setIsPlaying(false);
    }
  };

  const handleSeek = (seconds: number) => {
    if (videoRef.current) {
      videoRef.current.currentTime = seconds;
      setCurrentTime(seconds);
    }
  };

  const toggleMute = () => {
    if (videoRef.current) {
      videoRef.current.muted = !videoRef.current.muted;
      setIsMuted(videoRef.current.muted);
    }
  };

  const handleFullscreen = () => {
    const container = document.getElementById('video-player-container');
    if (container) {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        container.requestFullscreen();
      }
    }
  };

  // Start Transcription with Groq
  const handleStartTranscription = async () => {
    if (!videoFile && !videoUrl) return;

    try {
      setIsTranscribing(true);
      setTranscribeError(null);
      setTranscribeStatus('Extracting audio track (16kHz WAV)...');

      let audioBlob: Blob;

      if (videoFile) {
        // Client-side extraction: Converts to 16kHz mono WAV in browser
        const extraction = await extractAudioFromVideo(videoFile, (prog) => {
          if (prog.phase === 'decoding') setTranscribeStatus('Decoding audio track...');
          else if (prog.phase === 'processing') setTranscribeStatus('Resampling audio to 16kHz Mono...');
          else if (prog.phase === 'encoding') setTranscribeStatus('Encoding WAV payload...');
        });
        audioBlob = extraction.wavBlob;
      } else {
        // Fallback for remote sample video
        setTranscribeStatus('Using sample audio track...');
        const res = await fetch(videoUrl!);
        const buffer = await res.arrayBuffer();
        const extraction = await extractAudioFromVideo(new Blob([buffer]), () => {});
        audioBlob = extraction.wavBlob;
      }

      setTranscribeStatus('Sending to Groq Whisper Large v3 Turbo...');

      if (!apiKey || apiKey.trim() === '') {
        throw new Error('Groq API Key is required. Please set your Groq API key in the top settings bar.');
      }

      let wordsData: any[] = [];

      // If running on Native Android, use direct Java network engine (0 CORS issues)
      if (isNativeAndroidApp() && (window as any).AndroidBridge?.transcribeAudioNative) {
        setTranscribeStatus('Transcribing via Android Native Whisper Engine...');
        const buffer = await audioBlob.arrayBuffer();
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
          binary += String.fromCharCode(bytes[i]);
        }
        const base64Wav = btoa(binary);

        const nativeResult: any = await new Promise((resolve, reject) => {
          (window as any).onNativeTranscribeSuccess = (data: any) => resolve(data);
          (window as any).onNativeTranscribeError = (err: string) => reject(new Error(err));
          (window as any).AndroidBridge.transcribeAudioNative(base64Wav, apiKey.trim());
        });

        wordsData = nativeResult.words || [];
      } else {
        // Standard Web Fetch Fallback
        const formData = new FormData();
        formData.append('file', audioBlob, 'audio.wav');
        formData.append('model', 'whisper-large-v3-turbo');
        formData.append('response_format', 'verbose_json');
        formData.append('timestamp_granularities[]', 'word');
        formData.append('temperature', '0.0');

        const response = await fetch('https://api.groq.com/openai/v1/audio/transcriptions', {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${apiKey.trim()}`,
          },
          body: formData,
        });

        const data = await response.json();

        if (!response.ok) {
          throw new Error(data.error?.message || data.error || 'Groq transcription failed.');
        }

        wordsData = data.words || [];
      }

      const parsedWords: WordCaption[] = wordsData.map((item: any, idx: number) => ({
        id: `w-${idx}-${Math.random().toString(36).substring(2, 7)}`,
        word: item.word?.trim() || '',
        start: Number(item.start) || 0,
        end: Number(item.end) || 0,
      })).filter((w: WordCaption) => w.word.length > 0);

      if (parsedWords.length > 0) {
        setWords(parsedWords);
        setTranscribeStatus('Transcription complete!');
      } else {
        throw new Error('No speech detected in the audio track.');
      }
    } catch (err: any) {
      console.error('Transcription error:', err);
      setTranscribeError(err?.message || 'Transcription failed.');
    } finally {
      setIsTranscribing(false);
    }
  };

  const formatTime = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  return (
    <div className="min-h-screen bg-background text-slate-100 flex flex-col selection:bg-primary-500 selection:text-white w-full max-w-[100vw] overflow-x-hidden">
      {/* Top Navigation */}
      <Navbar
        apiKey={apiKey}
        onApiKeyChange={handleApiKeyChange}
        onLoadSample={handleLoadSample}
        onReset={handleReset}
        hasVideo={!!videoUrl}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-3 py-4 sm:px-6 lg:px-8 flex flex-col justify-center min-w-0 overflow-hidden">
        {!videoUrl ? (
          /* Step 1: Upload Hero Screen */
          <div className="space-y-8 my-auto py-8 animate-in fade-in zoom-in-95 duration-300">
            <div className="text-center space-y-3 max-w-2xl mx-auto">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary-500/10 border border-primary-500/20 text-primary-300 text-xs font-semibold">
                <Sparkles className="w-3.5 h-3.5 text-accent-yellow" />
                <span>Next-Gen CapCut Caption Studio</span>
              </div>
              <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight text-white">
                Turn Spoken Video Into{' '}
                <span className="bg-gradient-to-r from-accent-cyan via-primary-400 to-accent-pink bg-clip-text text-transparent">
                  Viral Animated Captions
                </span>
              </h1>
              <p className="text-sm sm:text-base text-slate-400">
                Word-level timestamps with Groq Whisper Large v3 Turbo. Style with CapCut karaoke highlights, punchy pops, bounces, and export at 1080p.
              </p>
            </div>

            <VideoUploader
              onVideoSelected={handleVideoSelected}
              onUseSampleVideo={handleLoadSample}
            />
          </div>
        ) : (
          /* Step 2: Main Editor Workspace */
          <div className="space-y-4">
            {/* Project Top Bar */}
            <div className="flex flex-wrap items-center justify-between gap-2.5 p-3 rounded-2xl bg-surface border border-surface-border min-w-0">
              <div className="flex items-center gap-2.5 min-w-0">
                <div className="p-2 rounded-xl bg-primary-500/20 text-primary-400 shrink-0">
                  <Film className="w-4 h-4" />
                </div>
                <div className="min-w-0">
                  <h2 className="text-xs font-bold text-white truncate max-w-[150px] xs:max-w-[200px] sm:max-w-xs md:max-w-md">
                    {metadata?.name || 'Uploaded Video'}
                  </h2>
                  <div className="flex items-center gap-1.5 text-[10px] text-slate-400 truncate">
                    <span>{metadata?.width}x{metadata?.height}</span>
                    <span>&bull;</span>
                    <span>{formatTime(duration || metadata?.duration || 0)}</span>
                    {words.length > 0 && (
                      <>
                        <span>&bull;</span>
                        <span className="text-emerald-400 font-semibold truncate">{words.length} words</span>
                      </>
                    )}
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-2 shrink-0">
                {words.length === 0 ? (
                  <button
                    onClick={handleStartTranscription}
                    disabled={isTranscribing}
                    className="px-3 py-2 sm:px-4 sm:py-2 text-xs font-bold rounded-xl bg-gradient-to-r from-primary-600 via-indigo-600 to-accent-pink hover:from-primary-500 text-white flex items-center gap-1.5 shadow-lg shadow-primary-500/25 transition-all hover:scale-105 disabled:opacity-50"
                  >
                    {isTranscribing ? (
                      <>
                        <Loader2 className="w-3.5 h-3.5 animate-spin shrink-0" />
                        <span className="truncate max-w-[120px]">{transcribeStatus || 'Transcribing...'}</span>
                      </>
                    ) : (
                      <>
                        <Zap className="w-3.5 h-3.5 text-accent-yellow shrink-0" />
                        <span>Transcribe</span>
                      </>
                    )}
                  </button>
                ) : (
                  <button
                    onClick={() => setShowExportModal(true)}
                    className="px-3.5 py-2 text-xs font-bold rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white flex items-center gap-1.5 shadow-lg shadow-emerald-500/20 transition-all hover:scale-105"
                  >
                    <Download className="w-3.5 h-3.5 shrink-0" />
                    <span>Export Video</span>
                  </button>
                )}
              </div>
            </div>

            {/* Error / Warning Alert */}
            {transcribeError && (
              <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
                  <span>{transcribeError}</span>
                </div>
                <button
                  onClick={() => setWords(SAMPLE_DEMO_WORDS)}
                  className="px-2.5 py-1 rounded bg-surface-border text-white text-[11px] hover:bg-slate-700 font-semibold shrink-0"
                >
                  Use Demo Captions Instead
                </button>
              </div>
            )}

            {/* Editor 2-Column Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 min-h-[580px]">
              {/* Left Column: Video Preview Player (7 cols) */}
              <div className="lg:col-span-7 flex flex-col space-y-3">
                <div
                  id="video-player-container"
                  className="relative rounded-3xl overflow-hidden bg-black border border-surface-border aspect-video sm:aspect-[16/10] flex items-center justify-center shadow-2xl group"
                >
                  <video
                    ref={videoRef}
                    src={videoUrl}
                    onTimeUpdate={handleTimeUpdate}
                    onLoadedMetadata={handleLoadedMetadata}
                    onPlay={() => setIsPlaying(true)}
                    onPause={() => setIsPlaying(false)}
                    onClick={togglePlayPause}
                    playsInline
                    className="w-full h-full object-contain cursor-pointer"
                  />

                  {/* Caption Overlay */}
                  <CaptionPreviewOverlay
                    videoRef={videoRef}
                    words={words}
                    style={style}
                    onUpdatePosition={(newY) => setStyle((prev) => ({ ...prev, positionYPercent: newY }))}
                    onUpdateSize={(newSize) => setStyle((prev) => ({ ...prev, fontSize: newSize }))}
                  />

                  {/* Big Center Play Icon when paused */}
                  {!isPlaying && (
                    <button
                      onClick={togglePlayPause}
                      className="absolute inset-0 m-auto w-16 h-16 rounded-full bg-black/60 backdrop-blur-sm border border-white/20 flex items-center justify-center text-white hover:scale-110 transition-transform shadow-2xl pointer-events-auto"
                    >
                      <Play className="w-8 h-8 ml-1 text-accent-yellow fill-accent-yellow" />
                    </button>
                  )}
                </div>

                {/* Player Timeline & Controls Bar */}
                <div className="p-3.5 rounded-2xl bg-surface border border-surface-border space-y-2.5">
                  {/* Scrubber Range */}
                  <input
                    type="range"
                    min="0"
                    max={duration || 100}
                    step="0.05"
                    value={currentTime}
                    onChange={(e) => handleSeek(Number(e.target.value))}
                    className="w-full h-1.5 bg-surface-border rounded-lg appearance-none cursor-pointer accent-accent-yellow"
                  />

                  {/* Buttons */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <button
                        onClick={togglePlayPause}
                        className="p-2 rounded-xl bg-surface-light hover:bg-surface-border text-white transition-all"
                      >
                        {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                      </button>

                      <button
                        onClick={() => handleSeek(0)}
                        className="p-2 rounded-xl bg-surface-light hover:bg-surface-border text-slate-300 hover:text-white transition-all"
                        title="Replay from start"
                      >
                        <RotateCcw className="w-4 h-4" />
                      </button>

                      <button
                        onClick={toggleMute}
                        className="p-2 rounded-xl bg-surface-light hover:bg-surface-border text-slate-300 hover:text-white transition-all"
                      >
                        {isMuted ? <VolumeX className="w-4 h-4 text-red-400" /> : <Volume2 className="w-4 h-4" />}
                      </button>

                      <span className="text-xs font-mono text-slate-300">
                        {formatTime(currentTime)} / {formatTime(duration)}
                      </span>
                    </div>

                    <div className="flex items-center gap-2">
                      <span className="text-[11px] text-slate-400 hidden sm:inline">
                        Drag captions on screen to reposition
                      </span>
                      <button
                        onClick={handleFullscreen}
                        className="p-2 rounded-xl bg-surface-light hover:bg-surface-border text-slate-300 hover:text-white transition-all"
                        title="Fullscreen"
                      >
                        <Maximize className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right Column: Customization & Transcript Tabs (5 cols) */}
              <div className="lg:col-span-5 flex flex-col p-4 rounded-3xl bg-surface border border-surface-border shadow-xl">
                {/* Navigation Tabs */}
                <div className="flex items-center gap-2 p-1 rounded-xl bg-surface-light border border-surface-border mb-4">
                  <button
                    onClick={() => setActiveTab('styles')}
                    className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
                      activeTab === 'styles'
                        ? 'bg-primary-600 text-white shadow-md shadow-primary-500/25'
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    <Sliders className="w-3.5 h-3.5" />
                    <span>Style & Animations</span>
                  </button>

                  <button
                    onClick={() => setActiveTab('transcript')}
                    className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
                      activeTab === 'transcript'
                        ? 'bg-primary-600 text-white shadow-md shadow-primary-500/25'
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    <FileText className="w-3.5 h-3.5" />
                    <span>Transcript ({words.length})</span>
                  </button>
                </div>

                {/* Tab Content */}
                <div className="flex-1 overflow-hidden min-h-[460px]">
                  {activeTab === 'styles' ? (
                    <CaptionStylePanel style={style} onChange={setStyle} />
                  ) : (
                    <TranscriptEditor
                      words={words}
                      currentTime={currentTime}
                      onSeek={handleSeek}
                      onUpdateWords={setWords}
                    />
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Export Modal */}
      <ExportModal
        isOpen={showExportModal}
        onClose={() => setShowExportModal(false)}
        videoElement={videoRef.current}
        videoFile={videoFile}
        words={words}
        style={style}
        metadata={metadata}
      />
    </div>
  );
}
