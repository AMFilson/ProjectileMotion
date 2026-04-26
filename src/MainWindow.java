/*
 * Name:    MainWindow.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 25th 2026
 * Desc:    The main game battle window handling input, state, and rendering.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/* 
 * =========================================================================
 * LEARNING: EVENT LISTENERS & EXCEPTION HANDLING
 * =========================================================================
 * 
 * Event Listeners:
 * Graphical User Interfaces (GUIs) are "event-driven". This means the program
 * sits idle until the user does something (clicks a button, types a key, etc.).
 * In MainWindow, we use classes like MouseAdapter and ActionListener to "listen"
 * for these events. When a user clicks the "FIRE" button, an event is triggered,
 * and the specific code inside the listener is executed.
 * 
 * Exception Handling (try-catch):
 * User input is unpredictable. When we read the text from the angle/power
 * text fields, it's just a String. We need a number (double or int). 
 * If the user types "hello" instead of "45", Double.parseDouble() will 
 * crash the program by throwing a NumberFormatException.
 * 
 * We use a try-catch block to gracefully "catch" this error and show the 
 * user a warning instead of crashing the game!
 * =========================================================================
 */

public class MainWindow extends JFrame {

    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 750;
    private static final double GRAVITY = 9.81;
    private static final double HIT_RADIUS = 1.0;
    private static final int MAX_BATTLEFIELD_WIDTH = 200;
    private static final int P1_START_POS = 0;
    private static final int P2_START_POS = 100;

    private final Color background = UIComponents.THEME_BACKGROUND;
    private final Color foreground = UIComponents.THEME_FOREGROUND;
    private Font pixelFont;

    private Player playerOne;
    private Player playerTwo;
    private int roundNum = 0;
    private boolean isPlayerOneTurn = true;
    private String statusText = "";

    private JLabel p1ScoreLabel, p2ScoreLabel;
    private JLabel p1PosLabel, p2PosLabel;
    private JLabel errorLabel;
    private JTextField angleField;
    private JTextField posShiftField;
    private AnimationPanel animationPanel;

    public MainWindow(String p1Name, TankData p1Tank, int p1Idx, String p2Name, TankData p2Tank, int p2Idx) {
        playerOne = new Player(p1Name);
        playerOne.setTank(p1Tank);
        playerOne.setSelectedTankIndex(p1Idx);

        playerTwo = new Player(p2Name);
        playerTwo.setTank(p2Tank);
        playerTwo.setSelectedTankIndex(p2Idx);

        loadFont();

        setTitle("BIT-REKT | BATTLE");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        playerOne.randomizePosition(P1_START_POS);
        playerTwo.randomizePosition(P2_START_POS);

        buildUI();
        refreshTurnUI();
    }

    public MainWindow() {
        this("PLAYER_1", new M8Greyhound(63.5, 88.2), 0,
                "PLAYER_2", new Flak88(78.0, 41.5), 1);
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
        if (pixelFont != null)
            animationPanel.setFont(pixelFont);
        animationPanel.updateGameState(playerOne.getTank(), playerOne.getPosition(), playerTwo.getTank(),
                playerTwo.getPosition(), roundNum, statusText);
        innerFrame.add(animationPanel, BorderLayout.CENTER);

        innerFrame.add(buildPlayer2Strip(), BorderLayout.SOUTH);
        frame.add(innerFrame, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        JLabel endBtn = createLabel("[ END GAME ]", 14f);
        endBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        endBtn.setFocusable(true);
        endBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                confirmEndGame();
            }

            public void mouseEntered(MouseEvent e) {
                endBtn.setForeground(UIComponents.THEME_ERROR);
            }

            public void mouseExited(MouseEvent e) {
                if (!endBtn.hasFocus()) {
                    endBtn.setForeground(foreground);
                }
            }
        });
        endBtn.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                endBtn.setForeground(UIComponents.THEME_ERROR);
            }

            public void focusLost(FocusEvent e) {
                endBtn.setForeground(foreground);
            }
        });
        endBtn.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        endBtn.getActionMap().put("onEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                confirmEndGame();
            }
        });
        endBtn.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "onSpace");
        endBtn.getActionMap().put("onSpace", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                confirmEndGame();
            }
        });
        footer.add(endBtn);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildPlayer1Strip() {
        JPanel strip = new JPanel();
        strip.setLayout(new GridBagLayout());
        strip.setOpaque(true);
        strip.setBackground(background);
        strip.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, foreground));
        strip.setPreferredSize(new Dimension(1024, 110));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel nameLbl = createLabel("Player 1: " + playerOne.getName(), 27f);
        nameLbl.setOpaque(true);
        nameLbl.setBackground(foreground);
        nameLbl.setForeground(background);
        nameLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel tankLbl = createLabel(playerOne.getTank().getName(), 27f);
        tankLbl.setOpaque(true);
        tankLbl.setBackground(foreground);
        tankLbl.setForeground(background);
        tankLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        infoPanel.add(nameLbl);
        infoPanel.add(Box.createHorizontalStrut(8));
        infoPanel.add(tankLbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        strip.add(infoPanel, gbc);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.X_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.add(buildHorizontalStat("PWR", (int) playerOne.getTank().getOffensivePower(),
                "Power affects the strength of your shot"));
        statsPanel.add(Box.createHorizontalStrut(24));
        statsPanel.add(buildHorizontalStat("MOB", (int) playerOne.getTank().getMobilityIndex(),
                "Mobility index affects how many units your tank can move in one turn"));
        statsPanel.add(Box.createHorizontalStrut(24));

        p1ScoreLabel = createLabel(String.valueOf(playerOne.getScore()), 33f);
        p1ScoreLabel.setFont(p1ScoreLabel.getFont().deriveFont(Font.BOLD));
        statsPanel
                .add(buildHorizontalStatWithLabel("SCORE", p1ScoreLabel, "Score is the amount of games you have won"));
        statsPanel.add(Box.createHorizontalStrut(24));

        p1PosLabel = createLabel(String.valueOf(playerOne.getPosition()), 33f);
        p1PosLabel.setFont(p1PosLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("POS", p1PosLabel, "Position is your current position on the map"));

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.VERTICAL;
        strip.add(statsPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.VERTICAL;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        filler.setPreferredSize(infoPanel.getPreferredSize());
        strip.add(filler, gbc);

        return strip;
    }

    private JPanel buildPlayer2Strip() {
        JPanel strip = new JPanel(new GridBagLayout());
        strip.setOpaque(true);
        strip.setBackground(background);
        strip.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, foreground));
        strip.setPreferredSize(new Dimension(1024, 120));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel nameLbl = createLabel("Player 2: " + playerTwo.getName(), 27f);
        nameLbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 2),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));

        JLabel tankLbl = createLabel(playerTwo.getTank().getName(), 27f);
        tankLbl.setOpaque(true);
        tankLbl.setBackground(foreground);
        tankLbl.setForeground(background);
        tankLbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 2),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));

        infoPanel.add(nameLbl);
        infoPanel.add(Box.createHorizontalStrut(8));
        infoPanel.add(tankLbl);

        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.gridx = 0;
        sgbc.gridy = 0;
        sgbc.weightx = 1.0;
        sgbc.weighty = 1.0;
        sgbc.anchor = GridBagConstraints.WEST;
        sgbc.fill = GridBagConstraints.BOTH;
        strip.add(infoPanel, sgbc);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.X_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.add(buildHorizontalStat("PWR", (int) playerTwo.getTank().getOffensivePower(),
                "Power affects the strength of your shot"));
        statsPanel.add(Box.createHorizontalStrut(24));
        statsPanel.add(buildHorizontalStat("MOB", (int) playerTwo.getTank().getMobilityIndex(),
                "Mobility index affects how many units your tank can move in one turn"));
        statsPanel.add(Box.createHorizontalStrut(24));

        p2ScoreLabel = createLabel(String.valueOf(playerTwo.getScore()), 33f);
        p2ScoreLabel.setFont(p2ScoreLabel.getFont().deriveFont(Font.BOLD));
        statsPanel
                .add(buildHorizontalStatWithLabel("SCORE", p2ScoreLabel, "Score is the amount of games you have won"));
        statsPanel.add(Box.createHorizontalStrut(24));

        p2PosLabel = createLabel(String.valueOf(playerTwo.getPosition()), 33f);
        p2PosLabel.setFont(p2PosLabel.getFont().deriveFont(Font.BOLD));
        statsPanel.add(buildHorizontalStatWithLabel("POS", p2PosLabel, "Position is your current position on the map"));

        sgbc.gridx = 1;
        sgbc.gridy = 0;
        sgbc.weightx = 0.0;
        sgbc.weighty = 1.0;
        sgbc.anchor = GridBagConstraints.CENTER;
        sgbc.fill = GridBagConstraints.VERTICAL;
        strip.add(statsPanel, sgbc);

        JPanel inputArea = new JPanel();
        inputArea.setLayout(new BoxLayout(inputArea, BoxLayout.X_AXIS));
        inputArea.setOpaque(true);
        inputArea.setBackground(UIComponents.THEME_PANEL_BG);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, foreground),
                BorderFactory.createEmptyBorder(0, 16, 0, 24)));

        JPanel inputFields = new JPanel();
        inputFields.setLayout(new BoxLayout(inputFields, BoxLayout.Y_AXIS));
        inputFields.setOpaque(false);

        JPanel angleInputRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        angleInputRow.setOpaque(false);
        angleInputRow.add(createLabel("SHOT ANGLE:", 12f));
        angleField = styledField(4);
        angleField.setToolTipText("Enter launch angle (0-180 degrees)");
        angleInputRow.add(angleField);

        JPanel shiftInputRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        shiftInputRow.setOpaque(false);
        shiftInputRow.add(createLabel("ADJUST POSITION:", 12f));
        posShiftField = styledField(4);
        posShiftField.setToolTipText("Enter units to move (forward is positive, backward is negative)");
        shiftInputRow.add(posShiftField);

        inputFields.add(angleInputRow);
        inputFields.add(Box.createVerticalStrut(4));
        inputFields.add(shiftInputRow);

        inputArea.add(inputFields);
        inputArea.add(Box.createHorizontalStrut(16));
        JPanel fireBtn = createButton("FIRE", () -> handleFire(), "Fire the projectile");
        fireBtn.setMaximumSize(fireBtn.getPreferredSize());
        inputArea.add(fireBtn);

        sgbc.gridx = 2;
        sgbc.gridy = 0;
        sgbc.gridheight = 2;
        sgbc.weightx = 1.0;
        sgbc.weighty = 1.0;
        sgbc.anchor = GridBagConstraints.EAST;
        sgbc.fill = GridBagConstraints.VERTICAL;
        strip.add(inputArea, sgbc);

        errorLabel = createLabel("", 14f);
        errorLabel.setForeground(UIComponents.THEME_ERROR);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 16));
        sgbc.gridx = 0;
        sgbc.gridy = 1;
        sgbc.gridheight = 1;
        sgbc.gridwidth = 2;
        sgbc.weightx = 1.0;
        sgbc.weighty = 0.0;
        sgbc.anchor = GridBagConstraints.WEST;
        sgbc.fill = GridBagConstraints.HORIZONTAL;
        strip.add(errorLabel, sgbc);

        return strip;
    }

    private JPanel buildHorizontalStat(String label, int value, String tooltip) {
        JLabel valLbl = createLabel(String.valueOf(value), 33f);
        valLbl.setFont(valLbl.getFont().deriveFont(Font.BOLD));
        return buildHorizontalStatWithLabel(label, valLbl, tooltip);
    }

    private JPanel buildHorizontalStatWithLabel(String label, JLabel valueLabel, String tooltip) {
        JPanel statBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statBox.setOpaque(false);
        JLabel lbl = createLabel(label, 18f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, foreground));
        statBox.add(lbl);
        statBox.add(valueLabel);
        statBox.setToolTipText(tooltip);
        return statBox;
    }

    private void refreshTurnUI() {
        if (isPlayerOneTurn)
            roundNum++;
        String activePlayerName = isPlayerOneTurn ? playerOne.getName() : playerTwo.getName();
        statusText = "ACTIVE: " + activePlayerName + "'s TURN";
        p1PosLabel.setText(String.valueOf(playerOne.getPosition()));
        p2PosLabel.setText(String.valueOf(playerTwo.getPosition()));
        animationPanel.updateGameState(playerOne.getTank(), playerOne.getPosition(), playerTwo.getTank(),
                playerTwo.getPosition(), roundNum, statusText);
        errorLabel.setText("");
        angleField.setText("");
        posShiftField.setText("");
        angleField.requestFocusInWindow();
    }

    private void handleFire() {
        Player activePlayer = isPlayerOneTurn ? playerOne : playerTwo;
        Player targetPlayer = isPlayerOneTurn ? playerTwo : playerOne;
        String activePlayerName = activePlayer.getName();
        TankData tank = activePlayer.getTank();
        int targetPos = targetPlayer.getPosition();

        errorLabel.setText("");

        double angle;
        try {
            angle = Double.parseDouble(angleField.getText().trim());
            if (angle < 0 || angle > 180)
                throw new NumberFormatException();
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

        activePlayer.setPosition(Math.max(0, Math.min(MAX_BATTLEFIELD_WIDTH,
                activePlayer.getPosition() + (isPlayerOneTurn ? positionShift : -positionShift))));

        double power = tank.getOffensivePower();
        double angleInRadians = Math.toRadians(angle);
        double timeInAir = (2 * power * Math.sin(angleInRadians)) / GRAVITY;
        double startX = activePlayer.getPosition();
        double landX = startX + (isPlayerOneTurn ? 1 : -1) * power * Math.cos(angleInRadians) * timeInAir;

        double impactDistance = Math.abs(landX - targetPos);
        boolean hit = impactDistance < HIT_RADIUS;

        animationPanel.updateGameState(playerOne.getTank(), playerOne.getPosition(), playerTwo.getTank(),
                playerTwo.getPosition(), roundNum, statusText);
        animationPanel.setLastShot(startX, landX, hit);

        if (hit) {
            activePlayer.setScore(activePlayer.getScore() + 1);
            if (isPlayerOneTurn)
                p1ScoreLabel.setText(String.valueOf(playerOne.getScore()));
            else
                p2ScoreLabel.setText(String.valueOf(playerTwo.getScore()));

            Main.incrementGamesPlayed();
            int gameNum = DataManager.getNextGameNumber();
            MatchRecord record = new MatchRecord(gameNum, playerOne.getName(), playerOne.getSelectedTankIndex(),
                    playerOne.getScore(), playerTwo.getName(), playerTwo.getSelectedTankIndex(), playerTwo.getScore());
            DataManager.appendMatchRecord(record);
            LeaderboardPanel.addMatchRecord(record);

            int choice = JOptionPane.showOptionDialog(this,
                    String.format(
                            "%s lands at %.1f \u2014 DIRECT HIT!\n\nScore: %s %d  |  %s %d\n\nPlay another round?",
                            activePlayerName, landX, playerOne.getName(), playerOne.getScore(), playerTwo.getName(),
                            playerTwo.getScore()),
                    "HIT!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[] { "BATTLE AGAIN", "MAIN MENU" }, "BATTLE AGAIN");

            if (choice == JOptionPane.YES_OPTION) {
                playerOne.randomizePosition(0);
                playerTwo.randomizePosition(100);
                roundNum = 0;
                animationPanel.clearShot();
                refreshTurnUI();
            } else {
                goToMainMenu();
            }
        } else {
            isPlayerOneTurn = !isPlayerOneTurn;
            refreshTurnUI();
            statusText = "\u26a0 " + String.format("%s: landed %.1f", activePlayerName, landX) + " | " + statusText;
            animationPanel.updateGameState(playerOne.getTank(), playerOne.getPosition(), playerTwo.getTank(),
                    playerTwo.getPosition(), roundNum, statusText);
        }
    }

    private void flash(String msg) {
        errorLabel.setText("\u26a0 " + msg);
    }

    private void confirmEndGame() {
        String[] options = { "Yes", "No" };
        int choice = UIComponents.showThemedDialog(this,
                "Are you sure you want to end the current game?",
                "End Game",
                options,
                "!",
                pixelFont);
        if (choice == 0) {
            goToMainMenu();
        }
    }

    private void goToMainMenu() {
        dispose();
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    private JLabel createLabel(String text, float size) {
        return UIComponents.createLabel(text, pixelFont, size);
    }

    private JTextField styledField(int cols) {
        JTextField textField = new JTextField(cols);
        textField.setFont(pixelFont.deriveFont(22f));
        textField.setForeground(foreground);
        textField.setBackground(background);
        textField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        textField.setCaretColor(foreground);
        return textField;
    }

    private JPanel createButton(String label, Runnable action, String tooltip) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(foreground);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(foreground, 2),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        buttonPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.setFocusable(true);
        buttonPanel.setToolTipText(tooltip);

        JLabel buttonLabel = createLabel(label, 24f);
        buttonLabel.setForeground(background);
        buttonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        buttonPanel.add(buttonLabel, BorderLayout.CENTER);

        Runnable onHover = () -> {
            buttonPanel.setBackground(background);
            buttonLabel.setForeground(foreground);
        };
        Runnable onUnhover = () -> {
            buttonPanel.setBackground(foreground);
            buttonLabel.setForeground(background);
        };

        buttonPanel.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                onHover.run();
            }

            public void focusLost(FocusEvent e) {
                onUnhover.run();
            }
        });

        buttonPanel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        buttonPanel.getActionMap().put("onEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });

        buttonPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                action.run();
            }

            public void mouseEntered(MouseEvent e) {
                onHover.run();
            }

            public void mouseExited(MouseEvent e) {
                if (!buttonPanel.hasFocus())
                    onUnhover.run();
            }
        });
        return buttonPanel;
    }
}
