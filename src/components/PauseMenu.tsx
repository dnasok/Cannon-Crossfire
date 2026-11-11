import React from 'react';

interface PauseMenuProps {
  onResume: () => void;
  onRestart: () => void;
  onQuit: () => void;
}

export const PauseMenu: React.FC<PauseMenuProps> = ({ onResume, onRestart, onQuit }) => {
  return (
    <div className="flex flex-col items-center justify-center gap-4 z-20 text-center bg-black/60 backdrop-blur-md p-6 sm:p-8 rounded-2xl border border-white/20 shadow-2xl">
      <h2 className="text-4xl sm:text-5xl font-game text-white drop-shadow-[0_3px_6px_rgba(0,0,0,0.8)] mb-2">
        Paused
      </h2>

      {/* Resume button */}
      <button
        onClick={onResume}
        className="group relative w-44 h-14 sm:w-48 sm:h-16 cursor-pointer transition-transform duration-150 active:scale-95 hover:scale-105 focus:outline-none"
      >
        <img
          src="/assets/button.png"
          alt="Resume Game"
          className="w-full h-full object-fill drop-shadow-md"
        />
        <span className="absolute inset-0 flex items-center justify-center font-game text-black text-lg sm:text-xl pt-0.5">
          Resume
        </span>
      </button>

      {/* Restart button */}
      <button
        onClick={onRestart}
        className="group relative w-44 h-14 sm:w-48 sm:h-16 cursor-pointer transition-transform duration-150 active:scale-95 hover:scale-105 focus:outline-none"
      >
        <img
          src="/assets/button.png"
          alt="Restart Game"
          className="w-full h-full object-fill drop-shadow-md"
        />
        <span className="absolute inset-0 flex items-center justify-center font-game text-black text-lg sm:text-xl pt-0.5">
          Restart
        </span>
      </button>

      {/* Quit to Menu button */}
      <button
        onClick={onQuit}
        className="group relative w-44 h-14 sm:w-48 sm:h-16 cursor-pointer transition-transform duration-150 active:scale-95 hover:scale-105 focus:outline-none"
      >
        <img
          src="/assets/button.png"
          alt="Quit Game"
          className="w-full h-full object-fill drop-shadow-md"
        />
        <span className="absolute inset-0 flex items-center justify-center font-game text-black text-base sm:text-lg pt-0.5">
          Quit to Menu
        </span>
      </button>
    </div>
  );
};
