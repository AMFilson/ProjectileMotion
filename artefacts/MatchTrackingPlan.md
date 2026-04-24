# Match History Tracking Implementation Plan

## Overview

Currently, the `DataManager` is designed to save/load individual game states via user dialogs. To track a historical record of all games played (a leaderboard/history), we should append match results to a hidden or persistent data file (e.g., `match_history.csv` or `leaderboard_data.txt`) automatically when a game concludes.

## Implementation Steps

### Step 1: Create a Data Model for Match Records

Create a new simple data class (POJO) to represent a completed game. This will make grouping the data much cleaner before writing it to a file.

- **Create `MatchRecord.java`**:
  - **Fields**: `int gameNumber`, `String p1Name`, `int p1TankName`, `int p1Score`, `String p2Name`, `int p2TankName`, `int p2Score`.
  - **Methods**: Constructor, Getters, and a `toCSV()` or `toFormattedString()` method to easily convert the record into a single line of text for saving.

### Step 2: Update `DataManager.java` for Automatic Appending

Add new static methods to handle writing to a persistent history file directly, bypassing the `JFileChooser` since this save should be automatic.

- **Add `appendMatchRecord(MatchRecord record)`**: Opens `match_history.csv` in **append mode** (using `new FileWriter(file, true)`). Writes the `record.toCSV()` output as a new line.
- **Add `getGamesPlayedCount()`**: Reads `match_history.csv` to count the lines (or uses a static tracker in `Main.java`) so you correctly assign the current "Game Number" for the next save.

### Step 3: Capture End-of-Game Logic

Depending on whether you are using the text-based `Game.java` or the GUI `MainWindow.java`, you need to capture the exact moment the game loop finishes.

- **Modify `Game.java` (in `runGame()`)** or **`MainWindow.java`**:
  - Find the loop exit where a player wins (e.g., when `hit == true`).
  - Increment the total games played counter by +1.
  - Gather the data:
    - **Player Names**: `playerOne.getName()`, `playerTwo.getName()`
    - **Tanks**: `playerOne.getSelectedTankIndex()`, `playerTwo.getSelectedTankIndex()`
    - **Scores**: `playerOne.getScore()`, `playerTwo.getScore()`
  - Instantiate `MatchRecord record = new MatchRecord(gameNumber, p1Name, ...);`
  - Call `DataManager.appendMatchRecord(record);`

### Step 4: Integrate with `LeaderboardPanel.java` (Optional but Recommended)

- Update `LeaderboardPanel.java` to read from `match_history.csv` using a new `DataManager.loadAllMatchRecords()` method, rather than using the hardcoded mock records currently defined in its `static` block.
- This will allow your UI leaderboard to dynamically generate its rows based on the actual stats tracked over time.
