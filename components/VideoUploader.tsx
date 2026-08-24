'use client';

import React, { useState, useRef } from 'react';
import { Upload, Film, FileVideo, Sparkles, AlertCircle, CheckCircle2 } from 'lucide-react';
import { VideoMetadata } from '@/lib/types';

interface VideoUploaderProps {
  onVideoSelected: (file: File | Blob, url: string, metadata: VideoMetadata) => void;
  onUseSampleVideo: () => void;
  isProcessing?: boolean;
}

export const VideoUploader: React.FC<VideoUploaderProps> = ({
  onVideoSelected,
  onUseSampleVideo,
  isProcessing = false,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [loadingMetadata, setLoadingMetadata] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const processSelectedFile = (file: File) => {
    setErrorMsg(null);

    const validTypes = ['video/mp4', 'video/quicktime', 'video/webm', 'video/x-matroska'];
    const validExtensions = ['.mp4', '.mov', '.webm', '.mkv'];
    const hasValidExtension = validExtensions.some(ext => file.name.toLowerCase().endsWith(ext));

    if (!validTypes.includes(file.type) && !hasValidExtension) {
      setErrorMsg('Please select a supported video file (.mp4, .mov, or .webm).');
      return;
    }

    setLoadingMetadata(true);
    const videoUrl = URL.createObjectURL(file);
    const tempVideo = document.createElement('video');
    tempVideo.preload = 'metadata';
    tempVideo.src = videoUrl;

    tempVideo.onloadedmetadata = () => {
      setLoadingMetadata(false);
      const metadata: VideoMetadata = {
        name: file.name,
        duration: tempVideo.duration,
        width: tempVideo.videoWidth,
        height: tempVideo.videoHeight,
        size: file.size,
        type: file.type || 'video/mp4',
      };
      onVideoSelected(file, videoUrl, metadata);
    };

    tempVideo.onerror = () => {
      setLoadingMetadata(false);
      setErrorMsg('Could not read video metadata. Please check the video file codec.');
    };
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processSelectedFile(e.dataTransfer.files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  return (
    <div className="w-full max-w-3xl mx-auto space-y-6">
      {/* Drag and drop upload container */}
      <div
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onClick={() => fileInputRef.current?.click()}
        className={`relative group cursor-pointer rounded-3xl border-2 border-dashed p-8 sm:p-12 text-center transition-all duration-300 backdrop-blur-xl ${
          isDragging
            ? 'border-accent-cyan bg-accent-cyan/10 scale-[1.01] shadow-2xl shadow-accent-cyan/20'
            : 'border-surface-border bg-surface/80 hover:border-primary-500/60 hover:bg-surface-light/80 shadow-xl'
        }`}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept="video/mp4,video/quicktime,video/webm"
          className="hidden"
          onChange={(e) => {
            if (e.target.files && e.target.files.length > 0) {
              processSelectedFile(e.target.files[0]);
            }
          }}
        />

        {/* Glow backdrop */}
        <div className="absolute inset-0 rounded-3xl bg-gradient-to-b from-primary-500/5 to-transparent pointer-events-none" />

        <div className="relative flex flex-col items-center justify-center space-y-4">
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-primary-600/30 via-accent-pink/20 to-accent-cyan/30 flex items-center justify-center border border-white/10 group-hover:scale-110 transition-transform duration-300 shadow-inner">
            <Upload className="w-9 h-9 text-white group-hover:text-accent-cyan transition-colors" />
          </div>

          <div className="space-y-1.5">
            <h3 className="text-xl font-bold text-white tracking-tight">
              Drag & Drop your video here
            </h3>
            <p className="text-sm text-slate-400">
              or <span className="text-primary-400 font-semibold underline underline-offset-4">browse files</span> from your computer
            </p>
          </div>

          {/* Feature Badges */}
          <div className="flex flex-wrap items-center justify-center gap-2 pt-2">
            <span className="text-[11px] font-medium px-2.5 py-1 rounded-md bg-surface-border text-slate-300">
              MP4, MOV, WEBM
            </span>
            <span className="text-[11px] font-medium px-2.5 py-1 rounded-md bg-emerald-500/10 text-emerald-300 border border-emerald-500/20 flex items-center gap-1">
              <CheckCircle2 className="w-3 h-3" /> 0s Upload Lag (Client-Side)
            </span>
            <span className="text-[11px] font-medium px-2.5 py-1 rounded-md bg-accent-yellow/10 text-accent-yellow border border-accent-yellow/20">
              Up to 4K 60fps
            </span>
          </div>
        </div>
      </div>

      {/* Error message */}
      {errorMsg && (
        <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-center gap-2.5">
          <AlertCircle className="w-4 h-4 shrink-0 text-red-400" />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Sample Video Card */}
      <div className="flex items-center justify-between p-4 rounded-2xl bg-surface/60 border border-surface-border backdrop-blur-md">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-primary-500/20 text-primary-300">
            <Film className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-semibold text-white">Don&apos;t have a video ready?</h4>
            <p className="text-[11px] text-slate-400">
              Test CaptionForge with our built-in sample demo clip and instant speech transcription.
            </p>
          </div>
        </div>

        <button
          onClick={onUseSampleVideo}
          className="px-4 py-2 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-500 text-white flex items-center gap-1.5 transition-all shadow-md shadow-primary-500/20 hover:scale-105 shrink-0"
        >
          <Sparkles className="w-3.5 h-3.5 text-accent-yellow" />
          <span>Load Demo Video</span>
        </button>
      </div>
    </div>
  );
};
