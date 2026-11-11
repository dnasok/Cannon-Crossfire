export type GameStateType =
  | { type: 'Ready' }
  | { type: 'Countdown'; count: number }
  | { type: 'Running' }
  | { type: 'Paused' }
  | { type: 'GameOver'; score: number; isNewHighScore: boolean };

export interface Player {
  x: number;
  y: number;
  radius: number;
  velX: number;
  velY: number;
}

export interface Cannon {
  id: number;
  x: number;
  y: number;
  angle: number;
  radius: number;
  nextFireTime: number;
}

export interface Cannonball {
  id: number;
  cannonId: number;
  x: number;
  y: number;
  velocityX: number;
  velocityY: number;
  radius: number;
}

export interface Coin {
  x: number;
  y: number;
  radius: number;
}

export interface OnlineScore {
  playerName: string;
  score: number;
}

export interface MenuBall {
  side: 'LEFT' | 'RIGHT' | 'TOP' | 'BOTTOM';
  x: number;
  y: number;
  vx: number;
  vy: number;
  r: number;
}
