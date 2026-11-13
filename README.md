# Cannon Crossfire

**Developer / Studio:** Baabearian Studios  
**Platform:** Native Android (Kotlin, Jetpack Compose, Material 3)  
**Package:** `com.baabearianstudios.cannoncrossfire`

---

## Description
**Cannon Crossfire** is an arcade survival dodge game built natively for Android. Steer your character across the arena using your phone's accelerometer tilt (or touch drag), dodge crossfire cannonballs fired from perimeter cannons, collect coins for +50 bonus points, and avoid the red kill zone border to set a new high score!

---

## Features
- **Native Jetpack Compose UI & Canvas Rendering:** Hardware-accelerated 2D arena rendering synced to display refresh rates (60Hz/90Hz/120Hz) with density-independent gameplay physics.
- **Accelerometer Tilt & Touch Steering:** Smooth inertia and damping physics driven by `Sensor.TYPE_ACCELEROMETER` with a live **Center Tilt** calibration button and bubble level indicator, plus touch-drag steering support.
- **Dynamic Cannon Crossfire:** Perimeter cannons spawn dynamically along all four edges and fire cannonballs with progressively increasing speed.
- **Coins & Kill Zone Border:** Bonus coins award +50 points; touching a cannonball or the red perimeter border ends the run.
- **Local High Score & Top 5 Leaderboard:** Persists personal best scores and Top 5 leaderboard entries using Android `SharedPreferences`.
- **Native Audio:** Low-latency `SoundPool` sound effects (`sfx_coin`, `sfx_hit`, `sfx_select`, `sfx_shoot`) and looping `MediaPlayer` background music (`bgm_menu`, `bgm_game`) with mute toggle.

---

## Project Structure
- `app/src/main/java/com/baabearianstudios/cannoncrossfire/MainActivity.kt` — Edge-to-edge `ComponentActivity` with `SensorEventListener` and lifecycle audio management.
- `app/src/main/java/com/baabearianstudios/cannoncrossfire/GameViewModel.kt` — MVVM state management (`StateFlow<GameUiState>`), countdown timer, and persistence coordination.
- `app/src/main/java/com/baabearianstudios/cannoncrossfire/GameEngine.kt` — Frame-rate-independent arcade physics, collision detection, and spawning logic.
- `app/src/main/java/com/baabearianstudios/cannoncrossfire/GameSoundManager.kt` — `SoundPool` SFX and `MediaPlayer` BGM controller.
- `app/src/main/java/com/baabearianstudios/cannoncrossfire/ScoreStorage.kt` — `SharedPreferences` storage for high scores and Top 5 leaderboard.
- `app/src/main/java/com/baabearianstudios/cannoncrossfire/ui/` — Compose screens (`StartMenuOverlay`, `PauseMenuOverlay`, `GameOverOverlay`), `GameArenaCanvas`, `MenuCannonballBackground`, and Material 3 theming (`Luckiest Guy` + `Plus Jakarta Sans`).

---

## Building
```bash
gradle assembleDebug
```
The signed APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
