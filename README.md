# BIT-REKT // Projectile Motion

**BIT-REKT** is a tactical artillery combat simulator built in Java. It blends classic pixel-art aesthetics with a high-fidelity physics engine to create a competitive "scorched earth" style experience for two players.

![BIT-REKT Logo](https://via.placeholder.com/800x200?text=BIT-REKT+ARTILLERY+COMBAT)

## Key Features

- **Tactical Versus Combat**: Two players compete head-to-head, adjusting angles and velocity to land the killing blow.
- **Persistent Career Tracking**: Every shot counts. All matches are automatically logged to `match_history.csv`, tracking winners, losers, and record-breaking scores.
- **Dynamic Leaderboard**: A real-time ranking system that aggregates historical data to identify the top pilots in the division.
- **Keyboard-First Design**: Fully accessible UI with comprehensive Tab and Enter/Space support for all menu items and combat controls.
- **Retro-Industrial UI**: A custom-built Swing interface featuring dithered progress bars, dashed borders, and the signature VT323 pixel font.
- **Tank Roster**: Choose from specialized units like the **M8 Greyhound**, **Flak 88**, or the experimental **Black Cat**, each with unique performance metrics.

## Technical Stack

- **Language**: Java 25 (OpenJDK)
- **Framework**: Java Swing (Custom UI Components)
- **Data Persistence**: CSV Flat-file storage (`match_history.csv`)
- **Graphics**: 2D Graphics engine with manual pixel-art rendering and interpolation.

## Project Structure

- `src/Main.java`: The application entry point.
- `src/MainMenu.java`: The primary hub and navigation system.
- `src/MainWindow.java`: The core battle interface and physics simulation logic.
- `src/DataManager.java`: Handles all file I/O for saving, loading, and match logging.
- `src/LeaderboardPanel.java`: Aggregates and displays the global rankings.
- `src/CharacterSelectPanel.java`: Username validation and tank configuration.

## How to Run

1. Ensure you have **JDK 25** or higher installed.
2. Compile all files in the `src/` directory.
3. Run the `Main` class.
4. Use **Tab** to navigate the menu and **Enter** to initialize the sequence.

## Author

**Andrew Filson**  
*Heavy Armored Division - Camp 30*  
*March 2026*
