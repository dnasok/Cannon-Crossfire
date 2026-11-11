# Cannon Crossfire 🏴‍☠️💣

**Developer / Studio:** Baabearian Studios  
**Platform:** Web (React + TypeScript + Vite + Tailwind CSS)  

---

## 📌 Description
**Cannon Crossfire** is an intense arcade survival dodge game. Steer your character across a hazardous arena, dodging an ever-increasing crossfire barrage of cannonballs fired from perimeter cannons while collecting coins to maximize your score!

---

## 🎮 Gameplay Overview & Features

In **Cannon Crossfire**, survival is all about balance, reflexes, and spatial awareness. Players take control of a character positioned within a bordered arena surrounded by wall-mounted cannons.

### 🌟 Key Features
- 🕹️ **Smooth Inertia & Balancing Physics:** Character movement models smooth momentum, acceleration, and friction.
- 💥 **Dynamic Cannon Hazards:** Cannons spawn dynamically along the outer walls (Top, Right, Bottom, Left), targeting the arena center and firing projectiles at accelerating speeds over time.
- 🪙 **Coin Collectibles:** Bonus coins appear randomly on the field; collecting a coin instantly grants **+50 bonus score points**.
- 🚨 **Kill Zone Arena Border:** A high-visibility red kill zone marks the arena perimeter. Touching the border or getting struck by a cannonball results in an instant Game Over.
- 🏆 **High Score & Leaderboard:** Automatically persists your personal best score and Top 5 Leaderboard locally.
- 🎵 **Authentic Audio & Sound Effects:** Features original background music and sound effects (cannon fires, coin pickups, collision hits, button selections) with mute toggle support.
- 🎨 **Faithful UI & Graphics:** Built with HTML5 Canvas and React, preserving all original sprite assets, menu backgrounds, and fonts.

---

## 🕹️ Controls

| Action | Control / Interaction |
| :--- | :--- |
| **Move / Steer** | Arrow Keys, WASD, Pointer / Touch Drag, or Mouse Tilt |
| **Collect Coins** | Navigate onto golden coin icons (+50 points) |
| **Pause Game** | Tap the **⏸** button in the top-right corner or press **P** / **Space** / **Esc** |
| **Toggle Mute** | Tap the Speaker icon in the top-left corner |
| **Menu Navigation** | Click or tap Start, Resume, Restart, and Submit buttons |

---

## 🛠️ Architecture & Tech Stack

- **Framework:** React 18
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **Bundler / Dev Server:** Vite
- **Graphics:** HTML5 Canvas 2D
- **Audio:** Web Audio / HTML5 Audio API with sound pooling
- **Persistence:** LocalStorage API

---

## 📱 Mobile Adjustments & Android Studio Publishing

### Mobile Speed & Gameplay Tuning
- **Cannonball Speed Reduced:** Starting velocity slowed from 7.0 to 3.8 px/frame with gentle progressive acceleration, providing ample reaction time on phone touchscreens.
- **Player Steering Tuned:** Input acceleration reduced from 6.0 to 3.2, sensitivity refined to 0.32, and friction damping increased to 0.89 with an 8.0 px/frame speed cap to prevent runaway sliding into walls.
- **Perimeter Kill Zone:** Fairer boundary detection allows players to utilize the full display without premature collision.
- **Touch Virtual Joystick:** Dragging anywhere on touch screens displays an intuitive responsive joystick.
- **Physical Device Tilt:** Integrated with device orientation / accelerometer sensors with an on-screen toggle (Tilt: ON/OFF).

### Publishing on Android Studio
1. **Open in Android Studio:** Open the root directory in Android Studio.
2. **Offline Web Assets:** The web build is automatically synchronized to `app/src/main/assets/dist/` via `python3 build_apk.py` / `npm run build`.
3. **MainActivity:** Implements an immersive, hardware-accelerated full-screen WebView with gesture-free audio playback and touch handling.
4. **Generate APK / AAB:** In Android Studio, go to **Build > Build Bundle(s) / APK(s) > Build APK(s)** or **Generate Signed Bundle / APK** to publish to Google Play.

---

## 🚀 Development

```bash
# Install dependencies
npm install

# Start development server on port 3000
npm run dev

# Build web application and package Android APK
python3 build_apk.py
```
