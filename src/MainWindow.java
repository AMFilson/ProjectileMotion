import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainWindow extends JFrame {

    private final Color background = UIComponents.THEME_BACKGROUND;
    private final Color foreground = UIComponents.THEME_FOREGROUND;
    private Font pixelFont;

    private Player p1;
    private Player p2;
    private int roundNum = 0;
    private boolean p1Turn = true;
    private String statusText = "";

    private JLabel p1ScoreLabel, p2ScoreLabel;
    private JLabel p1PosLabel, p2PosLabel;
    private JLabel errorLabel;
    private JTextField angleField;
    private JTextField posShiftField;
    private AnimationPanel animationPanel;

    public MainWindow(String p1Name, TankData p1Tank, int p1Idx, String p2Name, TankData p2Tank, int p2Idx) {
        p1 = new Player(p1Name);
        p1.setTank(p1Tank);
        p1.setSelectedTankIndex(p1Idx);

        p2 = new Player(p2Name);
        p2.setTank(p2Tank);
        p2.setSelectedTankIndex(p2Idx);

        loadFont();

        setTitle("BIT-REKT // BATTLE");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        p1.randomizePosition(0);
        p2.randomizePosition(100);

        buildUI();
        refreshTurnUI();
    }

    public MainWindow() {
        this("PLAYER_1", new TankData("M8 GREYHOUND", 63.5, 88.2), 0,
             "PLAYER_2", new TankData("FLAK 88", 78.0, 41.5), 1);
    }

    private void loadFont() {
        try {
            File fontFile = new File("src/fonts/VT323-Regular.ttf");
            pixelFont = fontFile.exists()
                    ? Font.createFont(Font.TRUETYPE_FONT, fontFile)
                    : new Font("Monospaced", Font.PLAIN, 16);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pixelFont);
            
            UIManager.put("ToolTip.font", pixelFont.deriveFont(18f));
            UIManager.put("ToolTip.background", background);
            UIManager.put("ToolTip.foreground", foreground);
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(foreground, 2));
        } catch (Exception e) {
            pixelFont = new Font("Monospaced", Font.PLAIN, 16);
        }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(background);
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(root);

        MainFramePanel frame = new MainFramePanel();
        frame.setLayout(new BorderLayout(0, 0));
        root.add(frame, BorderLayout.CENTER);

        JPanel innerFrame = new JPanel();
        innerFrame.setLayout(new BorderLayout(0, 0));
        innerFrame.setOpaque(false);
        innerFrame.setBorder(BorderFactory.createLineBorder(foreground, 2));

        innerFrame.add(buildPlayer1Strip(), BorderLayout.NORTH);

        animationPanel = new AnimationPanel();
        if (pixelFont != null) animationPanel.setFont(pixelFont);
        animationPanel.updateGameState(p1.getTank(), p1.getPosition(), p2.getTank(), p2.getPosition(), roundNum, statusText);
        innerFrame.add(animationPanel, BorderLayout.CENTER);

        innerFrame.add(buildPlayer2Strip(), BorderLayout.SOUTH);
        frame.add(innerFrame, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        JLabel endBtn = lbl("[ END GAME ]", 14f);
        endBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        endBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { goToMainMenu(); }
            public void mouseEntered(MouseEvent e) { endBtn.setForeground(new Color(100, 100, 100)); }
            public void mouseExited(MouseEvent e) { endBtn.setForeground(foreground); }
        });
        footer.add(endBtn);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildPlayer1Strip() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
        strip.setOpaque(true);
        strip.setBackground(background);
        strip.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, foreground));
        strip.setPreferredSize(new Dimension(1024, 110));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        JLabel nameLbl = lbl("P1: " + p1.getName(), 18f);
        nameLbl.setOpaque(true);
        nameLbl.setBackground(foreground);
        nameLbl.setForeground(background);
        nameLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        JLabel tankLbl = lbl(p1.getTank().getName(), 12f);
        tankLbl.setOpaque(true);
        tankLbl.setBackground(foreground);
        tankLbl.setForeground(background);
        tankLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        infoPanel.add(nameLbl);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(tankLbl);
        strip.add(infoPanel);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 8));
        statsPanel.setOpaque(false);
        statsPanel.add(buildHorizontalStat("PWR", (int)p1.getTank().getOffensivePower(), "Power affects the strength of your shot"));
        statsPanel.add(buildHorizontalStat("MOB", (int)p1.getTank().getMobilityIndex(), "Mobility index affects how many units your tank can move in one turn"));
        
        p1ScoreLabel = lbl(String.valueOf(p1.getScore()), 33f);
        p1ScoreLabel.setFont(p1ScoreLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("SCORE", p1ScoreLabel, "Score is the amount of games you have won"));
        
        p1PosLabel = lbl(String.valueOf(p1.getPosition()), 33f);
        p1PosLabel.setFont(p1PosLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("POS", p1PosLabel, "Position is your current position on the map"));

        strip.add(Box.createHorizontalGlue());
        strip.add(statsPanel);
        strip.add(Box.createHorizontalGlue());

        return strip;
    }

    private JPanel buildPlayer2Strip() {
        JPanel strip = new JPanel(new BorderLayout());
        strip.setOpaque(true);
        strip.setBackground(background);
        strip.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, foreground));
        strip.setPreferredSize(new Dimension(1024, 120));

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.X_AXIS));
        mainContent.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        JLabel nameLbl = lbl("P2: " + p2.getName(), 18f);
        nameLbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 2), BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        
        JLabel tankLbl = lbl(p2.getTank().getName(), 12f);
        tankLbl.setOpaque(true);
        tankLbl.setBackground(foreground);
        tankLbl.setForeground(background);
        tankLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        infoPanel.add(nameLbl);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(tankLbl);
        mainContent.add(infoPanel);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 8));
        statsPanel.setOpaque(false);
        statsPanel.add(buildHorizontalStat("PWR", (int)p2.getTank().getOffensivePower(), "Power affects the strength of your shot"));
        statsPanel.add(buildHorizontalStat("MOB", (int)p2.getTank().getMobilityIndex(), "Mobility index affects how many units your tank can move in one turn"));
        
        p2ScoreLabel = lbl(String.valueOf(p2.getScore()), 33f);
        p2ScoreLabel.setFont(p2ScoreLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("SCORE", p2ScoreLabel, "Score is the amount of games you have won"));
        
        p2PosLabel = lbl(String.valueOf(p2.getPosition()), 33f);
        p2PosLabel.setFont(p2PosLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("POS", p2PosLabel, "Position is your current position on the map"));

        mainContent.add(Box.createHorizontalGlue());
        mainContent.add(statsPanel);
        mainContent.add(Box.createHorizontalGlue());

        JPanel inputArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        inputArea.setOpaque(true);
        inputArea.setBackground(new Color(245, 245, 245));
        inputArea.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, foreground));
        
        JPanel inputFields = new JPanel();
        inputFields.setLayout(new BoxLayout(inputFields, BoxLayout.Y_AXIS));
        inputFields.setOpaque(false);
        
        JPanel angRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        angRow.setOpaque(false);
        angRow.add(lbl("SHOT ANGLE:", 12f));
        angleField = styledField(4);
        angleField.setToolTipText("Enter launch angle (0-180 degrees)");
        angRow.add(angleField);
        
        JPanel sftRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        sftRow.setOpaque(false);
        sftRow.add(lbl("ADJUST POSITION:", 12f));
        posShiftField = styledField(4);
        posShiftField.setToolTipText("Enter units to move (forward is positive, backward is negative)");
        sftRow.add(posShiftField);
        
        inputFields.add(angRow);
        inputFields.add(Box.createVerticalStrut(4));
        inputFields.add(sftRow);
        
        inputArea.add(inputFields);
        inputArea.add(createButton("FIRE", () -> handleFire(), "Fire the projectile"));
        mainContent.add(inputArea);

        strip.add(mainContent, BorderLayout.CENTER);
        
        errorLabel = lbl("", 14f);
        errorLabel.setForeground(new Color(200, 0, 0));
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 16));
        strip.add(errorLabel, BorderLayout.SOUTH);

        return strip;
    }

    private JPanel buildHorizontalStat(String label, int value, String tooltip) {
        JLabel valLbl = lbl(String.valueOf(value), 33f);
        valLbl.setFont(valLbl.getFont().deriveFont(Font.BOLD));
        return buildHorizontalStatWithLabel(label, valLbl, tooltip);
    }
    
    private JPanel buildHorizontalStatWithLabel(String label, JLabel valueLabel, String tooltip) {
        JPanel statBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statBox.setOpaque(false);
        JLabel lbl = lbl(label, 18f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, foreground));
        statBox.add(lbl);
        statBox.add(valueLabel);
        statBox.setToolTipText(tooltip);
        return statBox;
    }

    private void refreshTurnUI() {
        roundNum++;
        String activePlayerName = p1Turn ? p1.getName() : p2.getName();
        statusText = "ACTIVE: " + activePlayerName + "'s TURN";
        p1PosLabel.setText(String.valueOf(p1.getPosition()));
        p2PosLabel.setText(String.valueOf(p2.getPosition()));
        animationPanel.updateGameState(p1.getTank(), p1.getPosition(), p2.getTank(), p2.getPosition(), roundNum, statusText);
        errorLabel.setText("");
        angleField.setText("");
        posShiftField.setText("");
        angleField.requestFocusInWindow();
    }

    private void handleFire() {
        Player activePlayer = p1Turn ? p1 : p2;
        Player targetPlayer = p1Turn ? p2 : p1;
        String activePlayerName = activePlayer.getName();
        TankData tank = activePlayer.getTank();
        int targetPos = targetPlayer.getPosition();

        errorLabel.setText("");

        double angle;
        try {
            angle = Double.parseDouble(angleField.getText().trim());
            if (angle < 0 || angle > 180) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            flash("INVALID ANGLE \u2014 must be 0\u2013180");
            return;
        }

        int positionShift = 0;
        String psText = posShiftField.getText().trim();
        if (!psText.isEmpty()) {
            try {
                positionShift = Integer.parseInt(psText);
                int maxS = (int) tank.getMobilityIndex();
                if (Math.abs(positionShift) > maxS) {
                    flash("POSITION SHIFT exceeds MOB. INDEX cap of " + maxS);
                    return;
                }
            } catch (NumberFormatException ex) {
                flash("INVALID SHIFT \u2014 must be a whole number");
                return;
            }
        }

        activePlayer.setPosition(Math.max(0, Math.min(200, activePlayer.getPosition() + (p1Turn ? positionShift : -positionShift))));

        final double GRAVITY = 9.81;
        double pwr = tank.getOffensivePower();
        double rad = Math.toRadians(angle);
        double tof = (2 * pwr * Math.sin(rad)) / GRAVITY;
        double startX = activePlayer.getPosition();
        double landX = startX + (p1Turn ? 1 : -1) * pwr * Math.cos(rad) * tof;

        double dist = Math.abs(landX - targetPos);
        boolean hit = dist < 1.0;

        animationPanel.updateGameState(p1.getTank(), p1.getPosition(), p2.getTank(), p2.getPosition(), roundNum, statusText);
        animationPanel.setLastShot(startX, landX, hit);

        if (hit) {
            activePlayer.setScore(activePlayer.getScore() + 1);
            if (p1Turn) p1ScoreLabel.setText(String.valueOf(p1.getScore()));
            else        p2ScoreLabel.setText(String.valueOf(p2.getScore()));
            
            Main.gamesPlayed++;
            int gameNum = DataManager.getNextGameNumber();
            MatchRecord record = new MatchRecord(gameNum, p1.getName(), p1.getSelectedTankIndex(), p1.getScore(), p2.getName(), p2.getSelectedTankIndex(), p2.getScore());
            DataManager.appendMatchRecord(record);
            LeaderboardPanel.sessionHistory.add(record);

            int choice = JOptionPane.showOptionDialog(this,
                    String.format("%s lands at %.1f \u2014 DIRECT HIT!\n\nScore: %s %d  |  %s %d\n\nPlay another round?",
                            activePlayerName, landX, p1.getName(), p1.getScore(), p2.getName(), p2.getScore()),
                    "HIT!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[]{"BATTLE AGAIN", "MAIN MENU"}, "BATTLE AGAIN");

            if (choice == JOptionPane.YES_OPTION) {
                p1.randomizePosition(0);
                p2.randomizePosition(100);
                roundNum = 0;
                animationPanel.clearShot();
                refreshTurnUI();
            } else { goToMainMenu(); }
        } else {
            p1Turn = !p1Turn;
            refreshTurnUI();
            statusText = "\u26a0 " + String.format("%s: landed %.1f", activePlayerName, landX) + " | " + statusText;
            animationPanel.updateGameState(p1.getTank(), p1.getPosition(), p2.getTank(), p2.getPosition(), roundNum, statusText);
        }
    }

    private void flash(String msg) { 
        errorLabel.setText("\u26a0 " + msg); 
    }

    private void goToMainMenu() {
        dispose();
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    private JLabel lbl(String text, float size) {
        JLabel label = new JLabel(text);
        label.setFont(pixelFont.deriveFont(size));
        label.setForeground(foreground);
        return label;
    }

    private JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(pixelFont.deriveFont(22f));
        tf.setForeground(foreground);
        tf.setBackground(background);
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 1), BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        tf.setCaretColor(foreground);
        return tf;
    }

    private JPanel createButton(String label, Runnable action, String tooltip) {
        JPanel bp = new JPanel(new BorderLayout());
        bp.setOpaque(true);
        bp.setBackground(foreground);
        bp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 2), BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        bp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bp.setFocusable(true);
        bp.setToolTipText(tooltip);

        JLabel bl = lbl(label, 24f);
        bl.setForeground(background);
        bl.setHorizontalAlignment(SwingConstants.CENTER);
        bp.add(bl, BorderLayout.CENTER);

        Runnable onHover = () -> { bp.setBackground(background); bl.setForeground(foreground); };
        Runnable onUnhover = () -> { bp.setBackground(foreground); bl.setForeground(background); };

        bp.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { onHover.run(); }
            public void focusLost(FocusEvent e) { onUnhover.run(); }
        });

        bp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        bp.getActionMap().put("onEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { action.run(); }
        });

        bp.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { action.run(); }
            public void mouseEntered(MouseEvent e) { onHover.run(); }
            public void mouseExited(MouseEvent e) { if (!bp.hasFocus()) onUnhover.run(); }
        });
        return bp;
    }
}
