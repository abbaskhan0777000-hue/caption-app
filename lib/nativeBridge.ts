/**
 * CaptionForge Native Android Hardware Bridge
 * 
 * Directly bridges to the native Android C++ FFmpegKit / MediaCodec hardware engine.
 */

export interface NativeRenderRequest {
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
  return !!(window as any).AndroidBridge?.isNative?.() || !!(window as any).Capacitor?.isNativePlatform?.();
};

/**
 * Calls native Android Hardware C++ FFmpeg to burn captions into MP4
 */
export async function renderWithAndroidHardware(
  req: NativeRenderRequest,
  onProgress?: (progress: number) => void
): Promise<NativeRenderResponse> {
  if (!isNativeAndroidApp()) {
    return { success: false, error: 'Not running in native Android environment.' };
  }

  return new Promise((resolve) => {
    const androidBridge = (window as any).AndroidBridge;

    if (androidBridge && androidBridge.burnCaptions) {
      // Register global callbacks
      (window as any).onNativeProgress = (percent: number) => {
        onProgress?.(percent);
      };

      (window as any).onNativeSuccess = (outputPath: string) => {
        resolve({ success: true, outputPath });
      };

      (window as any).onNativeError = (errMsg: string) => {
        resolve({ success: false, error: errMsg });
      };

      // Trigger native C++ burn
      androidBridge.burnCaptions(req.videoBlobUrl || '', req.assContent);
    } else {
      resolve({ success: false, error: 'AndroidBridge interface not available.' });
    }
  });
}
