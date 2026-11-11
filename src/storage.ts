import { OnlineScore } from './types';

const HIGH_SCORE_KEY = 'cannon_crossfire_high_score';
const LEADERBOARD_KEY = 'cannon_crossfire_leaderboard';

const DEFAULT_LEADERBOARD: OnlineScore[] = [
  { playerName: 'Captain Black', score: 120 },
  { playerName: 'SeaDog99', score: 95 },
  { playerName: 'CannonMaster', score: 80 },
  { playerName: 'Barnaby', score: 65 },
  { playerName: 'PiratePete', score: 50 },
];

export function getLocalHighScore(): number {
  try {
    const val = localStorage.getItem(HIGH_SCORE_KEY);
    return val ? parseInt(val, 10) || 0 : 0;
  } catch {
    return 0;
  }
}

export function saveLocalHighScore(score: number): void {
  try {
    const current = getLocalHighScore();
    if (score > current) {
      localStorage.setItem(HIGH_SCORE_KEY, score.toString());
    }
  } catch {
    // Ignore storage errors
  }
}

export function getLeaderboard(): OnlineScore[] {
  try {
    const data = localStorage.getItem(LEADERBOARD_KEY);
    if (data) {
      const parsed = JSON.parse(data);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed.slice(0, 5);
      }
    }
  } catch {
    // Fall back to default
  }
  return DEFAULT_LEADERBOARD;
}

export function submitScoreToLeaderboard(playerName: string, score: number): OnlineScore[] {
  try {
    const currentList = getLeaderboard();
    const cleanName = playerName.trim() || 'Player';
    const updated = [...currentList, { playerName: cleanName, score }];
    // Sort descending by score
    updated.sort((a, b) => b.score - a.score);
    const top5 = updated.slice(0, 5);
    localStorage.setItem(LEADERBOARD_KEY, JSON.stringify(top5));
    return top5;
  } catch {
    return DEFAULT_LEADERBOARD;
  }
}
