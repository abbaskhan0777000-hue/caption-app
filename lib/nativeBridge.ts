/**
 * CaptionForge Native Android Hardware Bridge
 * 
 * Directly bridges to the native Android C++ FFmpegKit / MediaCodec hardware engine.
 */

export interface NativeRenderRequest {
  videoFile?: File | Blob | null;
  videoBlobUrl?: string;
  assContent: string;
  resolution: string;
  fps: number;
}

export interface NativeRenderResponse {
  success: boolean;
  outputPath?: string;
  error?: string;
}

export const isNativeAndroidApp = (): boolean => {
  if (typeof window === 'undefined') return false;
  return !!(window as any).AndroidBridge?.isNative?.();
};

/**
 * Transfers a video file from browser memory to Android native cache using streaming chunks
 */
export async function transferVideoToAndroidNative(
  file: Blob,
  filename: string = 'input_video.mp4',
  onProgress?: (pct: number) => void
): Promise<string> {
  const bridge = (window as any).AndroidBridge;
  if (!bridge || !bridge.startVideoUpload) {
    throw new Error('Native AndroidBridge not initialized.');
  }

  bridge.startVideoUpload(filename);

  const CHUNK_SIZE = 1024 * 512; // 512KB chunks
  const total = file.size;
  let offset = 0;

  while (offset < total) {
    const slice = file.slice(offset, offset + CHUNK_SIZE);
    const buffer = await slice.arrayBuffer();
    const bytes = new Uint8Array(buffer);
    
    // Efficient binary to base64
    let binary = '';
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    const base64Chunk = btoa(binary);

    bridge.appendVideoChunk(base64Chunk);
    offset += CHUNK_SIZE;
    if (onProgress) {
      onProgress(Math.min(99, Math.round((offset / total) * 100)));
    }
  }

  const nativePath = bridge.finishVideoUpload();
  if (!nativePath) {
    throw new Error('Failed to assemble video file on device.');
  }
  return nativePath;
}

/**
 * Calls native Android Hardware C++ FFmpeg to burn captions into MP4
 */
export async function renderWithAndroidHardware(
  req: NativeRenderRequest,
  onProgress?: (progress: number, statusText?: string) => void
): Promise<NativeRenderResponse> {
  if (!isNativeAndroidApp()) {
    return { success: false, error: 'Not running in native Android environment.' };
  }

  return new Promise(async (resolve) => {
    const androidBridge = (window as any).AndroidBridge;

    try {
      let nativeVideoPath = '';

      if (req.videoFile) {
        onProgress?.(0, 'Transferring video to Hardware Encoder...');
        nativeVideoPath = await transferVideoToAndroidNative(
          req.videoFile,
          'video.mp4',
          (pct) => onProgress?.(Math.round(pct * 0.2), `Preparing video payload (${pct}%)...`)
        );
      }

      onProgress?.(20, '⚡ Encoding with Android Hardware GPU...');

      // Register global callbacks
      (window as any).onNativeProgress = (percent: number) => {
        const scaled = 20 + Math.round(percent * 0.8);
        onProgress?.(scaled, `Hardware rendering: ${percent}%...`);
      };

      (window as any).onNativeSuccess = (outputPath: string) => {
        resolve({ success: true, outputPath });
      };

      (window as any).onNativeError = (errMsg: string) => {
        resolve({ success: false, error: errMsg });
      };

      // Trigger native C++ burn
      androidBridge.burnCaptions(nativeVideoPath, req.assContent);
    } catch (err: any) {
      resolve({ success: false, error: err?.message || 'Native render transfer failed.' });
    }
  });
}
