# Cannon Crossfire 🏴‍☠️💣

**Developer / Studio:** Baabearian Studios  
**Package Name:** `com.baabearianstudios.cannoncrossfire`  
**Platform:** Android (API Level 36+ / Min SDK 36)  

---

## 📌 Short Description

**Cannon Crossfire** is an intense 2D tilt-controlled arcade survival game for Android. Steer your player across a hazardous arena using your device's accelerometer, dodging an ever-increasing barrage of cannonballs fired from perimeter cannons while collecting coins to maximize your score!

---

## 🎮 Full Description & Gameplay Overview

In **Cannon Crossfire**, survival is all about balance, reflexes, and spatial awareness. Players take control of a character positioned within a bordered arena surrounded by wall-mounted cannons. 

As time progresses, cannons fire projectiles at increasing speeds toward the center of the arena. Navigating your character requires tilting your physical device—physics-based momentum and friction simulate smooth, tilt-balancing motion. To earn higher scores, players must stay alive while collecting bonus coins that randomly spawn across the map.

### 🌟 Key Features

- 📱 **Tilt-Based Physics Movement:** Uses the device's built-in accelerometer to drive character motion with smooth acceleration and drag mechanics.
- 💥 **Dynamic Cannon Hazards:** Cannons spawn dynamically along the outer walls (Top, Right, Bottom, Left), targeting the arena center and firing projectiles at accelerating speeds over time.
- 🪙 **Coin Collectibles:** Bonus coins appear randomly on the field; collecting a coin instantly grants **+50 bonus score points**.
- 🚨 **Kill Zone Arena Border:** A high-visibility red kill zone marks the arena edges. Touching the border or getting struck by a cannonball results in an instant Game Over.
- 🏆 **Local High Score Persistence:** Automatically saves your personal best score locally using an Android **Room Database**.
- 🎵 **Dynamic Audio & Sound Effects:** Features custom background music and sound effects (cannon fires, coin pickups, collision hits, button selections) powered by `SoundPool` and `MediaPlayer`.
- 🎨 **Modern Jetpack Compose UI:** Built entirely using declarative Jetpack Compose UI with custom Canvas rendering, smooth state management, and animated main menu backgrounds.

---

## 🕹️ Controls & Mechanics

| Action | Control / Interaction |
| :--- | :--- |
| **Move Player** | Tilt your device (X / Y accelerometer tilt) |
| **Collect Coins** | Navigate onto golden coin icons (+50 points) |
| **Pause Game** | Tap the **⏸** button in the top-right corner |
| **Menu Navigation** | Touch screen controls for Start, Restart, and Menu buttons |

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture Pattern:** MVVM (ViewModel + `StateFlow` + Kotlin Coroutines)
- **Local Persistence:** Room Database (SQLite DAO + `Flow`)
- **Sensors:** Android `SensorManager` (`Sensor.TYPE_ACCELEROMETER`)
- **Audio Engine:** Custom `AudioManager` using `SoundPool` (SFX) and `MediaPlayer` (BGM)
- **Build System:** Gradle (Kotlin DSL `.kts`, AGP, KSP)

---

## 🚀 Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-repo/Cannon-Crossfire.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio (Ladybug / 2024.2+ recommended).
   - Select **Open an Existing Project** and choose the project root folder.
3. **Build & Run:**
   - Select an Android device or Emulator (ensure accelerometer support is enabled).
   - Click **Run 'app'** (`Shift + F10`).

---

## 📜 License

Copyright © 2025 Baabearian Studios. All rights reserved.
