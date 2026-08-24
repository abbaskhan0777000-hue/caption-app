'use client';

import React, { useState } from 'react';
import { Sparkles, Key, Check, RefreshCw, Film, ExternalLink } from 'lucide-react';

interface NavbarProps {
  apiKey: string;
  onApiKeyChange: (key: string) => void;
  onLoadSample: () => void;
  onReset: () => void;
  hasVideo: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  apiKey,
  onApiKeyChange,
  onLoadSample,
  onReset,
  hasVideo,
}) => {
  const [showKeyModal, setShowKeyModal] = useState(false);
  const [tempKey, setTempKey] = useState(apiKey);
  const [savedSuccess, setSavedSuccess] = useState(false);

  const handleSaveKey = () => {
    onApiKeyChange(tempKey);
    setSavedSuccess(true);
    setTimeout(() => {
      setSavedSuccess(false);
      setShowKeyModal(false);
    }, 1000);
  };

  return (
    <>
      <header className="sticky top-0 z-40 w-full max-w-[100vw] border-b border-surface-border bg-surface/90 backdrop-blur-md px-3 sm:px-6 lg:px-8 py-2.5 overflow-hidden">
        <div className="max-w-7xl mx-auto flex items-center justify-between gap-2 min-w-0">
          {/* Logo */}
          <div className="flex items-center gap-2 sm:gap-3 min-w-0">
            <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-primary-600 via-accent-pink to-accent-yellow p-0.5 shadow-lg shadow-primary-500/20 shrink-0">
              <div className="w-full h-full bg-surface rounded-[9px] flex items-center justify-center">
                <Sparkles className="w-4 h-4 sm:w-5 sm:h-5 text-accent-yellow" />
              </div>
            </div>
            <div className="min-w-0">
              <div className="flex items-center gap-1.5 min-w-0">
                <span className="font-extrabold text-base sm:text-lg tracking-tight bg-gradient-to-r from-white via-slate-200 to-primary-300 bg-clip-text text-transparent truncate">
                  CaptionForge
                </span>
                <span className="text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.2 rounded-full bg-primary-500/20 text-primary-300 border border-primary-500/30 shrink-0">
                  AI
                </span>
              </div>
              <p className="text-[10px] text-slate-400 hidden md:block truncate">
                CapCut-Style Animated Captions
              </p>
            </div>
          </div>

          {/* Right Action Bar */}
          <div className="flex items-center gap-1.5 sm:gap-2.5 shrink-0">
            {!hasVideo && (
              <button
                onClick={onLoadSample}
                className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium bg-surface-light hover:bg-surface-border text-slate-200 border border-surface-border transition-all"
              >
                <Film className="w-3.5 h-3.5 text-accent-cyan shrink-0" />
                <span className="hidden xs:inline text-[11px] sm:text-xs">Sample</span>
              </button>
            )}

            {hasVideo && (
              <button
                onClick={onReset}
                className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium bg-surface-light hover:bg-red-500/20 text-slate-300 hover:text-red-300 border border-surface-border transition-all"
                title="Start with a new video"
              >
                <RefreshCw className="w-3.5 h-3.5 shrink-0" />
                <span className="hidden sm:inline text-xs">New Video</span>
              </button>
            )}

            {/* Groq API Key Config Button */}
            <button
              onClick={() => {
                setTempKey(apiKey);
                setShowKeyModal(true);
              }}
              className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
                apiKey
                  ? 'bg-emerald-950/40 text-emerald-300 border-emerald-500/30 hover:bg-emerald-900/50'
                  : 'bg-amber-950/40 text-amber-300 border-amber-500/40 hover:bg-amber-900/50 animate-pulse'
              }`}
            >
              <Key className="w-3.5 h-3.5 shrink-0" />
              <span className="text-[11px] sm:text-xs">{apiKey ? 'Groq Key' : 'Set Key'}</span>
            </button>
          </div>
        </div>
      </header>

      {/* Groq API Key Modal */}
      {showKeyModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="w-full max-w-md bg-surface border border-surface-border rounded-2xl p-5 sm:p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-lg bg-primary-500/20 text-primary-400">
                  <Key className="w-5 h-5" />
                </div>
                <h3 className="font-semibold text-white text-base">Groq API Configuration</h3>
              </div>
              <button
                onClick={() => setShowKeyModal(false)}
                className="text-slate-400 hover:text-white text-base"
              >
                &times;
              </button>
            </div>

            <p className="text-xs text-slate-300 leading-relaxed">
              CaptionForge uses <strong>Groq Whisper Large v3 Turbo</strong> to transcribe audio with word-level timestamps in seconds.
            </p>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">
                Groq API Key (starts with <code className="text-accent-cyan">gsk_...</code>)
              </label>
              <input
                type="password"
                value={tempKey}
                onChange={(e) => setTempKey(e.target.value)}
                placeholder="gsk_..."
                className="w-full px-3.5 py-2.5 rounded-xl bg-surface-light border border-surface-border text-white text-sm focus:outline-none focus:border-primary-500 font-mono"
              />
            </div>

            <div className="flex items-center justify-between pt-2">
              <a
                href="https://console.groq.com/keys"
                target="_blank"
                rel="noreferrer"
                className="text-xs text-primary-400 hover:text-primary-300 flex items-center gap-1 hover:underline truncate"
              >
                Get free key <ExternalLink className="w-3 h-3 shrink-0" />
              </a>

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setShowKeyModal(false)}
                  className="px-3.5 py-2 text-xs font-medium rounded-lg text-slate-400 hover:text-white bg-surface-light transition-all"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleSaveKey}
                  disabled={savedSuccess}
                  className="px-4 py-2 text-xs font-semibold rounded-lg bg-gradient-to-r from-primary-600 to-indigo-600 hover:from-primary-500 text-white flex items-center gap-1.5 shadow-md transition-all"
                >
                  {savedSuccess ? (
                    <>
                      <Check className="w-4 h-4 text-emerald-400" /> Saved!
                    </>
                  ) : (
                    'Save Key'
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
