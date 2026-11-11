import React, { useEffect, useRef } from 'react';
import { Cannon, Cannonball, Coin, Player } from '../types';

interface GameCanvasProps {
  player: Player;
  cannons: Cannon[];
  cannonballs: Cannonball[];
  coin: Coin | null;
  playerImg: HTMLImageElement | null;
  enemyImg: HTMLImageElement | null;
  cannonballImg: HTMLImageElement | null;
  coinImg: HTMLImageElement | null;
  width: number;
  height: number;
}

export const GameCanvas: React.FC<GameCanvasProps> = ({
  player,
  cannons,
  cannonballs,
  coin,
  playerImg,
  enemyImg,
  cannonballImg,
  coinImg,
  width,
  height,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || width <= 0 || height <= 0) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    ctx.scale(dpr, dpr);

    ctx.clearRect(0, 0, width, height);

    // 1. Draw Kill Zone border (Red stroke, width 20px)
    ctx.save();
    ctx.strokeStyle = '#ef4444'; // Red
    ctx.lineWidth = 14;
    ctx.strokeRect(7, 7, width - 14, height - 14);
    ctx.restore();

    // 2. Draw Cannons
    cannons.forEach((c) => {
      ctx.save();
      ctx.translate(c.x, c.y);
      // Angle in radians (angle was in degrees)
      const rad = (c.angle * Math.PI) / 180;
      ctx.rotate(rad);

      if (enemyImg && enemyImg.complete) {
        ctx.drawImage(
          enemyImg,
          -c.radius,
          -c.radius,
          c.radius * 2,
          c.radius * 2
        );
      } else {
        ctx.beginPath();
        ctx.arc(0, 0, c.radius, 0, Math.PI * 2);
        ctx.fillStyle = '#475569';
        ctx.fill();
        ctx.lineWidth = 3;
        ctx.strokeStyle = '#0f172a';
        ctx.stroke();
      }
      ctx.restore();
    });

    // 3. Draw Coin
    if (coin) {
      ctx.save();
      if (coinImg && coinImg.complete) {
        ctx.drawImage(
          coinImg,
          coin.x - coin.radius,
          coin.y - coin.radius,
          coin.radius * 2,
          coin.radius * 2
        );
      } else {
        ctx.beginPath();
        ctx.arc(coin.x, coin.y, coin.radius, 0, Math.PI * 2);
        ctx.fillStyle = '#eab308';
        ctx.fill();
        ctx.lineWidth = 3;
        ctx.strokeStyle = '#ca8a04';
        ctx.stroke();
      }
      ctx.restore();
    }

    // 4. Draw Cannonballs
    cannonballs.forEach((cb) => {
      ctx.save();
      if (cannonballImg && cannonballImg.complete) {
        ctx.drawImage(
          cannonballImg,
          cb.x - cb.radius,
          cb.y - cb.radius,
          cb.radius * 2,
          cb.radius * 2
        );
      } else {
        ctx.beginPath();
        ctx.arc(cb.x, cb.y, cb.radius, 0, Math.PI * 2);
        ctx.fillStyle = '#171717';
        ctx.fill();
        ctx.lineWidth = 2;
        ctx.strokeStyle = '#404040';
        ctx.stroke();
      }
      ctx.restore();
    });

    // 5. Draw Player
    ctx.save();
    if (playerImg && playerImg.complete) {
      ctx.drawImage(
        playerImg,
        player.x - player.radius,
        player.y - player.radius,
        player.radius * 2,
        player.radius * 2
      );
    } else {
      ctx.beginPath();
      ctx.arc(player.x, player.y, player.radius, 0, Math.PI * 2);
      ctx.fillStyle = '#3b82f6';
      ctx.fill();
      ctx.lineWidth = 3;
      ctx.strokeStyle = '#1e3a8a';
      ctx.stroke();
    }
    ctx.restore();
  }, [player, cannons, cannonballs, coin, playerImg, enemyImg, cannonballImg, coinImg, width, height]);

  return (
    <canvas
      ref={canvasRef}
      style={{ width: `${width}px`, height: `${height}px` }}
      className="absolute inset-0 block w-full h-full pointer-events-none"
    />
  );
};
