/**
 * Pure TypeScript WebM EBML Duration & Metadata Fixer
 * 
 * Why this is essential:
 * Chromium / Firefox MediaRecorder creates WebM video streams without the 'Duration'
 * header element (duration = Infinity / NaN). This causes video players and browsers to 
 * stall, freeze, stutter, and repeatedly stop and play in intervals when playing back!
 * 
 * This module reads the EBML container, locates the Segment Info block, and writes the 
 * exact floating-point Duration value into the header.
 */

export async function fixWebmDuration(blob: Blob, durationMs: number): Promise<Blob> {
  const arrayBuffer = await blob.arrayBuffer();
  const bytes = new Uint8Array(arrayBuffer);

  // EBML Header IDs
  // 0x18538067: Segment
  // 0x1549A966: Info
  // 0x2AD7B1: TimecodeScale / TimestampScale (4 bytes int)
  // 0x4489: Duration (float)

  const segmentPos = findElement(bytes, [0x18, 0x53, 0x80, 0x67], 0);
  if (segmentPos === -1) {
    console.warn('Could not find Segment in WebM container');
    return blob;
  }

  const infoPos = findElement(bytes, [0x15, 0x49, 0xa9, 0x66], segmentPos);
  if (infoPos === -1) {
    console.warn('Could not find Info in WebM container');
    return blob;
  }

  // Check if Duration (0x4489) element already exists inside Info
  const durationPos = findElement(bytes, [0x44, 0x89], infoPos);
  
  if (durationPos !== -1 && durationPos < infoPos + 200) {
    // Overwrite existing duration float (usually 8-byte float64 or 4-byte float32)
    const lenByte = bytes[durationPos + 2];
    const dataView = new DataView(arrayBuffer);
    if (lenByte === 4 || lenByte === 0x84) {
      dataView.setFloat32(durationPos + 3, durationMs, false);
      return new Blob([dataView.buffer], { type: blob.type });
    } else if (lenByte === 8 || lenByte === 0x88) {
      dataView.setFloat64(durationPos + 3, durationMs, false);
      return new Blob([dataView.buffer], { type: blob.type });
    }
  }

  // If Duration element does not exist, inject Duration (0x4489, 8 bytes float64) into Info
  const durationElement = createDurationElement(durationMs);
  
  // Find injection point right after Info header and size
  let offset = infoPos + 4;
  const sizeLength = getVIntLength(bytes[offset]);
  offset += sizeLength;

  // Rebuild arrayBuffer with injected duration element
  const newLength = bytes.length + durationElement.length;
  const newBytes = new Uint8Array(newLength);
  
  newBytes.set(bytes.subarray(0, offset), 0);
  newBytes.set(durationElement, offset);
  newBytes.set(bytes.subarray(offset), offset + durationElement.length);

  return new Blob([newBytes.buffer], { type: blob.type });
}

function findElement(bytes: Uint8Array, pattern: number[], startPos = 0): number {
  const maxSearch = Math.min(bytes.length - pattern.length, startPos + 5000);
  for (let i = startPos; i < maxSearch; i++) {
    let match = true;
    for (let j = 0; j < pattern.length; j++) {
      if (bytes[i + j] !== pattern[j]) {
        match = false;
        break;
      }
    }
    if (match) return i;
  }
  return -1;
}

function getVIntLength(byte: number): number {
  if (byte & 0x80) return 1;
  if (byte & 0x40) return 2;
  if (byte & 0x20) return 3;
  if (byte & 0x10) return 4;
  return 1;
}

function createDurationElement(durationMs: number): Uint8Array {
  // EBML element: 0x4489 (Duration), size: 0x88 (8 bytes), data: Float64
  const element = new Uint8Array(11);
  element[0] = 0x44;
  element[1] = 0x89;
  element[2] = 0x88; // 8 bytes length (VINT)

  const view = new DataView(element.buffer);
  view.setFloat64(3, durationMs, false); // Big endian float64

  return element;
}
