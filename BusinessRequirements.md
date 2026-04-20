# Implement "PANZER-BIT" Retro Terminal Main Menu

Based on the visual analysis of the provided mockup, I will implement a complete, standalone Java Swing main menu that identically matches the 8-bit military-industrial aesthetic of the "PANZER-BIT" design.

## User Review Required
> [!IMPORTANT]
> Since you provided a visual reference and asked to "make it look exactly like this", I am proposing to **write the actual Java Swing code** to implement this menu for your `ProjectileMotion` game directly, replacing the current prompt generation exercise. 
> 
> *If you instead only wanted an updated text prompt to give to another AI, let me know and I can provide that instead.* Otherwise, review the plan below to build it into your project!

## Proposed Changes

We will introduce a new `MainMenu.java` window that launches first, and handles the transition into your existing `MainWindow.java` (the actual game data entry GUI) when the player clicks the "DEPLOY UNIT" button.

### Core Architecture

#### [NEW] src/MainMenu.java
I will create a highly customized `JFrame` encompassing the following structure:
*   **Custom Dot-Grid Background:** Overriding `paintComponent` to draw the subtle steel-blue dot matrix (`#B0C4DE` background).
*   **Container Borders:** Utilizing `BorderFactory.createCompoundBorder` to create the thick black / thin white / thin black double-container frame.
*   **Typography:** The entire UI will utilize a bold `Monospaced` terminal font to replicate the retro feel perfectly.
*   **Left Column (Navigation):** 
    *   Implementing custom `JButton`s for `DEPLOY UNIT`, `ARMORY`, `STRATEGY`, `SETTINGS`, and `TERMINATE`. 
    *   Adding `MouseListener`s to these buttons to replicate the light-grey hover backgrounds requested.
    *   `DEPLOY UNIT` will hide this menu and launch `MainWindow()`.
    *   `TERMINATE` will exit the application.
*   **Center Column (Display):** 
    *   Implementing a custom `JPanel` drawing a hard-coded 8-bit pixel art silhouette of an armored tank to replicate the graphic exactly, without requiring external image files.
*   **Right Column (Stats):** 
    *   Custom vertical layout with large, dominant numeric values and slim progress bars (custom `JPanel`s with specific `setBackground` sizing) to represent "OFFENSIVE POWER" and other stats.
    *   A small `JTextArea` to hold the `> SYSTEM LOGS`.

#### [MODIFY] src/Main.java
*   Update the `main` method to instantiate `MainMenu` instead of `MainWindow` on startup.

## Verification Plan

### Manual Verification
1. Run `Main.java`. 
2. Verify the visual layout accurately matches the provided "PANZER-BIT" mock-up (colors, typography, grid, columns).
3. Test button hover states.
4. Click `DEPLOY UNIT` to ensure the old `MainWindow` successfully launches and the main menu closes.
5. Click `TERMINATE` to ensure the application exits cleanly.
