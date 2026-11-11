import React from 'react';

interface StartMenuProps {
  onStartClick: () => void;
  highScore: number;
}

export const StartMenu: React.FC<StartMenuProps> = ({ onStartClick, highScore }) => {
  return (
    <div className="flex flex-col items-center justify-center gap-6 z-20 text-center px-4">
      <div className="space-y-2">
        <h1 className="text-4xl sm:text-6xl font-game text-amber-400 drop-shadow-[0_4px_8px_rgba(0,0,0,0.9)] tracking-wider">
          Cannon Crossfire
        </h1>
        {highScore > 0 && (
          <p className="text-lg sm:text-xl font-game text-yellow-300 drop-shadow-[0_2px_4px_rgba(0,0,0,0.8)]">
            High Score: {highScore}
          </p>
        )}
      </div>

      {/* Start Button matching original button.png graphic */}
      <button
        onClick={onStartClick}
        className="group relative w-56 h-16 sm:w-64 sm:h-20 cursor-pointer transition-transform duration-150 active:scale-95 hover:scale-105 focus:outline-none"
      >
        <img
          src="/assets/button.png"
          alt="Start Game"
          className="w-full h-full object-fill drop-shadow-lg"
        />
        <span className="absolute inset-0 flex items-center justify-center font-game text-black text-xl sm:text-2xl pt-1">
          Start Game
        </span>
      </button>

      {/* Quick Controls Hint */}
      <div className="bg-black/60 backdrop-blur-sm border border-amber-500/30 rounded-xl p-4 max-w-sm text-sm text-neutral-200 shadow-xl space-y-1">
        <p className="font-game text-amber-300 text-base">Controls</p>
        <p>📱 <span className="font-semibold text-white">Tilt Your Phone</span> to steer & balance</p>
        <p className="text-xs text-neutral-300">⌨️ <span className="font-medium text-white">Arrow Keys / WASD</span> on desktop</p>
        <p className="text-xs text-amber-200/90 pt-1">Dodge cannonballs, grab coins (+50 pts), and avoid the red perimeter!</p>
      </div>
    </div>
  );
};
