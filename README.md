# Cannon Crossfire 🏴‍☠️💣 (Android Application)

**Developer / Studio:** Baabearian Studios  
**Platform:** Android (Kotlin, Jetpack, Android Studio) & Web Runtime  
**Package:** `com.baabearianstudios.cannoncrossfire`

---

## 📌 Description
**Cannon Crossfire** is an intense arcade survival dodge game rewritten for Android. Steer your character across a hazardous arena, dodging an ever-increasing crossfire barrage of cannonballs fired from perimeter cannons while collecting coins to maximize your score!

---

## 🎮 Gameplay Features
- 🕹️ **Smooth Inertia & Balancing Physics:** Character movement models smooth momentum, acceleration, and friction. Supports physical phone tilt (accelerometer) and touch controls.
- 💥 **Dynamic Cannon Hazards:** Cannons spawn dynamically along the perimeter walls (Top, Right, Bottom, Left), targeting the arena center and firing projectiles at accelerating speeds over time.
- 🪙 **Coin Collectibles:** Bonus coins appear randomly on the field; collecting a coin instantly grants **+50 bonus score points**.
- 🚨 **Kill Zone Arena Border:** A high-visibility red kill zone marks the arena perimeter. Touching the border or getting struck by a cannonball results in an instant Game Over.
- 🏆 **High Score & Leaderboard:** Automatically persists your personal best score and Top 5 Leaderboard locally via `SharedPreferences`.
- 🎵 **Authentic Audio & Sound Effects:** Features original background music and sound effects (cannon fires, coin pickups, collision hits, button selections) using Android `SoundPool` and `MediaPlayer`.
- 🎨 **Faithful UI & Graphics:** Preserves all original sprite assets (`player.png`, `enemy.png`, `cannonball.png`, `coin.png`), backgrounds, and the custom *Luckiest Guy* typeface.

---

## 📱 Android Architecture & Source Code

The Android application is structured in `app/src/main/`:

- **`MainActivity.kt`**: Fullscreen immersive Android Activity handling system window insets, accelerometer sensor registration, and lifecycle.
- **`GameModels.kt`**: Native Kotlin data models (`GameState`, `Player`, `Cannon`, `Cannonball`, `Coin`, `OnlineScore`, `MenuBall`).
- **`GameEngine.kt`**: Real-time 60 FPS physics engine implementing steering physics, boundary collision detection, cannon firing schedules, and score tracking.
- **`GameCanvasView.kt`**: Hardware-accelerated 2D Android Canvas drawing player sprites, cannons, cannonballs, coins, and the kill zone border.
- **`GameSoundManager.kt`**: Low-latency audio manager leveraging `SoundPool` for SFX and `MediaPlayer` for menu/game background music.
- **`ScoreStorage.kt`**: Android `SharedPreferences` persistence for local personal bests and top 5 leaderboard entries.
- **Resources (`res/`)**:
  - `res/drawable/`: Original sprites, backgrounds, buttons, and custom launcher icon.
  - `res/raw/`: Sound effects (`sfx_coin.ogg`, `sfx_hit.ogg`, `sfx_select.ogg`, `sfx_shoot.ogg`) and background music (`bgm_menu.wav`, `bgm_game.wav`).
  - `res/font/`: Local bundled `luckiest_guy.ttf` font.
  - `res/values/`: `strings.xml`, `colors.xml`.

---

## 🕹️ Controls

| Action | Control / Interaction |
| :--- | :--- |
| **Move / Steer** | Physical Device Tilt (Accelerometer) or Arrow Keys / Touch |
| **Calibrate Tilt** | Tap **Center Tilt** button to re-zero level |
| **Collect Coins** | Navigate onto golden coin icons (+50 points) |
| **Pause Game** | Tap the **⏸** button in the top-right corner |
| **Toggle Mute** | Tap the Speaker icon in the top-left corner |
| **Menu Navigation** | Tap Start Game, Resume, Restart, and Submit Score buttons |

---

## 🚀 Building & Packaging

```bash
# Package the Android APK
python3 build_apk.py

# Verify compilation
npm run build
```

APK output locations:
- `app/build/outputs/apk/debug/app-debug.apk`
- `.build-outputs/app-debug.apk`
