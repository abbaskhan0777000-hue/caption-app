import { NextRequest, NextResponse } from 'next/server';
import { transcribeAudioWithGroq } from '@/lib/groq';

export const dynamic = 'force-dynamic';
export const maxDuration = 60; // 60s max execution time

export async function POST(req: NextRequest) {
  try {
    const formData = await req.formData();
    const audioFile = formData.get('audio') as File | null;
    const apiKey = (formData.get('apiKey') as string) || process.env.GROQ_API_KEY || '';
    const language = (formData.get('language') as string) || undefined;

    if (!audioFile) {
      return NextResponse.json(
        { error: 'No audio file provided in request.' },
        { status: 400 }
      );
    }

    if (!apiKey) {
      return NextResponse.json(
        { 
          error: 'Groq API Key is required. Please add your Groq API key in the top settings bar or set GROQ_API_KEY in your .env.local file.',
          missingKey: true
        },
        { status: 400 }
      );
    }

    const arrayBuffer = await audioFile.arrayBuffer();
    const buffer = Buffer.from(arrayBuffer);

    const result = await transcribeAudioWithGroq({
      audioBuffer: buffer,
      mimeType: audioFile.type || 'audio/wav',
      apiKey,
      language,
    });

    return NextResponse.json({
      success: true,
      text: result.text,
      words: result.words,
    });
  } catch (error: any) {
    console.error('Transcription API error:', error);
    const isRateLimit = error?.message?.includes('rate limit') || error?.message?.includes('429');
    const isAuth = error?.message?.includes('API Key') || error?.message?.includes('401');

    return NextResponse.json(
      {
        error: error?.message || 'Failed to transcribe audio.',
        isRateLimit,
        isAuth,
      },
      { status: isRateLimit ? 429 : isAuth ? 401 : 500 }
    );
  }
}
