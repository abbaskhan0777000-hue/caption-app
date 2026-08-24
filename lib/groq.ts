import Groq, { toFile } from 'groq-sdk';
import { WordCaption } from './types';

export interface TranscribeAudioOptions {
  audioBuffer: Buffer;
  mimeType?: string;
  apiKey?: string;
  language?: string;
  prompt?: string;
}

export async function transcribeAudioWithGroq(
  options: TranscribeAudioOptions
): Promise<{ text: string; words: WordCaption[] }> {
  const apiKey = options.apiKey || process.env.GROQ_API_KEY;

  if (!apiKey || apiKey.trim() === '') {
    throw new Error(
      'Groq API key is missing. Please provide your Groq API key in the top settings bar or set GROQ_API_KEY in your environment.'
    );
  }

  const groq = new Groq({
    apiKey: apiKey.trim(),
  });

  try {
    // Use official groq-sdk toFile helper for reliable buffer conversion in Node.js
    const audioFile = await toFile(options.audioBuffer, 'audio.wav', {
      type: options.mimeType || 'audio/wav',
    });

    // Call Groq Whisper Large v3 Turbo with word-level timestamps
    const transcription = await groq.audio.transcriptions.create({
      file: audioFile,
      model: 'whisper-large-v3-turbo',
      response_format: 'verbose_json',
      timestamp_granularities: ['word'],
      language: options.language,
      prompt: options.prompt,
      temperature: 0.0,
    });

    const wordsData = (transcription as any).words || [];
    const words: WordCaption[] = wordsData.map((item: any, idx: number) => ({
      id: `w-${idx}-${Math.random().toString(36).substring(2, 7)}`,
      word: item.word?.trim() || '',
      start: Number(item.start) || 0,
      end: Number(item.end) || 0,
    })).filter((w: WordCaption) => w.word.length > 0);

    return {
      text: transcription.text || '',
      words,
    };
  } catch (error: any) {
    console.error('Groq transcription error:', error);

    // Friendly error handling for common scenarios
    if (error?.status === 429 || error?.message?.includes('429') || error?.message?.includes('Rate limit')) {
      throw new Error(
        'Groq API rate limit reached (20 req/min or daily token quota). Please wait a moment or check your Groq console.'
      );
    }
    if (error?.status === 401 || error?.message?.includes('401') || error?.message?.includes('Invalid API Key')) {
      throw new Error('Invalid Groq API Key. Please verify your Groq API key.');
    }

    throw new Error(error?.message || 'Failed to transcribe audio with Groq Whisper.');
  }
}
