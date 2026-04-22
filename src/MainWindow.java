import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Name:    MainWindow.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Desc:    The active battle window that opens when both players click BATTLE.
 *          Receives player names and tank data from CharacterSelectPanel,
 *          then runs the projectile-physics game loop via a Swing GUI.
 *
 * How the game works:
 *   1. Both players are placed at random X positions on a number line (0-200).
 *   2. Players alternate turns entering an angle (0–180 degrees).
 *   3. The game calculates where their shot lands using projectile physics.
 *   4. A hit is scored when the shot lands within 1 unit of the opponent.
 *   5. The game prompts to replay or return to the main menu.
 *
 * Tank stats' effect on gameplay:
 *   - offensivePower → caps the maximum launch power for that player's shots.
 *   - mobilityIndex  → caps how far that player can reposition per round.
 */
public class MainWindow extends JFrame {

    // -----------------------------------------------------------------------
    // LEARNING (Constants — final fields):
    //   Fields marked 'final' cannot be reassigned after they're first set.
    //   'private final Color bg' = a colour that's specific to THIS object (private),
    //   set once, and never changes (final).  These hex-equivalent colours match
    //   the MainMenu palette for visual consistency across screens.
    // -----------------------------------------------------------------------

    /** Background colour — matches MainMenu's light grey (#eff3f1). */
    private final Color bg = new Color(239, 243, 241);

    /** Foreground colour — pure black for all text and borders. */
    private final Color fg = new Color(0, 0, 0);

    /** The VT323 monospace font loaded from disk (fallback: Monospaced system font). */
    private Font vt323;

    // -----------------------------------------------------------------------
    // LEARNING (Storing player data from another screen):
    //   When the BATTLE button is clicked in CharacterSelectPanel, it creates a 
    //   new MainWindow and passes player names + tank objects as constructor 
    //   arguments. Those values are stored here as instance fields so every method 
    //   in this class can use them without needing to pass them around all the time.
    // -----------------------------------------------------------------------

    /** Player 1's display name (e.g., "GHOST"). */
    private String p1Name;

    /** Player 1's selected tank with its stats (offensivePower, mobilityIndex). */
    private TankData p1Tank;

    /** Player 2's display name. */
    private String p2Name;

    /** Player 2's selected tank. */
    private TankData p2Tank;

    // -----------------------------------------------------------------------
    // Game state — these fields change as rounds progress
    // -----------------------------------------------------------------------

    /** Player 1's current X-position on the battlefield number line. */
    private int p1Position;

    /** Player 2's current X-position on the battlefield number line. */
    private int p2Position;

    /** Running total of hits landed by Player 1 this session. */
    private int p1Score = 0;

    /** Running total of hits landed by Player 2 this session. */
    private int p2Score = 0;

    /** How many rounds have been played in the current game session. */
    private int roundNum = 0;

    /**
     * Whose turn it is. true = Player 1's turn, false = Player 2's turn.
     * LEARNING (boolean flag for alternating turns):
     *   A simple boolean that flips with '!p1Turn' each round is an elegant way 
     *   to alternate between two states without a counter or array index.
     */
    private boolean p1Turn = true;

    // -----------------------------------------------------------------------
    // UI Components — stored as fields so game logic methods can update them
    // LEARNING: Not all components need to be fields. Only the ones that need
    // to be READ or UPDATED after initial construction should be stored here.
    // -----------------------------------------------------------------------

    /** Displays "ROUND 01", "ROUND 02", etc. at the bottom. */
    private JLabel roundLabel;

    /** Live score counters shown in each player's side strip. */
    private JLabel p1ScoreLabel, p2ScoreLabel;

    /** Live position labels updated each time positions change. */
    private JLabel p1PosLabel, p2PosLabel;

    /** Multi-purpose status display: shows whose turn it is, hit results, or errors. */
    private JLabel statusLabel;

    /** Text field where the active player enters their launch angle (0–180°). */
    private JTextField angleField;

    /** Text field for optional repositioning shift (limited by mobilityIndex). */
    private JTextField posShiftField;

    /** The drawing canvas where battle animations would be rendered. */
    private AnimationPanel animationPanel;

    // -----------------------------------------------------------------------
    // CONSTRUCTORS
    // -----------------------------------------------------------------------

    /**
     * Primary constructor — receives full player configuration from CharacterSelectPanel.
     *
     * LEARNING (Constructor chaining / the 'this(...)' call):
     *   The no-arg constructor below uses 'this(...)' to call THIS constructor 
     *   with default values. This avoids duplicating setup code.
     *
     * @param p1Name Player 1's chosen name.
     * @param p1Tank Player 1's chosen tank and its stats.
     * @param p2Name Player 2's chosen name.
     * @param p2Tank Player 2's chosen tank and its stats.
     */
    public MainWindow(String p1Name, TankData p1Tank, String p2Name, TankData p2Tank) {
        // Store incoming player data as fields for use throughout this class
        this.p1Name = p1Name;
        this.p1Tank = p1Tank;
        this.p2Name = p2Name;
        this.p2Tank = p2Tank;

        loadFont(); // Must load font BEFORE any UI components are created

        // Standard JFrame (window) configuration
        setTitle("BIT-REKT // BATTLE");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centre the window on screen

        // Randomize positions — P1 on the left (0-99), P2 on the right (100-199)
        p1Position = (int) (Math.random() * 100);
        p2Position = 100 + (int) (Math.random() * 100);

        buildUI();       // Assemble all Swing components into the window
        refreshTurnUI(); // Set up the display for the first turn
    }

    /**
     * No-argument constructor for backward compatibility.
     * Opens the battle window with default/placeholder player data.
     *
     * LEARNING (delegating constructors):
     *   'this(...)' calls the other constructor in the same class. This means all 
     *   the real initialization code lives in one place, avoiding duplication.
     */
    public MainWindow() {
        this("PLAYER_1", new TankData("M8 GREYHOUND", 63.5, 88.2),
             "PLAYER_2", new TankData("FLAK 88", 78.0, 41.5));
    }

    // -----------------------------------------------------------------------
    // FONT LOADING
    // -----------------------------------------------------------------------

    /**
     * Loads the custom VT323 pixel font from the src/fonts directory.
     * Falls back to the system Monospaced font if the file is not found.
     *
     * LEARNING (Font.createFont):
     *   Java can load external .ttf (TrueType Font) files at runtime using 
     *   Font.createFont(). After loading, you call:
     *     GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)
     *   to make the font available to all Swing components in this session.
     *
     * LEARNING (Ternary operator):
     *   'condition ? valueIfTrue : valueIfFalse'
     *   is shorthand for a two-branch if/else that produces a value.
     *   Here: if the font file exists → load it; otherwise → use fallback.
     */
    private void loadFont() {
        try {
            File f = new File("src/fonts/VT323-Regular.ttf");
            vt323 = f.exists()
                    ? Font.createFont(Font.TRUETYPE_FONT, f)    // Custom pixel font
                    : new Font("Monospaced", Font.PLAIN, 16);   // Fallback
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(vt323);
        } catch (Exception e) {
            vt323 = new Font("Monospaced", Font.PLAIN, 16); // Catch-all fallback
        }
    }

    // -----------------------------------------------------------------------
    // UI CONSTRUCTION
    // -----------------------------------------------------------------------

    /**
     * Builds and assembles the entire battle window UI.
     *
     * LEARNING (Layout Managers):
     *   Swing uses "layout managers" to decide how to position and size child 
     *   components inside a container. Common ones used here:
     *     - BorderLayout: divides a panel into NORTH/SOUTH/EAST/WEST/CENTER zones.
     *     - GridBagLayout: cell-based grid with fine-grained control.
     *     - BoxLayout: stacks items in a single row (X_AXIS) or column (Y_AXIS).
     *     - FlowLayout: flows items left-to-right, wrapping to next line.
     *
     * LEARNING (Composition — building complex UIs from simple panels):
     *   Each section (header, centre, bottom) is built as its own JPanel and 
     *   nested inside larger panels. Thinking in terms of nested rectangles makes 
     *   complex layouts manageable.
     */
    private void buildUI() {
        // Root panel centred inside the JFrame using GridBagLayout
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(bg);
        add(root); // Add the root to the JFrame's default content pane

        // MainFramePanel draws the decorative border around the game area
        MainFramePanel frame = new MainFramePanel();
        frame.setPreferredSize(new Dimension(900, 620));
        frame.setLayout(new BorderLayout(0, 0));
        root.add(frame);

        // --- HEADER (NORTH zone) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false); // Transparent — lets the parent's background show through

        // Bottom border + internal padding for the header bar
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, fg), // 2px bottom line only
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));   // Internal padding

        // Title block: two stacked labels (subtitle + main title)
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS)); // Stack vertically
        titleBlock.setOpaque(false);
        titleBlock.add(lbl("HEAVY ARMORED DIVISION", 12f)); // Small subtitle
        titleBlock.add(lbl("BIT-REKT", 48f));               // Large game title
        header.add(titleBlock, BorderLayout.WEST);

        // Right-side status info (location, mode, clock) — updated by a Timer
        JLabel systemStatus = lbl("", 14f);
        systemStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(systemStatus, BorderLayout.EAST);
        frame.add(header, BorderLayout.NORTH);

        // LEARNING (javax.swing.Timer — repeated background tasks):
        //   Timer(delay, listener) fires an ActionEvent every 'delay' milliseconds.
        //   Here: every 1000ms (1 second) we update the clock in the header.
        //   The ActionEvent 'e' is the parameter in the lambda — we don't use it here.
        Timer clock = new Timer(1000, e -> {
            String t = new SimpleDateFormat("HH:mm:ss").format(new Date());
            // HTML in JLabel: Swing supports a subset of HTML inside labels.
            // This allows multi-line text and 'align' attributes.
            systemStatus.setText("<html><p align='right' style='line-height:0.8'>" +
                    "LOCATION: CAMP 30<br>MODE: VERSUS_2P<br>TIME: " + t + "</p></html>");
        });
        clock.setInitialDelay(0); // Fire immediately (don't wait 1 second for the first tick)
        clock.start();

        // --- CENTRE: two player strips flanking the animation canvas ---
        JPanel centre = new JPanel(new BorderLayout(0, 0));
        centre.setOpaque(false);

        centre.add(buildPlayerStrip(1), BorderLayout.WEST);  // Player 1 side panel

        // The animation canvas sits in the middle — will show projectile paths
        animationPanel = new AnimationPanel();
        animationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 0, 8, 0),
                new DashedBorder(fg, 1, 4)));
        centre.add(animationPanel, BorderLayout.CENTER);

        centre.add(buildPlayerStrip(2), BorderLayout.EAST);  // Player 2 side panel

        frame.add(centre, BorderLayout.CENTER);

        // --- BOTTOM: round counter, input fields, and footer ---
        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, fg), // Top border only
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        // Round number + status message row
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        roundLabel  = lbl("ROUND 00", 20f);
        statusLabel = lbl("", 18f);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusRow.add(roundLabel,  BorderLayout.WEST);
        statusRow.add(statusLabel, BorderLayout.EAST);
        bottom.add(statusRow, BorderLayout.NORTH);

        // Input fields row (angle + position shift + FIRE button)
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        inputRow.setOpaque(false);

        inputRow.add(lbl("ANGLE (0-180):", 18f));
        angleField = styledField(6); // 6-character wide input box
        inputRow.add(angleField);

        inputRow.add(lbl("POS SHIFT:", 18f));
        posShiftField = styledField(6);
        inputRow.add(posShiftField);

        // FIRE button — triggers the physics calculation when clicked
        JPanel fireBtn = createButton("FIRE", () -> handleFire());
        inputRow.add(fireBtn);

        bottom.add(inputRow, BorderLayout.CENTER);

        // Footer: credits on the left, "MAIN MENU" link on the right
        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setOpaque(false);
        footerRow.add(lbl("CREATED BY ANDREW FILSON", 14f), BorderLayout.WEST);

        // Clickable "[ MAIN MENU ]" label — acts like a hyperlink
        JLabel backLbl = lbl("[ MAIN MENU ]", 14f);
        backLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        backLbl.addMouseListener(new MouseAdapter() {
            // LEARNING (MouseAdapter):
            //   MouseAdapter implements MouseListener with empty methods for all events.
            //   We override only the ones we care about, keeping the code concise.
            public void mousePressed(MouseEvent e)  { goToMainMenu(); }
            public void mouseEntered(MouseEvent e)  { backLbl.setForeground(new Color(80, 80, 80)); }
            public void mouseExited(MouseEvent e)   { backLbl.setForeground(fg); }
        });
        footerRow.add(backLbl, BorderLayout.EAST);
        bottom.add(footerRow, BorderLayout.SOUTH);

        frame.add(bottom, BorderLayout.SOUTH);
    }

    // -----------------------------------------------------------------------
    // PLAYER STRIP BUILDER
    // -----------------------------------------------------------------------

    /**
     * Builds and returns the vertical side panel for one player.
     * Displays their name, tank type, stats, current score, and position.
     *
     * @param num Player number (1 or 2). Determines left/right side and data source.
     * @return    A configured JPanel ready to be added to the centre area.
     *
     * LEARNING (ternary operator for player-specific data):
     *   'num == 1 ? p1Name : p2Name' is used throughout to pick the right value
     *   for whichever player this strip belongs to. This keeps one method serving
     *   both players without writing the code twice.
     */
    private JPanel buildPlayerStrip(int num) {
        String   name = num == 1 ? p1Name : p2Name;
        TankData tank = num == 1 ? p1Tank : p2Tank;

        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS)); // Stack items vertically
        strip.setOpaque(false);
        strip.setPreferredSize(new Dimension(180, 0)); // Fixed width; height fills automatically

        // LEARNING (MatteBorder — one-sided borders):
        //   MatteBorder(top, left, bottom, right, color) draws a border only on 
        //   the specified sides. Here P1 gets a right border, P2 gets a left border,
        //   creating the visual divider between the strip and the animation panel.
        int border = (num == 1) ? 0 : 1;
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, border, 0, 1 - border, fg),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));

        // --- Player heading chip (inverted for P1, outlined for P2) ---
        JLabel heading = lbl(String.format("PLAYER %02d", num), 28f);
        if (num == 1) {
            // Player 1 chip: solid black background with white text
            heading.setOpaque(true);
            heading.setBackground(fg);
            heading.setForeground(bg);
            heading.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        } else {
            // Player 2 chip: black outline with normal text
            heading.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 2),
                    BorderFactory.createEmptyBorder(2, 6, 2, 4)));
        }
        heading.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align within BoxLayout
        strip.add(heading);
        strip.add(Box.createVerticalStrut(6)); // 6px vertical gap

        // Player name
        JLabel nameLbl = lbl(name, 20f);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(nameLbl);
        strip.add(Box.createVerticalStrut(10));

        // Tank name displayed as a small inverted chip
        JLabel tankLbl = lbl(tank.getName(), 14f);
        tankLbl.setOpaque(true);
        tankLbl.setBackground(fg);
        tankLbl.setForeground(bg);
        tankLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tankLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(tankLbl);
        strip.add(Box.createVerticalStrut(10));

        // Tank stat display boxes (Offensive Power, Mobility Index)
        strip.add(buildMiniStat("OFF. POWER", (int) tank.getOffensivePower()));
        strip.add(Box.createVerticalStrut(6));
        strip.add(buildMiniStat("MOB. INDEX",  (int) tank.getMobilityIndex()));
        strip.add(Box.createVerticalStrut(10));

        // Score section — labelled with a bottom rule
        JLabel scoreHead = lbl("SCORE", 13f);
        scoreHead.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        scoreHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(scoreHead);

        // Large numeric score value — stored as field so handleFire() can update it
        JLabel scoreVal = lbl("0", 30f);
        scoreVal.setFont(scoreVal.getFont().deriveFont(Font.BOLD));
        scoreVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(scoreVal);

        // Assign to the correct field based on player number
        if (num == 1) p1ScoreLabel = scoreVal; else p2ScoreLabel = scoreVal;

        strip.add(Box.createVerticalStrut(8));

        // Position section — updated every round by refreshTurnUI()
        JLabel posHead = lbl("POSITION", 13f);
        posHead.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        posHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(posHead);

        JLabel posVal = lbl("---", 24f);
        posVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(posVal);

        if (num == 1) p1PosLabel = posVal; else p2PosLabel = posVal;

        // Vertical 'glue' pushes everything above it upwards — keeps items top-aligned
        strip.add(Box.createVerticalGlue());
        return strip;
    }

    /**
     * Creates a small compact stat box showing a label and a numeric value.
     *
     * @param label The stat name (e.g., "OFF. POWER").
     * @param value The numeric stat value to display.
     * @return      A styled JPanel containing the label and value.
     */
    private JPanel buildMiniStat(String label, int value) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Stat label with a bottom underline
        JLabel lbl = lbl(label, 12f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(lbl);

        // Large bold numeric value
        JLabel val = lbl(String.valueOf(value), 22f);
        val.setFont(val.getFont().deriveFont(Font.BOLD));
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(val);
        return box;
    }

    // -----------------------------------------------------------------------
    // GAME LOGIC
    // -----------------------------------------------------------------------

    /**
     * Updates all turn-related UI elements to reflect the current player's turn.
     * Called at the start of each round and after a new game session begins.
     *
     * LEARNING (Single Responsibility — helper methods):
     *   Instead of scattering label.setText(...) calls throughout the code,
     *   we group all "refresh the UI for the current turn" logic here.
     *   Any method that needs to update the turn display just calls refreshTurnUI().
     */
    private void refreshTurnUI() {
        roundNum++;
        roundLabel.setText(String.format("ROUND %02d", roundNum)); // %02d = zero-padded 2 digits

        // Determine whose data to show based on the p1Turn flag
        String   current = p1Turn ? p1Name : p2Name;
        TankData tank    = p1Turn ? p1Tank  : p2Tank;

        // Status bar: shows active player's name + their tank's stat caps
        statusLabel.setText(current + "'s TURN  |  MAX PWR: " +
                String.format("%.0f", tank.getOffensivePower()) +  // %.0f = no decimal places
                "  MOB: " + String.format("%.0f", tank.getMobilityIndex()));

        // Sync position labels with current game state
        p1PosLabel.setText(String.valueOf(p1Position));
        p2PosLabel.setText(String.valueOf(p2Position));

        // Keep the global game counter in sync with round number
        Main.gamesPlayed = roundNum;

        // Clear input fields ready for the next player's entry
        angleField.setText("");
        posShiftField.setText("");

        // Auto-focus the angle field so the active player can type immediately
        angleField.requestFocusInWindow();
    }

    /**
     * Processes a player's "FIRE" action — validates inputs, applies physics,
     * checks for a hit, and updates the game state accordingly.
     *
     * LEARNING (Input validation before game logic):
     *   We validate BOTH inputs before doing ANY calculations. This ensures the 
     *   game state is never corrupted by partial input (e.g., a valid angle but 
     *   an invalid position shift).
     *
     * LEARNING (Projectile physics in this context):
     *   The same formulas from Game.java and Player.java are used here:
     *     time of flight (tof) = (2 * power * sin(θ)) / g
     *     landing X (landX)    = startPosition + power * cos(θ) * tof
     *   A 'miss' distance < 1 unit from the opponent's position counts as a hit.
     */
    private void handleFire() {
        // Identify the active player's data for this turn
        String   currentName = p1Turn ? p1Name     : p2Name;
        TankData tank        = p1Turn ? p1Tank      : p2Tank;
        int      targetPos   = p1Turn ? p2Position  : p1Position;

        // --- Validate angle input ---
        double angle;
        try {
            angle = Double.parseDouble(angleField.getText().trim());
            if (angle < 0 || angle > 180) throw new NumberFormatException(); // Force range check
        } catch (NumberFormatException ex) {
            flash("INVALID ANGLE — must be 0–180");
            return; // Exit early — don't proceed with bad input
        }

        // --- Validate optional position shift input ---
        int shift = 0; // Default: no repositioning
        String shiftTxt = posShiftField.getText().trim();
        if (!shiftTxt.isEmpty()) {
            try {
                shift = Integer.parseInt(shiftTxt);
                int maxShift = (int) tank.getMobilityIndex(); // Tank stat caps the max shift

                // LEARNING (Math.abs for absolute deviation):
                //   Math.abs(shift) handles both +10 and -10 (moving left or right) 
                //   equally. We only care if the MAGNITUDE exceeds the cap.
                if (Math.abs(shift) > maxShift) {
                    flash("POSITION SHIFT exceeds MOB. INDEX cap of " + maxShift);
                    return;
                }
            } catch (NumberFormatException ex) {
                flash("INVALID SHIFT — must be a whole number");
                return;
            }
        }

        // --- Apply position shift ---
        if (p1Turn) p1Position += shift;
        else        p2Position += shift;

        // --- Physics calculation ---
        final double GRAVITY  = 9.81;
        double maxPower = tank.getOffensivePower(); // Full power used (could be user input later)
        double rad      = Math.toRadians(angle);
        double tof      = (2 * maxPower * Math.sin(rad)) / GRAVITY; // Time of flight
        double landX    = (p1Turn ? p1Position : p2Position) + maxPower * Math.cos(rad) * tof;

        // --- Hit detection ---
        double  miss = Math.abs(landX - targetPos);
        boolean hit  = miss < 1.0; // Within 1 unit = direct hit

        // --- Resolve outcome ---
        String result;
        if (hit) {
            result = currentName + " HIT! ROUND OVER.";

            // Update the correct score label for the hitting player
            if (p1Turn) {
                p1Score++;
                p1ScoreLabel.setText(String.valueOf(p1Score));
            } else {
                p2Score++;
                p2ScoreLabel.setText(String.valueOf(p2Score));
            }
            Main.gamesPlayed++;

            // LEARNING (JOptionPane.showOptionDialog — custom modal dialogue):
            //   showOptionDialog lets you define exactly which buttons appear.
            //   The return value tells you which button the user clicked (0 = first, 1 = second).
            int choice = JOptionPane.showOptionDialog(this,
                    String.format("%s lands at %.1f — DIRECT HIT!\n\nScore: %s %d  |  %s %d\n\nPlay another round?",
                            currentName, landX, p1Name, p1Score, p2Name, p2Score),
                    "HIT!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{"BATTLE AGAIN", "MAIN MENU"},
                    "BATTLE AGAIN");

            if (choice == JOptionPane.YES_OPTION) {
                // Reset for a fresh round — new random positions, reset round counter
                p1Position = (int) (Math.random() * 100);
                p2Position = 100 + (int) (Math.random() * 100);
                roundNum   = 0;
                refreshTurnUI();
            } else {
                goToMainMenu(); // Player chose to return to the menu
            }
        } else {
            // Nobody hit — log the miss and switch turns
            result = String.format("%s: landed %.1f  (missed by %.1f)", currentName, landX, miss);
            p1Turn = !p1Turn; // Flip between true and false — alternates whose turn it is
            refreshTurnUI();
        }

        // Update status bar with the result of this shot
        statusLabel.setText(result);
    }

    /**
     * Displays a warning message in the status label (prefixed with ⚠).
     * Called when the player provides invalid input before firing.
     *
     * @param msg The warning text to display.
     */
    private void flash(String msg) {
        statusLabel.setText("⚠ " + msg);
    }

    /**
     * Closes this battle window and re-opens the Main Menu.
     *
     * LEARNING (dispose vs System.exit):
     *   'dispose()' closes THIS specific window and frees its resources,
     *   but the JVM keeps running — allowing us to open MainMenu right after.
     *   'System.exit(0)' would shut down the entire application.
     */
    private void goToMainMenu() {
        dispose(); // Close only this window
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true)); // Open main menu on EDT
    }

    // -----------------------------------------------------------------------
    // UI HELPER METHODS
    // LEARNING (DRY principle — Don't Repeat Yourself):
    //   These private helpers avoid repeating the same 4-5 lines every time we
    //   need a styled label, field, or button. If the style changes, we update 
    //   it in ONE place instead of hunting through dozens of lines.
    // -----------------------------------------------------------------------

    /**
     * Creates a styled JLabel using the VT323 font at the given size.
     *
     * @param txt  The label text.
     * @param size Font size in points (e.g., 24f).
     * @return     A configured JLabel.
     */
    private JLabel lbl(String txt, float size) {
        JLabel l = new JLabel(txt);
        l.setFont(vt323.deriveFont(size)); // deriveFont creates a sized variant of the loaded font
        l.setForeground(fg);
        l.setOpaque(false);
        return l;
    }

    /**
     * Creates a styled JTextField matching the BIT-REKT aesthetic.
     *
     * @param cols The number of character columns wide this field should be.
     * @return     A configured JTextField.
     */
    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(vt323.deriveFont(22f));
        f.setForeground(fg);
        f.setBackground(bg);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),       // Outer solid border
                BorderFactory.createEmptyBorder(3, 6, 3, 6))); // Inner padding
        f.setCaretColor(fg); // The blinking cursor colour
        return f;
    }

    /**
     * Creates a clickable button panel with press/hover effects.
     *
     * LEARNING (Runnable / Lambda for callbacks):
     *   The 'action' parameter is a Runnable — an interface with a single method 'run()'.
     *   We pass in what to DO when the button is clicked using a lambda:
     *       createButton("FIRE", () -> handleFire())
     *   This is called a "callback" — the button panel doesn't need to know what 
     *   specific logic runs; it just calls 'action.run()' on click. This decouples 
     *   the button's visual behaviour from the application logic.
     *
     * @param label  Text displayed on the button.
     * @param action Code to run when the button is pressed.
     * @return       A styled, interactive JPanel acting as a button.
     */
    private JPanel createButton(String label, Runnable action) {
        JPanel btn = new JPanel(new BorderLayout());
        btn.setOpaque(true);
        btn.setBackground(fg); // Start with solid black background
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 2),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = lbl(label, 24f);
        lbl.setForeground(bg);                              // White text on black background
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        btn.add(lbl, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { action.run(); }  // Execute the callback
            public void mouseEntered(MouseEvent e)  {
                // Hover effect: invert colours (white bg, black text)
                btn.setBackground(bg); lbl.setForeground(fg); btn.repaint();
            }
            public void mouseExited(MouseEvent e) {
                // Restore: back to black bg, white text
                btn.setBackground(fg); lbl.setForeground(bg); btn.repaint();
            }
        });
        return btn;
    }

    // -----------------------------------------------------------------------
    // NOTE: DashedBorder and MainFramePanel have been removed from here.
    // They are now shared top-level classes in UIComponents.java, used by
    // both MainWindow and MainMenu to avoid code duplication.
    // -----------------------------------------------------------------------
}
