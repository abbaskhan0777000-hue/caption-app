'use client';

import React, { useState, useEffect, useRef } from 'react';
import { WordCaption } from '@/lib/types';
import {
  Search,
  Check,
  Edit2,
  Trash2,
  Plus,
  Play,
  RotateCcw,
  Sparkles,
  Scissors,
  FileText,
} from 'lucide-react';

interface TranscriptEditorProps {
  words: WordCaption[];
  currentTime: number;
  onSeek: (seconds: number) => void;
  onUpdateWords: (newWords: WordCaption[]) => void;
}

export const TranscriptEditor: React.FC<TranscriptEditorProps> = ({
  words,
  currentTime,
  onSeek,
  onUpdateWords,
}) => {
  const [editingWordId, setEditingWordId] = useState<string | null>(null);
  const [editValue, setEditValue] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [replaceQuery, setReplaceQuery] = useState('');
  const [showSearchReplace, setShowSearchReplace] = useState(false);
  const activeWordRef = useRef<HTMLButtonElement | null>(null);

  // Keep active word scrolled into view gently during playback
  useEffect(() => {
    if (activeWordRef.current) {
      activeWordRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'nearest',
        inline: 'center',
      });
    }
  }, [currentTime]);

  const handleStartEdit = (word: WordCaption) => {
    setEditingWordId(word.id);
    setEditValue(word.word);
  };

  const handleSaveEdit = (wordId: string) => {
    if (!editValue.trim()) {
      handleDeleteWord(wordId);
      return;
    }

    const updated = words.map((w) =>
      w.id === wordId ? { ...w, word: editValue.trim() } : w
    );
    onUpdateWords(updated);
    setEditingWordId(null);
    setEditValue('');
  };

  const handleDeleteWord = (wordId: string) => {
    const updated = words.filter((w) => w.id !== wordId);
    onUpdateWords(updated);
    if (editingWordId === wordId) {
      setEditingWordId(null);
    }
  };

  const handleAddWordAfter = (index: number) => {
    const prev = words[index];
    const next = words[index + 1];
    const newStart = prev ? prev.end : 0;
    const newEnd = next ? Math.min(next.start, newStart + 0.4) : newStart + 0.4;

    const newWord: WordCaption = {
      id: `w-custom-${Date.now()}`,
      word: 'NewWord',
      start: Number(newStart.toFixed(2)),
      end: Number(newEnd.toFixed(2)),
    };

    const updated = [...words];
    updated.splice(index + 1, 0, newWord);
    onUpdateWords(updated);
    handleStartEdit(newWord);
  };

  const handleSearchReplace = () => {
    if (!searchQuery.trim()) return;
    const regex = new RegExp(searchQuery.trim(), 'gi');
    const updated = words.map((w) => ({
      ...w,
      word: w.word.replace(regex, replaceQuery),
    }));
    onUpdateWords(updated);
    setSearchQuery('');
    setReplaceQuery('');
    setShowSearchReplace(false);
  };

  return (
    <div className="h-full flex flex-col space-y-4 w-full min-w-0 overflow-x-hidden">
      {/* Header with Search & Stats */}
      <div className="flex items-center justify-between gap-2 border-b border-surface-border pb-3">
        <div className="flex items-center gap-2">
          <FileText className="w-4 h-4 text-accent-cyan" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
            Transcript & Word Timestamps
          </h3>
          <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-surface-border text-slate-300">
            {words.length} words
          </span>
        </div>

        <button
          onClick={() => setShowSearchReplace(!showSearchReplace)}
          className={`flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-medium border transition-all ${
            showSearchReplace
              ? 'bg-primary-600/30 border-primary-500 text-white'
              : 'bg-surface border-surface-border text-slate-300 hover:bg-surface-light'
          }`}
        >
          <Search className="w-3.5 h-3.5" />
          <span>Find & Replace</span>
        </button>
      </div>

      {/* Find & Replace Bar */}
      {showSearchReplace && (
        <div className="p-3 rounded-xl bg-surface border border-surface-border space-y-2 animate-in fade-in duration-150">
          <div className="grid grid-cols-2 gap-2">
            <input
              type="text"
              placeholder="Find word..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="text-xs bg-surface-light border border-surface-border px-3 py-1.5 rounded-lg text-white focus:outline-none focus:border-primary-500"
            />
            <input
              type="text"
              placeholder="Replace with..."
              value={replaceQuery}
              onChange={(e) => setReplaceQuery(e.target.value)}
              className="text-xs bg-surface-light border border-surface-border px-3 py-1.5 rounded-lg text-white focus:outline-none focus:border-primary-500"
            />
          </div>
          <div className="flex justify-end gap-2">
            <button
              onClick={() => setShowSearchReplace(false)}
              className="px-2.5 py-1 text-xs text-slate-400 hover:text-white"
            >
              Cancel
            </button>
            <button
              onClick={handleSearchReplace}
              className="px-3 py-1 text-xs font-semibold bg-primary-600 hover:bg-primary-500 text-white rounded-lg transition-all"
            >
              Replace All
            </button>
          </div>
        </div>
      )}

      {/* Interactive Word Pills Container */}
      <div className="flex-1 overflow-y-auto pr-1 space-y-3 custom-scrollbar">
        {words.length === 0 ? (
          <div className="text-center py-12 text-slate-400 text-xs">
            No words transcribed yet. Upload a video to transcribe speech automatically.
          </div>
        ) : (
          <div className="flex flex-wrap gap-2 items-center p-3 rounded-2xl bg-surface/50 border border-surface-border">
            {words.map((wordObj, index) => {
              const isActive = currentTime >= wordObj.start && currentTime <= wordObj.end;
              const isEditing = editingWordId === wordObj.id;

              if (isEditing) {
                return (
                  <div
                    key={wordObj.id}
                    className="inline-flex items-center gap-1 p-1 rounded-xl bg-primary-600/30 border border-primary-500 shadow-md"
                  >
                    <input
                      type="text"
                      autoFocus
                      value={editValue}
                      onChange={(e) => setEditValue(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') handleSaveEdit(wordObj.id);
                        if (e.key === 'Escape') setEditingWordId(null);
                      }}
                      className="w-24 px-2 py-1 text-xs bg-surface text-white rounded border border-primary-400 focus:outline-none font-bold"
                    />
                    <button
                      onClick={() => handleSaveEdit(wordObj.id)}
                      className="p-1 text-emerald-400 hover:text-emerald-300"
                      title="Save"
                    >
                      <Check className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleDeleteWord(wordObj.id)}
                      className="p-1 text-red-400 hover:text-red-300"
                      title="Delete"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                );
              }

              return (
                <div
                  key={wordObj.id}
                  className="group relative inline-flex items-center"
                >
                  <button
                    ref={isActive ? activeWordRef : null}
                    onClick={() => onSeek(wordObj.start)}
                    className={`px-2.5 py-1.5 rounded-xl text-xs font-medium transition-all duration-150 flex items-center gap-1.5 border ${
                      isActive
                        ? 'bg-accent-yellow text-black border-accent-yellow font-bold shadow-lg shadow-accent-yellow/20 scale-105 z-10'
                        : 'bg-surface border-surface-border text-slate-200 hover:bg-surface-light hover:border-slate-600'
                    }`}
                  >
                    <span>{wordObj.word}</span>
                    <span
                      className={`text-[9px] font-mono ${
                        isActive ? 'text-black/70' : 'text-slate-500'
                      }`}
                    >
                      {wordObj.start.toFixed(1)}s
                    </span>
                  </button>

                  {/* Hover action menu */}
                  <div className="absolute -top-7 left-1/2 -translate-x-1/2 hidden group-hover:flex items-center gap-0.5 bg-surface-light border border-surface-border rounded-lg p-0.5 shadow-xl z-30">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleStartEdit(wordObj);
                      }}
                      className="p-1 hover:bg-surface-border rounded text-slate-300 hover:text-white"
                      title="Edit spelling"
                    >
                      <Edit2 className="w-3 h-3" />
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleAddWordAfter(index);
                      }}
                      className="p-1 hover:bg-surface-border rounded text-slate-300 hover:text-emerald-400"
                      title="Add word after"
                    >
                      <Plus className="w-3 h-3" />
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteWord(wordObj.id);
                      }}
                      className="p-1 hover:bg-surface-border rounded text-slate-300 hover:text-red-400"
                      title="Delete word"
                    >
                      <Trash2 className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Helpful Hint */}
      <div className="p-2.5 rounded-xl bg-surface/80 border border-surface-border text-[11px] text-slate-400 flex items-center justify-between">
        <span>💡 Click any word to jump video time. Hover to edit spelling.</span>
        <button
          onClick={() => {
            const fullText = words.map((w) => w.word).join(' ');
            navigator.clipboard.writeText(fullText);
            alert('Full transcript copied to clipboard!');
          }}
          className="text-primary-400 hover:text-primary-300 font-medium"
        >
          Copy All
        </button>
      </div>
    </div>
  );
};
