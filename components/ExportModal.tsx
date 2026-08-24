'use client';

import React, { useState, useEffect } from 'react';
import { WordCaption, CaptionStyleConfig, VideoMetadata, ExportSettings, ExportResolution, ExportFps, ExportBitrate, ExportAspectRatio, ExportSpeed } from '@/lib/types';
import { renderVideoWithCaptionsInBrowser } from '@/lib/videoRenderer';
import { isNativeAndroidApp, renderWithAndroidHardware } from '@/lib/nativeBridge';
import { generateAssSubtitles, generateSrtSubtitles, generateVttSubtitles } from '@/lib/assGenerator';
import {
  Download,
  CheckCircle,
  FileCode,
  Film,
  Sparkles,
  Loader2,
  X,
  Tv,
  Smartphone,
  Gauge,
  Layers,
  ShieldCheck,
  Zap,
} from 'lucide-react';

interface ExportModalProps {
  isOpen: boolean;
  onClose: () => void;
  videoElement: HTMLVideoElement | null;
  videoFile?: File | null;
  words: WordCaption[];
  style: CaptionStyleConfig;
  metadata: VideoMetadata | null;
}

export const ExportModal: React.FC<ExportModalProps> = ({
  isOpen,
  onClose,
  videoElement,
  videoFile,
  words,
  style,
  metadata,
}) => {
  const [isMobile, setIsMobile] = useState(false);
  const [isNativeApp, setIsNativeApp] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusText, setStatusText] = useState('');
  const [exportedUrl, setExportedUrl] = useState<string | null>(null);
  const [exportError, setExportError] = useState<string | null>(null);

  const [resolution, setResolution] = useState<ExportResolution>('720p');
  const [fps, setFps] = useState<ExportFps>(30);
  const [bitrate, setBitrate] = useState<ExportBitrate>('high');
  const [aspectRatio, setAspectRatio] = useState<ExportAspectRatio>('original');

  useEffect(() => {
    const mobileCheck = typeof navigator !== 'undefined' && /Android|iPhone|iPad|iPod|Mobile/i.test(navigator.userAgent);
    setIsMobile(mobileCheck);
    setIsNativeApp(isNativeAndroidApp());
    if (mobileCheck) {
      setResolution('720p');
      setFps(30);
      setBitrate('high');
    } else {
      setResolution('720p');
      setFps(30);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleStartExport = async () => {
    if (!videoElement) {
      setExportError('Video player is not initialized.');
      return;
    }

    try {
      setIsExporting(true);
      setExportError(null);
      setProgress(0);

      // Check if running on Android Native App
      if (isNativeAndroidApp()) {
        setStatusText('Starting Android Native Hardware GPU Encoder...');
        
        // Generate .ass subtitles directly in client
        const assContent = generateAssSubtitles(
          words,
          style,
          metadata?.width || 1280,
          metadata?.height || 720
        );

        const nativeResult = await renderWithAndroidHardware(
          {
            videoFile: videoFile,
            assContent,
            resolution,
            fps,
          },
          (prog, status) => {
            setProgress(prog);
            if (status) setStatusText(status);
          }
        );

        if (nativeResult.success && nativeResult.outputPath) {
          setExportedUrl(nativeResult.outputPath);
          setIsExporting(false);
          return;
        } else if (nativeResult.error) {
          throw new Error(nativeResult.error);
        }
      }

      // Browser Engine Fallback
      setStatusText(`Starting ${resolution.toUpperCase()} engine...`);

      const exportSettings: ExportSettings = {
        resolution,
        fps,
        bitrate,
        aspectRatio,
      };

      const outputBlob = await renderVideoWithCaptionsInBrowser(
        videoElement,
        words,
        style,
        exportSettings,
        (prog, text) => {
          setProgress(prog);
          setStatusText(text);
        }
      );

      const url = URL.createObjectURL(outputBlob);
      setExportedUrl(url);
      setIsExporting(false);
    } catch (err: any) {
      console.error('Export failed:', err);
      setExportError(err?.message || 'Export error. Try 720p resolution or download .ASS subtitles.');
      setIsExporting(false);
    }
  };

  const handleDownloadSubtitles = (format: 'ass' | 'srt' | 'vtt') => {
    try {
      let fileContent = '';
      let mimeType = 'text/plain';
      const filename = `captionforge_${format}_subtitles.${format}`;

      if (format === 'ass') {
        fileContent = generateAssSubtitles(words, style, metadata?.width || 1280, metadata?.height || 720);
        mimeType = 'text/x-ssa';
      } else if (format === 'srt') {
        fileContent = generateSrtSubtitles(words, style.wordsPerChunk || 4);
        mimeType = 'application/x-subrip';
      } else if (format === 'vtt') {
        fileContent = generateVttSubtitles(words, style.wordsPerChunk || 4);
        mimeType = 'text/vtt';
      }

      const blob = new Blob([fileContent], { type: mimeType });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err: any) {
      alert(err?.message || 'Error exporting subtitles.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-xl bg-surface border border-surface-border rounded-3xl p-5 sm:p-7 shadow-2xl space-y-4 relative overflow-hidden max-h-[92vh] overflow-y-auto custom-scrollbar">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-gradient-to-tr from-primary-600 to-accent-pink text-white shadow-lg shadow-primary-500/20">
              <Download className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">Export Studio</h3>
              <p className="text-xs text-slate-400">
                {isNativeApp ? '⚡ Android Native GPU Hardware Export' : 'Burn-in Captions &bull; Instant Subtitle Files'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-surface-light transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Video Status & Customization Area */}
        {!exportedUrl ? (
          <div className="space-y-4">
            {/* Native Hardware Banner */}
            {isNativeApp && (
              <div className="flex items-center gap-2.5 p-3 rounded-2xl bg-primary-500/15 border border-primary-500/30 text-primary-300 text-xs">
                <Zap className="w-4 h-4 shrink-0 text-accent-yellow" />
                <span>
                  <strong>Native Android GPU Active:</strong> 100% InShot-speed hardware rendering enabled.
                </span>
              </div>
            )}

            {/* Resolution Selector */}
            <div className="space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                  <Tv className="w-3.5 h-3.5 text-accent-cyan" /> Burn-In Resolution
                </label>
                <span className="text-[10px] text-emerald-400 font-semibold">720p HD is fast & stable</span>
              </div>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { id: '720p', label: '720p HD', sub: 'Fast & Stable ⚡' },
                  { id: '1080p', label: '1080p Full HD', sub: 'Crisp High-Res' },
                  { id: 'original', label: 'Source', sub: 'Match Video' },
                ].map((r) => (
                  <button
                    key={r.id}
                    onClick={() => setResolution(r.id as ExportResolution)}
                    className={`p-2.5 rounded-xl text-left border transition-all ${
                      resolution === r.id
                        ? 'bg-primary-600/30 border-primary-500 text-white font-bold ring-1 ring-primary-400'
                        : 'bg-surface-light border-surface-border text-slate-400 hover:text-white'
                    }`}
                  >
                    <p className="text-xs font-bold">{r.label}</p>
                    <p className="text-[10px] text-slate-500">{r.sub}</p>
                  </button>
                ))}
              </div>
            </div>

            {/* FPS & Aspect Ratio */}
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                  <Gauge className="w-3.5 h-3.5 text-accent-yellow" /> Frame Rate (FPS)
                </label>
                <div className="grid grid-cols-2 gap-1.5">
                  {[
                    { val: 30, label: '30 FPS' },
                    { val: 24, label: '24 FPS' },
                  ].map((f) => (
                    <button
                      key={f.val}
                      onClick={() => setFps(f.val as ExportFps)}
                      className={`p-2 rounded-xl text-center border transition-all ${
                        fps === f.val
                          ? 'bg-accent-yellow/20 border-accent-yellow text-accent-yellow font-bold'
                          : 'bg-surface-light border-surface-border text-slate-400 hover:text-white'
                      }`}
                    >
                      <p className="text-xs font-bold">{f.label}</p>
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                  <Smartphone className="w-3.5 h-3.5 text-accent-pink" /> Aspect Ratio
                </label>
                <div className="grid grid-cols-3 gap-1">
                  {[
                    { id: 'original', label: 'Original' },
                    { id: '9:16', label: '9:16' },
                    { id: '16:9', label: '16:9' },
                  ].map((a) => (
                    <button
                      key={a.id}
                      onClick={() => setAspectRatio(a.id as ExportAspectRatio)}
                      className={`p-2 rounded-xl text-center border transition-all ${
                        aspectRatio === a.id
                          ? 'bg-accent-pink/20 border-accent-pink text-pink-300 font-bold'
                          : 'bg-surface-light border-surface-border text-slate-400 hover:text-white'
                      }`}
                    >
                      <p className="text-[11px] font-bold">{a.label}</p>
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Progress Bar during render */}
            {isExporting && (
              <div className="space-y-2 py-2">
                <div className="flex justify-between text-xs font-semibold">
                  <span className="text-white flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin text-accent-cyan" />
                    {statusText || `Burning captions...`}
                  </span>
                  <span className="text-accent-yellow font-mono">{progress}%</span>
                </div>
                <div className="w-full h-3 bg-surface-border rounded-full overflow-hidden p-0.5">
                  <div
                    className="h-full bg-gradient-to-r from-primary-500 via-accent-pink to-accent-yellow rounded-full transition-all duration-150"
                    style={{ width: `${progress}%` }}
                  />
                </div>
              </div>
            )}

            {exportError && (
              <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs">
                {exportError}
              </div>
            )}

            {/* Start Export Button */}
            <button
              onClick={handleStartExport}
              disabled={isExporting}
              className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-primary-600 via-indigo-600 to-accent-pink hover:from-primary-500 hover:to-accent-pink text-white font-bold text-sm shadow-xl shadow-primary-500/25 flex items-center justify-center gap-2 transition-all hover:scale-[1.01] active:scale-[0.99] disabled:opacity-50"
            >
              {isExporting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Burning Captions ({progress}%)...</span>
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4 text-accent-yellow" />
                  <span>
                    {isNativeApp ? '⚡ Export via Android Hardware' : `Burn & Export Video (${resolution.toUpperCase()})`}
                  </span>
                </>
              )}
            </button>
          </div>
        ) : (
          /* Finished State */
          <div className="space-y-4">
            <div className="rounded-2xl overflow-hidden border border-surface-border bg-black aspect-video max-h-56 flex items-center justify-center">
              <video
                src={exportedUrl}
                controls
                autoPlay
                className="w-full h-full object-contain"
              />
            </div>

            <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs">
              <CheckCircle className="w-4 h-4 shrink-0" />
              <span>
                Video rendered successfully at <strong>{resolution.toUpperCase()} • {fps}FPS</strong>!
              </span>
            </div>

            <div className="flex gap-3">
              <a
                href={exportedUrl}
                download={`captionforge_${resolution}_video.mp4`}
                className="flex-1 py-3 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-emerald-500/20 transition-all hover:scale-105"
              >
                <Download className="w-4 h-4" />
                <span>Download Video File</span>
              </a>

              <button
                onClick={() => setExportedUrl(null)}
                className="px-4 py-3 rounded-xl bg-surface-light hover:bg-surface-border text-slate-300 font-semibold text-xs transition-all"
              >
                Render Another
              </button>
            </div>
          </div>
        )}

        {/* Instant Subtitle Files Section */}
        <div className="border-t border-surface-border pt-3.5 space-y-2">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <Zap className="w-3.5 h-3.5 text-accent-yellow" /> Instant Subtitle Download (0s Export)
            </h4>
            <span className="text-[10px] text-accent-cyan font-semibold">100% Quality &bull; No Lag</span>
          </div>

          <p className="text-[11px] text-slate-400 leading-relaxed">
            Download your styled subtitle file with word animations and import into CapCut, Premiere, or DaVinci:
          </p>

          <div className="grid grid-cols-3 gap-2">
            <button
              onClick={() => handleDownloadSubtitles('ass')}
              className="p-2.5 rounded-xl bg-surface-light border border-surface-border hover:border-primary-500 text-left transition-all group"
            >
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-bold text-white group-hover:text-primary-300">
                  .ASS
                </span>
                <FileCode className="w-3.5 h-3.5 text-accent-cyan" />
              </div>
              <p className="text-[10px] text-slate-400">Full CapCut Karaoke \k tags</p>
            </button>

            <button
              onClick={() => handleDownloadSubtitles('srt')}
              className="p-2.5 rounded-xl bg-surface-light border border-surface-border hover:border-primary-500 text-left transition-all group"
            >
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-bold text-white group-hover:text-primary-300">
                  .SRT
                </span>
                <FileCode className="w-3.5 h-3.5 text-accent-yellow" />
              </div>
              <p className="text-[10px] text-slate-400">Universal SubRip format</p>
            </button>

            <button
              onClick={() => handleDownloadSubtitles('vtt')}
              className="p-2.5 rounded-xl bg-surface-light border border-surface-border hover:border-primary-500 text-left transition-all group"
            >
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-bold text-white group-hover:text-primary-300">
                  .VTT
                </span>
                <FileCode className="w-3.5 h-3.5 text-accent-pink" />
              </div>
              <p className="text-[10px] text-slate-400">Web Video Text Tracks</p>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
