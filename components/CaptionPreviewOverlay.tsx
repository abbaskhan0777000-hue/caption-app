'use client';

import React, { useEffect, useState, useRef } from 'react';
import { WordCaption, CaptionStyleConfig } from '@/lib/types';
import { chunkWords } from '@/lib/assGenerator';
import { Maximize2, Move } from 'lucide-react';

interface CaptionPreviewOverlayProps {
  videoRef: React.RefObject<HTMLVideoElement>;
  words: WordCaption[];
  style: CaptionStyleConfig;
  onUpdatePosition?: (posYPercent: number) => void;
  onUpdateSize?: (newFontSize: number) => void;
}

export const CaptionPreviewOverlay: React.FC<CaptionPreviewOverlayProps> = ({
  videoRef,
  words,
  style,
  onUpdatePosition,
  onUpdateSize,
}) => {
  const [currentTime, setCurrentTime] = useState(0);
  const [isInteracting, setIsInteracting] = useState(false);
  const [isSelected, setIsSelected] = useState(false);
  const [liveFontSize, setLiveFontSize] = useState<number | null>(null);

  const overlayRef = useRef<HTMLDivElement>(null);
  const captionBoxRef = useRef<HTMLDivElement>(null);
  const animationFrameRef = useRef<number | null>(null);

  const currentStyleRef = useRef(style);
  currentStyleRef.current = style;

  // Touch gesture tracker
  const gestureRef = useRef<{
    startY: number;
    startPosYPercent: number;
    initialDistance: number;
    initialFontSize: number;
    isPinching: boolean;
    isDragging: boolean;
    isCornerResizing: boolean;
  }>({
    startY: 0,
    startPosYPercent: 78,
    initialDistance: 0,
    initialFontSize: 52,
    isPinching: false,
    isDragging: false,
    isCornerResizing: false,
  });

  // Sync with video.currentTime via requestAnimationFrame for 60fps
  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;

    let isRunning = true;

    const tick = () => {
      if (!isRunning) return;
      if (video) {
        setCurrentTime(video.currentTime);
      }
      animationFrameRef.current = requestAnimationFrame(tick);
    };

    animationFrameRef.current = requestAnimationFrame(tick);

    return () => {
      isRunning = false;
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
    };
  }, [videoRef]);

  // Pre-calculate chunks
  const chunks = React.useMemo(() => {
    return chunkWords(words, style.wordsPerChunk);
  }, [words, style.wordsPerChunk]);

  // Find active chunk
  const activeChunk = chunks.find(
    (chunk) => currentTime >= chunk.start && currentTime <= chunk.end + 0.15
  );

  // Auto-hide bounding frame 3 seconds after interaction
  useEffect(() => {
    if (!isSelected || isInteracting) return;
    const timer = setTimeout(() => {
      setIsSelected(false);
    }, 3500);
    return () => clearTimeout(timer);
  }, [isSelected, isInteracting]);

  // ----------------------------------------------------------------------
  // NATIVE TOUCH GESTURE ENGINE (1-Finger Drag + 2-Finger Pinch-to-Resize)
  // ----------------------------------------------------------------------
  useEffect(() => {
    const captionEl = captionBoxRef.current;
    const overlayEl = overlayRef.current;
    if (!captionEl || !overlayEl) return;

    const onTouchStart = (e: TouchEvent) => {
      if (e.target && (e.target as HTMLElement).closest('.corner-handle')) return;
      
      e.preventDefault();
      setIsInteracting(true);
      setIsSelected(true);

      const touches = e.touches;

      if (touches.length === 1) {
        // 1-Finger Drag
        gestureRef.current = {
          startY: touches[0].clientY,
          startPosYPercent: currentStyleRef.current.positionYPercent,
          initialDistance: 0,
          initialFontSize: currentStyleRef.current.fontSize,
          isPinching: false,
          isDragging: true,
          isCornerResizing: false,
        };
      } else if (touches.length >= 2) {
        // 2-Finger Pinch / Spread to resize font
        const dist = Math.hypot(
          touches[0].clientX - touches[1].clientX,
          touches[0].clientY - touches[1].clientY
        );
        gestureRef.current = {
          ...gestureRef.current,
          initialDistance: dist,
          initialFontSize: currentStyleRef.current.fontSize,
          isPinching: true,
          isDragging: false,
        };
      }
    };

    const onTouchMove = (e: TouchEvent) => {
      if (!gestureRef.current.isDragging && !gestureRef.current.isPinching) return;
      e.preventDefault();

      const touches = e.touches;

      if (touches.length === 1 && gestureRef.current.isDragging && !gestureRef.current.isPinching) {
        // 1-Finger Drag Position
        const deltaY = touches[0].clientY - gestureRef.current.startY;
        const overlayHeight = overlayEl.getBoundingClientRect().height || 400;
        const deltaPercent = (deltaY / overlayHeight) * 100;
        
        const newPercent = Math.max(
          10,
          Math.min(90, Math.round(gestureRef.current.startPosYPercent + deltaPercent))
        );
        onUpdatePosition?.(newPercent);
      } else if (touches.length >= 2 && gestureRef.current.isPinching) {
        // 2-Finger Pinch to resize up to 200px
        const currentDist = Math.hypot(
          touches[0].clientX - touches[1].clientX,
          touches[0].clientY - touches[1].clientY
        );

        if (gestureRef.current.initialDistance > 10 && onUpdateSize) {
          const ratio = currentDist / gestureRef.current.initialDistance;
          const newSize = Math.round(
            Math.max(14, Math.min(200, gestureRef.current.initialFontSize * ratio))
          );
          setLiveFontSize(newSize);
          onUpdateSize(newSize);
        }
      }
    };

    const onTouchEnd = (e: TouchEvent) => {
      if (e.touches.length === 0) {
        setIsInteracting(false);
        setLiveFontSize(null);
        gestureRef.current.isDragging = false;
        gestureRef.current.isPinching = false;
        gestureRef.current.isCornerResizing = false;
      } else if (e.touches.length === 1) {
        gestureRef.current.isPinching = false;
        gestureRef.current.startY = e.touches[0].clientY;
        gestureRef.current.startPosYPercent = currentStyleRef.current.positionYPercent;
        gestureRef.current.isDragging = true;
      }
    };

    captionEl.addEventListener('touchstart', onTouchStart, { passive: false });
    window.addEventListener('touchmove', onTouchMove, { passive: false });
    window.addEventListener('touchend', onTouchEnd, { passive: false });
    window.addEventListener('touchcancel', onTouchEnd, { passive: false });

    return () => {
      captionEl.removeEventListener('touchstart', onTouchStart);
      window.removeEventListener('touchmove', onTouchMove);
      window.removeEventListener('touchend', onTouchEnd);
      window.removeEventListener('touchcancel', onTouchEnd);
    };
  }, [onUpdatePosition, onUpdateSize]);

  // ----------------------------------------------------------------------
  // DESKTOP MOUSE DRAG ENGINE
  // ----------------------------------------------------------------------
  const handleMouseDown = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsInteracting(true);
    setIsSelected(true);

    const startY = e.clientY;
    const startPosYPercent = currentStyleRef.current.positionYPercent;
    const overlayHeight = overlayRef.current?.getBoundingClientRect().height || 400;

    const onMouseMove = (moveEvt: MouseEvent) => {
      const deltaY = moveEvt.clientY - startY;
      const deltaPercent = (deltaY / overlayHeight) * 100;
      const newPercent = Math.max(10, Math.min(90, Math.round(startPosYPercent + deltaPercent)));
      onUpdatePosition?.(newPercent);
    };

    const onMouseUp = () => {
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('mouseup', onMouseUp);
      setIsInteracting(false);
      setLiveFontSize(null);
    };

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
  };

  // Corner Scale Handle Dragging (1-finger or mouse drag to scale up to 200px)
  const handleCornerHandleStart = (e: React.MouseEvent | React.TouchEvent) => {
    e.stopPropagation();
    e.preventDefault();
    setIsInteracting(true);
    setIsSelected(true);

    const startX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const initialFontSize = currentStyleRef.current.fontSize;

    const onMove = (moveEvt: MouseEvent | TouchEvent) => {
      const currentX = 'touches' in moveEvt ? moveEvt.touches[0].clientX : moveEvt.clientX;
      const deltaX = currentX - startX;
      const scaleDelta = deltaX * 0.5;
      const newSize = Math.round(Math.max(14, Math.min(200, initialFontSize + scaleDelta)));
      setLiveFontSize(newSize);
      onUpdateSize?.(newSize);
    };

    const onEnd = () => {
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mouseup', onEnd);
      window.removeEventListener('touchmove', onMove);
      window.removeEventListener('touchend', onEnd);
      setIsInteracting(false);
      setLiveFontSize(null);
    };

    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onEnd);
    window.addEventListener('touchmove', onMove, { passive: false });
    window.addEventListener('touchend', onEnd);
  };

  if (!activeChunk || activeChunk.words.length === 0) {
    return (
      <div
        ref={overlayRef}
        className="absolute inset-0 pointer-events-none select-none z-20"
      />
    );
  }

  // Generate dynamic shadow according to shadowStyle
  const buildShadow = (color: string) => {
    if (style.shadowStyle === 'none') return 'none';
    if (style.shadowStyle === '3d-hard') {
      return `0 4px 0 ${style.shadowColor || '#000000'}, 0 8px 0 rgba(0,0,0,0.85)`;
    }
    if (style.shadowStyle === 'neon-glow') {
      return `0 0 10px ${style.shadowColor || color}, 0 0 25px ${style.shadowColor || color}, 0 0 45px ${style.shadowColor || color}`;
    }
    return `${style.shadowOffsetX || 0}px ${style.shadowOffsetY || 4}px ${style.shadowBlur || 10}px ${style.shadowColor || 'rgba(0,0,0,0.85)'}`;
  };

  const buildStrokeShadow = (baseShadow: string) => {
    if (style.strokeWidth <= 0) return baseShadow;
    const sw = style.strokeWidth;
    const sc = style.strokeColor;
    const strokeRings = `
      -${sw}px -${sw}px 0 ${sc},
      ${sw}px -${sw}px 0 ${sc},
      -${sw}px ${sw}px 0 ${sc},
      ${sw}px ${sw}px 0 ${sc},
      0px ${sw}px 0 ${sc},
      0px -${sw}px 0 ${sc},
      ${sw}px 0px 0 ${sc},
      -${sw}px 0px 0 ${sc}
    `;
    return baseShadow !== 'none' ? `${strokeRings}, ${baseShadow}` : strokeRings;
  };

  const defaultTextShadow = buildStrokeShadow(buildShadow(style.textColor));
  const activeGlowShadow = buildStrokeShadow(buildShadow(style.highlightColor));

  const isBoxActive = isSelected || isInteracting;

  // Responsive font size calculation without upper ceiling restrictions
  const currentFontSize = liveFontSize || style.fontSize;
  const overlayHeight = overlayRef.current?.clientHeight || 450;
  const responsiveScale = overlayHeight / 720;
  const renderedFontSizePx = Math.max(12, Math.round(currentFontSize * responsiveScale));

  return (
    <div
      ref={overlayRef}
      onClick={() => setIsSelected(false)}
      className="absolute inset-0 pointer-events-auto select-none z-20 overflow-hidden flex flex-col items-center justify-start touch-none"
    >
      {/* Center Safe-Zone Alignment Guide */}
      {isInteracting && Math.abs(style.positionYPercent - 50) < 3 && (
        <div className="absolute inset-x-0 top-1/2 -translate-y-1/2 h-[1px] bg-accent-cyan/80 pointer-events-none z-10 border-t border-dashed border-accent-cyan" />
      )}

      {/* Main Draggable & Scalable Caption Box */}
      <div
        ref={captionBoxRef}
        style={{
          position: 'absolute',
          top: `${style.positionYPercent}%`,
          transform: 'translateY(-50%)',
          fontFamily: `"${style.fontFamily}", Inter, sans-serif`,
          letterSpacing: `${style.letterSpacing}px`,
          lineHeight: style.lineHeight,
        }}
        onMouseDown={handleMouseDown}
        onClick={(e) => {
          e.stopPropagation();
          setIsSelected(true);
        }}
        className={`cursor-grab active:cursor-grabbing transition-all duration-75 px-4 py-2 text-center max-w-[94%] flex flex-wrap items-center justify-center gap-x-2.5 gap-y-1 touch-none ${
          isBoxActive
            ? 'ring-2 ring-accent-cyan ring-offset-2 ring-offset-black/80 rounded-2xl scale-[1.01] shadow-2xl bg-black/30 backdrop-blur-[2px]'
            : ''
        }`}
      >
        {/* Clean Floating Size & Position Pill Badge during interaction */}
        {isBoxActive && (
          <div className="absolute -top-8 left-1/2 -translate-x-1/2 px-2.5 py-0.5 rounded-full bg-accent-cyan text-black font-extrabold text-[10px] uppercase tracking-wider flex items-center gap-1.5 shadow-xl pointer-events-none animate-in fade-in zoom-in-95 duration-100 z-30 whitespace-nowrap">
            <Move className="w-2.5 h-2.5" />
            <span>Size: {currentFontSize}px &bull; Pos: {style.positionYPercent}%</span>
          </div>
        )}

        {/* CapCut Mobile Corner Resize Handles */}
        {isBoxActive && (
          <>
            <div className="absolute -top-1.5 -left-1.5 w-3 h-3 rounded-full bg-white border-2 border-accent-cyan shadow-md pointer-events-none" />
            <div className="absolute -top-1.5 -right-1.5 w-3 h-3 rounded-full bg-white border-2 border-accent-cyan shadow-md pointer-events-none" />
            <div className="absolute -bottom-1.5 -left-1.5 w-3 h-3 rounded-full bg-white border-2 border-accent-cyan shadow-md pointer-events-none" />
            
            {/* Bottom-Right Corner Scale Handle */}
            <div
              onMouseDown={handleCornerHandleStart}
              onTouchStart={handleCornerHandleStart}
              className="corner-handle absolute -bottom-3.5 -right-3.5 w-7 h-7 rounded-full bg-accent-cyan text-black flex items-center justify-center shadow-2xl border-2 border-white cursor-nwse-resize active:scale-125 transition-transform z-30 pointer-events-auto"
              title="Drag with finger or mouse to adjust font size"
            >
              <Maximize2 className="w-3.5 h-3.5" />
            </div>
          </>
        )}

        {/* Full line background box */}
        {style.backgroundColor && style.backgroundColor !== 'transparent' && (
          <div
            className="absolute inset-0 -z-10 transition-all pointer-events-none shadow-xl"
            style={{
              backgroundColor: style.backgroundColor,
              padding: `${style.backgroundPadding}px`,
              borderRadius: `${style.backgroundRounded}px`,
            }}
          />
        )}

        {/* Rendered Words with Direct Real-Time Font Size */}
        {activeChunk.words.map((wordObj, idx) => {
          const isActive = currentTime >= wordObj.start && currentTime <= wordObj.end;

          let displayWord = wordObj.word;
          if (style.textTransform === 'uppercase') {
            displayWord = displayWord.toUpperCase();
          } else if (style.textTransform === 'capitalize') {
            displayWord = displayWord.charAt(0).toUpperCase() + displayWord.slice(1);
          }

          let wordScale = 1.0;
          let wordTilt = 0;
          let wordTextColor = style.textColor;
          let wordShadow = defaultTextShadow;
          let isMarkerActive = false;
          let isPillActive = false;

          if (isActive) {
            wordTextColor = style.highlightColor;
            wordScale = style.activeWordScale || 1.2;
            wordTilt = style.activeWordTilt || 0;
            wordShadow = activeGlowShadow;

            if (style.activeWordEffect === 'marker') {
              isMarkerActive = true;
              wordTextColor = style.highlightTextColor || '#121212';
              wordShadow = 'none';
            } else if (style.activeWordEffect === 'pill' || style.activeWordEffect === 'box') {
              isPillActive = true;
              wordTextColor = style.highlightTextColor || '#000000';
              wordShadow = 'none';
            }
          }

          const transformStyle = isActive
            ? `scale(${wordScale}) rotate(${wordTilt}deg)`
            : 'scale(1) rotate(0deg)';

          return (
            <span
              key={wordObj.id || idx}
              className="relative inline-flex items-center justify-center select-none will-change-transform transition-all duration-75"
              style={{
                fontSize: `${renderedFontSizePx}px`,
                fontWeight: 900,
                color: wordTextColor,
                textShadow: wordShadow,
                transform: transformStyle,
                zIndex: isActive ? 10 : 1,
              }}
            >
              {/* Marker Highlighter Pen Streak */}
              {isMarkerActive && (
                <span
                  className="absolute inset-x-[-6px] inset-y-[-2px] -z-10 rounded-md shadow-lg pointer-events-none"
                  style={{
                    backgroundColor: style.highlightBgColor || '#FFF275',
                    transform: 'skewX(-6deg)',
                  }}
                />
              )}

              {/* Solid Pill Badge */}
              {isPillActive && (
                <span
                  className="absolute inset-x-[-8px] inset-y-[-3px] -z-10 rounded-xl shadow-2xl pointer-events-none border border-white/20"
                  style={{
                    backgroundColor: style.highlightBgColor || '#00FF66',
                  }}
                />
              )}

              {displayWord}
            </span>
          );
        })}
      </div>
    </div>
  );
};
