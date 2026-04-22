import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The BIT-REKT battle window.
 * Receives player configuration from CharacterSelectPanel and runs
 * the projectile-physics game loop via GUI controls.
 *
 * Game rules (based on Game.java logic):
 *  - Each player's max SHOT POWER is capped by their tank's Offensive Power stat.
 *  - Each player's max POSITION SHIFT per round is capped by their tank's Mobility Index stat.
 *  - Players alternate entering angle + optional position shift; physics resolves hits.
 */
public class MainWindow extends JFrame {

    // --- Colours matching MainMenu aesthetic ---
    private final Color bg = new Color(239, 243, 241);
    private final Color fg = new Color(0, 0, 0);

    private Font vt323;

    // --- Player data ---
    private String p1Name;
    private TankData p1Tank;
    private String p2Name;
    private TankData p2Tank;

    // --- Game state ---
    private int p1Position;
    private int p2Position;
    private int p1Score = 0;
    private int p2Score = 0;
    private int roundNum = 0;
    private boolean p1Turn = true; // alternates

    // --- UI Components ---
    private JLabel roundLabel;
    private JLabel p1ScoreLabel, p2ScoreLabel;
    private JLabel p1PosLabel, p2PosLabel;
    private JLabel statusLabel;
    private JTextField angleField;
    private JTextField posShiftField;
    private AnimationPanel animationPanel;

    /**
     * Constructs the battle window with player configurations from Character Select.
     */
    public MainWindow(String p1Name, TankData p1Tank, String p2Name, TankData p2Tank) {
        this.p1Name  = p1Name;
        this.p1Tank  = p1Tank;
        this.p2Name  = p2Name;
        this.p2Tank  = p2Tank;

        loadFont();

        setTitle("BIT-REKT // BATTLE");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Random starting positions
        p1Position = (int)(Math.random() * 100);
        p2Position = 100 + (int)(Math.random() * 100);

        buildUI();
        refreshTurnUI();
    }

    /** No-arg constructor for legacy compatibility (opens blank window). */
    public MainWindow() {
        this("PLAYER_1", new TankData("M8 GREYHOUND", 63.5, 88.2),
             "PLAYER_2", new TankData("FLAK 88", 78.0, 41.5));
    }

    private void loadFont() {
        try {
            File f = new File("src/fonts/VT323-Regular.ttf");
            vt323 = f.exists() ? Font.createFont(Font.TRUETYPE_FONT, f) : new Font("Monospaced", Font.PLAIN, 16);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(vt323);
        } catch (Exception e) {
            vt323 = new Font("Monospaced", Font.PLAIN, 16);
        }
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(bg);
        add(root);

        MainFramePanel frame = new MainFramePanel();
        frame.setPreferredSize(new Dimension(900, 620));
        frame.setLayout(new BorderLayout(0, 0));
        root.add(frame);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, fg),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(lbl("HEAVY ARMORED DIVISION", 12f));
        titleBlock.add(lbl("BIT-REKT", 48f));
        header.add(titleBlock, BorderLayout.WEST);

        JLabel systemStatus = lbl("", 14f);
        systemStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(systemStatus, BorderLayout.EAST);
        frame.add(header, BorderLayout.NORTH);

        // Live clock
        Timer clock = new Timer(1000, e -> {
            String t = new SimpleDateFormat("HH:mm:ss").format(new Date());
            systemStatus.setText("<html><p align='right' style='line-height:0.8'>" +
                    "LOCATION: CAMP 30<br>MODE: VERSUS_2P<br>TIME: " + t + "</p></html>");
        });
        clock.setInitialDelay(0);
        clock.start();

        // --- CENTRE: animation + side panels ---
        JPanel centre = new JPanel(new BorderLayout(0, 0));
        centre.setOpaque(false);

        // Left player info strip
        centre.add(buildPlayerStrip(1), BorderLayout.WEST);

        // Animation panel
        animationPanel = new AnimationPanel();
        animationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 0, 8, 0),
                new DashedBorder(fg, 1, 4)));
        centre.add(animationPanel, BorderLayout.CENTER);

        // Right player info strip
        centre.add(buildPlayerStrip(2), BorderLayout.EAST);

        frame.add(centre, BorderLayout.CENTER);

        // --- BOTTOM: input + fire panel ---
        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, fg),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        // Round / status
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        roundLabel = lbl("ROUND 00", 20f);
        statusLabel = lbl("", 18f);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusRow.add(roundLabel, BorderLayout.WEST);
        statusRow.add(statusLabel, BorderLayout.EAST);
        bottom.add(statusRow, BorderLayout.NORTH);

        // Input area
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        inputRow.setOpaque(false);

        inputRow.add(lbl("ANGLE (0-180):", 18f));
        angleField = styledField(6);
        inputRow.add(angleField);

        inputRow.add(lbl("POS SHIFT:", 18f));
        posShiftField = styledField(6);
        inputRow.add(posShiftField);

        // FIRE button
        JPanel fireBtn = createButton("FIRE", () -> handleFire());
        inputRow.add(fireBtn);

        bottom.add(inputRow, BorderLayout.CENTER);

        // Footer credits
        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setOpaque(false);
        footerRow.add(lbl("CREATED BY ANDREW FILSON", 14f), BorderLayout.WEST);
        JLabel backLbl = lbl("[ MAIN MENU ]", 14f);
        backLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        backLbl.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { goToMainMenu(); }
            public void mouseEntered(MouseEvent e) { backLbl.setForeground(new Color(80,80,80)); }
            public void mouseExited(MouseEvent e)  { backLbl.setForeground(fg); }
        });
        footerRow.add(backLbl, BorderLayout.EAST);
        bottom.add(footerRow, BorderLayout.SOUTH);

        frame.add(bottom, BorderLayout.SOUTH);
    }

    /** Builds the side info strip for a player (score, position, tank). */
    private JPanel buildPlayerStrip(int num) {
        String name  = num == 1 ? p1Name  : p2Name;
        TankData tank = num == 1 ? p1Tank  : p2Tank;

        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS));
        strip.setOpaque(false);
        strip.setPreferredSize(new Dimension(180, 0));

        int border = num == 1 ? 0 : 1;
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, border, 0, 1 - border, fg),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));

        // Player heading
        JLabel heading = lbl(String.format("PLAYER %02d", num), 28f);
        if (num == 1) {
            heading.setOpaque(true); heading.setBackground(fg); heading.setForeground(bg);
            heading.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        } else {
            heading.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 2),
                    BorderFactory.createEmptyBorder(2, 6, 2, 4)));
        }
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(heading);
        strip.add(Box.createVerticalStrut(6));

        // Name
        JLabel nameLbl = lbl(name, 20f);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(nameLbl);
        strip.add(Box.createVerticalStrut(10));

        // Tank name (inverted chip)
        JLabel tankLbl = lbl(tank.getName(), 14f);
        tankLbl.setOpaque(true); tankLbl.setBackground(fg); tankLbl.setForeground(bg);
        tankLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tankLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(tankLbl);
        strip.add(Box.createVerticalStrut(10));

        // Stat boxes
        strip.add(buildMiniStat("OFF. POWER", (int) tank.getOffensivePower()));
        strip.add(Box.createVerticalStrut(6));
        strip.add(buildMiniStat("MOB. INDEX",  (int) tank.getMobilityIndex()));
        strip.add(Box.createVerticalStrut(10));

        // Score + Position
        JLabel scoreHead = lbl("SCORE", 13f);
        scoreHead.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        scoreHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(scoreHead);

        JLabel scoreVal = lbl("0", 30f);
        scoreVal.setFont(scoreVal.getFont().deriveFont(Font.BOLD));
        scoreVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(scoreVal);
        if (num == 1) p1ScoreLabel = scoreVal; else p2ScoreLabel = scoreVal;

        strip.add(Box.createVerticalStrut(8));

        JLabel posHead = lbl("POSITION", 13f);
        posHead.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        posHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(posHead);

        JLabel posVal = lbl("---", 24f);
        posVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        strip.add(posVal);
        if (num == 1) p1PosLabel = posVal; else p2PosLabel = posVal;

        strip.add(Box.createVerticalGlue());
        return strip;
    }

    private JPanel buildMiniStat(String label, int value) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = lbl(label, 12f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(lbl);

        JLabel val = lbl(String.valueOf(value), 22f);
        val.setFont(val.getFont().deriveFont(Font.BOLD));
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(val);
        return box;
    }

    // -----------------------------------------------------------------------
    // Game logic
    // -----------------------------------------------------------------------

    private void refreshTurnUI() {
        roundNum++;
        roundLabel.setText(String.format("ROUND %02d", roundNum));
        String current = p1Turn ? p1Name : p2Name;
        TankData tank  = p1Turn ? p1Tank  : p2Tank;
        statusLabel.setText(current + "'s TURN  |  MAX PWR: " +
                String.format("%.0f", tank.getOffensivePower()) +
                "  MOB: " + String.format("%.0f", tank.getMobilityIndex()));
        p1PosLabel.setText(String.valueOf(p1Position));
        p2PosLabel.setText(String.valueOf(p2Position));
        Main.gamesPlayed = roundNum;
        angleField.setText("");
        posShiftField.setText("");
        angleField.requestFocusInWindow();
    }

    private void handleFire() {
        String currentName = p1Turn ? p1Name : p2Name;
        TankData tank      = p1Turn ? p1Tank  : p2Tank;
        int currentPos     = p1Turn ? p1Position : p2Position;
        int targetPos      = p1Turn ? p2Position : p1Position;

        // Parse angle
        double angle;
        try {
            angle = Double.parseDouble(angleField.getText().trim());
            if (angle < 0 || angle > 180) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            flash("INVALID ANGLE — must be 0–180");
            return;
        }

        // Parse optional position shift (capped by mobility index)
        int shift = 0;
        String shiftTxt = posShiftField.getText().trim();
        if (!shiftTxt.isEmpty()) {
            try {
                shift = Integer.parseInt(shiftTxt);
                int maxShift = (int) tank.getMobilityIndex();
                if (Math.abs(shift) > maxShift) {
                    flash("POSITION SHIFT exceeds MOB. INDEX cap of " + maxShift);
                    return;
                }
            } catch (NumberFormatException ex) {
                flash("INVALID SHIFT — must be a whole number");
                return;
            }
        }

        // Apply position shift
        if (p1Turn) p1Position += shift;
        else         p2Position += shift;

        // Physics: power is capped by Offensive Power stat
        double maxPower = tank.getOffensivePower();
        double power    = maxPower; // full power used for simplicity; can be an input later

        // Time of flight + horizontal distance
        final double GRAVITY = 9.81;
        double rad    = Math.toRadians(angle);
        double tof    = (2 * power * Math.sin(rad)) / GRAVITY;
        double landX  = (p1Turn ? p1Position : p2Position) + power * Math.cos(rad) * tof;

        double miss   = Math.abs(landX - targetPos);
        boolean hit   = miss < 1.0;

        String result;
        if (hit) {
            result = currentName + " HIT! ROUND OVER.";
            if (p1Turn) { p1Score++; p1ScoreLabel.setText(String.valueOf(p1Score)); }
            else         { p2Score++; p2ScoreLabel.setText(String.valueOf(p2Score)); }
            Main.gamesPlayed++;
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
                p1Position = (int)(Math.random() * 100);
                p2Position = 100 + (int)(Math.random() * 100);
                roundNum = 0;
                refreshTurnUI();
            } else {
                goToMainMenu();
            }
        } else {
            result = String.format("%s: landed %.1f  (missed by %.1f)", currentName, landX, miss);
            p1Turn = !p1Turn;
            refreshTurnUI();
        }

        statusLabel.setText(result);
    }

    private void flash(String msg) {
        statusLabel.setText("⚠ " + msg);
    }

    private void goToMainMenu() {
        dispose();
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    // -----------------------------------------------------------------------
    // UI helpers
    // -----------------------------------------------------------------------

    private JLabel lbl(String txt, float size) {
        JLabel l = new JLabel(txt);
        l.setFont(vt323.deriveFont(size));
        l.setForeground(fg);
        l.setOpaque(false);
        return l;
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(vt323.deriveFont(22f));
        f.setForeground(fg);
        f.setBackground(bg);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        f.setCaretColor(fg);
        return f;
    }

    private JPanel createButton(String label, Runnable action) {
        JPanel btn = new JPanel(new BorderLayout());
        btn.setOpaque(true);
        btn.setBackground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 2),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = lbl(label, 24f);
        lbl.setForeground(bg);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        btn.add(lbl, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { action.run(); }
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg); lbl.setForeground(fg); btn.repaint();
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(fg); lbl.setForeground(bg); btn.repaint();
            }
        });
        return btn;
    }

    // --- Inner classes mirroring MainMenu for consistent aesthetics ---

    class MainFramePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(fg);
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(4, 4, getWidth() - 8, getHeight() - 8);
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            // Corner brackets
            int b = 15;
            drawBracket(g2, -10, -10, b, true, true);
            drawBracket(g2, getWidth() + 10 - b, -10, b, true, false);
            drawBracket(g2, -10, getHeight() + 10 - b, b, false, true);
            drawBracket(g2, getWidth() + 10 - b, getHeight() + 10 - b, b, false, false);
            g2.dispose();
        }
        private void drawBracket(Graphics2D g, int x, int y, int s, boolean top, boolean left) {
            g.setColor(fg);
            if (top  && left)  { g.fillRect(x, y, s, 2); g.fillRect(x, y, 2, s); }
            else if (top)       { g.fillRect(x, y, s, 2); g.fillRect(x+s-2, y, 2, s); }
            else if (left)      { g.fillRect(x, y+s-2, s, 2); g.fillRect(x, y, 2, s); }
            else                { g.fillRect(x, y+s-2, s, 2); g.fillRect(x+s-2, y, 2, s); }
        }
    }

    class DashedBorder extends javax.swing.border.AbstractBorder {
        private Color color; private int thickness, dashLen;
        DashedBorder(Color c, int t, int d) { color = c; thickness = t; dashLen = d; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            float[] dash = { dashLen };
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawRect(x, y, w - 1, h - 1);
            g2.dispose();
        }
    }
}
