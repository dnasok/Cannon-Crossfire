import React, { useEffect, useRef } from 'react';
import { MenuBall } from '../types';

interface MenuCannonballBackgroundProps {
  cannonballImg: HTMLImageElement | null;
}

export const MenuCannonballBackground: React.FC<MenuCannonballBackgroundProps> = ({
  cannonballImg,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let balls: MenuBall[] = [];

    const resize = () => {
      canvas.width = canvas.parentElement?.clientWidth || window.innerWidth;
      canvas.height = canvas.parentElement?.clientHeight || window.innerHeight;
      initBalls();
    };

    const spawnBall = (
      side: 'LEFT' | 'RIGHT' | 'TOP' | 'BOTTOM',
      w: number,
      h: number
    ): MenuBall => {
      const r = 12;
      const speed = 6;

      switch (side) {
        case 'LEFT':
          return { side, x: -r, y: Math.random() * h, vx: speed, vy: 0, r };
        case 'RIGHT':
          return { side, x: w + r, y: Math.random() * h, vx: -speed, vy: 0, r };
        case 'TOP':
          return { side, x: Math.random() * w, y: -r, vx: 0, vy: speed, r };
        case 'BOTTOM':
          return { side, x: Math.random() * w, y: h + r, vx: 0, vy: -speed, r };
      }
    };

    const initBalls = () => {
      const w = canvas.width;
      const h = canvas.height;
      if (w <= 0 || h <= 0) return;

      balls = [
        spawnBall('LEFT', w, h),
        spawnBall('RIGHT', w, h),
        spawnBall('TOP', w, h),
        spawnBall('BOTTOM', w, h),
      ];
    };

    resize();
    window.addEventListener('resize', resize);

    const render = () => {
      const w = canvas.width;
      const h = canvas.height;

      ctx.clearRect(0, 0, w, h);

      for (let i = 0; i < balls.length; i++) {
        const b = balls[i];
        const nx = b.x + b.vx;
        const ny = b.y + b.vy;

        const out =
          nx < -b.r * 3 || nx > w + b.r * 3 || ny < -b.r * 3 || ny > h + b.r * 3;

        if (out) {
          balls[i] = spawnBall(b.side, w, h);
        } else {
          b.x = nx;
          b.y = ny;
        }

        // Draw cannonball
        if (cannonballImg && cannonballImg.complete) {
          ctx.drawImage(
            cannonballImg,
            b.x - b.r,
            b.y - b.r,
            b.r * 2,
            b.r * 2
          );
        } else {
          ctx.beginPath();
          ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2);
          ctx.fillStyle = '#1c1c1c';
          ctx.fill();
          ctx.lineWidth = 2;
          ctx.strokeStyle = '#444';
          ctx.stroke();
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('resize', resize);
    };
  }, [cannonballImg]);

  return <canvas ref={canvasRef} className="absolute inset-0 w-full h-full pointer-events-none" />;
};
