import React, { useState, useEffect, useRef, useCallback } from 'react';
import { GameStateType, Player, Cannon, Cannonball, Coin, OnlineScore } from './types';
import { audioManager } from './audio';
import { getLocalHighScore, saveLocalHighScore, getLeaderboard, submitScoreToLeaderboard } from './storage';
import { StartMenu } from './components/StartMenu';
import { PauseMenu } from './components/PauseMenu';
import { GameOverMenu } from './components/GameOverMenu';
import { GameCanvas } from './components/GameCanvas';
import { MenuCannonballBackground } from './components/MenuCannonballBackground';
import { Volume2, VolumeX } from 'lucide-react';

export const App: React.FC = () => {
  const [gameState, setGameState] = useState<GameStateType>({ type: 'Ready' });
  const [score, setScore] = useState<number>(0);
  const [highScore, setHighScore] = useState<number>(0);
  const [leaderboard, setLeaderboard] = useState<OnlineScore[]>([]);
  const [isMuted, setIsMuted] = useState<boolean>(false);

  // Screen size
  const [dimensions, setDimensions] = useState<{ width: number; height: number }>({
    width: window.innerWidth || 800,
    height: window.innerHeight || 600,
  });

  const containerRef = useRef<HTMLDivElement | null>(null);

  // Game entities ref for requestAnimationFrame loop
  const playerRef = useRef<Player>({
    x: 400,
    y: 300,
    radius: 37.5,
    velX: 0,
    velY: 0,
  });
  const cannonsRef = useRef<Cannon[]>([]);
  const cannonballsRef = useRef<Cannonball[]>([]);
  const coinRef = useRef<Coin | null>(null);
  const gameTimeRef = useRef<number>(0);
  const lastScoreUpdateTimeRef = useRef<number>(0);
  const nextCannonIdRef = useRef<number>(0);
  const nextCannonSpawnTimeRef = useRef<number>(0);
  const scoreRef = useRef<number>(0);
  const finalScoreRef = useRef<number>(0);

  // Accel input
  const accelRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });
  const keysPressedRef = useRef<{ [key: string]: boolean }>({});
  const touchOriginRef = useRef<{ x: number; y: number } | null>(null);

  // Render state synced from ref for Canvas
  const [renderPlayer, setRenderPlayer] = useState<Player>(playerRef.current);
  const [renderCannons, setRenderCannons] = useState<Cannon[]>([]);
  const [renderCannonballs, setRenderCannonballs] = useState<Cannonball[]>([]);
  const [renderCoin, setRenderCoin] = useState<Coin | null>(null);

  // Image assets
  const [playerImg, setPlayerImg] = useState<HTMLImageElement | null>(null);
  const [enemyImg, setEnemyImg] = useState<HTMLImageElement | null>(null);
  const [cannonballImg, setCannonballImg] = useState<HTMLImageElement | null>(null);
  const [coinImg, setCoinImg] = useState<HTMLImageElement | null>(null);

  // Load static images once
  useEffect(() => {
    const pImg = new Image();
    pImg.src = '/assets/player.png';
    pImg.onload = () => setPlayerImg(pImg);

    const eImg = new Image();
    eImg.src = '/assets/enemy.png';
    eImg.onload = () => setEnemyImg(eImg);

    const cbImg = new Image();
    cbImg.src = '/assets/cannonball.png';
    cbImg.onload = () => setCannonballImg(cbImg);

    const cImg = new Image();
    cImg.src = '/assets/coin.png';
    cImg.onload = () => setCoinImg(cImg);

    setHighScore(getLocalHighScore());
    setLeaderboard(getLeaderboard());
  }, []);

  // Update container dimensions
  useEffect(() => {
    const updateSize = () => {
      if (containerRef.current) {
        const w = containerRef.current.clientWidth;
        const h = containerRef.current.clientHeight;
        if (w > 0 && h > 0) {
          setDimensions({ width: w, height: h });
        }
      }
    };
    updateSize();
    window.addEventListener('resize', updateSize);
    return () => window.removeEventListener('resize', updateSize);
  }, []);

  // Sound & Music triggers based on GameState
  useEffect(() => {
    switch (gameState.type) {
      case 'Ready':
      case 'GameOver':
      case 'Paused':
        audioManager.playMenuMusic();
        break;
      case 'Countdown':
      case 'Running':
        audioManager.playGameMusic();
        break;
    }
  }, [gameState.type]);

  const toggleSound = () => {
    const muted = audioManager.toggleMute();
    setIsMuted(muted);
  };

  // Helper to spawn single cannon on edge
  const spawnSingleCannon = useCallback((): Cannon => {
    const { width, height } = dimensions;
    const centerX = width / 2;
    const centerY = height / 2;
    const cannonRadius = 37.5;

    const side = Math.floor(Math.random() * 4);
    let x: number;
    let y: number;

    switch (side) {
      case 0: // Top
        x = Math.random() * (width - cannonRadius * 2) + cannonRadius;
        y = cannonRadius;
        break;
      case 1: // Right
        x = width - cannonRadius;
        y = Math.random() * (height - cannonRadius * 2) + cannonRadius;
        break;
      case 2: // Bottom
        x = Math.random() * (width - cannonRadius * 2) + cannonRadius;
        y = height - cannonRadius;
        break;
      default: // Left
        x = cannonRadius;
        y = Math.random() * (height - cannonRadius * 2) + cannonRadius;
        break;
    }

    const angleToCenter = (Math.atan2(centerY - y, centerX - x) * 180) / Math.PI;

    return {
      id: nextCannonIdRef.current++,
      x,
      y,
      angle: angleToCenter,
      radius: cannonRadius,
      nextFireTime: gameTimeRef.current + Math.floor(Math.random() * 1000),
    };
  }, [dimensions]);

  const spawnCoin = useCallback(() => {
    const { width, height } = dimensions;
    const padding = 75;
    const x = Math.random() * (width - padding * 2) + padding;
    const y = Math.random() * (height - padding * 2) + padding;
    const newCoin = { x, y, radius: 30 };
    coinRef.current = newCoin;
    setRenderCoin(newCoin);
  }, [dimensions]);

  // End Game
  const endGame = useCallback(() => {
    const currentFinal = scoreRef.current;
    finalScoreRef.current = currentFinal;
    const currHighScore = getLocalHighScore();
    const isNew = currentFinal > currHighScore;

    if (isNew) {
      saveLocalHighScore(currentFinal);
      setHighScore(currentFinal);
    }
    setLeaderboard(getLeaderboard());
    setGameState({ type: 'GameOver', score: currentFinal, isNewHighScore: isNew });
  }, []);

  // Start new game
  const startGame = useCallback(() => {
    audioManager.playSound('select');
    const { width, height } = dimensions;

    gameTimeRef.current = 0;
    scoreRef.current = 0;
    setScore(0);
    lastScoreUpdateTimeRef.current = 0;

    const initPlayer = {
      x: width / 2,
      y: height / 2,
      radius: 37.5,
      velX: 0,
      velY: 0,
    };
    playerRef.current = initPlayer;
    setRenderPlayer({ ...initPlayer });

    cannonballsRef.current = [];
    setRenderCannonballs([]);
    nextCannonIdRef.current = 0;

    // Spawn 7 initial cannons
    const initCannons: Cannon[] = [];
    for (let i = 0; i < 7; i++) {
      initCannons.push(spawnSingleCannon());
    }
    cannonsRef.current = initCannons;
    setRenderCannons([...initCannons]);

    spawnCoin();
    nextCannonSpawnTimeRef.current = 5000;

    // Start 3..2..1 countdown
    let count = 3;
    setGameState({ type: 'Countdown', count: 3 });

    const interval = setInterval(() => {
      count -= 1;
      if (count >= 1) {
        setGameState({ type: 'Countdown', count });
      } else {
        clearInterval(interval);
        setGameState({ type: 'Running' });
      }
    }, 1000);
  }, [dimensions, spawnSingleCannon, spawnCoin]);

  const pauseGame = useCallback(() => {
    audioManager.playSound('select');
    setGameState({ type: 'Paused' });
  }, []);

  const resumeGame = useCallback(() => {
    audioManager.playSound('select');
    let count = 3;
    setGameState({ type: 'Countdown', count: 3 });

    const interval = setInterval(() => {
      count -= 1;
      if (count >= 1) {
        setGameState({ type: 'Countdown', count });
      } else {
        clearInterval(interval);
        setGameState({ type: 'Running' });
      }
    }, 1000);
  }, []);

  const quitToMenu = useCallback(() => {
    audioManager.playSound('select');
    setGameState({ type: 'Ready' });
  }, []);

  const submitScore = useCallback((playerName: string) => {
    audioManager.playSound('select');
    const updated = submitScoreToLeaderboard(playerName, finalScoreRef.current);
    setLeaderboard(updated);
  }, []);

  // Keyboard input listeners
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      keysPressedRef.current[e.key.toLowerCase()] = true;
      if (e.key === ' ' || e.key === 'p' || e.key === 'Escape') {
        if (gameState.type === 'Running') {
          pauseGame();
        } else if (gameState.type === 'Paused') {
          resumeGame();
        }
      }
    };

    const handleKeyUp = (e: KeyboardEvent) => {
      keysPressedRef.current[e.key.toLowerCase()] = false;
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, [gameState.type, pauseGame, resumeGame]);

  // Main game loop (RAF)
  useEffect(() => {
    if (gameState.type !== 'Running') return;

    let animId: number;
    let lastTimestamp = performance.now();

    const loop = (timestamp: number) => {
      const dt = Math.min(timestamp - lastTimestamp, 50); // cap max delta
      lastTimestamp = timestamp;

      gameTimeRef.current += dt;
      const gameTime = gameTimeRef.current;
      const { width, height } = dimensions;

      // 1. Score update: increases every 100ms
      if (gameTime - lastScoreUpdateTimeRef.current >= 100) {
        const pointsToAdd = Math.floor((gameTime - lastScoreUpdateTimeRef.current) / 100);
        scoreRef.current += pointsToAdd;
        lastScoreUpdateTimeRef.current = gameTime;
        setScore(scoreRef.current);
      }

      // 2. Accel calculation from Keyboard & Touch/Tilt
      let ax = 0;
      let ay = 0;

      const keys = keysPressedRef.current;
      if (keys['arrowleft'] || keys['a']) ax += 6;
      if (keys['arrowright'] || keys['d']) ax -= 6;
      if (keys['arrowup'] || keys['w']) ay -= 6;
      if (keys['arrowdown'] || keys['s']) ay += 6;

      // Combine with touch/joystick input if active
      if (accelRef.current.x !== 0 || accelRef.current.y !== 0) {
        ax += accelRef.current.x;
        ay += accelRef.current.y;
      }

      // 3. Player physics update (from Player.kt)
      // velX += -accelX * sensitivity; velY += accelY * sensitivity; vel *= 0.92;
      const sensitivity = 0.5;
      const player = playerRef.current;
      player.velX += -ax * sensitivity;
      player.velY += ay * sensitivity;

      player.velX *= 0.92;
      player.velY *= 0.92;

      player.x += player.velX;
      player.y += player.velY;

      // Clamp within screen
      player.x = Math.max(0, Math.min(width, player.x));
      player.y = Math.max(0, Math.min(height, player.y));

      // 4. Update Cannonballs
      const newCannonballs: Cannonball[] = [];
      const updatedCannons = [...cannonsRef.current];

      for (let i = 0; i < cannonballsRef.current.length; i++) {
        const cb = cannonballsRef.current[i];
        const nx = cb.x + cb.velocityX;
        const ny = cb.y + cb.velocityY;

        const despawned = nx < 0 || nx > width || ny < 0 || ny > height;
        if (despawned) {
          // Despawn: remove cannon that fired it, spawn a new cannon
          const cannonIdx = updatedCannons.findIndex((c) => c.id === cb.cannonId);
          if (cannonIdx !== -1) {
            updatedCannons.splice(cannonIdx, 1);
            updatedCannons.push(spawnSingleCannon());
          }
        } else {
          newCannonballs.push({ ...cb, x: nx, y: ny });
        }
      }

      // 5. Fire Cannons
      updatedCannons.forEach((cannon) => {
        if (gameTime >= cannon.nextFireTime) {
          const angleRad = (cannon.angle * Math.PI) / 180;
          const speed = 7 + gameTime / 15000;

          newCannonballs.push({
            id: Math.floor(Math.random() * 1000000),
            cannonId: cannon.id,
            x: cannon.x,
            y: cannon.y,
            velocityX: Math.cos(angleRad) * speed,
            velocityY: Math.sin(angleRad) * speed,
            radius: 15,
          });

          cannon.nextFireTime = Number.MAX_SAFE_INTEGER;
          audioManager.playSound('shoot');
        }
      });

      // 6. Spawn more cannons over time
      if (gameTime >= nextCannonSpawnTimeRef.current) {
        updatedCannons.push(spawnSingleCannon());
        const spawnDelay = Math.max(1000, 5000 - Math.floor(gameTime / 10));
        nextCannonSpawnTimeRef.current = gameTime + spawnDelay;
      }

      cannonsRef.current = updatedCannons;
      cannonballsRef.current = newCannonballs;

      // 7. Check Collisions: Player vs Cannonball
      let collision = false;
      for (let i = 0; i < newCannonballs.length; i++) {
        const cb = newCannonballs[i];
        const dx = player.x - cb.x;
        const dy = player.y - cb.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < player.radius + cb.radius) {
          // Remove cannonball, respawn cannon, play hit, endGame
          newCannonballs.splice(i, 1);
          const cIdx = updatedCannons.findIndex((c) => c.id === cb.cannonId);
          if (cIdx !== -1) {
            updatedCannons.splice(cIdx, 1);
            updatedCannons.push(spawnSingleCannon());
          }
          audioManager.playSound('hit');
          endGame();
          collision = true;
          break;
        }
      }

      // 8. If no cannonball collision, check Coin & Kill Zone
      if (!collision) {
        // Coin collision
        const coin = coinRef.current;
        if (coin) {
          const dx = player.x - coin.x;
          const dy = player.y - coin.y;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < player.radius + coin.radius) {
            scoreRef.current += 50;
            setScore(scoreRef.current);
            coinRef.current = null;
            audioManager.playSound('coin');
            spawnCoin();
          }
        }

        // Kill Zone: if player touches the border within player.radius
        const killZone = player.radius;
        if (
          player.x < killZone ||
          player.x > width - killZone ||
          player.y < killZone ||
          player.y > height - killZone
        ) {
          audioManager.playSound('hit');
          endGame();
          collision = true;
        }
      }

      // Sync state to trigger canvas renders
      setRenderPlayer({ ...player });
      setRenderCannons([...cannonsRef.current]);
      setRenderCannonballs([...cannonballsRef.current]);

      if (!collision && gameState.type === 'Running') {
        animId = requestAnimationFrame(loop);
      }
    };

    animId = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(animId);
  }, [gameState.type, dimensions, spawnSingleCannon, spawnCoin, endGame]);

  // Touch and Mouse steering interaction for mobile & tablet / pointer
  const handlePointerDown = (e: React.PointerEvent) => {
    if (gameState.type !== 'Running') return;
    touchOriginRef.current = { x: e.clientX, y: e.clientY };
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (gameState.type !== 'Running' || !touchOriginRef.current) return;
    const dx = e.clientX - touchOriginRef.current.x;
    const dy = e.clientY - touchOriginRef.current.y;
    const maxDelta = 80;
    // Normalized to -6 .. 6
    const clampedX = Math.max(-maxDelta, Math.min(maxDelta, dx)) / maxDelta;
    const clampedY = Math.max(-maxDelta, Math.min(maxDelta, dy)) / maxDelta;
    // Tilting: left drag produces negative accelX in Android sense
    accelRef.current = {
      x: -clampedX * 6,
      y: clampedY * 6,
    };
  };

  const handlePointerUp = () => {
    touchOriginRef.current = null;
    accelRef.current = { x: 0, y: 0 };
  };

  return (
    <div
      ref={containerRef}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onPointerCancel={handlePointerUp}
      className="relative w-screen h-screen overflow-hidden bg-black select-none touch-none"
    >
      {/* Background Graphic */}
      <img
        src={
          gameState.type === 'Ready'
            ? '/assets/menu_background.png'
            : gameState.type === 'GameOver'
            ? '/assets/gameover_background.png'
            : gameState.type === 'Paused'
            ? '/assets/pause_background.png'
            : '/assets/game_background.png'
        }
        alt="Background"
        className="absolute inset-0 w-full h-full object-cover pointer-events-none"
      />

      {/* Dark overlay */}
      <div
        className={`absolute inset-0 pointer-events-none transition-colors duration-300 ${
          gameState.type === 'Ready' || gameState.type === 'GameOver'
            ? 'bg-black/40'
            : 'bg-black/15'
        }`}
      />

      {/* Audio Mute/Unmute Toggle in top-left */}
      <button
        onClick={toggleSound}
        title={isMuted ? 'Unmute Sound' : 'Mute Sound'}
        className="absolute top-4 left-4 z-40 p-2.5 rounded-full bg-black/60 hover:bg-black/80 text-white border border-white/20 shadow-lg cursor-pointer transition-transform active:scale-90"
      >
        {isMuted ? <VolumeX className="w-5 h-5 text-red-400" /> : <Volume2 className="w-5 h-5 text-amber-400" />}
      </button>

      {/* GAME STATE SCREENS */}

      {/* 1. READY SCREEN */}
      {gameState.type === 'Ready' && (
        <>
          <MenuCannonballBackground cannonballImg={cannonballImg} />
          <div className="absolute inset-0 flex items-center justify-center">
            <StartMenu onStartClick={startGame} highScore={highScore} />
          </div>
        </>
      )}

      {/* 2. GAME CANVAS (Rendered during Countdown, Running, Paused) */}
      {(gameState.type === 'Running' ||
        gameState.type === 'Countdown' ||
        gameState.type === 'Paused') && (
        <>
          <GameCanvas
            player={renderPlayer}
            cannons={renderCannons}
            cannonballs={renderCannonballs}
            coin={renderCoin}
            playerImg={playerImg}
            enemyImg={enemyImg}
            cannonballImg={cannonballImg}
            coinImg={coinImg}
            width={dimensions.width}
            height={dimensions.height}
          />

          {/* Top Score display */}
          <div className="absolute top-4 left-0 right-0 flex justify-center pointer-events-none z-30">
            <span className="font-game text-xl sm:text-2xl text-neutral-800 bg-white/70 px-4 py-1 rounded-full shadow-md">
              Score: {score}
            </span>
          </div>

          {/* Pause button at top-right */}
          {gameState.type === 'Running' && (
            <button
              onClick={pauseGame}
              className="absolute top-4 right-4 z-40 w-12 h-12 flex items-center justify-center bg-black/50 hover:bg-black/70 rounded-full text-white text-2xl font-bold cursor-pointer border border-white/30 shadow-lg transition-transform active:scale-90"
              title="Pause Game (P / Esc)"
            >
              ⏸
            </button>
          )}

          {/* Countdown display */}
          {gameState.type === 'Countdown' && (
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none z-40">
              <span className="font-game text-8xl sm:text-9xl text-neutral-800 drop-shadow-[0_4px_12px_rgba(255,255,255,0.7)] animate-ping">
                {gameState.count}
              </span>
            </div>
          )}

          {/* Paused Menu */}
          {gameState.type === 'Paused' && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/40 z-40">
              <PauseMenu
                onResume={resumeGame}
                onRestart={startGame}
                onQuit={quitToMenu}
              />
            </div>
          )}
        </>
      )}

      {/* 3. GAME OVER SCREEN */}
      {gameState.type === 'GameOver' && (
        <div className="absolute inset-0 flex items-center justify-center z-40">
          <GameOverMenu
            score={gameState.score}
            highScore={highScore}
            isNewHighScore={gameState.isNewHighScore}
            onlineLeaderboard={leaderboard}
            onRestartClick={quitToMenu}
            onSubmitScore={submitScore}
          />
        </div>
      )}
    </div>
  );
};

export default App;
