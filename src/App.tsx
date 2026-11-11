import React, { useState, useEffect, useRef, useCallback } from 'react';
import { GameStateType, Player, Cannon, Cannonball, Coin, OnlineScore } from './types';
import { audioManager } from './audio';
import { getLocalHighScore, saveLocalHighScore, getLeaderboard, submitScoreToLeaderboard } from './storage';
import { StartMenu } from './components/StartMenu';
import { PauseMenu } from './components/PauseMenu';
import { GameOverMenu } from './components/GameOverMenu';
import { GameCanvas } from './components/GameCanvas';
import { MenuCannonballBackground } from './components/MenuCannonballBackground';
import { Volume2, VolumeX, Compass } from 'lucide-react';

export const App: React.FC = () => {
  const [gameState, setGameState] = useState<GameStateType>({ type: 'Ready' });
  const [score, setScore] = useState<number>(0);
  const [highScore, setHighScore] = useState<number>(0);
  const [leaderboard, setLeaderboard] = useState<OnlineScore[]>([]);
  const [isMuted, setIsMuted] = useState<boolean>(false);

  // Mobile tilt control: active by default as mobile controls are exclusively tilt
  const [tiltAngles, setTiltAngles] = useState<{ gamma: number; beta: number }>({ gamma: 0, beta: 0 });
  const [calibratedOffset, setCalibratedOffset] = useState<{ gamma: number; beta: number }>({ gamma: 0, beta: 30 });
  const [hasTiltSensor, setHasTiltSensor] = useState<boolean>(false);

  // Screen size
  const [dimensions, setDimensions] = useState<{ width: number; height: number }>({
    width: window.innerWidth || 400,
    height: window.innerHeight || 700,
  });

  const containerRef = useRef<HTMLDivElement | null>(null);

  // Game entities ref for requestAnimationFrame loop
  const playerRef = useRef<Player>({
    x: 200,
    y: 350,
    radius: 34,
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

  // Tilt accelerometer input
  const tiltAccelRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });
  const keysPressedRef = useRef<{ [key: string]: boolean }>({});
  const calibOffsetRef = useRef<{ gamma: number; beta: number }>(calibratedOffset);

  useEffect(() => {
    calibOffsetRef.current = calibratedOffset;
  }, [calibratedOffset]);

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
    window.addEventListener('orientationchange', updateSize);
    return () => {
      window.removeEventListener('resize', updateSize);
      window.removeEventListener('orientationchange', updateSize);
    };
  }, []);

  // Native Android Studio WebView sensor hook
  useEffect(() => {
    (window as unknown as { onNativeTilt: (ax: number, ay: number) => void }).onNativeTilt = (
      ax: number,
      ay: number
    ) => {
      setHasTiltSensor(true);
      // In Android, tilting phone left creates positive accelX, tilting down creates positive accelY
      const normX = -Math.max(-9.8, Math.min(9.8, ax)) / 9.8;
      const normY = Math.max(-9.8, Math.min(9.8, ay - 4.5)) / 9.8;

      setTiltAngles({ gamma: normX * 25, beta: normY * 25 });
      tiltAccelRef.current = {
        x: normX * 3.2,
        y: normY * 3.2,
      };
    };
  }, []);

  // DeviceOrientation sensor listener for mobile web / Android browsers
  useEffect(() => {
    const handleDeviceOrientation = (event: DeviceOrientationEvent) => {
      if (event.gamma !== null && event.beta !== null) {
        setHasTiltSensor(true);

        const currentGamma = event.gamma; // Left-to-right tilt (-90 to 90)
        const currentBeta = event.beta;   // Front-to-back tilt (-180 to 180)

        const calib = calibOffsetRef.current;
        const diffX = currentGamma - calib.gamma;
        const diffY = currentBeta - calib.beta;

        // Clamp to ±25 degrees
        const tiltX = Math.max(-25, Math.min(25, diffX));
        const tiltY = Math.max(-25, Math.min(25, diffY));

        setTiltAngles({ gamma: tiltX, beta: tiltY });

        // Gentle, precise acceleration: -3.2 to 3.2
        tiltAccelRef.current = {
          x: -(tiltX / 25) * 3.2,
          y: (tiltY / 25) * 3.2,
        };
      }
    };

    window.addEventListener('deviceorientation', handleDeviceOrientation, true);
    return () => {
      window.removeEventListener('deviceorientation', handleDeviceOrientation, true);
    };
  }, []);

  // Calibrate center tilt to currently held angle
  const calibrateTiltCenter = () => {
    setCalibratedOffset({
      gamma: tiltAngles.gamma + calibratedOffset.gamma,
      beta: tiltAngles.beta + calibratedOffset.beta,
    });
    audioManager.playSound('select');
  };

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

  // Helper to spawn single cannon on edge with mobile scaling
  const spawnSingleCannon = useCallback((): Cannon => {
    const { width, height } = dimensions;
    const centerX = width / 2;
    const centerY = height / 2;
    const cannonRadius = Math.max(26, Math.min(36, width * 0.08));

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
      nextFireTime: gameTimeRef.current + Math.floor(Math.random() * 800) + 400,
    };
  }, [dimensions]);

  const spawnCoin = useCallback(() => {
    const { width, height } = dimensions;
    const padding = Math.max(45, Math.min(65, width * 0.12));
    const x = Math.random() * (width - padding * 2) + padding;
    const y = Math.random() * (height - padding * 2) + padding;
    const coinRadius = Math.max(22, Math.min(28, width * 0.065));
    const newCoin = { x, y, radius: coinRadius };
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

    // Request device orientation permission if required by browser (e.g. iOS)
    if (
      typeof (DeviceOrientationEvent as unknown as { requestPermission?: () => Promise<string> })
        .requestPermission === 'function'
    ) {
      (DeviceOrientationEvent as unknown as { requestPermission: () => Promise<string> })
        .requestPermission()
        .catch(() => {});
    }

    const { width, height } = dimensions;

    gameTimeRef.current = 0;
    scoreRef.current = 0;
    setScore(0);
    lastScoreUpdateTimeRef.current = 0;

    const pRadius = Math.max(26, Math.min(34, width * 0.08));
    const initPlayer = {
      x: width / 2,
      y: height / 2,
      radius: pRadius,
      velX: 0,
      velY: 0,
    };
    playerRef.current = initPlayer;
    setRenderPlayer({ ...initPlayer });

    cannonballsRef.current = [];
    setRenderCannonballs([]);
    nextCannonIdRef.current = 0;

    const initialCount = width < 500 ? 5 : 6;
    const initCannons: Cannon[] = [];
    for (let i = 0; i < initialCount; i++) {
      initCannons.push(spawnSingleCannon());
    }
    cannonsRef.current = initCannons;
    setRenderCannons([...initCannons]);

    spawnCoin();
    nextCannonSpawnTimeRef.current = 6000;

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

  // Keyboard input listeners (desktop fallback)
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

  // Main game loop (RAF) - Slower starting cannonball speed capped at 3.8, exclusive tilt controls
  useEffect(() => {
    if (gameState.type !== 'Running') return;

    let animId: number;
    let lastTimestamp = performance.now();

    const loop = (timestamp: number) => {
      const dt = Math.min(timestamp - lastTimestamp, 50);
      lastTimestamp = timestamp;

      gameTimeRef.current += dt;
      const gameTime = gameTimeRef.current;
      const { width, height } = dimensions;

      // 1. Score update
      if (gameTime - lastScoreUpdateTimeRef.current >= 100) {
        const pointsToAdd = Math.floor((gameTime - lastScoreUpdateTimeRef.current) / 100);
        scoreRef.current += pointsToAdd;
        lastScoreUpdateTimeRef.current = gameTime;
        setScore(scoreRef.current);
      }

      // 2. Accel calculation from TILT (mobile) & Keyboard (desktop fallback)
      let ax = 0;
      let ay = 0;

      // Desktop keyboard input
      const keys = keysPressedRef.current;
      if (keys['arrowleft'] || keys['a']) ax += 3.2;
      if (keys['arrowright'] || keys['d']) ax -= 3.2;
      if (keys['arrowup'] || keys['w']) ay -= 3.2;
      if (keys['arrowdown'] || keys['s']) ay += 3.2;

      // Mobile: strictly tilt-based physics (no touch-drag)
      ax += tiltAccelRef.current.x;
      ay += tiltAccelRef.current.y;

      // 3. Player physics update (Tilt balancing)
      const sensitivity = 0.32;
      const player = playerRef.current;
      player.velX += -ax * sensitivity;
      player.velY += ay * sensitivity;

      // Friction
      player.velX *= 0.89;
      player.velY *= 0.89;

      // Velocity Clamp
      const maxSpeed = 7.0;
      player.velX = Math.max(-maxSpeed, Math.min(maxSpeed, player.velX));
      player.velY = Math.max(-maxSpeed, Math.min(maxSpeed, player.velY));

      player.x += player.velX;
      player.y += player.velY;

      // Clamp within screen boundaries
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
          const cannonIdx = updatedCannons.findIndex((c) => c.id === cb.cannonId);
          if (cannonIdx !== -1) {
            updatedCannons.splice(cannonIdx, 1);
            updatedCannons.push(spawnSingleCannon());
          }
        } else {
          newCannonballs.push({ ...cb, x: nx, y: ny });
        }
      }

      // 5. Fire Cannons: Much slower starting speed (1.8 px/frame), increasingly faster, capped at 3.8 px/frame
      const START_SPEED = 1.8;
      const MAX_SPEED_CAP = 3.8;
      // Reaches cap gradually over 40 seconds
      const currentSpeed = Math.min(MAX_SPEED_CAP, START_SPEED + (gameTime / 40000) * (MAX_SPEED_CAP - START_SPEED));

      updatedCannons.forEach((cannon) => {
        if (gameTime >= cannon.nextFireTime) {
          const angleRad = (cannon.angle * Math.PI) / 180;

          newCannonballs.push({
            id: Math.floor(Math.random() * 1000000),
            cannonId: cannon.id,
            x: cannon.x,
            y: cannon.y,
            velocityX: Math.cos(angleRad) * currentSpeed,
            velocityY: Math.sin(angleRad) * currentSpeed,
            radius: Math.max(12, Math.min(15, width * 0.035)),
          });

          cannon.nextFireTime = Number.MAX_SAFE_INTEGER;
          audioManager.playSound('shoot');
        }
      });

      // 6. Spawn more cannons over time
      if (gameTime >= nextCannonSpawnTimeRef.current) {
        const maxCannons = width < 500 ? 8 : 11;
        if (updatedCannons.length < maxCannons) {
          updatedCannons.push(spawnSingleCannon());
        }
        const spawnDelay = Math.max(2000, 6000 - Math.floor(gameTime / 15));
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

        if (dist < (player.radius + cb.radius) * 0.85) {
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

      // 8. Coin & Kill Zone Check
      if (!collision) {
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

        const killZoneLimit = 16 + player.radius * 0.45;
        if (
          player.x < killZoneLimit ||
          player.x > width - killZoneLimit ||
          player.y < killZoneLimit ||
          player.y > height - killZoneLimit
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

  return (
    <div
      ref={containerRef}
      className="relative w-screen h-screen overflow-hidden bg-black select-none touch-none"
      style={{
        paddingTop: 'env(safe-area-inset-top, 0px)',
        paddingBottom: 'env(safe-area-inset-bottom, 0px)',
        paddingLeft: 'env(safe-area-inset-left, 0px)',
        paddingRight: 'env(safe-area-inset-right, 0px)',
      }}
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

      {/* Top Mobile Controls Bar (Safe area padded) */}
      <div className="absolute top-4 left-4 z-40 flex items-center gap-2">
        {/* Audio Mute/Unmute Toggle */}
        <button
          onClick={toggleSound}
          title={isMuted ? 'Unmute Sound' : 'Mute Sound'}
          className="p-3 rounded-full bg-black/60 active:bg-black/80 hover:bg-black/70 text-white border border-white/20 shadow-lg cursor-pointer transition-transform active:scale-90"
        >
          {isMuted ? <VolumeX className="w-5 h-5 text-red-400" /> : <Volume2 className="w-5 h-5 text-amber-400" />}
        </button>

        {/* Calibrate / Center Tilt Button */}
        {hasTiltSensor && gameState.type === 'Running' && (
          <button
            onClick={calibrateTiltCenter}
            title="Calibrate neutral tilt position"
            className="px-3 py-2 rounded-full bg-black/60 border border-amber-400/40 text-amber-300 text-xs font-game flex items-center gap-1.5 shadow-lg active:scale-95"
          >
            <Compass className="w-4 h-4 animate-spin-slow" />
            <span>Center Tilt</span>
          </button>
        )}
      </div>

      {/* Tilt Horizon / Bubble Level Indicator for Mobile Players */}
      {hasTiltSensor && gameState.type === 'Running' && (
        <div className="absolute bottom-5 left-1/2 transform -translate-x-1/2 z-30 pointer-events-none flex flex-col items-center gap-1 opacity-75">
          <div className="w-20 h-5 rounded-full border border-amber-400/50 bg-black/40 backdrop-blur-xs relative flex items-center justify-center overflow-hidden">
            {/* Center crosshair */}
            <div className="w-0.5 h-full bg-amber-400/40" />
            {/* Level bubble */}
            <div
              className="w-3.5 h-3.5 rounded-full bg-amber-400 shadow-[0_0_8px_rgba(251,191,36,0.8)] absolute transition-transform duration-75"
              style={{
                transform: `translateX(${Math.max(-28, Math.min(28, tiltAngles.gamma * 1.1))}px)`,
              }}
            />
          </div>
          <span className="font-game text-[10px] text-amber-200 tracking-wider">
            TILT TO STEER
          </span>
        </div>
      )}

      {/* GAME STATE SCREENS */}

      {/* 1. READY SCREEN */}
      {gameState.type === 'Ready' && (
        <>
          <MenuCannonballBackground cannonballImg={cannonballImg} />
          <div className="absolute inset-0 flex items-center justify-center p-4">
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
            <span className="font-game text-xl sm:text-2xl text-neutral-900 bg-white/85 px-4 py-1 rounded-full shadow-lg border border-amber-400/40">
              Score: {score}
            </span>
          </div>

          {/* Pause button at top-right with comfortable mobile hit area */}
          {gameState.type === 'Running' && (
            <button
              onClick={pauseGame}
              className="absolute top-4 right-4 z-40 w-12 h-12 min-w-[48px] min-h-[48px] flex items-center justify-center bg-black/60 hover:bg-black/80 rounded-full text-white text-2xl font-bold cursor-pointer border border-white/30 shadow-lg transition-transform active:scale-90"
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
            <div className="absolute inset-0 flex items-center justify-center bg-black/40 z-40 p-4">
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
        <div className="absolute inset-0 flex items-center justify-center z-40 p-4">
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
