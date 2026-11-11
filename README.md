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

## 🚀 Development

```bash
# Install dependencies
npm install

# Start development server on port 3000
npm run dev

# Build for production
npm run build
```
