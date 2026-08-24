/**
 * Client-Side Audio Extractor for CaptionForge
 * Extracts the audio track from any video File/Blob directly in the browser
 * and resamples it to 16kHz Mono 16-bit PCM WAV.
 * 
 * Why this is crucial:
 * A 1GB 4K/1080p video audio track becomes just ~2-5MB of 16kHz WAV,
 * uploading in under 1 second instead of waiting minutes to upload GBs of video!
 */

export interface AudioExtractionProgress {
  phase: 'decoding' | 'processing' | 'encoding';
  progress: number; // 0 to 100
}

export async function extractAudioFromVideo(
  videoFile: File | Blob,
  onProgress?: (progress: AudioExtractionProgress) => void
): Promise<{ wavBlob: Blob; duration: number }> {
  onProgress?.({ phase: 'decoding', progress: 10 });

  const arrayBuffer = await videoFile.arrayBuffer();
  onProgress?.({ phase: 'decoding', progress: 30 });

  // Use AudioContext to decode the video's audio track
  const audioContext = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)();
  
  let audioBuffer: AudioBuffer;
  try {
    audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
  } catch (err) {
    console.error('AudioContext decodeAudioData error:', err);
    throw new Error('Failed to decode audio track from video file. Make sure the video contains an active audio track.');
  } finally {
    audioContext.close();
  }

  onProgress?.({ phase: 'processing', progress: 50 });

  const targetSampleRate = 16000;
  const duration = audioBuffer.duration;
  const targetLength = Math.ceil(duration * targetSampleRate);

  // Render to 16kHz Mono using OfflineAudioContext for maximum speed & quality
  const offlineContext = new OfflineAudioContext(1, targetLength, targetSampleRate);
  const source = offlineContext.createBufferSource();
  source.buffer = audioBuffer;
  source.connect(offlineContext.destination);
  source.start(0);

  onProgress?.({ phase: 'processing', progress: 70 });
  const renderedBuffer = await offlineContext.startRendering();

  onProgress?.({ phase: 'encoding', progress: 85 });
  const wavBlob = audioBufferToWavBlob(renderedBuffer);

  onProgress?.({ phase: 'encoding', progress: 100 });

  return {
    wavBlob,
    duration,
  };
}

/**
 * Converts an AudioBuffer (16kHz Mono) into a valid 16-bit PCM WAV Blob
 */
function audioBufferToWavBlob(buffer: AudioBuffer): Blob {
  const numChannels = 1;
  const sampleRate = buffer.sampleRate;
  const format = 1; // PCM
  const bitDepth = 16;
  const bytesPerSample = bitDepth / 8;
  const blockAlign = numChannels * bytesPerSample;
  
  const channelData = buffer.getChannelData(0);
  const dataLength = channelData.length * bytesPerSample;
  const bufferLength = 44 + dataLength;

  const arrayBuffer = new ArrayBuffer(bufferLength);
  const view = new DataView(arrayBuffer);

  // RIFF identifier
  writeString(view, 0, 'RIFF');
  // RIFF chunk length
  view.setUint32(4, 36 + dataLength, true);
  // RIFF type
  writeString(view, 8, 'WAVE');
  // format chunk identifier
  writeString(view, 12, 'fmt ');
  // format chunk length
  view.setUint32(16, 16, true);
  // sample format (raw PCM)
  view.setUint16(20, format, true);
  // channel count
  view.setUint16(22, numChannels, true);
  // sample rate
  view.setUint32(24, sampleRate, true);
  // byte rate (sample rate * block align)
  view.setUint32(28, sampleRate * blockAlign, true);
  // block align (channel count * bytes per sample)
  view.setUint16(32, blockAlign, true);
  // bits per sample
  view.setUint16(34, bitDepth, true);
  // data chunk identifier
  writeString(view, 36, 'data');
  // data chunk length
  view.setUint32(40, dataLength, true);

  // Write 16-bit PCM audio samples with clipping protection
  let offset = 44;
  for (let i = 0; i < channelData.length; i++, offset += 2) {
    const s = Math.max(-1, Math.min(1, channelData[i]));
    view.setInt16(offset, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
  }

  return new Blob([view], { type: 'audio/wav' });
}

function writeString(view: DataView, offset: number, string: string): void {
  for (let i = 0; i < string.length; i++) {
    view.setUint8(offset + i, string.charCodeAt(i));
  }
}
