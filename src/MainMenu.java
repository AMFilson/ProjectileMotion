/* 
 * Name:    MainMenu.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 24th 2026
 * Desc:    The main navigation hub for BIT-REKT. 
 *          Manages the outer shell and screen transitions (Home, Leaderboard, New Game).
 */

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MainMenu.java
 *
 * The entry-point JFrame for the BIT-REKT application.
 * Manages the outer shell (title bar, sidebar navigation, header, footer)
 * and a CardLayout that switches between three screens:
 * - HOME : CanvasArea tank preview + stats sidebar
 * - LEADERBOARD : LeaderboardPanel
 * - NEW_GAME : CharacterSelectPanel
 *
 * All inner panel classes have been extracted to their own files:
 * UIComponents.java -> DashedBorder, DitheredBar, MainFramePanel
 * CanvasArea.java -> Home-screen tank preview canvas
 * LeaderboardPanel.java -> Leaderboard card
 * CharacterSelectPanel.java -> NEW GAME card + PlayerColumn
 */
public class MainMenu extends JFrame {

    private Font pixelFont;

    // Shared tank roster — passed by reference to CanvasArea and
    // CharacterSelectPanel
    private java.util.List<TankData> tanks = new java.util.ArrayList<>();
    private int currentTankIndex = 0;

    // Home-screen components updated by the arrow buttons in updateInfoPanel()
    private JPanel infoPanel;
    private CanvasArea canvas;
    private int flickerStep = 0;

    // CardLayout controls which screen is currently shown
    private CardLayout cardLayout;
    private JPanel cardContentPanel;

    // Nav item helpers — allow resetting all highlights when changing cards
    private java.util.List<JPanel> navItemsList = new java.util.ArrayList<>();
    private java.util.List<Runnable> navHighlightResetters = new java.util.ArrayList<>();

    // Timer fields for lifecycle management
    private Timer blinkTimer;
    private Timer timeTaker;
    private Timer flickerAct;

    // Card Panel fields to prevent duplicate instantiation
    private JPanel homePanel;
    private LeaderboardPanel leaderboardPanel;
    private CharacterSelectPanel characterSelectPanel;
    private HowToPlayPanel howToPlayPanel;

    public MainMenu() {
        setTitle("BIT-REKT");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Populate the shared tank roster
        tanks.add(new TankData("M8 GREYHOUND"));
        tanks.add(new TankData("FLAK 88"));
        tanks.add(new TankData("BLACK CAT"));

        // Global repaint timer — drives the blinking/flicker animations
        blinkTimer = new Timer(600, e -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof MainMenu)
                    w.repaint();
            }
        });
        blinkTimer.start();

        // Load the VT323 pixel font (fallback: system Monospaced)
        try {
            File fontFile = new File("src/fonts/VT323-Regular.ttf");
            if (fontFile.exists()) {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pixelFont);
            } else {
                pixelFont = new Font("Monospaced", Font.PLAIN, 16);
            }
        } catch (Exception e) {
            pixelFont = new Font("Monospaced", Font.PLAIN, 16);
        }

        // Global Tooltip Style (matches uicomponents aesthetic)
        UIManager.put("ToolTip.font", pixelFont.deriveFont(18f));
        UIManager.put("ToolTip.background", UIComponents.THEME_BACKGROUND);
        UIManager.put("ToolTip.foreground", UIComponents.THEME_FOREGROUND);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 2));

        // Custom crosshair cursor
        BufferedImage cursorImg = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = cursorImg.createGraphics();
        cg.setColor(Color.BLACK);
        cg.fillRect(9, 0, 2, 20);
        cg.fillRect(0, 9, 20, 2);
        cg.dispose();
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(10, 10), "crosshair"));

        // Root panel with dot-matrix scanline background
        JPanel rootPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.setColor(UIComponents.THEME_BACKGROUND);
                graphics.fillRect(0, 0, getWidth(), getHeight());
                Graphics2D graphics2d = (Graphics2D) graphics.create();
                graphics2d.setColor(UIComponents.THEME_FOREGROUND);
                graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
                for (int y = 0; y < getHeight(); y += 4)
                    for (int x = 0; x < getWidth(); x += 4)
                        graphics2d.fillRect(x, y, 1, 1);
                graphics2d.dispose();
            }
        };

        // Outer decorative frame (shared component from UIComponents.java)
        MainFramePanel mainFrame = new MainFramePanel();
        mainFrame.setPreferredSize(new Dimension(900, 600));
        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.fill = GridBagConstraints.BOTH;
        layoutConstraints.insets = new Insets(6, 6, 6, 6);

        // =====================================================================
        // HEADER
        // =====================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UIComponents.THEME_FOREGROUND),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(createLabel("HEAVY ARMORED DIVISION", 12f));
        titleBlock.add(Box.createVerticalStrut(-5));
        titleBlock.add(createLabel("BIT-REKT", 48f));

        JLabel systemStatus = createLabel(
                "<html><p align='right' style='line-height:0.8'>LOCATION: CAMP 30<br>TIME: 19:04:25</p></html>", 16f);
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(systemStatus, BorderLayout.EAST);

        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.gridwidth = 3;
        layoutConstraints.weightx = 1.0;
        layoutConstraints.weighty = 0.0;
        mainFrame.add(headerPanel, layoutConstraints);

        // =====================================================================
        // LEFT SIDEBAR NAV
        // =====================================================================
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UIComponents.THEME_FOREGROUND),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        String[] navNames = { "NEW GAME", "HOW TO PLAY", "LEADERBOARD", "SAVE/LOAD", "TERMINATE" };
        String[] navIds = { "01", "02", "03", "04", "05" };

        for (int i = 0; i < navNames.length; i++) {
            JPanel navItem = createNavItem(navNames[i], navIds[i]);
            navItemsList.add(navItem);
            if (i == 0) {
                // Auto-focus "NEW GAME" on launch so keyboard navigation works immediately
                SwingUtilities.invokeLater(() -> navItem.requestFocusInWindow());
            }
            sidebar.add(navItem);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());
        JPanel homeBtn = createNavItem("HOME", "06");
        homeBtn.setToolTipText("Return to the main command center");
        navItemsList.add(homeBtn);
        sidebar.add(homeBtn);

        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        layoutConstraints.gridwidth = 1;
        layoutConstraints.weightx = 0.0;
        layoutConstraints.weighty = 1.0;
        sidebar.setPreferredSize(new Dimension(220, 0));
        mainFrame.add(sidebar, layoutConstraints);

        // =====================================================================
        // HOME PANEL (canvas + info sidebar) — first card in CardLayout
        // =====================================================================
        canvas = new CanvasArea(tanks, () -> currentTankIndex, pixelFont);
        canvas.setOpaque(false);
        canvas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                new DashedBorder(UIComponents.THEME_FOREGROUND, 1, 4)));

        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, UIComponents.THEME_FOREGROUND),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        updateInfoPanel();
        infoPanel.setPreferredSize(new Dimension(185, 0));

        homePanel = new JPanel(new GridBagLayout());
        homePanel.setOpaque(false);
        GridBagConstraints homeLayoutConstraints = new GridBagConstraints();
        homeLayoutConstraints.fill = GridBagConstraints.BOTH;
        homeLayoutConstraints.gridx = 0;
        homeLayoutConstraints.gridy = 0;
        homeLayoutConstraints.weightx = 1.0;
        homeLayoutConstraints.weighty = 1.0;
        homePanel.add(canvas, homeLayoutConstraints);
        homeLayoutConstraints.gridx = 1;
        homeLayoutConstraints.weightx = 0.0;
        homePanel.add(infoPanel, homeLayoutConstraints);

        // Pre-instantiate card panels
        leaderboardPanel = new LeaderboardPanel(pixelFont);
        characterSelectPanel = new CharacterSelectPanel(tanks, pixelFont);
        howToPlayPanel = new HowToPlayPanel(pixelFont);

        // =====================================================================
        // CARD LAYOUT (HOME / LEADERBOARD / NEW_GAME)
        // =====================================================================
        cardLayout = new CardLayout();
        cardContentPanel = new JPanel(cardLayout);
        cardContentPanel.setOpaque(false);

        cardContentPanel.add(homePanel, "HOME");
        cardContentPanel.add(leaderboardPanel, "LEADERBOARD");
        cardContentPanel.add(characterSelectPanel, "NEW_GAME");
        cardContentPanel.add(howToPlayPanel, "HOW_TO_PLAY");

        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 1;
        layoutConstraints.gridwidth = 2;
        layoutConstraints.weightx = 1.0;
        layoutConstraints.weighty = 1.0;
        mainFrame.add(cardContentPanel, layoutConstraints);

        // =====================================================================
        // FOOTER
        // =====================================================================
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, UIComponents.THEME_FOREGROUND),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        footerPanel.add(createLabel("CREATED BY ANDREW FILSON", 16f), BorderLayout.WEST);

        JLabel rightFooterLabel = createLabel("PRESS [ENTER] TO INITIALIZE", 16f);
        rightFooterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        footerPanel.add(rightFooterLabel, BorderLayout.EAST);

        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 2;
        layoutConstraints.gridwidth = 3;
        layoutConstraints.weightx = 1.0;
        layoutConstraints.weighty = 0.0;
        mainFrame.add(footerPanel, layoutConstraints);

        rootPanel.add(mainFrame);
        add(rootPanel);

        // Live clock (updates system status label every second)
        timeTaker = new Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            systemStatus.setText("<html><p align='right' style='line-height:0.8'>LOCATION: CAMP 30<br>TIME: "
                    + time + "</p></html>");
        });
        timeTaker.start();

        // Subtle opacity flicker animation on the main frame
        flickerAct = new Timer(100, e -> {
            flickerStep = (flickerStep + 1) % 40;
            float frameOpacity = 1.0f;
            if (flickerStep == 5)
                frameOpacity = 0.95f;
            if (flickerStep == 7)
                frameOpacity = 0.98f;
            mainFrame.setOpacity(frameOpacity);
            mainFrame.repaint();
        });
        flickerAct.start();
    }

    @Override
    public void dispose() {
        if (blinkTimer != null) blinkTimer.stop();
        if (timeTaker != null) timeTaker.stop();
        if (flickerAct != null) flickerAct.stop();
        super.dispose();
    }

    // =========================================================================
    // LABEL FACTORY
    // =========================================================================

    private JLabel createLabel(String text, float fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(pixelFont.deriveFont(fontSize));
        label.setForeground(UIComponents.THEME_FOREGROUND);
        label.setOpaque(false);
        return label;
    }

    // =========================================================================
    // NAV ITEM FACTORY
    // =========================================================================

    private JPanel createNavItem(String title, String num) {
        JPanel panel = new JPanel(new BorderLayout());

        // Tooltip for each nav item
        if (title.equals("NEW GAME"))
            panel.setToolTipText("Start a new game session");
        else if (title.equals("HOW TO PLAY"))
            panel.setToolTipText("View gameplay instructions");
        else if (title.equals("LEADERBOARD"))
            panel.setToolTipText("View global high scores");
        else if (title.equals("SAVE/LOAD"))
            panel.setToolTipText("Save or load your game state");
        else if (title.equals("TERMINATE"))
            panel.setToolTipText("Exit the application");

        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel navTitleLabel = createLabel("□ " + title, 24f);
        JLabel navNumberLabel = createLabel(num, 24f);
        panel.add(navTitleLabel, BorderLayout.WEST);
        panel.add(navNumberLabel, BorderLayout.EAST);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setFocusable(true);

        Runnable onHover = () -> {
            panel.setOpaque(true);
            panel.setBackground(UIComponents.THEME_FOREGROUND);
            navTitleLabel.setForeground(UIComponents.THEME_BACKGROUND);
            navTitleLabel.setText("■ " + title);
            navNumberLabel.setForeground(UIComponents.THEME_BACKGROUND);
            panel.repaint();
        };
        Runnable onUnhover = () -> {
            panel.setOpaque(false);
            navTitleLabel.setForeground(UIComponents.THEME_FOREGROUND);
            navTitleLabel.setText("□ " + title);
            navNumberLabel.setForeground(UIComponents.THEME_FOREGROUND);
            panel.repaint();
        };

        navHighlightResetters.add(onUnhover);

        panel.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                onHover.run();
            }

            public void focusLost(FocusEvent e) {
                onUnhover.run();
            }
        });

        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handleNavClick(title);
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                onHover.run();
            }

            public void mouseExited(MouseEvent e) {
                if (!isNavActive(title))
                    onUnhover.run();
            }

            public void mousePressed(MouseEvent e) {
                handleNavClick(title);
            }
        });

        return panel;
    }

    private boolean isNavActive(String title) {
        return false; // Future: track active card to keep nav item highlighted
    }

    // =========================================================================
    // NAV ROUTING
    // =========================================================================

    private void handleNavClick(String title) {
        for (Runnable resetHighlight : navHighlightResetters)
            resetHighlight.run();

        if (title.equals("NEW GAME"))
            cardLayout.show(cardContentPanel, "NEW_GAME");
        else if (title.equals("HOW TO PLAY"))
            cardLayout.show(cardContentPanel, "HOW_TO_PLAY");
        else if (title.equals("LEADERBOARD")) {
            leaderboardPanel.refreshUI();
            cardLayout.show(cardContentPanel, "LEADERBOARD");
        } else if (title.equals("SAVE/LOAD"))
            handleSaveLoad();
        else if (title.equals("TERMINATE"))
            System.exit(0);
        else
            cardLayout.show(cardContentPanel, "HOME");
    }

    private void handleSaveLoad() {
        Object[] options = { "Save", "Load", "Cancel" };
        int dialogChoice = JOptionPane.showOptionDialog(this,
                "Would you like to Save or Load a game file?",
                "SAVE/LOAD",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[2]);

        if (dialogChoice == JOptionPane.YES_OPTION) {
            java.util.List<MatchRecord> history = LeaderboardPanel.sessionHistory;
            String[] dataToSave;
            if (history.isEmpty()) {
                dataToSave = new String[] { "No match records found." };
            } else {
                dataToSave = new String[history.size()];
                for (int i = 0; i < history.size(); i++) {
                    dataToSave[i] = history.get(i).toCSV();
                }
            }
            DataManager.saveGame(this, dataToSave);
        } else if (dialogChoice == JOptionPane.NO_OPTION) {
            String[] loadedData = DataManager.loadGame(this);
            if (loadedData != null) {
                LeaderboardPanel.sessionHistory.clear();
                for (String line : loadedData) {
                    if (line.equals("No match records found."))
                        continue;
                    String[] parts = line.split(",");
                    if (parts.length == 7) {
                        try {
                            LeaderboardPanel.sessionHistory.add(new MatchRecord(
                                    Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]),
                                    Integer.parseInt(parts[3]),
                                    parts[4], Integer.parseInt(parts[5]), Integer.parseInt(parts[6])));
                        } catch (NumberFormatException e) {
                            // Ignore malformed lines
                        }
                    }
                }
                LeaderboardPanel.refreshLeaderboardData();
                Main.gamesPlayed = LeaderboardPanel.sessionHistory.size();
                leaderboardPanel.refreshUI();
                cardLayout.show(cardContentPanel, "LEADERBOARD");
            }
        }
    }

    // =========================================================================
    // HOME / INFO PANEL BUILDERS
    // =========================================================================

    private void updateInfoPanel() {
        if (infoPanel == null)
            return;
        infoPanel.removeAll();

        TankData currentTank = tanks.get(currentTankIndex);
        infoPanel.add(createStatBox("OFFENSIVE POWER", String.format("%.1f", currentTank.getOffensivePower()),
                (int) currentTank.getOffensivePower(), false));
        infoPanel.add(Box.createVerticalStrut(16));
        infoPanel.add(createStatBox("MOBILITY INDEX", String.format("%.1f", currentTank.getMobilityIndex()),
                (int) currentTank.getMobilityIndex(), false));
        infoPanel.add(Box.createVerticalStrut(16));
        infoPanel.add(createExpBlock());
        infoPanel.add(Box.createVerticalGlue());

        JPanel arrowPanel = new JPanel();
        arrowPanel.setLayout(new BoxLayout(arrowPanel, BoxLayout.Y_AXIS));
        arrowPanel.setOpaque(false);
        arrowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        arrowPanel.add(createArrowButton("^", () -> {
            currentTankIndex = (currentTankIndex - 1 + tanks.size()) % tanks.size();
            canvas.repaint();
            updateInfoPanel();
        }));
        arrowPanel.add(Box.createVerticalStrut(8));
        arrowPanel.add(createIconButton(() -> {
            for (TankData tank : tanks)
                tank.rerollStats();
            updateInfoPanel();
            canvas.repaint();
        }));
        arrowPanel.add(Box.createVerticalStrut(8));
        arrowPanel.add(createArrowButton("v", () -> {
            currentTankIndex = (currentTankIndex + 1) % tanks.size();
            canvas.repaint();
            updateInfoPanel();
        }));

        infoPanel.add(arrowPanel);
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private JPanel createStatBox(String labelText, String valueText, int percentage, boolean dithered) {
        JPanel statBox = new JPanel();
        statBox.setLayout(new BoxLayout(statBox, BoxLayout.Y_AXIS));
        statBox.setOpaque(false);
        statBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        statBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        statBox.setToolTipText("Detailed stats for the selected unit: " + labelText);

        JLabel statLabel = createLabel(labelText, 12f);
        statLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.THEME_FOREGROUND));
        statLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        statBox.add(statLabel);

        JLabel statValueLabel = createLabel(valueText, 24f);
        statValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statValueLabel.setFont(statValueLabel.getFont().deriveFont(Font.BOLD));
        statValueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statBox.add(statValueLabel);

        DitheredBar progressBar = new DitheredBar(percentage, dithered);
        progressBar.setPreferredSize(new Dimension(160, 12));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        statBox.add(progressBar);
        statBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        return statBox;
    }

    private JPanel createExpBlock() {
        JPanel expBox = new JPanel();
        expBox.setLayout(new BoxLayout(expBox, BoxLayout.Y_AXIS));
        expBox.setOpaque(false);
        expBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        expBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        expBox.setToolTipText("Your current level and pilot proficiency");

        JPanel levelHeaderRow = new JPanel(new BorderLayout());
        levelHeaderRow.setOpaque(false);
        levelHeaderRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        levelHeaderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        levelHeaderRow.add(createLabel("CURRENT LVL", 12f), BorderLayout.WEST);
        levelHeaderRow.add(createLabel("L_12", 12f), BorderLayout.EAST);
        levelHeaderRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.THEME_FOREGROUND));
        expBox.add(levelHeaderRow);

        JLabel expValueLabel = createLabel(" ", 24f);
        expValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        expValueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        expBox.add(expValueLabel);

        DitheredBar progressBar = new DitheredBar(65, true);
        progressBar.setPreferredSize(new Dimension(160, 12));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        expBox.add(progressBar);
        expBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        expBox.setToolTipText("Pilot proficiency and level progress");
        return expBox;
    }

    private JPanel createArrowButton(String title, Runnable action) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        panel.setToolTipText(title.equals("^") ? "Previous tank" : "Next tank");

        JLabel arrowLabel = createLabel(title, 20f);
        arrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(arrowLabel, BorderLayout.CENTER);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setFocusable(true);

        Runnable onHover = () -> {
            panel.setOpaque(true);
            panel.setBackground(UIComponents.THEME_FOREGROUND);
            arrowLabel.setForeground(UIComponents.THEME_BACKGROUND);
            panel.repaint();
        };
        Runnable onUnhover = () -> {
            panel.setOpaque(false);
            arrowLabel.setForeground(UIComponents.THEME_FOREGROUND);
            panel.repaint();
        };

        panel.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                onHover.run();
            }

            public void focusLost(FocusEvent e) {
                onUnhover.run();
            }
        });
        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (action != null)
                    action.run();
            }
        });
        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                onHover.run();
            }

            public void mouseExited(MouseEvent e) {
                if (!panel.hasFocus())
                    onUnhover.run();
            }

            public void mousePressed(MouseEvent e) {
                if (action != null)
                    action.run();
            }
        });
        return panel;
    }

    private JPanel createIconButton(Runnable action) {
        JPanel panel = new JPanel() {
            private boolean isHovered = false;
            {
                setOpaque(false);
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 1),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)));
                setToolTipText("Randomize tank unit specifications");
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFocusable(true);

                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        isHovered = true;
                        setOpaque(true);
                        setBackground(UIComponents.THEME_FOREGROUND);
                        repaint();
                    }

                    public void focusLost(FocusEvent e) {
                        isHovered = false;
                        setOpaque(false);
                        repaint();
                    }
                });
                getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
                getActionMap().put("onEnter", new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        if (action != null)
                            action.run();
                    }
                });
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        setOpaque(true);
                        setBackground(UIComponents.THEME_FOREGROUND);
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        if (!hasFocus()) {
                            isHovered = false;
                            setOpaque(false);
                            repaint();
                        }
                    }

                    public void mousePressed(MouseEvent e) {
                        if (action != null)
                            action.run();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D graphics2d = (Graphics2D) graphics.create();
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                graphics2d.setColor(isHovered ? UIComponents.THEME_BACKGROUND : UIComponents.THEME_FOREGROUND);
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                graphics2d.translate(centerX - 8, centerY - 8);
                // Refresh icon (two curved arrow shapes)
                int[] arrowLeftXPoints = { 3, 3, 2, 2, 1, 1, 3, 3, 8, 8, 4, 4, 6, 6, 5, 5, 4, 4, 3 };
                int[] arrowLeftYPoints = { 2, 3, 3, 4, 4, 5, 5, 13, 13, 12, 12, 5, 5, 4, 4, 3, 3, 2, 2 };
                graphics2d.fillPolygon(arrowLeftXPoints, arrowLeftYPoints, 19);
                int[] arrowRightXPoints = { 7, 7, 12, 12, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 13, 13, 7 };
                int[] arrowRightYPoints = { 3, 4, 4, 11, 11, 12, 12, 13, 13, 14, 14, 13, 13, 12, 12, 11, 11, 3, 3 };
                graphics2d.fillPolygon(arrowRightXPoints, arrowRightYPoints, 19);
                graphics2d.dispose();
            }
        };
        return panel;
    }
}
