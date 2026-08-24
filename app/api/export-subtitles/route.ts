import { NextRequest, NextResponse } from 'next/server';
import { generateAssSubtitles, generateSrtSubtitles, generateVttSubtitles } from '@/lib/assGenerator';
import { CaptionStyleConfig, WordCaption } from '@/lib/types';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { words, style, format, width, height } = body as {
      words: WordCaption[];
      style: CaptionStyleConfig;
      format: 'ass' | 'srt' | 'vtt';
      width?: number;
      height?: number;
    };

    if (!words || !Array.isArray(words)) {
      return NextResponse.json({ error: 'Invalid or missing words array.' }, { status: 400 });
    }

    let fileContent = '';
    let contentType = 'text/plain';
    let filename = 'captions';

    if (format === 'ass') {
      fileContent = generateAssSubtitles(words, style, width || 1920, height || 1080);
      contentType = 'text/x-ssa';
      filename = 'captions.ass';
    } else if (format === 'srt') {
      fileContent = generateSrtSubtitles(words, style.wordsPerChunk || 4);
      contentType = 'application/x-subrip';
      filename = 'captions.srt';
    } else if (format === 'vtt') {
      fileContent = generateVttSubtitles(words, style.wordsPerChunk || 4);
      contentType = 'text/vtt';
      filename = 'captions.vtt';
    } else {
      return NextResponse.json({ error: 'Unsupported format requested.' }, { status: 400 });
    }

    return new NextResponse(fileContent, {
      headers: {
        'Content-Type': contentType,
        'Content-Disposition': `attachment; filename="${filename}"`,
      },
    });
  } catch (error: any) {
    console.error('Subtitle export error:', error);
    return NextResponse.json({ error: error?.message || 'Subtitle generation failed.' }, { status: 500 });
  }
}
