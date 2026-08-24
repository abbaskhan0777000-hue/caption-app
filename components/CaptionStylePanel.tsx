'use client';

import React, { useState, useRef } from 'react';
import { CaptionStyleConfig, FontChoice, ActiveWordEffect, ShadowStyle, CustomFontItem } from '@/lib/types';
import { STYLE_PRESETS, INITIAL_FONTS, loadGoogleFontDynamically, registerCustomFontFile } from '@/lib/captionStyles';
import {
  Sparkles,
  Type,
  Palette,
  MoveVertical,
  Flame,
  Zap,
  Sliders,
  Layers,
  Crown,
  Upload,
  Search,
  Plus,
  Check,
  Maximize2,
  RotateCw,
  Highlighter,
  AlignVerticalJustifyCenter,
  AlignVerticalJustifyStart,
  AlignVerticalJustifyEnd,
} from 'lucide-react';

interface CaptionStylePanelProps {
  style: CaptionStyleConfig;
  onChange: (newStyle: CaptionStyleConfig) => void;
}

export const CaptionStylePanel: React.FC<CaptionStylePanelProps> = ({ style, onChange }) => {
  const [fontsList, setFontsList] = useState<CustomFontItem[]>(INITIAL_FONTS);
  const [customGoogleFontInput, setCustomGoogleFontInput] = useState('');
  const [isFontLoading, setIsFontLoading] = useState(false);
  const [fontLoadSuccess, setFontLoadSuccess] = useState<string | null>(null);
  const fontFileInputRef = useRef<HTMLInputElement>(null);

  const updateStyle = (patch: Partial<CaptionStyleConfig>) => {
    onChange({ ...style, ...patch });
  };

  const applyPreset = (presetConfig: Partial<CaptionStyleConfig>) => {
    onChange({ ...style, ...presetConfig });
  };

  // Handle uploading custom TTF / OTF / WOFF font file
  const handleUploadFontFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      try {
        setIsFontLoading(true);
        const newFontItem = await registerCustomFontFile(file);
        setFontsList((prev) => [newFontItem, ...prev]);
        updateStyle({ fontFamily: newFontItem.name });
        setFontLoadSuccess(`Added custom font: ${newFontItem.name}`);
        setTimeout(() => setFontLoadSuccess(null), 3000);
      } catch (err: any) {
        alert('Failed to load font file: ' + err.message);
      } finally {
        setIsFontLoading(false);
      }
    }
  };

  // Handle loading any Google Font dynamically by name
  const handleLoadGoogleFont = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customGoogleFontInput.trim()) return;

    const fontName = customGoogleFontInput.trim();
    setIsFontLoading(true);
    const success = await loadGoogleFontDynamically(fontName);
    setIsFontLoading(false);

    if (success) {
      const newFontItem: CustomFontItem = {
        name: fontName,
        label: `${fontName} (Google Font)`,
        category: 'Google Web Font',
        isCustom: true,
      };
      if (!fontsList.some((f) => f.name.toLowerCase() === fontName.toLowerCase())) {
        setFontsList((prev) => [newFontItem, ...prev]);
      }
      updateStyle({ fontFamily: fontName });
      setFontLoadSuccess(`Loaded font: ${fontName}`);
      setCustomGoogleFontInput('');
      setTimeout(() => setFontLoadSuccess(null), 3000);
    } else {
      alert(`Could not load "${fontName}". Make sure the Google Font name is spelled correctly.`);
    }
  };

  const activeEffects: Array<{ id: ActiveWordEffect; name: string; desc: string; icon: string }> = [
    { id: 'color', name: 'Color Switch', desc: 'Classic CapCut yellow/bright highlight', icon: '🎨' },
    { id: 'marker', name: 'Marker Streak', desc: 'Ali Abdaal highlighter pen streak', icon: '🖍️' },
    { id: 'pill', name: 'Solid Pill Badge', desc: 'Alex Hormozi high-contrast badge', icon: '💊' },
    { id: 'scale-pop', name: '3D Scale Punch', desc: 'MrBeast punchy word scale + tilt', icon: '💥' },
    { id: 'glow', name: 'Neon Aurora Glow', desc: 'Intense luminous cyberpunk glow', icon: '🔮' },
  ];

  const shadowStyles: Array<{ id: ShadowStyle; label: string; desc: string }> = [
    { id: 'soft', label: 'Soft Shadow', desc: 'Natural cinematic drop shadow' },
    { id: '3d-hard', label: '3D Hard Shadow', desc: 'MrBeast bold extrusion shadow' },
    { id: 'neon-glow', label: 'Neon Glow', desc: 'Multi-layer electric glow aura' },
    { id: 'none', label: 'None', desc: 'Clean flat appearance' },
  ];

  return (
    <div className="h-full flex flex-col overflow-y-auto space-y-6 pr-1 custom-scrollbar w-full min-w-0 overflow-x-hidden">
      {/* 1. CapCut Viral Presets Bar */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Flame className="w-4 h-4 text-accent-yellow" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
              Trending Creator Presets
            </h3>
          </div>
          <span className="text-[10px] text-accent-cyan font-semibold">1-Click Apply</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          {STYLE_PRESETS.map((preset) => (
            <button
              key={preset.id}
              onClick={() => applyPreset(preset.config)}
              className="group text-left p-3 rounded-2xl bg-surface border border-surface-border hover:border-primary-500 hover:bg-surface-light transition-all flex flex-col justify-between space-y-2 relative overflow-hidden shadow-sm hover:shadow-lg"
            >
              <div className="flex items-center justify-between w-full">
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-surface-border text-slate-200 border border-white/5">
                  {preset.badge}
                </span>
                <span
                  className="w-3.5 h-3.5 rounded-full shadow-md"
                  style={{ backgroundColor: preset.thumbnailColor }}
                />
              </div>

              <div>
                <div className="flex items-center justify-between">
                  <p className="text-xs font-extrabold text-white group-hover:text-primary-300 transition-colors">
                    {preset.name}
                  </p>
                  <span className="text-[9px] text-slate-400 font-medium">{preset.creator}</span>
                </div>
                <p className="text-[10px] text-slate-400 line-clamp-2 mt-0.5 leading-relaxed">
                  {preset.description}
                </p>
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* 2. Custom Fonts & Typography */}
      <div className="space-y-3 border-t border-surface-border pt-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Type className="w-4 h-4 text-accent-pink" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
              Font Library & Custom Fonts
            </h3>
          </div>

          <button
            onClick={() => fontFileInputRef.current?.click()}
            className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-semibold bg-primary-600/20 hover:bg-primary-600/30 text-primary-300 border border-primary-500/30 transition-all"
            title="Upload .ttf, .otf, .woff file"
          >
            <Upload className="w-3 h-3" />
            <span>Upload Font File</span>
          </button>
          <input
            ref={fontFileInputRef}
            type="file"
            accept=".ttf,.otf,.woff,.woff2"
            onChange={handleUploadFontFile}
            className="hidden"
          />
        </div>

        {/* Search / Load Any Google Font Form */}
        <form onSubmit={handleLoadGoogleFont} className="flex gap-2">
          <div className="relative flex-1">
            <input
              type="text"
              placeholder="Type any Google Font (e.g. Bangers, Outfit, Rubik)..."
              value={customGoogleFontInput}
              onChange={(e) => setCustomGoogleFontInput(e.target.value)}
              className="w-full text-xs bg-surface-light border border-surface-border pl-3 pr-8 py-2 rounded-xl text-white focus:outline-none focus:border-primary-500 font-medium placeholder:text-slate-500"
            />
          </div>
          <button
            type="submit"
            disabled={isFontLoading || !customGoogleFontInput.trim()}
            className="px-3 py-2 text-xs font-bold bg-surface-light hover:bg-surface-border border border-surface-border text-slate-200 rounded-xl transition-all disabled:opacity-40 flex items-center gap-1"
          >
            <Plus className="w-3.5 h-3.5 text-accent-cyan" />
            <span>Load</span>
          </button>
        </form>

        {fontLoadSuccess && (
          <div className="p-2 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2">
            <Check className="w-3.5 h-3.5" />
            <span>{fontLoadSuccess}</span>
          </div>
        )}

        {/* Font Cards Grid */}
        <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto pr-1 custom-scrollbar">
          {fontsList.map((f) => {
            const isSelected = style.fontFamily === f.name;
            return (
              <button
                key={f.name}
                onClick={() => updateStyle({ fontFamily: f.name })}
                className={`p-2.5 rounded-xl text-left border transition-all ${
                  isSelected
                    ? 'bg-primary-600/30 border-primary-400 text-white shadow-md ring-1 ring-primary-400'
                    : 'bg-surface border-surface-border text-slate-300 hover:bg-surface-light'
                }`}
              >
                <p className="text-sm font-black truncate" style={{ fontFamily: `"${f.name}", sans-serif` }}>
                  {f.name}
                </p>
                <div className="flex items-center justify-between mt-1">
                  <span className="text-[9px] text-slate-400 truncate">{f.label}</span>
                  <span className="text-[8px] font-semibold px-1 rounded bg-surface-border text-primary-300">
                    {f.category}
                  </span>
                </div>
              </button>
            );
          })}
        </div>

        {/* Font Size & Words per line */}
        <div className="space-y-3 pt-2">
          {/* Numeric Font Size Controller */}
          <div className="p-3 rounded-2xl bg-surface border border-surface-border space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-300">Font Size (Exact Number)</span>
              <div className="flex items-center gap-1.5">
                <button
                  onClick={() => updateStyle({ fontSize: Math.max(12, style.fontSize - 4) })}
                  className="w-7 h-7 rounded-lg bg-surface-light hover:bg-surface-border text-slate-300 flex items-center justify-center font-bold text-sm"
                  title="Decrease 4px"
                >
                  -
                </button>
                <div className="flex items-center gap-1 bg-surface-light px-2.5 py-1 rounded-xl border border-surface-border">
                  <input
                    type="number"
                    min="12"
                    max="200"
                    value={style.fontSize}
                    onChange={(e) => {
                      const val = Number(e.target.value);
                      if (!isNaN(val) && val >= 10 && val <= 250) {
                        updateStyle({ fontSize: val });
                      }
                    }}
                    className="w-12 text-center text-xs font-mono font-bold bg-transparent text-primary-400 focus:outline-none"
                  />
                  <span className="text-[10px] text-slate-400 font-mono">px</span>
                </div>
                <button
                  onClick={() => updateStyle({ fontSize: Math.min(200, style.fontSize + 4) })}
                  className="w-7 h-7 rounded-lg bg-surface-light hover:bg-surface-border text-slate-300 flex items-center justify-center font-bold text-sm"
                  title="Increase 4px"
                >
                  +
                </button>
              </div>
            </div>

            {/* Slider */}
            <input
              type="range"
              min="12"
              max="180"
              value={style.fontSize}
              onChange={(e) => updateStyle({ fontSize: Number(e.target.value) })}
              className="w-full accent-primary-500"
            />

            {/* Quick Size Presets */}
            <div className="flex gap-1.5 pt-1">
              {[
                { label: 'Small', size: 36 },
                { label: 'Standard', size: 52 },
                { label: 'Large', size: 76 },
                { label: 'Hormozi', size: 105 },
                { label: 'Mega', size: 140 },
              ].map((p) => (
                <button
                  key={p.size}
                  onClick={() => updateStyle({ fontSize: p.size })}
                  className={`flex-1 py-1 rounded-lg text-[10px] font-bold border transition-all ${
                    style.fontSize === p.size
                      ? 'bg-primary-600 border-primary-500 text-white shadow-sm'
                      : 'bg-surface-light border-surface-border text-slate-400 hover:text-white'
                  }`}
                >
                  {p.size}px
                </button>
              ))}
            </div>
          </div>

          {/* Words Per Screen */}
          <div className="p-3 rounded-2xl bg-surface border border-surface-border">
            <div className="flex justify-between text-xs text-slate-300 mb-1.5">
              <span>Words Per Screen</span>
              <span className="text-accent-cyan font-mono font-bold">
                {style.wordsPerChunk === 1 ? '1 (Hormozi Rapid)' : `${style.wordsPerChunk} words`}
              </span>
            </div>
            <input
              type="range"
              min="1"
              max="6"
              value={style.wordsPerChunk}
              onChange={(e) => updateStyle({ wordsPerChunk: Number(e.target.value) })}
              className="w-full accent-accent-cyan"
            />
          </div>
        </div>

        {/* Text Transform Buttons */}
        <div className="flex items-center gap-2 pt-1">
          <span className="text-xs text-slate-400">Case:</span>
          {(['uppercase', 'capitalize', 'none'] as const).map((mode) => (
            <button
              key={mode}
              onClick={() => updateStyle({ textTransform: mode })}
              className={`px-3 py-1 text-xs rounded-lg font-bold border transition-all ${
                style.textTransform === mode
                  ? 'bg-primary-600 text-white border-primary-500'
                  : 'bg-surface text-slate-400 border-surface-border hover:bg-surface-light'
              }`}
            >
              {mode === 'uppercase' ? 'UPPERCASE' : mode === 'capitalize' ? 'Capitalize' : 'Normal'}
            </button>
          ))}
        </div>
      </div>

      {/* 3. Active Word Effects & Highlighters */}
      <div className="space-y-3 border-t border-surface-border pt-4">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-accent-cyan" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
            Active Word Effect (Spoken Word)
          </h3>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
          {activeEffects.map((eff) => {
            const isSelected = style.activeWordEffect === eff.id;
            return (
              <button
                key={eff.id}
                onClick={() => updateStyle({ activeWordEffect: eff.id })}
                className={`p-2.5 rounded-xl border text-left transition-all ${
                  isSelected
                    ? 'bg-primary-600/25 border-primary-400 text-white shadow-md ring-1 ring-primary-500'
                    : 'bg-surface border-surface-border text-slate-300 hover:bg-surface-light'
                }`}
              >
                <div className="flex items-center gap-1.5 mb-1">
                  <span className="text-base">{eff.icon}</span>
                  <span className="text-xs font-bold truncate">{eff.name}</span>
                </div>
                <p className="text-[9px] text-slate-400 line-clamp-1">{eff.desc}</p>
              </button>
            );
          })}
        </div>

        {/* Dynamic Controls for Active Word */}
        <div className="space-y-3 p-3.5 rounded-2xl bg-surface-light border border-surface-border">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <div className="flex justify-between text-xs text-slate-300 mb-1">
                <span className="flex items-center gap-1">
                  <Maximize2 className="w-3 h-3 text-primary-400" /> Active Scale
                </span>
                <span className="text-primary-400 font-mono font-bold">
                  {style.activeWordScale || 1.2}x
                </span>
              </div>
              <input
                type="range"
                min="1.0"
                max="1.5"
                step="0.05"
                value={style.activeWordScale || 1.2}
                onChange={(e) => updateStyle({ activeWordScale: Number(e.target.value) })}
                className="w-full accent-primary-500"
              />
            </div>

            <div>
              <div className="flex justify-between text-xs text-slate-300 mb-1">
                <span className="flex items-center gap-1">
                  <RotateCw className="w-3 h-3 text-accent-yellow" /> Active Tilt
                </span>
                <span className="text-accent-yellow font-mono font-bold">
                  {style.activeWordTilt || 0}°
                </span>
              </div>
              <input
                type="range"
                min="-8"
                max="8"
                step="1"
                value={style.activeWordTilt || 0}
                onChange={(e) => updateStyle({ activeWordTilt: Number(e.target.value) })}
                className="w-full accent-accent-yellow"
              />
            </div>
          </div>

          {/* Marker / Pill Color Options */}
          {(style.activeWordEffect === 'marker' || style.activeWordEffect === 'pill' || style.activeWordEffect === 'box') && (
            <div className="grid grid-cols-2 gap-3 pt-2 border-t border-surface-border">
              <div>
                <span className="text-[11px] font-semibold text-slate-300 block mb-1">
                  Badge Background
                </span>
                <div className="flex items-center gap-2">
                  <input
                    type="color"
                    value={style.highlightBgColor || '#FFE600'}
                    onChange={(e) => updateStyle({ highlightBgColor: e.target.value })}
                    className="w-7 h-7 rounded-lg cursor-pointer bg-transparent border-0"
                  />
                  <input
                    type="text"
                    value={style.highlightBgColor || '#FFE600'}
                    onChange={(e) => updateStyle({ highlightBgColor: e.target.value })}
                    className="w-full text-xs font-mono bg-surface px-2 py-1 rounded text-white border border-surface-border"
                  />
                </div>
              </div>

              <div>
                <span className="text-[11px] font-semibold text-slate-300 block mb-1">
                  Badge Text Color
                </span>
                <div className="flex items-center gap-2">
                  <input
                    type="color"
                    value={style.highlightTextColor || '#000000'}
                    onChange={(e) => updateStyle({ highlightTextColor: e.target.value })}
                    className="w-7 h-7 rounded-lg cursor-pointer bg-transparent border-0"
                  />
                  <input
                    type="text"
                    value={style.highlightTextColor || '#000000'}
                    onChange={(e) => updateStyle({ highlightTextColor: e.target.value })}
                    className="w-full text-xs font-mono bg-surface px-2 py-1 rounded text-white border border-surface-border"
                  />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 4. Colors, Stroke & 3D Shadow Styles */}
      <div className="space-y-3 border-t border-surface-border pt-4">
        <div className="flex items-center gap-2">
          <Palette className="w-4 h-4 text-emerald-400" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
            Colors, Stroke & Shadows
          </h3>
        </div>

        <div className="grid grid-cols-2 gap-3">
          {/* Main Text Color */}
          <div className="p-3 rounded-2xl bg-surface border border-surface-border space-y-2">
            <span className="text-[11px] font-semibold text-slate-300 block">Base Text Color</span>
            <div className="flex items-center gap-2">
              <input
                type="color"
                value={style.textColor}
                onChange={(e) => updateStyle({ textColor: e.target.value })}
                className="w-8 h-8 rounded-lg cursor-pointer bg-transparent border-0"
              />
              <input
                type="text"
                value={style.textColor}
                onChange={(e) => updateStyle({ textColor: e.target.value })}
                className="w-full text-xs font-mono bg-surface-light px-2 py-1 rounded text-white border border-surface-border"
              />
            </div>
          </div>

          {/* Active Spoken Word Color */}
          <div className="p-3 rounded-2xl bg-surface border border-surface-border space-y-2">
            <span className="text-[11px] font-semibold text-slate-300 block">Active Spoken Word</span>
            <div className="flex items-center gap-2">
              <input
                type="color"
                value={style.highlightColor}
                onChange={(e) => updateStyle({ highlightColor: e.target.value })}
                className="w-8 h-8 rounded-lg cursor-pointer bg-transparent border-0"
              />
              <input
                type="text"
                value={style.highlightColor}
                onChange={(e) => updateStyle({ highlightColor: e.target.value })}
                className="w-full text-xs font-mono bg-surface-light px-2 py-1 rounded text-white border border-surface-border"
              />
            </div>
          </div>
        </div>

        {/* Shadow Style Selection */}
        <div className="space-y-2">
          <span className="text-xs text-slate-300 font-semibold">Shadow & Extrusion Style</span>
          <div className="grid grid-cols-2 gap-2">
            {shadowStyles.map((s) => (
              <button
                key={s.id}
                onClick={() => updateStyle({ shadowStyle: s.id })}
                className={`p-2 rounded-xl text-left border transition-all ${
                  style.shadowStyle === s.id
                    ? 'bg-primary-600/30 border-primary-500 text-white font-bold'
                    : 'bg-surface border-surface-border text-slate-400 hover:bg-surface-light'
                }`}
              >
                <p className="text-xs">{s.label}</p>
                <p className="text-[9px] text-slate-500">{s.desc}</p>
              </button>
            ))}
          </div>
        </div>

        {/* Outline / Stroke Width */}
        <div className="p-3.5 rounded-2xl bg-surface border border-surface-border space-y-2">
          <div className="flex justify-between items-center text-xs text-slate-300 font-semibold">
            <span>Stroke Outline Width</span>
            <span className="text-primary-400 font-mono">{style.strokeWidth}px</span>
          </div>
          <div className="flex items-center gap-3">
            <input
              type="color"
              value={style.strokeColor}
              onChange={(e) => updateStyle({ strokeColor: e.target.value })}
              className="w-7 h-7 rounded-md cursor-pointer bg-transparent"
            />
            <input
              type="range"
              min="0"
              max="14"
              value={style.strokeWidth}
              onChange={(e) => updateStyle({ strokeWidth: Number(e.target.value) })}
              className="w-full accent-primary-500"
            />
          </div>
        </div>
      </div>

      {/* 5. Placement & Screen Position */}
      <div className="space-y-3 border-t border-surface-border pt-4">
        <div className="flex items-center gap-2">
          <MoveVertical className="w-4 h-4 text-accent-yellow" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
            Placement & Safe Zone
          </h3>
        </div>

        <div className="space-y-2">
          <div className="flex justify-between text-xs text-slate-300">
            <span>Vertical Placement</span>
            <span className="text-accent-yellow font-mono font-bold">{style.positionYPercent}%</span>
          </div>
          <input
            type="range"
            min="15"
            max="88"
            value={style.positionYPercent}
            onChange={(e) => updateStyle({ positionYPercent: Number(e.target.value) })}
            className="w-full accent-accent-yellow"
          />

          <div className="flex gap-2 pt-1">
            <button
              onClick={() => updateStyle({ positionYPercent: 20 })}
              className="flex-1 py-2 text-xs font-semibold rounded-xl bg-surface border border-surface-border text-slate-300 hover:bg-surface-light flex items-center justify-center gap-1"
            >
              <AlignVerticalJustifyStart className="w-3.5 h-3.5" /> Top (20%)
            </button>
            <button
              onClick={() => updateStyle({ positionYPercent: 50 })}
              className="flex-1 py-2 text-xs font-semibold rounded-xl bg-surface border border-surface-border text-slate-300 hover:bg-surface-light flex items-center justify-center gap-1"
            >
              <AlignVerticalJustifyCenter className="w-3.5 h-3.5" /> Center (50%)
            </button>
            <button
              onClick={() => updateStyle({ positionYPercent: 78 })}
              className="flex-1 py-2 text-xs font-semibold rounded-xl bg-surface border border-surface-border text-slate-300 hover:bg-surface-light flex items-center justify-center gap-1"
            >
              <AlignVerticalJustifyEnd className="w-3.5 h-3.5" /> Bottom (78%)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
