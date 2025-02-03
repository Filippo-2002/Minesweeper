# Minesweeper Android Application

## Overview
This is an implementation of the classic **Minesweeper** game as an Android application. The goal is to provide a fun and interactive experience while learning and refining Android development skills.

## Features
- **Landing Page:**
  - 12-by-10 grid of cells
  - Display of remaining unflagged mines (left) and elapsed seconds (right)
  - Toggle button to switch between digging and flagging mode
- **Game Logic:**
  - Random placement of 4 mines at the start of each game
  - Users can place flags on unrevealed cells
  - Users can reveal cells
  - Display of the number of adjacent mines for revealed cells
  - Recursive reveal of adjacent cells when a cell with 0 adjacent mines is revealed
  - Correct reporting of game result (win/loss)
- **Result Page:**
  - Display of elapsed time and game outcome
  - Button to restart the game

## Development Environment
- **Android Studio Version:** Koala | 2024.1.1 (or newer)
- **API Level:** 24
- **Android Virtual Device (AVD):** Pixel 2 + Nougat 24 (Android 7.0)


## Installation & Setup
1. Clone the repository:
   ```sh
   git clone <repository_url>
   ```
2. Open the project in **Android Studio**.
3. Set up an **Android Virtual Device (AVD)** (Pixel 2 + Nougat API 24 recommended).
4. Run the application on an emulator or physical device.

## Contribution
Feel free to fork this repository and submit pull requests with improvements or new features.


