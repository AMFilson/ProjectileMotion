# Goal Description

Implement the HTML/CSS Leaderboard design as a native Java Swing view within the existing BIT-REKT game menu. The new view will replace the current window content seamlessly rather than opening a new window, preserving the cyberpunk design of the app. We will leverage existing UI components from `MainMenu.java` (like `vt323` fonts, colors, `DitheredBar`, brackets) to ensure visual consistency and faithfully translate the provided aesthetic into Swing.

## User Review Required
No further user review required on the plan since feedback has been integrated. I am ready to proceed with execution.

## Proposed Changes

---

### UI Core & Routing

#### [MODIFY] [MainMenu.java](file:///c:/Users/andyf/Documents/GitHub/ProjectileMotion/src/MainMenu.java)
- **Container Structure**: Refactor the main content area to utilize a `CardLayout`. 
  - `CardLayout` handles complex UI panel switching without issues for logic/animations. 
  - We will extract the current layout (Center Canvas + Right InfoPanel) into a `HomeView` card, and the new Leaderboard design (Center Table + Right InfoPanel) into a `LeaderboardView` card.
- **Sidebar & Header/Footer**: The left column (sidebar) will remain exactly the same as the current main menu, retaining the EXP bar and nav labels ("NEW GAME", "HOW TO PLAY", etc.). The sidebar will update its visual "active" or highlighted state depending on whether the user is on the Home view or Leaderboard view.
- **Data Model**: Expose access to the `scoreBoard` array list (mentioned as currently in `Main.java`/`Game.java`) so the Leaderboard table can fetch real records instead of using hardcoded dummies. We will mock or wire this up with the real player data and their chosen tanks dynamically.
- **Dynamic Tanks**: Modify the `CanvasArea` graphics rendering code so it can be invoked to paint the specific tank chosen by each player within their specific Leaderboard row. Leftover empty rows can either be invisible or show placeholder info.

#### [NEW] [LeaderboardView.java](file:///c:/Users/andyf/Documents/GitHub/ProjectileMotion/src/LeaderboardView.java) (or embedded panel in MainMenu)
- Create the content that will sit in the center and right columns.
- **Center Table (`LeaderboardCanvas`)**: Build the ranking table displaying `RANK`, `UNIT` (SVG replacement), `OPERATOR_ID`, and `HIGH_SCORE`. The tanks will be rendered dynamically based on the real player's chosen tank.
- **Right Column (`InfoPanel`)**: Provide the "YOUR RANKING", "SEASON PROGRESS", and "LEGACY STATUS" specific to the Leaderboard screen, using the existing styling (`createStatBox`, `DitheredBar`).

## Verification Plan

### Automated Tests
- N/A - UI only task.

### Manual Verification
- Compile code using `javac src/*.java` or the equivalent running powershell.
- Run using `java -cp src Main`.
- Navigate to the "LEADERBOARD" button to ensure it transitions seamlessly.
- Verify that the left sidebar remains unchanged (except for which button is active).
- Verify that Leaderboard rows pull from a mock or real `scoreBoard` data source, rendering the correct dynamically chosen tank icon for each row.
