import React, { useState } from 'react';
import { OnlineScore } from '../types';

interface GameOverMenuProps {
  score: number;
  highScore: number;
  isNewHighScore: boolean;
  onlineLeaderboard: OnlineScore[];
  onRestartClick: () => void;
  onSubmitScore: (name: string) => void;
}

export const GameOverMenu: React.FC<GameOverMenuProps> = ({
  score,
  highScore,
  isNewHighScore,
  onlineLeaderboard,
  onRestartClick,
  onSubmitScore,
}) => {
  const [playerName, setPlayerName] = useState('Player');
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (playerName.trim()) {
      onSubmitScore(playerName.trim());
      setSubmitted(true);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center gap-4 z-20 text-center bg-black/75 backdrop-blur-md p-6 sm:p-8 rounded-2xl border border-red-500/30 max-w-md w-full mx-4 shadow-2xl">
      <h2 className="text-4xl sm:text-6xl font-game text-red-500 tracking-wider drop-shadow-[0_4px_8px_rgba(0,0,0,0.9)] animate-pulse">
        Game Over
      </h2>

      {/* Leaderboard or Local High Score */}
      {onlineLeaderboard.length === 0 ? (
        <div className="flex flex-col items-center">
          <p className="font-game text-xl sm:text-2xl text-yellow-400">Local High Score</p>
          <p className="font-game text-3xl sm:text-4xl text-white">{highScore}</p>
        </div>
      ) : (
        <div className="w-full max-w-xs flex flex-col items-center gap-2 bg-neutral-900/80 p-3 rounded-lg border border-yellow-500/30">
          <p className="font-game text-xl text-yellow-400 tracking-wide">Top 5 Players</p>
          <div className="w-full divide-y divide-neutral-800">
            {onlineLeaderboard.map((item, idx) => (
              <div
                key={idx}
                className="flex justify-between items-center py-1 text-sm sm:text-base font-game"
              >
                <span className="text-neutral-200">
                  {idx + 1}. {item.playerName}
                </span>
                <span className="text-amber-400 font-bold">{item.score}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Score */}
      <div className="space-y-1">
        <p className="text-2xl sm:text-3xl font-game text-white drop-shadow-md">
          Your Score: <span className="text-amber-400">{score}</span>
        </p>
        {isNewHighScore && (
          <p className="text-xl font-game text-yellow-300 drop-shadow animate-bounce">
            ★ New High Score! ★
          </p>
        )}
      </div>

      {/* Form if New High Score and not yet submitted */}
      {isNewHighScore && !submitted ? (
        <form onSubmit={handleSubmit} className="flex flex-col items-center gap-3 w-full max-w-xs">
          <input
            type="text"
            value={playerName}
            onChange={(e) => setPlayerName(e.target.value)}
            maxLength={15}
            placeholder="Enter your name"
            className="w-full px-4 py-2 bg-black/80 border-2 border-yellow-500/60 rounded-lg text-white font-game text-center text-lg focus:outline-none focus:border-yellow-400"
          />

          <div className="flex gap-3 justify-center items-center w-full mt-1">
            <button
              type="submit"
              className="group relative w-36 h-12 cursor-pointer transition-transform duration-150 active:scale-95 hover:scale-105 focus:outline-none"
            >
              <img
                src="/assets/button.png"
                alt="Submit Score"
                className="w-full h-full object-fill drop-shadow-md"
              />
              <span className="absolute inset-0 flex items-center justify-center font-game text-black text-sm pt-0.5">
                Submit Score
              </span>
            </button>

            <button
              type="button"
              onClick={onRestartClick}
              className="group relative w-36 h-12 cursor-pointer transition-transform duration-150 active:scale-95 hover:scale-105 focus:outline-none"
            >
              <img
                src="/assets/button.png"
                alt="Back to Menu"
                className="w-full h-full object-fill drop-shadow-md"
              />
              <span className="absolute inset-0 flex items-center justify-center font-game text-black text-sm pt-0.5">
                Back to Menu
              </span>
            </button>
          </div>
        </form>
      ) : (
        <button
          onClick={onRestartClick}
          className="group relative w-48 h-16 cursor-pointer transition-transform duration-150 active:scale-95 hover:scale-105 focus:outline-none mt-2"
        >
          <img
            src="/assets/button.png"
            alt="Restart Game"
            className="w-full h-full object-fill drop-shadow-md"
          />
          <span className="absolute inset-0 flex items-center justify-center font-game text-black text-xl pt-0.5">
            Restart Game
          </span>
        </button>
      )}
    </div>
  );
};
