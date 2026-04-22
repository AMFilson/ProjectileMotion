# New Game / Character Select Screen — Revised Plan

## Summary

The **Character Select** screen opens inside the same `JFrame` as the Main Menu using the existing `CardLayout` — no new window is created. When the user clicks **NEW GAME**, the `"NEW_GAME"` card is shown. When both players click **READY** and hit **BATTLE**, the game launches via `MainWindow`.

Game logic is based on the existing `Game.java` design, adapted for GUI:
- **Player stats** (Offensive Power, Mobility Index) are **determined by the selected tank** on this screen — players cannot change them.
- During the battle, players can only adjust **angle** and their **position** (capped by Mobility Index).

---

## Proposed Changes

---

### TankData.java

#### [MODIFY] [TankData.java](file:///c:/Users/andyf/Documents/GitHub/ProjectileMotion/src/TankData.java)

- Add **fixed preset stats** for the 3 tanks (instead of random) so stats stay stable throughout the Character Select screen.
- Remove the `armorRating` field — not needed.
- Keep existing `offensivePower` and `mobilityIndex` fields.
- Add a constructor `TankData(String name, double op, double mi)` — it already exists, just standardize it:

| Tank | Offensive Power | Mobility Index |
|---|---|---|
| M8 GREYHOUND | 63.5 | 88.2 |
| FLAK 88 | 78.0 | 41.5 |
| BLACK CAT | 55.0 | 72.0 |

These will be the **preset values** used when the game starts from Character Select.

---

### MainMenu.java — Core Changes

#### [MODIFY] [MainMenu.java](file:///c:/Users/andyf/Documents/GitHub/ProjectileMotion/src/MainMenu.java)

**1. Register `"NEW_GAME"` card:**
```java
cardContentPanel.add(new CharacterSelectPanel(), "NEW_GAME");
```

**2. Update `handleNavClick`** — switch to the new card instead of opening `MainWindow`:
```java
if (title.equals("NEW GAME")) {
    cardLayout.show(cardContentPanel, "NEW_GAME");
}
```

**3. Add `CharacterSelectPanel` inner class** (detailed below).

---

### CharacterSelectPanel — Inner Class Layout

Two `PlayerColumn` sub-panels side by side, separated by a thin divider line. Below them, a shared footer with the **BATTLE** button.

```
┌─────────────────────────────────────────────────┐
│ PLAYER 01  [SYS.OP.1] │ PLAYER 02  [SYS.OP.2]  │
│ > IDENT: [GHOST____]  │ > IDENT: [________]     │
│ ┌─── [TANK PREVIEW] ─┐│ ┌─── [TANK PREVIEW] ─┐  │
│ │  < [pixel art]   > ││ │  < [pixel art]   > │  │
│ │   M8 GREYHOUND     ││ │   FLAK 88          │  │
│ └────────────────────┘│ └────────────────────┘  │
│ [OFFENSIVE POWER  ██ ]│ [OFFENSIVE POWER  ██  ] │
│ [MOBILITY INDEX   ██ ]│ [MOBILITY INDEX   ██  ] │
│ [STATUS:  READY ]     │ [STATUS:  STANDBY ]      │
├────────────────────────────────────────────────-┤
│ CREATED BY ANDREW FILSON  [BATTLE]  AWAITING P2 │
└─────────────────────────────────────────────────┘
```

---

### PlayerColumn Details

Each `PlayerColumn` is a `JPanel` with `BoxLayout(Y_AXIS)` containing:

| Section | Details |
|---|---|
| **Header** | `PLAYER 01` label (inverted for P1, outlined for P2) + `[SYS.OP.1]` |
| **Name Input** | `> IDENT:` prefix + `JTextField` (max 8 chars, uppercase, VT323 font, dashed bottom border on idle, solid on focus) |
| **Tank Carousel** | `DashedBorder` panel with `<` / `>` buttons and tank pixel-art canvas |
| **Tank Name** | Inverted label beneath the canvas |
| **Stat Bars** | `OFFENSIVE POWER` + `MOBILITY INDEX` rows (using existing `DitheredBar`, solid fill) |
| **Status Button** | Toggles between `[ READY ]` (filled) and `[ STANDBY ]` (outlined) |

#### Tank Carousel:
- Both players start on **M8 GREYHOUND** (index 0).
- Tanks are shared: `[M8 GREYHOUND, FLAK 88, BLACK CAT]` — same list for both, either can pick the same tank simultaneously.
- Cycling via `<` / `>` buttons updates the stat bars and tank art in real time.
- Tank pixel-art rendering reuses the **existing drawing logic** already present in `CanvasArea` and the Leaderboard row painter.

#### Stat Bars:
- Reuse `createStatBox()` with the existing `DitheredBar` (solid fill for OP/MI — no armor bar).
- Values are driven by the selected tank's `TankData` stats.

---

### Save/Load Integration

#### [MODIFY] [DataManager.java](file:///c:/Users/andyf/Documents/GitHub/ProjectileMotion/src/DataManager.java)

When a game is saved, the following data is written to file:

```
PLAYER1_NAME=GHOST
PLAYER1_TANK_INDEX=0
PLAYER2_NAME=GUEST_2
PLAYER2_TANK_INDEX=1
```

When loaded, `CharacterSelectPanel` populates the name fields and sets tank indices from the file. This pairs with the existing `DataManager.saveGame()` / `loadGame()` structure.

---

### MainWindow.java — Rebuild as Battle Window

#### [MODIFY] [MainWindow.java](file:///c:/Users/andyf/Documents/GitHub/ProjectileMotion/src/MainWindow.java)

Rebuilt as the **battle screen** that launches after BATTLE is clicked. Accepts a constructor:
```java
public MainWindow(String p1Name, int p1TankIndex, String p2Name, int p2TankIndex)
```

The window will:
- Display the `AnimationPanel` in the center.
- Show player names, tank stats, angle input fields, and a "FIRE" button.
- Contain the projectile physics logic from `Game.java` adapted for GUI (`runGame()` loop → button-driven rounds).
- Track score and display who wins.

---

## Suggestions

> [!TIP]
> **Mobility Index → position range**: the Mobility Index (0–100) could directly cap how far a player can shift their **starting position** each round. For example, a tank with `MI = 88.2` could move up to 88 units, while `MI = 41.5` caps at 41. This adds strategic weight to tank choice.

> [!TIP]
> **Offensive Power → shot power cap**: similarly, `offensivePower` could replace the free 1–100 power input in `Game.java`, meaning a player's max power is their tank's OP stat. This replaces the free power entry with a stat-driven system.

---

## Verification Plan

### Automated
- `javac` compile of all files after changes
- `java -cp bin Main` to launch and smoke-test

### Manual
- Click **NEW GAME** → Character Select opens in same window ✓  
- **HOME** button returns to Main Menu ✓  
- `<` / `>` carousels cycle tanks, stat bars update in real time ✓  
- Name input accepts uppercase, 8-char max ✓  
- Status button toggles READY ↔ STANDBY for each player ✓  
- **BATTLE** button disabled until both READY; clicking launches `MainWindow` ✓  
- Save/Load from Main Menu sidebar persists player names and tank choices ✓
