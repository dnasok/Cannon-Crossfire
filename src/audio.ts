export type SoundEffect = 'hit' | 'select' | 'shoot' | 'coin';

class SoundManager {
  private bgmMenu: HTMLAudioElement | null = null;
  private bgmGame: HTMLAudioElement | null = null;
  private currentTrack: 'menu' | 'game' | null = null;
  private sfxMap: Map<SoundEffect, HTMLAudioElement[]> = new Map();
  private isMuted: boolean = false;
  private volume: number = 0.7;
  private initialized: boolean = false;

  constructor() {
    // Lazy initialized on first user interaction or call
  }

  public init() {
    if (this.initialized) return;
    this.initialized = true;

    // Create BGM elements
    this.bgmMenu = new Audio('/assets/bgm_menu.wav');
    this.bgmMenu.loop = true;
    this.bgmMenu.volume = this.volume * 0.8;

    this.bgmGame = new Audio('/assets/bgm_game.wav');
    this.bgmGame.loop = true;
    this.bgmGame.volume = this.volume * 0.8;

    // Preload pool of SFX
    const sfxFiles: Record<SoundEffect, string> = {
      hit: '/assets/sfx_hit.ogg',
      select: '/assets/sfx_select.ogg',
      shoot: '/assets/sfx_shoot.ogg',
      coin: '/assets/sfx_coin.ogg',
    };

    (Object.keys(sfxFiles) as SoundEffect[]).forEach((key) => {
      const pool: HTMLAudioElement[] = [];
      for (let i = 0; i < 4; i++) {
        const audio = new Audio(sfxFiles[key]);
        audio.volume = this.volume;
        pool.push(audio);
      }
      this.sfxMap.set(key, pool);
    });
  }

  public playSound(sound: SoundEffect) {
    if (!this.initialized) this.init();
    if (this.isMuted) return;

    const pool = this.sfxMap.get(sound);
    if (!pool || pool.length === 0) return;

    // Find available audio or least playing
    const audio = pool.find((a) => a.paused || a.ended) || pool[0];
    try {
      audio.currentTime = 0;
      audio.volume = this.volume;
      const playPromise = audio.play();
      if (playPromise !== undefined) {
        playPromise.catch(() => {
          // Ignore autoplay restriction errors
        });
      }
    } catch {
      // Ignored
    }
  }

  public playMenuMusic() {
    if (!this.initialized) this.init();
    if (this.currentTrack === 'menu' && this.bgmMenu && !this.bgmMenu.paused) return;

    this.stopGameMusic();
    this.currentTrack = 'menu';

    if (this.bgmMenu && !this.isMuted) {
      try {
        this.bgmMenu.currentTime = 0;
        this.bgmMenu.volume = this.volume * 0.8;
        const p = this.bgmMenu.play();
        if (p) p.catch(() => {});
      } catch {
        // Ignored
      }
    }
  }

  public playGameMusic() {
    if (!this.initialized) this.init();
    if (this.currentTrack === 'game' && this.bgmGame && !this.bgmGame.paused) return;

    this.stopMenuMusic();
    this.currentTrack = 'game';

    if (this.bgmGame && !this.isMuted) {
      try {
        this.bgmGame.currentTime = 0;
        this.bgmGame.volume = this.volume * 0.8;
        const p = this.bgmGame.play();
        if (p) p.catch(() => {});
      } catch {
        // Ignored
      }
    }
  }

  public stopMenuMusic() {
    if (this.bgmMenu) {
      this.bgmMenu.pause();
      this.bgmMenu.currentTime = 0;
    }
  }

  public stopGameMusic() {
    if (this.bgmGame) {
      this.bgmGame.pause();
      this.bgmGame.currentTime = 0;
    }
  }

  public stopMusic() {
    this.stopMenuMusic();
    this.stopGameMusic();
    this.currentTrack = null;
  }

  public toggleMute(): boolean {
    this.isMuted = !this.isMuted;
    if (this.isMuted) {
      if (this.bgmMenu) this.bgmMenu.pause();
      if (this.bgmGame) this.bgmGame.pause();
    } else {
      if (this.currentTrack === 'menu') this.playMenuMusic();
      else if (this.currentTrack === 'game') this.playGameMusic();
    }
    return this.isMuted;
  }

  public getMuted(): boolean {
    return this.isMuted;
  }
}

export const audioManager = new SoundManager();
