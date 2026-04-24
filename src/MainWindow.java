import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainWindow extends JFrame {

    private final Color background = new Color(239, 243, 241);
    private final Color foreground = new Color(0, 0, 0);
    private Font pixelFont;

    private String p1Name;
    private TankData p1Tank;
    private String p2Name;
    private TankData p2Tank;
    private int p1TankIndex, p2TankIndex;

    private int p1Position;
    private int p2Position;
    private int p1Score = 0;
    private int p2Score = 0;
    private int roundNum = 0;
    private boolean p1Turn = true;

    private JLabel p1ScoreLabel, p2ScoreLabel;
    private JLabel p1PosLabel, p2PosLabel;
    private JLabel statusLabel;
    private JTextField angleField;
    private JTextField posShiftField;
    private AnimationPanel animationPanel;

    public MainWindow(String p1Name, TankData p1Tank, int p1Idx, String p2Name, TankData p2Tank, int p2Idx) {
        this.p1Name = p1Name;
        this.p1Tank = p1Tank;
        this.p1TankIndex = p1Idx;
        this.p2Name = p2Name;
        this.p2Tank = p2Tank;
        this.p2TankIndex = p2Idx;

        loadFont();

        setTitle("BIT-REKT // BATTLE");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        p1Position = (int) (Math.random() * 100);
        p2Position = 100 + (int) (Math.random() * 100);

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
        animationPanel.updateGameState(p1Tank, p1Position, p2Tank, p2Position, roundNum);
        innerFrame.add(animationPanel, BorderLayout.CENTER);

        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);
        bottomSection.add(buildPlayer2Strip());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(foreground);
        footer.setForeground(background);
        footer.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        
        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftFooter.setOpaque(false);
        
        statusLabel = lbl("ACTIVE: MIGGY'S TURN", 14f);
        statusLabel.setForeground(background);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        
        leftFooter.add(statusLabel);
        footer.add(leftFooter, BorderLayout.WEST);

        bottomSection.add(footer);
        innerFrame.add(bottomSection, BorderLayout.SOUTH);

        frame.add(innerFrame, BorderLayout.CENTER);
    }

    private JPanel buildPlayer1Strip() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
        strip.setOpaque(true);
        strip.setBackground(background);
        strip.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, foreground));
        strip.setPreferredSize(new Dimension(1024, 60));
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        JLabel nameLbl = lbl("P1: " + p1Name, 18f);
        nameLbl.setOpaque(true);
        nameLbl.setBackground(foreground);
        nameLbl.setForeground(background);
        nameLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        JLabel tankLbl = lbl(p1Tank.getName(), 12f);
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
        statsPanel.add(buildHorizontalStat("PWR", (int)p1Tank.getOffensivePower()));
        statsPanel.add(buildHorizontalStat("MOB", (int)p1Tank.getMobilityIndex()));
        
        p1ScoreLabel = lbl("0", 22f);
        p1ScoreLabel.setFont(p1ScoreLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("SCORE", p1ScoreLabel));
        
        p1PosLabel = lbl(String.valueOf(p1Position), 22f);
        p1PosLabel.setFont(p1PosLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("POS", p1PosLabel));

        strip.add(Box.createHorizontalGlue());
        strip.add(statsPanel);
        strip.add(Box.createHorizontalGlue());

        return strip;
    }

    private JPanel buildPlayer2Strip() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
        strip.setOpaque(true);
        strip.setBackground(background);
        strip.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, foreground));
        strip.setPreferredSize(new Dimension(1024, 60));
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        JLabel nameLbl = lbl("P2: " + p2Name, 18f);
        nameLbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(foreground, 2),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        JLabel tankLbl = lbl(p2Tank.getName(), 12f);
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
        statsPanel.add(buildHorizontalStat("PWR", (int)p2Tank.getOffensivePower()));
        statsPanel.add(buildHorizontalStat("MOB", (int)p2Tank.getMobilityIndex()));
        
        p2ScoreLabel = lbl("0", 22f);
        p2ScoreLabel.setFont(p2ScoreLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("SCORE", p2ScoreLabel));
        
        p2PosLabel = lbl(String.valueOf(p2Position), 22f);
        p2PosLabel.setFont(p2PosLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("POS", p2PosLabel));

        strip.add(Box.createHorizontalGlue());
        strip.add(statsPanel);
        strip.add(Box.createHorizontalGlue());

        JPanel inputArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        inputArea.setOpaque(true);
        inputArea.setBackground(new Color(245, 245, 245));
        inputArea.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, foreground));
        
        JPanel inputFields = new JPanel();
        inputFields.setLayout(new BoxLayout(inputFields, BoxLayout.Y_AXIS));
        inputFields.setOpaque(false);
        
        JPanel angRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        angRow.setOpaque(false);
        angRow.add(lbl("ANG:", 12f));
        angleField = styledField(4);
        angRow.add(angleField);
        
        JPanel sftRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        sftRow.setOpaque(false);
        sftRow.add(lbl("SFT:", 12f));
        posShiftField = styledField(4);
        sftRow.add(posShiftField);
        
        inputFields.add(angRow);
        inputFields.add(Box.createVerticalStrut(4));
        inputFields.add(sftRow);
        
        inputArea.add(inputFields);
        inputArea.add(createButton("FIRE", () -> handleFire()));

        strip.add(inputArea);

        return strip;
    }

    private JPanel buildHorizontalStat(String label, int value) {
        JLabel valLbl = lbl(String.valueOf(value), 22f);
        valLbl.setFont(valLbl.getFont().deriveFont(Font.BOLD));
        return buildHorizontalStatWithLabel(label, valLbl);
    }
    
    private JPanel buildHorizontalStatWithLabel(String label, JLabel valueLabel) {
        JPanel statBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statBox.setOpaque(false);
        JLabel lbl = lbl(label, 12f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, foreground));
        statBox.add(lbl);
        statBox.add(valueLabel);
        return statBox;
    }

    private void refreshTurnUI() {
        roundNum++;
        String activePlayerName = p1Turn ? p1Name : p2Name;
        statusLabel.setText("ACTIVE: " + activePlayerName + "'s TURN");
        p1PosLabel.setText(String.valueOf(p1Position));
        p2PosLabel.setText(String.valueOf(p2Position));
        animationPanel.updateGameState(p1Tank, p1Position, p2Tank, p2Position, roundNum);
        angleField.setText("");
        posShiftField.setText("");
        angleField.requestFocusInWindow();
    }

    private void handleFire() {
        String activePlayerName = p1Turn ? p1Name : p2Name;
        TankData tank = p1Turn ? p1Tank : p2Tank;
        int targetPos = p1Turn ? p2Position : p1Position;

        double angle;
        try {
            angle = Double.parseDouble(angleField.getText().trim());
            if (angle < 0 || angle > 180) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            flash("INVALID ANGLE — must be 0–180");
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
                flash("INVALID SHIFT — must be a whole number");
                return;
            }
        }

        if (p1Turn) p1Position = Math.max(0, Math.min(200, p1Position + positionShift));
        else        p2Position = Math.max(0, Math.min(200, p2Position + positionShift));

        final double GRAVITY = 9.81;
        double pwr = tank.getOffensivePower();
        double rad = Math.toRadians(angle);
        double tof = (2 * pwr * Math.sin(rad)) / GRAVITY;
        double startX = p1Turn ? p1Position : p2Position;
        double landX = startX + pwr * Math.cos(rad) * tof;

        double dist = Math.abs(landX - targetPos);
        boolean hit = dist < 1.0;

        animationPanel.updateGameState(p1Tank, p1Position, p2Tank, p2Position, roundNum);
        animationPanel.setLastShot(startX, landX, hit, p1Turn);

        if (hit) {
            if (p1Turn) { p1Score++; p1ScoreLabel.setText(String.valueOf(p1Score)); }
            else { p2Score++; p2ScoreLabel.setText(String.valueOf(p2Score)); }
            Main.gamesPlayed++;
            int gameNum = DataManager.getNextGameNumber();
            MatchRecord record = new MatchRecord(gameNum, p1Name, p1TankIndex, p1Score, p2Name, p2TankIndex, p2Score);
            DataManager.appendMatchRecord(record);
            LeaderboardPanel.sessionHistory.add(record);

            int choice = JOptionPane.showOptionDialog(this,
                    String.format("%s lands at %.1f — DIRECT HIT!\n\nScore: %s %d  |  %s %d\n\nPlay another round?",
                            activePlayerName, landX, p1Name, p1Score, p2Name, p2Score),
                    "HIT!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[]{"BATTLE AGAIN", "MAIN MENU"}, "BATTLE AGAIN");

            if (choice == JOptionPane.YES_OPTION) {
                p1Position = (int) (Math.random() * 100);
                p2Position = 100 + (int) (Math.random() * 100);
                roundNum = 0;
                animationPanel.clearShot();
                refreshTurnUI();
            } else { goToMainMenu(); }
        } else {
            p1Turn = !p1Turn;
            refreshTurnUI();
            statusLabel.setText("⚠ " + String.format("%s: landed %.1f", activePlayerName, landX) + " | " + statusLabel.getText());
        }
    }

    private void flash(String msg) { statusLabel.setText("⚠ " + msg); }

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

    private JPanel createButton(String label, Runnable action) {
        JPanel bp = new JPanel(new BorderLayout());
        bp.setOpaque(true);
        bp.setBackground(foreground);
        bp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 2), BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        bp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel bl = lbl(label, 24f);
        bl.setForeground(background);
        bl.setHorizontalAlignment(SwingConstants.CENTER);
        bp.add(bl, BorderLayout.CENTER);
        bp.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { action.run(); }
            public void mouseEntered(MouseEvent e) { bp.setBackground(background); bl.setForeground(foreground); }
            public void mouseExited(MouseEvent e) { bp.setBackground(foreground); bl.setForeground(background); }
        });
        return bp;
    }
}
