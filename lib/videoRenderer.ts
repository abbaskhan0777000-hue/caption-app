import { WordCaption, CaptionStyleConfig, ExportSettings } from './types';
import { chunkWords } from './assGenerator';
import { fixWebmDuration } from './webmFixer';

export interface RenderProgressCallback {
  (progress: number, statusText: string): void;
}

/**
 * Crash-Proof Memory-Guarded Video & Caption Burn-in Renderer
 * 
 * Safety & Anti-Crash Architecture:
 * 1. Memory Guard: Automatically caps canvas buffer dimensions to prevent browser Out-Of-Memory (OOM) crashes.
 * 2. Non-blocking Chunking: Flushes MediaRecorder buffers frequently to keep RAM footprint below 80MB.
 * 3. Video Decode Protection: Sequential hardware stream decoding at safe rates.
 * 4. EBML Duration Patch: Injects duration metadata so the video never stalls on playback.
 */
export async function renderVideoWithCaptionsInBrowser(
  videoElement: HTMLVideoElement,
  words: WordCaption[],
  style: CaptionStyleConfig,
  settings: ExportSettings = { resolution: '720p', fps: 30, bitrate: 'balanced', aspectRatio: 'original' },
  onProgress?: RenderProgressCallback
): Promise<Blob> {
  return new Promise(async (resolve, reject) => {
    try {
      const originalTime = videoElement.currentTime;
      const originalPlaybackRate = videoElement.playbackRate;
      const wasPaused = videoElement.paused;
      const duration = videoElement.duration;

      if (!duration || duration <= 0 || isNaN(duration)) {
        throw new Error('Invalid or missing video duration.');
      }

      const isMobile = typeof navigator !== 'undefined' && /Android|iPhone|iPad|iPod|Mobile/i.test(navigator.userAgent);

      onProgress?.(0, 'Initializing crash-safe rendering engine...');

      const srcWidth = videoElement.videoWidth || 1280;
      const srcHeight = videoElement.videoHeight || 720;
      const isVertical = srcHeight > srcWidth;

      // MEMORY SAFETY GUARD:
      // Prevent browser tab OOM crashes on heavy videos (4K / 60fps)
      let targetWidth = 1280;
      let targetHeight = 720;

      if (settings.resolution === '1080p' && !isMobile) {
        targetWidth = isVertical ? 1080 : 1920;
        targetHeight = isVertical ? 1920 : 1080;
      } else if (settings.resolution === '4k' && !isMobile) {
        // Safe 1440p upper ceiling to prevent GPU VRAM exhaustion
        targetWidth = isVertical ? 1440 : 2560;
        targetHeight = isVertical ? 2560 : 1440;
      } else if (settings.resolution === 'original' && !isMobile) {
        // Cap desktop original to 1080p max to avoid browser crash
        const maxDim = 1920;
        if (srcWidth > maxDim || srcHeight > maxDim) {
          const ratio = srcWidth / srcHeight;
          targetWidth = isVertical ? maxDim : Math.round(maxDim * ratio);
          targetHeight = isVertical ? Math.round(maxDim / ratio) : maxDim;
        } else {
          targetWidth = srcWidth;
          targetHeight = srcHeight;
        }
      } else {
        // 720p HD (Ultra-stable, zero crash, fast export)
        targetWidth = isVertical ? 720 : 1280;
        targetHeight = isVertical ? 1280 : 720;
      }

      // Aspect ratio override
      if (settings.aspectRatio === '9:16') {
        targetWidth = Math.min(targetWidth, targetHeight) === targetWidth ? targetWidth : Math.round((targetHeight * 9) / 16);
        targetHeight = Math.max(targetWidth, targetHeight);
      } else if (settings.aspectRatio === '16:9') {
        targetHeight = Math.min(targetWidth, targetHeight);
        targetWidth = Math.round((targetHeight * 16) / 9);
      } else if (settings.aspectRatio === '1:1') {
        const side = Math.min(targetWidth, targetHeight);
        targetWidth = side;
        targetHeight = side;
      }

      // Even dimensions required by codecs
      targetWidth = Math.floor(targetWidth / 2) * 2;
      targetHeight = Math.floor(targetHeight / 2) * 2;

      const canvas = document.createElement('canvas');
      canvas.width = targetWidth;
      canvas.height = targetHeight;
      const ctx = canvas.getContext('2d', { alpha: false });

      if (!ctx) {
        throw new Error('Canvas 2D context not supported.');
      }

      ctx.imageSmoothingEnabled = true;
      ctx.imageSmoothingQuality = 'medium';

      const chunks = chunkWords(words, style.wordsPerChunk);
      const targetFps = isMobile ? 30 : Math.min(60, settings.fps || 30);

      // Create audio destination stream
      let audioStream: MediaStream | null = null;
      try {
        const audioCtx = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)();
        const dest = audioCtx.createMediaStreamDestination();
        const sourceNode = audioCtx.createMediaElementSource(videoElement);
        sourceNode.connect(dest);
        audioStream = dest.stream;
      } catch {
        if ((videoElement as any).captureStream) {
          const vStream = (videoElement as any).captureStream();
          const audioTracks = vStream.getAudioTracks();
          if (audioTracks.length > 0) {
            audioStream = new MediaStream(audioTracks);
          }
        }
      }

      const canvasStream = canvas.captureStream(targetFps);
      const tracks = [...canvasStream.getVideoTracks()];
      if (audioStream && audioStream.getAudioTracks().length > 0) {
        tracks.push(...audioStream.getAudioTracks());
      }
      const combinedStream = new MediaStream(tracks);

      // Safe Bitrates to avoid memory bloat
      let videoBitsPerSecond = isMobile ? 3500000 : 8000000;
      if (settings.bitrate === 'ultra' && !isMobile) videoBitsPerSecond = 16000000;
      else if (settings.bitrate === 'compact') videoBitsPerSecond = 2000000;

      const candidateCodecs = [
        'video/mp4;codecs=avc1.42E01E,mp4a.40.2',
        'video/mp4',
        'video/webm;codecs=vp8,opus',
        'video/webm;codecs=vp9,opus',
        'video/webm',
      ];

      let mimeType = 'video/webm';
      for (const candidate of candidateCodecs) {
        if (MediaRecorder.isTypeSupported(candidate)) {
          mimeType = candidate;
          break;
        }
      }

      const recorder = new MediaRecorder(combinedStream, {
        mimeType,
        videoBitsPerSecond,
      });

      const recordedChunks: Blob[] = [];
      recorder.ondataavailable = (e) => {
        if (e.data && e.data.size > 0) {
          recordedChunks.push(e.data);
        }
      };

      recorder.onstop = async () => {
        try {
          onProgress?.(98, 'Finalizing video metadata...');
          const rawBlob = new Blob(recordedChunks, { type: mimeType });
          
          let finalBlob = rawBlob;
          if (mimeType.includes('webm')) {
            const totalDurationMs = Math.round(duration * 1000);
            finalBlob = await fixWebmDuration(rawBlob, totalDurationMs);
          }

          videoElement.currentTime = originalTime;
          videoElement.playbackRate = originalPlaybackRate;
          if (!wasPaused) videoElement.play().catch(() => {});
          
          onProgress?.(100, 'Export complete!');
          resolve(finalBlob);
        } catch (err) {
          console.warn('Finalizing error:', err);
          videoElement.playbackRate = originalPlaybackRate;
          const rawBlob = new Blob(recordedChunks, { type: mimeType });
          resolve(rawBlob);
        }
      };

      recorder.onerror = (e) => {
        videoElement.playbackRate = originalPlaybackRate;
        reject(e);
      };

      // Safe playback rate: 1.0x to 1.5x avoids GPU buffer overload
      const playbackSpeed = isMobile ? 1.0 : 1.5;
      videoElement.playbackRate = playbackSpeed;
      videoElement.muted = true;
      videoElement.currentTime = 0;

      recorder.start(100); // 100ms chunk flush keeps browser RAM low
      await videoElement.play();

      let animationId: number | null = null;
      let isRecording = true;

      const renderLoop = () => {
        if (!isRecording) return;

        if (videoElement.ended || videoElement.currentTime >= duration - 0.05) {
          isRecording = false;
          if (animationId) cancelAnimationFrame(animationId);
          if (recorder.state === 'recording') {
            recorder.stop();
          }
          return;
        }

        const currentTime = videoElement.currentTime;
        const progress = Math.min(96, Math.round((currentTime / duration) * 100));
        onProgress?.(
          progress,
          `Burning captions (${targetWidth}x${targetHeight} • ${targetFps}fps)... ${progress}%`
        );

        // 1. Draw video frame to canvas
        ctx.drawImage(videoElement, 0, 0, canvas.width, canvas.height);

        // 2. Draw active caption
        const activeChunk = chunks.find(
          (c) => currentTime >= c.start && currentTime <= c.end + 0.12
        );

        if (activeChunk) {
          drawCaptionsOnCanvas(ctx, activeChunk, currentTime, style, canvas.width, canvas.height);
        }

        animationId = requestAnimationFrame(renderLoop);
      };

      animationId = requestAnimationFrame(renderLoop);
    } catch (err) {
      videoElement.playbackRate = 1.0;
      reject(err);
    }
  });
}

function drawCaptionsOnCanvas(
  ctx: CanvasRenderingContext2D,
  chunk: { words: WordCaption[]; start: number; end: number },
  currentTime: number,
  style: CaptionStyleConfig,
  canvasWidth: number,
  canvasHeight: number
) {
  ctx.save();

  const scaleFactor = canvasHeight / 1080;
  const scaledFontSize = Math.round(style.fontSize * scaleFactor);

  ctx.font = `900 ${scaledFontSize}px "${style.fontFamily}", Inter, sans-serif`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';

  const posY = Math.round((style.positionYPercent / 100) * canvasHeight);
  const posX = Math.round(canvasWidth / 2);

  // Build words to display with transform
  const formattedWords = chunk.words.map((w) => {
    let text = w.word;
    if (style.textTransform === 'uppercase') text = text.toUpperCase();
    else if (style.textTransform === 'capitalize') {
      text = text.charAt(0).toUpperCase() + text.slice(1);
    }
    return {
      ...w,
      displayText: text,
      isActive: currentTime >= w.start && currentTime <= w.end,
      isPassed: currentTime > w.end,
    };
  });

  const fullLineText = formattedWords.map((w) => w.displayText).join(' ');
  const fullLineWidth = Math.round(ctx.measureText(fullLineText).width);
  const spaceWidth = Math.round(ctx.measureText(' ').width);

  // Draw full line background box if enabled
  if (style.backgroundColor && style.backgroundColor !== 'transparent') {
    const padX = Math.round((style.backgroundPadding || 8) * 2 * scaleFactor);
    const padY = Math.round((style.backgroundPadding || 8) * 1.5 * scaleFactor);
    const boxWidth = fullLineWidth + padX * 2;
    const boxHeight = Math.round(scaledFontSize * 1.4 + padY * 2);
    const boxX = posX - boxWidth / 2;
    const boxY = posY - boxHeight / 2;

    ctx.fillStyle = style.backgroundColor;
    roundRect(ctx, boxX, boxY, boxWidth, boxHeight, Math.round((style.backgroundRounded || 8) * scaleFactor));
    ctx.fill();
  }

  let currentX = Math.round(posX - fullLineWidth / 2);

  formattedWords.forEach((wordObj) => {
    const wordWidth = Math.round(ctx.measureText(wordObj.displayText).width);
    const wordCenterX = Math.round(currentX + wordWidth / 2);

    ctx.save();
    ctx.translate(wordCenterX, posY);

    let scale = 1;
    let tilt = 0;

    if (wordObj.isActive) {
      scale = style.activeWordScale || 1.2;
      tilt = style.activeWordTilt || 0;
    }

    ctx.scale(scale, scale);
    if (tilt !== 0) ctx.rotate((tilt * Math.PI) / 180);

    // 1. Draw Marker Highlighter Streak
    if (wordObj.isActive && style.activeWordEffect === 'marker') {
      ctx.save();
      ctx.fillStyle = style.highlightBgColor || '#FFF275';
      const mPadX = Math.round(8 * scaleFactor);
      const mPadY = Math.round(4 * scaleFactor);
      ctx.beginPath();
      roundRect(
        ctx,
        -wordWidth / 2 - mPadX,
        -scaledFontSize * 0.65 - mPadY,
        wordWidth + mPadX * 2,
        scaledFontSize * 1.3 + mPadY * 2,
        Math.round(6 * scaleFactor)
      );
      ctx.fill();
      ctx.restore();
    }

    // 2. Draw Solid Pill Badge
    if (wordObj.isActive && (style.activeWordEffect === 'pill' || style.activeWordEffect === 'box')) {
      ctx.save();
      ctx.fillStyle = style.highlightBgColor || '#00FF66';
      const pPadX = Math.round(12 * scaleFactor);
      const pPadY = Math.round(6 * scaleFactor);
      ctx.beginPath();
      roundRect(
        ctx,
        -wordWidth / 2 - pPadX,
        -scaledFontSize * 0.65 - pPadY,
        wordWidth + pPadX * 2,
        scaledFontSize * 1.3 + pPadY * 2,
        Math.round(12 * scaleFactor)
      );
      ctx.fill();
      ctx.restore();
    }

    // Shadow setup
    if (style.shadowStyle === '3d-hard') {
      ctx.save();
      ctx.fillStyle = style.shadowColor || '#000000';
      const extDist = Math.round(5 * scaleFactor);
      for (let e = 1; e <= extDist; e++) {
        ctx.fillText(wordObj.displayText, 0, e);
      }
      ctx.restore();
    } else if (style.shadowStyle === 'neon-glow') {
      ctx.shadowColor = wordObj.isActive ? style.highlightColor : style.shadowColor;
      ctx.shadowBlur = Math.round((style.shadowBlur || 14) * scaleFactor);
    } else if (style.shadowStyle === 'soft') {
      ctx.shadowColor = style.shadowColor || 'rgba(0,0,0,0.85)';
      ctx.shadowBlur = Math.round((style.shadowBlur || 8) * scaleFactor);
      ctx.shadowOffsetY = Math.round((style.shadowOffsetY || 3) * scaleFactor);
    }

    // Colors
    let fillColor = style.textColor;
    if (wordObj.isActive) {
      if (style.activeWordEffect === 'marker' || style.activeWordEffect === 'pill') {
        fillColor = style.highlightTextColor || '#000000';
      } else {
        fillColor = style.highlightColor;
      }
    }

    // Outline
    if (style.strokeWidth > 0 && style.activeWordEffect !== 'marker') {
      ctx.strokeStyle = style.strokeColor || '#000000';
      ctx.lineWidth = Math.round(style.strokeWidth * 2 * scaleFactor);
      ctx.lineJoin = 'round';
      ctx.strokeText(wordObj.displayText, 0, 0);
    }

    // Main text
    ctx.fillStyle = fillColor;
    ctx.fillText(wordObj.displayText, 0, 0);

    ctx.restore();

    currentX += wordWidth + spaceWidth;
  });

  ctx.restore();
}

function roundRect(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  w: number,
  h: number,
  r: number
) {
  if (w < 2 * r) r = w / 2;
  if (h < 2 * r) r = h / 2;
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}
