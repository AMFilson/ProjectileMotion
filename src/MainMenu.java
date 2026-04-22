import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The initial landing menu of the 'BIT-REKT' application.
 * Features 3-tank selection, experience progression, and keyboard navigation.
 */
public class MainMenu extends JFrame {

    private Font vt323_base;

    private Color bg = new Color(239, 243, 241); // #eff3f1
    private Color fg = new Color(0, 0, 0); // #000000

    private java.util.List<TankData> tanks = new java.util.ArrayList<>();
    private int currentTankIndex = 0;

    private JPanel infoPanel;
    private CanvasArea canvas;
    private int flickerStep = 0;

    private CardLayout cardLayout;
    private JPanel cardContentPanel;
    private java.util.List<JPanel> navItemsList = new java.util.ArrayList<>();
    private java.util.List<Runnable> navHighlightResetters = new java.util.ArrayList<>();

    public MainMenu() {
        setTitle("BIT-REKT");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tanks.add(new TankData("M8 GREYHOUND"));
        tanks.add(new TankData("FLAK 88"));
        tanks.add(new TankData("BLACK CAT"));

        // Global blink timer for UI elements
        Timer blinkTimer = new Timer(600, e -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof MainMenu) {
                    w.repaint();
                }
            }
        });
        blinkTimer.start();

        // Load the Custom Font
        try {
            File fontFile = new File("src/fonts/VT323-Regular.ttf");
            if (fontFile.exists()) {
                vt323_base = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(vt323_base);
            } else {
                vt323_base = new Font("Monospaced", Font.PLAIN, 16);
            }
        } catch (Exception e) {
            vt323_base = new Font("Monospaced", Font.PLAIN, 16);
        }

        // Set Custom Cursor
        BufferedImage cursorImg = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = cursorImg.createGraphics();
        cg.setColor(Color.BLACK);
        cg.fillRect(9, 0, 2, 20);
        cg.fillRect(0, 9, 20, 2);
        cg.dispose();
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(10, 10), "crosshair");
        setCursor(customCursor);

        JPanel rootPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(bg);
                g.fillRect(0, 0, getWidth(), getHeight());

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(fg);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
                for (int y = 0; y < getHeight(); y += 4) {
                    for (int x = 0; x < getWidth(); x += 4) {
                        g2.fillRect(x, y, 1, 1);
                    }
                }
                g2.dispose();
            }
        };

        // --- MAIN FRAME UI ---
        MainFramePanel mainFrame = new MainFramePanel();
        mainFrame.setPreferredSize(new Dimension(900, 600));
        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, fg),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(createLabel("HEAVY ARMORED DIVISION", 12f));
        titleBlock.add(Box.createVerticalStrut(-5));
        titleBlock.add(createLabel("BIT-REKT", 48f));

        JLabel systemStatus = createLabel(
                "<html><p align='right' style='line-height:0.8'>LOCATION: CAMP 30<br>TIME: 19:04:25</p></html>",
                16f);
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(systemStatus, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        mainFrame.add(headerPanel, gbc);

        // LEFT SIDEBAR
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, fg),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        String[] navNames = { "NEW GAME", "HOW TO PLAY", "LEADERBOARD", "SAVE/LOAD", "TERMINATE" };
        String[] navIds = { "01", "02", "03", "04", "05" };

        for (int i = 0; i < navNames.length; i++) {
            JPanel navItem = createNavItem(navNames[i], navIds[i]);
            navItemsList.add(navItem);
            if (i == 0) {
                // Request focus for NEW GAME on launch
                SwingUtilities.invokeLater(() -> navItem.requestFocusInWindow());
            }
            sidebar.add(navItem);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());
        JPanel homeBtn = createNavItem("HOME", "06");
        navItemsList.add(homeBtn);
        sidebar.add(homeBtn);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        sidebar.setPreferredSize(new Dimension(220, 0));
        mainFrame.add(sidebar, gbc);

        // CENTER CANVAS
        canvas = new CanvasArea();
        canvas.setOpaque(false);
        canvas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                new DashedBorder(fg, 1, 4)));

        // RIGHT INFO PANEL
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, fg),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        updateInfoPanel();
        infoPanel.setPreferredSize(new Dimension(185, 0));

        // CARD CONTENT PANEL
        cardLayout = new CardLayout();
        cardContentPanel = new JPanel(cardLayout);
        cardContentPanel.setOpaque(false);

        JPanel homePanel = new JPanel(new GridBagLayout());
        homePanel.setOpaque(false);
        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.fill = GridBagConstraints.BOTH;
        hgbc.gridx = 0;
        hgbc.gridy = 0;
        hgbc.weightx = 1.0;
        hgbc.weighty = 1.0;
        homePanel.add(canvas, hgbc);

        hgbc.gridx = 1;
        hgbc.gridy = 0;
        hgbc.weightx = 0.0;
        hgbc.weighty = 1.0;
        homePanel.add(infoPanel, hgbc);

        cardContentPanel.add(homePanel, "HOME");
        cardContentPanel.add(new LeaderboardPanel(), "LEADERBOARD");
        cardContentPanel.add(new CharacterSelectPanel(), "NEW_GAME");

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Spans across canvas and info column
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainFrame.add(cardContentPanel, gbc);

        // FOOTER
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, fg),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        footerPanel.add(createLabel("CREATED BY ANDREW FILSON", 16f), BorderLayout.WEST);

        JLabel rightFooterLabel = createLabel("PRESS [ENTER] TO INITIALIZE", 16f);
        rightFooterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        footerPanel.add(rightFooterLabel, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        mainFrame.add(footerPanel, gbc);

        rootPanel.add(mainFrame);
        add(rootPanel);

        Timer timeTaker = new Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            systemStatus.setText("<html><p align='right' style='line-height:0.8'>LOCATION: CAMP 30<br>TIME: " + time
                    + "</p></html>");
        });
        timeTaker.start();

        Timer flickerAct = new Timer(100, e -> {
            flickerStep = (flickerStep + 1) % 40;
            float alpha = 1.0f;
            if (flickerStep == 5)
                alpha = 0.95f;
            if (flickerStep == 7)
                alpha = 0.98f;
            mainFrame.setOpacity(alpha);
            mainFrame.repaint();
        });
        flickerAct.start();
    }

    private JLabel createLabel(String txt, float fontSize) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(vt323_base.deriveFont(fontSize));
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        return lbl;
    }

    private JPanel createNavItem(String title, String num) {
        JPanel panel = new JPanel(new BorderLayout());
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
                BorderFactory.createLineBorder(fg, 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel leftLbl = createLabel("□ " + title, 24f);
        JLabel rightLbl = createLabel(num, 24f);
        panel.add(leftLbl, BorderLayout.WEST);
        panel.add(rightLbl, BorderLayout.EAST);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setFocusable(true);

        Runnable highlight = () -> {
            panel.setOpaque(true);
            panel.setBackground(fg);
            leftLbl.setForeground(bg);
            leftLbl.setText("■ " + title);
            rightLbl.setForeground(bg);
            panel.repaint();
        };

        Runnable unhighlight = () -> {
            panel.setOpaque(false);
            leftLbl.setForeground(fg);
            leftLbl.setText("□ " + title);
            rightLbl.setForeground(fg);
            panel.repaint();
        };

        navHighlightResetters.add(unhighlight);

        panel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                highlight.run();
            }

            @Override
            public void focusLost(FocusEvent e) {
                unhighlight.run();
            }
        });

        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNavClick(title);
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                highlight.run();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isNavActive(title))
                    unhighlight.run();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleNavClick(title);
            }
        });
        return panel;
    }

    private boolean isNavActive(String title) {
        // Here we could track real 'active' state. For simplicity, just return true if
        // it's the active tab.
        // Actually, returning false means the hover effect resets when mouse leaves.
        // Let's implement active tab state:
        return false;
    }

    private void handleNavClick(String title) {
        for (Runnable r : navHighlightResetters)
            r.run();

        if (title.equals("NEW GAME")) {
            cardLayout.show(cardContentPanel, "NEW_GAME");
        } else if (title.equals("LEADERBOARD")) {
            cardLayout.show(cardContentPanel, "LEADERBOARD");
        } else if (title.equals("SAVE/LOAD")) {
            handleSaveLoad();
        } else if (title.equals("TERMINATE")) {
            System.exit(0);
        } else {
            cardLayout.show(cardContentPanel, "HOME");
        }
    }

    private void handleSaveLoad() {
        Object[] options = { "Save", "Load", "Cancel" };
        int n = JOptionPane.showOptionDialog(this,
                "Would you like to Save or Load a game file?",
                "SAVE/LOAD",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        if (n == JOptionPane.YES_OPTION) {
            // Mock data for saving, since we are in main menu
            String[] mockData = { "No actual game data to save from menu yet" };
            DataManager.saveGame(this, mockData);
        } else if (n == JOptionPane.NO_OPTION) {
            DataManager.loadGame(this);
            // Handle loaded data if needed
        }
    }

    private JPanel createExpBlock() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        box.setToolTipText("Your current level and pilot proficiency");

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        topRow.add(createLabel("CURRENT LVL", 12f), BorderLayout.WEST);
        topRow.add(createLabel("L_12", 12f), BorderLayout.EAST);
        topRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        box.add(topRow);

        // Add middle space to match createStatBox height
        JLabel val = createLabel(" ", 24f);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        val.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        box.add(val);

        DitheredBar bar = new DitheredBar(65, true);
        bar.setPreferredSize(new Dimension(160, 12));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(bar);

        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        return box;
    }

    private void updateInfoPanel() {
        if (infoPanel == null)
            return;
        infoPanel.removeAll();

        TankData currentTank = tanks.get(currentTankIndex);

        String opStr = String.format("%.1f", currentTank.getOffensivePower());
        infoPanel.add(createStatBox("OFFENSIVE POWER", opStr, (int) currentTank.getOffensivePower(), false));
        infoPanel.add(Box.createVerticalStrut(16));

        String miStr = String.format("%.1f", currentTank.getMobilityIndex());
        infoPanel.add(createStatBox("MOBILITY INDEX", miStr, (int) currentTank.getMobilityIndex(), false));
        infoPanel.add(Box.createVerticalStrut(16));

        // Moved CURRENT LVL Block here
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
            for (TankData t : tanks)
                t.rerollStats();
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

    private JPanel createStatBox(String labelTxt, String valTxt, int percentage, boolean dithered) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        box.setToolTipText("Detailed stats for the selected unit: " + labelTxt);

        JLabel lbl = createLabel(labelTxt, 12f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        box.add(lbl);

        JLabel val = createLabel(valTxt, 24f);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        val.setFont(val.getFont().deriveFont(Font.BOLD));
        val.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        box.add(val);

        DitheredBar bar = new DitheredBar(percentage, dithered);
        bar.setPreferredSize(new Dimension(160, 12));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(bar);

        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        return box;
    }

    private JPanel createArrowButton(String title, Runnable action) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        panel.setToolTipText(title.equals("^") ? "Previous tank unit" : "Next tank unit");

        JLabel lbl = createLabel(title, 20f);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl, BorderLayout.CENTER);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setFocusable(true);

        Runnable hl = () -> {
            panel.setOpaque(true);
            panel.setBackground(fg);
            lbl.setForeground(bg);
            panel.repaint();
        };
        Runnable uhl = () -> {
            panel.setOpaque(false);
            lbl.setForeground(fg);
            panel.repaint();
        };

        panel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                hl.run();
            }

            @Override
            public void focusLost(FocusEvent e) {
                uhl.run();
            }
        });

        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (action != null)
                    action.run();
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hl.run();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!panel.hasFocus())
                    uhl.run();
            }

            @Override
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
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(fg, 1),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)));
                setToolTipText("Randomize tank unit specifications");
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFocusable(true);

                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        isHovered = true;
                        setOpaque(true);
                        setBackground(fg);
                        repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        isHovered = false;
                        setOpaque(false);
                        repaint();
                    }
                });

                getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
                getActionMap().put("onEnter", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (action != null)
                            action.run();
                    }
                });

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        setOpaque(true);
                        setBackground(fg);
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!hasFocus()) {
                            isHovered = false;
                            setOpaque(false);
                            repaint();
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (action != null)
                            action.run();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setColor(isHovered ? bg : fg);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.translate(cx - 8, cy - 8);
                // Refresh icon shapes
                int[] x1 = { 3, 3, 2, 2, 1, 1, 3, 3, 8, 8, 4, 4, 6, 6, 5, 5, 4, 4, 3 };
                int[] y1 = { 2, 3, 3, 4, 4, 5, 5, 13, 13, 12, 12, 5, 5, 4, 4, 3, 3, 2, 2 };
                g2.fillPolygon(x1, y1, 19);
                int[] x2 = { 7, 7, 12, 12, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 13, 13, 7 };
                int[] y2 = { 3, 4, 4, 11, 11, 12, 12, 13, 13, 14, 14, 13, 13, 12, 12, 11, 11, 3, 3 };
                g2.fillPolygon(x2, y2, 19);
                g2.dispose();
            }
        };
        return panel;
    }

    class MainFramePanel extends JPanel {
        private float opacity = 1.0f;

        public void setOpacity(float o) {
            this.opacity = o;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2.setColor(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(fg);
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(4, 4, getWidth() - 8, getHeight() - 8);
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            int bSize = 15;
            drawBracket(g2, -10, -10, bSize, true, true);
            drawBracket(g2, getWidth() + 10 - bSize, -10, bSize, true, false);
            drawBracket(g2, -10, getHeight() + 10 - bSize, bSize, false, true);
            drawBracket(g2, getWidth() + 10 - bSize, getHeight() + 10 - bSize, bSize, false, false);
            g2.dispose();
        }

        private void drawBracket(Graphics2D g, int x, int y, int size, boolean top, boolean left) {
            g.setColor(fg);
            if (top && left) {
                g.fillRect(x, y, size, 2);
                g.fillRect(x, y, 2, size);
            } else if (top && !left) {
                g.fillRect(x, y, size, 2);
                g.fillRect(x + size - 2, y, 2, size);
            } else if (!top && left) {
                g.fillRect(x, y + size - 2, size, 2);
                g.fillRect(x, y, 2, size);
            } else if (!top && !left) {
                g.fillRect(x, y + size - 2, size, 2);
                g.fillRect(x + size - 2, y, 2, size);
            }
        }
    }

    class CanvasArea extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            TankData currentTank = tanks.get(currentTankIndex);
            String tankName = currentTank.getName();
            g2.setFont(vt323_base.deriveFont(32f));
            FontMetrics sfm = g2.getFontMetrics();
            int sw = sfm.stringWidth(tankName);
            int sh = 40;
            int sy = getHeight() - 60;
            int tx = cx - sw / 2;
            int ty = sy + (sh + sfm.getAscent()) / 2 - 4;
            g2.setColor(fg);
            g2.fillRect(tx - 10, sy + 5, sw + 20, sh - 10);
            g2.setColor(bg);
            g2.drawString(tankName, tx, ty);
            g2.translate(cx - 150, cy - 150);
            g2.scale(4.6875, 4.6875);
            g2.setColor(fg);

            if (currentTankIndex == 0) {
                // M8 GREYHOUND
                g2.fillRect(10, 44, 44, 10);
                g2.fillRect(12, 42, 40, 2);
                g2.setColor(bg);
                g2.fillRect(14, 46, 4, 6);
                g2.fillRect(22, 46, 4, 6);
                g2.fillRect(30, 46, 4, 6);
                g2.fillRect(38, 46, 4, 6);
                g2.fillRect(46, 46, 4, 6);
                g2.setColor(fg);
                g2.fillRect(14, 34, 36, 10);
                g2.fillRect(18, 32, 28, 2);
                g2.fillRect(22, 24, 20, 8);
                g2.fillRect(24, 22, 16, 2);
                g2.fillRect(42, 26, 18, 4);
                g2.fillRect(58, 25, 2, 6);
                g2.fillRect(26, 20, 8, 2);
                g2.setColor(bg);
                g2.fillRect(24, 24, 2, 2);
                g2.fillRect(26, 26, 2, 2);
            } else if (currentTankIndex == 1) {
                // FLAK 88
                g2.fillRect(24, 48, 16, 6);
                g2.fillRect(18, 46, 28, 2);
                g2.fillRect(28, 38, 8, 8);
                g2.fillRect(30, 32, 4, 6);
                g2.fillRect(32, 20, 4, 16);
                g2.fillRect(34, 12, 4, 10);
                g2.fillRect(36, 4, 4, 10);
                g2.fillRect(38, -4, 2, 8);
            } else if (currentTankIndex == 2) {
                // BLACK CAT
                g2.fillRect(6, 46, 52, 8);
                g2.setColor(bg);
                g2.fillRect(10, 48, 6, 4);
                g2.fillRect(22, 48, 6, 4);
                g2.fillRect(34, 48, 6, 4);
                g2.fillRect(46, 48, 6, 4);
                g2.setColor(fg);
                g2.fillRect(10, 38, 44, 8);
                g2.fillRect(14, 34, 34, 4);
                g2.fillRect(16, 24, 24, 10);
                g2.fillRect(40, 28, 20, 2);
                g2.fillRect(58, 27, 4, 4);
            }
            g2.dispose();
        }
    }

    class DitheredBar extends JPanel {
        private int percent;
        private boolean dithered;

        public DitheredBar(int percent, boolean dithered) {
            this.percent = percent;
            this.dithered = dithered;
            setBorder(BorderFactory.createLineBorder(fg, 1));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = (int) (getWidth() * (percent / 100.0));
            Graphics2D g2 = (Graphics2D) g.create();
            if (dithered) {
                BufferedImage fillImg = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
                Graphics2D fG = fillImg.createGraphics();
                fG.setColor(fg);
                fG.drawLine(0, 2, 2, 0);
                fG.drawLine(2, 4, 4, 2);
                fG.dispose();
                g2.setPaint(new TexturePaint(fillImg, new Rectangle(0, 0, 4, 4)));
            } else {
                g2.setColor(fg);
            }
            g2.fillRect(0, 0, width, getHeight());
            g2.dispose();
        }
    }

    class DashedBorder extends javax.swing.border.AbstractBorder {
        private Color color;
        private int thickness;
        private int dashLength;

        public DashedBorder(Color color, int thickness, int dashLength) {
            this.color = color;
            this.thickness = thickness;
            this.dashLength = dashLength;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            float[] dash = { dashLength };
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2.drawRect(x, y, width - 1, height - 1);
            g2.dispose();
        }
    }

    class LeaderboardPanel extends JPanel {
        public LeaderboardPanel() {
            setLayout(new GridBagLayout());
            setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            // CENTER CANVAS (Leaderboard Table)
            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
            tablePanel.setOpaque(false);
            tablePanel.setBorder(new DashedBorder(fg, 1, 4));

            // Table Header
            JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
            headerRow.setOpaque(false);
            headerRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, fg),
                    BorderFactory.createEmptyBorder(0, 20, 8, 20)));
            headerRow.add(createLabel("RANK", 20f));
            headerRow.add(createLabel("UNIT", 20f));
            headerRow.add(createLabel("PLAYER", 20f)); // Changed from OPERATOR_ID
            JLabel hsLabel = createLabel("HIGH_SCORE", 20f);
            hsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            headerRow.add(hsLabel);

            headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            tablePanel.add(headerRow);
            tablePanel.add(Box.createVerticalStrut(10));

            // Generate Rows using playersList
            java.util.List<Player> players = Main.playersList;
            if (players == null)
                players = new java.util.ArrayList<>();

            for (int i = 0; i < players.size() && i < 5; i++) {
                Player p = players.get(i);
                JPanel row = createRankRow(i + 1, p.getName(), p.getSelectedTankIndex(), p.getScore());
                tablePanel.add(row);
            }

            // Bottom Text
            tablePanel.add(Box.createVerticalGlue());
            // Footer text removed as per green circle request

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            add(tablePanel, gbc);

            // RIGHT INFO PANEL (Leaderboard Info)
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setOpaque(false);
            rightPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 1, 0, 0, fg),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));

            rightPanel.add(createLeaderboardStatBox("GAMES PLAYED", String.valueOf(Main.gamesPlayed), 0, false));
            rightPanel.add(Box.createVerticalStrut(20));
            rightPanel.add(createLeaderboardStatBox("SEASON PROGRESS", "WEEK 04", 45, true));
            rightPanel.add(Box.createVerticalStrut(20));

            // Legacy Status Box (Inverted)
            JPanel legacyBox = new JPanel();
            legacyBox.setLayout(new BoxLayout(legacyBox, BoxLayout.Y_AXIS));
            legacyBox.setOpaque(true);
            legacyBox.setBackground(fg);
            legacyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            legacyBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)));

            JLabel lbl = createLabel("LEGACY STATUS", 12f);
            lbl.setForeground(bg);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, bg));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            legacyBox.add(lbl);

            JLabel val = createLabel("PLATINUM TIER", 18f);
            val.setForeground(bg);
            val.setAlignmentX(Component.LEFT_ALIGNMENT);
            val.setFont(val.getFont().deriveFont(Font.BOLD));
            legacyBox.add(val);

            JLabel unlockLbl = createLabel("UNLOCKED: X-CALIBER_SKIN", 12f);
            unlockLbl.setForeground(bg);
            unlockLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            legacyBox.add(unlockLbl);

            rightPanel.add(legacyBox);
            rightPanel.add(Box.createVerticalGlue());

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            rightPanel.setPreferredSize(new Dimension(185, 0));
            add(rightPanel, gbc);
        }

        private JPanel createRankRow(int rank, String name, int tankIndex, int score) {
            JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
            row.setOpaque(true);
            row.setBackground(new Color(0, 0, 0, 0)); // Transparent usually
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0, 50)),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            // #1 is YOU
            if (name.contains("YOU")) {
                row.setBackground(new Color(0, 0, 0, 15)); // Highlight
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(fg, 1),
                        BorderFactory.createEmptyBorder(11, 19, 11, 19))); // compensate for border thickness, plus
                                                                           // padding
            }

            // 1. Pixel Badge inside a wrapper
            JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            badgeWrapper.setOpaque(false);
            JLabel badge = createLabel(String.format("#%02d", rank), 24f);
            badge.setOpaque(true);
            if (rank == 1 || rank == 3) {
                badge.setBackground(fg);
                badge.setForeground(bg);
            } else {
                badge.setBackground(new Color(0, 0, 0, 0));
                badge.setForeground(fg);
                badge.setBorder(rank == 2 ? BorderFactory.createLineBorder(fg, 2) : new DashedBorder(fg, 1, 4));
            }
            badge.setPreferredSize(new Dimension(40, 40));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badgeWrapper.add(badge);
            row.add(badgeWrapper);

            // 2. Unit SVG representation (Custom drawing panel)
            JPanel unitPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    // scale and center appropriately
                    g2.translate(0, -10); // nudge
                    g2.scale(0.8, 0.8);

                    // We duplicate the canvas drawing logic but just for the specific tank
                    // M8 GREYHOUND: 0, FLAK 88: 1, BLACK CAT: 2
                    g2.setColor(fg);
                    if (tankIndex == 0) {
                        g2.fillRect(10, 44, 44, 10);
                        g2.fillRect(12, 42, 40, 2);
                        g2.setColor(bg);
                        g2.fillRect(14, 46, 4, 6);
                        g2.fillRect(22, 46, 4, 6);
                        g2.fillRect(30, 46, 4, 6);
                        g2.fillRect(38, 46, 4, 6);
                        g2.fillRect(46, 46, 4, 6);
                        g2.setColor(fg);
                        g2.fillRect(14, 34, 36, 10);
                        g2.fillRect(18, 32, 28, 2);
                        g2.fillRect(22, 24, 20, 8);
                        g2.fillRect(24, 22, 16, 2);
                        g2.fillRect(42, 26, 18, 4);
                        g2.fillRect(58, 25, 2, 6);
                        g2.fillRect(26, 20, 8, 2);
                        g2.setColor(bg);
                        g2.fillRect(24, 24, 2, 2);
                        g2.fillRect(26, 26, 2, 2);
                    } else if (tankIndex == 1) {
                        g2.fillRect(24, 48, 16, 6);
                        g2.fillRect(18, 46, 28, 2);
                        g2.fillRect(28, 38, 8, 8);
                        g2.fillRect(30, 32, 4, 6);
                        g2.fillRect(32, 20, 4, 16);
                        g2.fillRect(34, 12, 4, 10);
                        g2.fillRect(36, 4, 4, 10);
                        g2.fillRect(38, -4, 2, 8);
                    } else if (tankIndex == 2) {
                        g2.fillRect(6, 46, 52, 8);
                        g2.setColor(bg);
                        g2.fillRect(10, 48, 6, 4);
                        g2.fillRect(22, 48, 6, 4);
                        g2.fillRect(34, 48, 6, 4);
                        g2.fillRect(46, 48, 6, 4);
                        g2.setColor(fg);
                        g2.fillRect(10, 38, 44, 8);
                        g2.fillRect(14, 34, 34, 4);
                        g2.fillRect(16, 24, 24, 10);
                        g2.fillRect(40, 28, 20, 2);
                        g2.fillRect(58, 27, 4, 4);
                    }
                    g2.dispose();
                }
            };
            unitPanel.setOpaque(false);
            row.add(unitPanel);

            // 3. Name
            JLabel nameLabel = createLabel(name, 24f);
            row.add(nameLabel);

            // 4. Score
            // Commas formatting
            String scoreStr = String.format("%,d", score);
            JLabel scoreLbl = createLabel(scoreStr, 24f);
            scoreLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(scoreLbl);

            return row;
        }

        private JPanel createLeaderboardStatBox(String labelTxt, String valTxt, int percentage, boolean dithered) {
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setOpaque(false);
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)));

            JLabel lbl = createLabel(labelTxt, 12f);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(lbl);

            JLabel val = createLabel(valTxt, 24f);
            val.setAlignmentX(Component.LEFT_ALIGNMENT);
            val.setFont(val.getFont().deriveFont(Font.BOLD));
            box.add(val);

            DitheredBar bar = new DitheredBar(percentage, dithered);
            bar.setPreferredSize(new Dimension(160, 12));
            bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(bar);

            return box;
        }
    }

    // =========================================================================
    // CHARACTER SELECT PANEL
    // =========================================================================
    class CharacterSelectPanel extends JPanel {
        private PlayerColumn p1Col;
        private PlayerColumn p2Col;
        private JLabel battleStatusLabel;
        private JPanel battleBtn;
        private boolean blinkOn = true;

        public CharacterSelectPanel() {
            setLayout(new BorderLayout(0, 0));
            setOpaque(false);

            // --- MAIN CONTENT: two player columns with divider ---
            JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 0, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 50));
                    g.drawLine(getWidth() / 2, 20, getWidth() / 2, getHeight() - 20);
                }
            };
            columnsPanel.setOpaque(false);

            p1Col = new PlayerColumn(1, "MIGGY", true);
            p2Col = new PlayerColumn(2, "", false);
            columnsPanel.add(p1Col);
            columnsPanel.add(p2Col);
            add(columnsPanel, BorderLayout.CENTER);

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            JPanel footerLeft = new JPanel();
            footerLeft.setOpaque(false);
            footer.add(footerLeft, BorderLayout.WEST);

            battleStatusLabel = new JLabel("AWAITING P2...") {
                @Override
                protected void paintComponent(Graphics g) {
                    blinkOn = (System.currentTimeMillis() / 600) % 2 == 0;
                    if (blinkOn || (p1Col.isReady() && p2Col.isReady())) {
                        super.paintComponent(g);
                    }
                }
            };
            battleStatusLabel.setFont(vt323_base.deriveFont(20f));
            battleStatusLabel.setForeground(fg);
            battleStatusLabel.setPreferredSize(new Dimension(200, 40)); // Ensure enough room
            battleStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            battleBtn = createBattleButton();
            battleBtn.setEnabled(false);
            JPanel battleWrapper = new JPanel(new BorderLayout());
            battleWrapper.setOpaque(false);
            battleWrapper.add(battleBtn, BorderLayout.CENTER);
            battleWrapper.add(battleStatusLabel, BorderLayout.EAST);

            // Add a placeholder to the WEST to keep button centered
            JPanel westPlaceholder = new JPanel();
            westPlaceholder.setOpaque(false);
            westPlaceholder.setPreferredSize(new Dimension(200, 10));
            battleWrapper.add(westPlaceholder, BorderLayout.WEST);

            footer.add(battleWrapper, BorderLayout.SOUTH);

            add(footer, BorderLayout.SOUTH);
            onStatusChanged(); // Set initial status string
        }

        void onStatusChanged() {
            boolean p1Ready = p1Col.isReady();
            boolean p2Ready = p2Col.isReady();
            boolean bothReady = p1Ready && p2Ready;

            battleBtn.setEnabled(bothReady);

            if (bothReady) {
                battleStatusLabel.setText("[ ALL SYSTEMS GO ]");
            } else if (!p1Ready && !p2Ready) {
                battleStatusLabel.setText("AWAITING PLAYERS...");
            } else if (!p1Ready) {
                battleStatusLabel.setText("AWAITING P1...");
            } else {
                battleStatusLabel.setText("AWAITING P2...");
            }

            repaint();
        }

        private JPanel createBattleButton() {
            JPanel mainBtn = new JPanel(new BorderLayout()) {
                private BufferedImage ditherPattern;
                {
                    ditherPattern = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = ditherPattern.createGraphics();
                    g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 80));
                    g2.drawLine(0, 2, 2, 0);
                    g2.drawLine(2, 4, 4, 2);
                    g2.dispose();
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (!isEnabled()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setPaint(new TexturePaint(ditherPattern, new Rectangle(0, 0, 4, 4)));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                }
            };
            mainBtn.setOpaque(false);
            mainBtn.setPreferredSize(new Dimension(220, 54));
            mainBtn.setMaximumSize(new Dimension(220, 54));
            mainBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 2),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            JLabel lbl = createLabel("BATTLE", 24f);
            lbl.setForeground(fg);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            mainBtn.add(lbl, BorderLayout.CENTER);

            mainBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            mainBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!mainBtn.isEnabled())
                        return;
                    String p1Name = p1Col.getPlayerName();
                    int p1TankIdx = p1Col.getSelectedTankIndex();
                    String p2Name = p2Col.getPlayerName();
                    int p2TankIdx = p2Col.getSelectedTankIndex();
                    if (Main.playersList.size() >= 2) {
                        Main.playersList.get(0).setName(p1Name);
                        Main.playersList.get(0).setSelectedTankIndex(p1TankIdx);
                        Main.playersList.get(1).setName(p2Name);
                        Main.playersList.get(1).setSelectedTankIndex(p2TankIdx);
                    }
                    TankData t1 = tanks.get(p1TankIdx);
                    TankData t2 = tanks.get(p2TankIdx);
                    SwingUtilities.invokeLater(() -> {
                        MainWindow mw = new MainWindow(p1Name, t1, p2Name, t2);
                        mw.setVisible(true);
                    });
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (mainBtn.isEnabled()) {
                        mainBtn.setOpaque(true);
                        mainBtn.setBackground(fg);
                        lbl.setForeground(bg);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mainBtn.setOpaque(false);
                    lbl.setForeground(fg);
                    lbl.setText("BATTLE");
                }
            });
            return mainBtn;
        }

        class PlayerColumn extends JPanel {
            private int playerNum;
            private int selectedTankIndex = 0;
            private boolean ready = false;
            private JTextField nameField;
            private JPanel tankCanvas;
            private JLabel tankNameLabel;
            private JPanel statsPanel;
            private JPanel statusBtnWrapper;
            private JPanel statusBtnMain;
            private JPanel statusBtnShadow;
            private JLabel statusLbl;
            private JLabel statusVal;

            PlayerColumn(int num, String defaultName, boolean startReady) {
                this.playerNum = num;
                this.ready = startReady;
                setLayout(new BorderLayout(0, 0));
                setOpaque(false);

                JPanel inner = new JPanel();
                inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
                inner.setOpaque(false);
                inner.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

                JPanel headerRow = new JPanel(new BorderLayout());
                headerRow.setOpaque(false);
                headerRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));

                JLabel playerLabel = createLabel(String.format("PLAYER %02d", num), 36f);
                if (num == 1) {
                    playerLabel.setOpaque(true);
                    playerLabel.setBackground(fg);
                    playerLabel.setForeground(bg);
                    playerLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                } else {
                    playerLabel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(fg, 2),
                            BorderFactory.createEmptyBorder(2, 10, 2, 10)));
                }
                headerRow.add(playerLabel, BorderLayout.WEST);

                tankNameLabel = createLabel(tanks.get(0).getName(), 24f);
                tankNameLabel.setOpaque(true);
                tankNameLabel.setBackground(fg);
                tankNameLabel.setForeground(bg);
                tankNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                tankNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                headerRow.add(tankNameLabel, BorderLayout.EAST);

                headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
                inner.add(headerRow);
                inner.add(Box.createVerticalStrut(20));

                JPanel identRow = new JPanel();
                identRow.setLayout(new BoxLayout(identRow, BoxLayout.X_AXIS));
                identRow.setOpaque(false);
                identRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                identRow.setPreferredSize(new Dimension(0, 50));
                identRow.add(createLabel("NAME:", 24f));
                identRow.add(Box.createHorizontalStrut(5));

                nameField = new JTextField(defaultName, 8);
                nameField.setFont(vt323_base.deriveFont(24f));
                nameField.setForeground(fg);
                nameField.setBackground(bg);
                nameField.setOpaque(false);
                nameField.setBorder(new DashedBorder(fg, 1, 4));
                nameField.setCaretColor(fg);
                nameField.setMaximumSize(new Dimension(140, 44));
                nameField.setPreferredSize(new Dimension(140, 44));

                nameField.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        nameField.setOpaque(true);
                        nameField.setBackground(fg);
                        nameField.setForeground(bg);
                        nameField.setCaretColor(bg);
                    }

                    public void focusLost(FocusEvent e) {
                        nameField.setOpaque(false);
                        nameField.setBackground(bg);
                        nameField.setForeground(fg);
                        nameField.setCaretColor(fg);
                    }
                });
                identRow.add(nameField);
                identRow.add(Box.createHorizontalStrut(5));

                // --- STATUS BUTTON (Resized to fix corner notch and clipping) ---
                statusBtnWrapper = new JPanel(null);
                statusBtnWrapper.setOpaque(false);
                statusBtnWrapper.setPreferredSize(new Dimension(144, 44));
                statusBtnWrapper.setMaximumSize(new Dimension(144, 44));

                statusBtnShadow = new JPanel();
                statusBtnShadow.setBackground(fg);
                statusBtnShadow.setBounds(4, 4, 140, 40);

                statusBtnMain = new JPanel(new BorderLayout());
                statusBtnMain.setBorder(BorderFactory.createLineBorder(fg, 2));
                statusBtnMain.setBounds(0, 0, 144, 40); // Slightly wider to cover shadow notch

                statusLbl = createLabel("STATUS", 14f);
                statusVal = createLabel(ready ? "[ READY ]" : "[ STANDBY ]", 14f);
                statusLbl.setVerticalAlignment(SwingConstants.CENTER);
                statusVal.setVerticalAlignment(SwingConstants.CENTER);
                statusVal.setHorizontalAlignment(SwingConstants.RIGHT);
                statusBtnMain.add(statusLbl, BorderLayout.WEST);
                statusBtnMain.add(statusVal, BorderLayout.EAST);
                statusBtnMain.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                statusBtnWrapper.add(statusBtnMain);
                statusBtnWrapper.add(statusBtnShadow);

                statusBtnMain.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                statusBtnMain.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        statusBtnMain.setLocation(2, 2);
                        statusBtnShadow.setVisible(false);
                        toggleStatus();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        statusBtnMain.setLocation(0, 0);
                        if (ready)
                            statusBtnShadow.setVisible(true);
                    }
                });

                identRow.add(statusBtnWrapper);

                inner.add(identRow);
                inner.add(Box.createVerticalStrut(20));

                JPanel carousel = new JPanel(new BorderLayout(0, 0)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(fg);
                        g2.setStroke(new BasicStroke(2));
                        int s = 12;
                        g2.drawLine(0, 0, s, 0);
                        g2.drawLine(0, 0, 0, s);
                        g2.drawLine(getWidth() - 1, 0, getWidth() - 1 - s, 0);
                        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, s);
                        g2.drawLine(0, getHeight() - 1, s, getHeight() - 1);
                        g2.drawLine(0, getHeight() - 1, 0, getHeight() - 1 - s);
                        g2.drawLine(getWidth() - 1, getHeight() - 1, getWidth() - 1 - s, getHeight() - 1);
                        g2.drawLine(getWidth() - 1, getHeight() - 1, getWidth() - 1, getHeight() - 1 - s);
                        g2.dispose();
                    }
                };
                carousel.setOpaque(false);
                carousel.setBorder(new DashedBorder(fg, 1, 4));
                carousel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
                carousel.setPreferredSize(new Dimension(260, 260));

                carousel.add(createCarouselBtn("<", () -> {
                    selectedTankIndex = (selectedTankIndex - 1 + tanks.size()) % tanks.size();
                    refreshTankView();
                }, true), BorderLayout.WEST);
                tankCanvas = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        // Better centering for 2.0x scale (Tank is ~36x22px unscaled)
                        g2.translate(getWidth() / 2 - 40, getHeight() / 2 - 26);
                        g2.scale(2.0, 2.0);
                        drawTankPixelArt(g2, selectedTankIndex, fg, bg);
                        g2.dispose();
                    }
                };
                tankCanvas.setOpaque(false);
                carousel.add(tankCanvas, BorderLayout.CENTER);
                carousel.add(createCarouselBtn(">", () -> {
                    selectedTankIndex = (selectedTankIndex + 1) % tanks.size();
                    refreshTankView();
                }, false), BorderLayout.EAST);

                inner.add(carousel);
                inner.add(Box.createVerticalStrut(20));

                statsPanel = new JPanel();
                statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
                statsPanel.setOpaque(false);
                inner.add(statsPanel);
                refreshTankView();

                inner.add(Box.createVerticalStrut(20));

                applyStatusStyle();
                add(inner, BorderLayout.CENTER);
            }

            private void refreshTankView() {
                TankData t = tanks.get(selectedTankIndex);
                tankNameLabel.setText(t.getName());
                tankCanvas.repaint();
                statsPanel.removeAll();
                statsPanel.add(buildFidelityStatRow("OFFENSIVE POWER", t.getOffensivePower(), false));
                statsPanel.add(Box.createVerticalStrut(10));
                statsPanel.add(buildFidelityStatRow("MOBILITY INDEX", t.getMobilityIndex(), false));
                statsPanel.revalidate();
                statsPanel.repaint();
            }

            private void toggleStatus() {
                ready = !ready;
                applyStatusStyle();
                onStatusChanged();
            }

            private void applyStatusStyle() {
                statusLbl.setVerticalAlignment(SwingConstants.CENTER);
                statusVal.setVerticalAlignment(SwingConstants.CENTER);
                if (ready) {
                    statusBtnMain.setOpaque(true);
                    statusBtnMain.setBackground(fg);
                    statusLbl.setForeground(bg);
                    statusVal.setForeground(bg);
                    statusVal.setText("[ READY ]");
                    statusBtnMain.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(fg, 2),
                            BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                    statusBtnShadow.setVisible(true);
                } else {
                    statusBtnMain.setOpaque(false);
                    statusBtnMain.setBackground(bg);
                    statusLbl.setForeground(fg);
                    statusVal.setForeground(fg);
                    statusVal.setText("[ STANDBY ]");
                    statusBtnMain.setBorder(BorderFactory.createCompoundBorder(
                            new DashedBorder(fg, 1, 4),
                            BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                    statusBtnShadow.setVisible(false);
                }
            }

            private JPanel buildFidelityStatRow(String label, double val, boolean dithered) {
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                p.setOpaque(true);
                p.setBackground(bg);
                p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(fg, 1),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)));
                p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

                JPanel top = new JPanel(new BorderLayout());
                top.setOpaque(false);
                top.add(createLabel(label, 16f), BorderLayout.WEST);
                top.add(createLabel(String.format("%.1f", val), 16f), BorderLayout.EAST);
                p.add(top);
                p.add(Box.createVerticalStrut(4));

                JPanel barContainer = new JPanel(new BorderLayout());
                barContainer.setOpaque(false);
                barContainer.setPreferredSize(new Dimension(0, 18));
                barContainer.setBorder(BorderFactory.createLineBorder(fg, 1));
                DitheredBar bar = new DitheredBar((int) val, dithered);
                bar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                barContainer.add(bar);
                p.add(barContainer);
                return p;
            }

            private JPanel createCarouselBtn(String label, Runnable action, boolean left) {
                JPanel btn = new JPanel(new BorderLayout());
                btn.setOpaque(false);
                btn.setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, left ? 0 : 1, 0, left ? 1 : 0, fg),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                JLabel lbl = createLabel(label, 28f);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                btn.add(lbl, BorderLayout.CENTER);
                btn.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        action.run();
                    }

                    public void mouseEntered(MouseEvent e) {
                        btn.setOpaque(true);
                        btn.setBackground(fg);
                        lbl.setForeground(bg);
                        btn.repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        btn.setOpaque(false);
                        lbl.setForeground(fg);
                        btn.repaint();
                    }
                });
                return btn;
            }

            private void drawTankPixelArt(Graphics2D g2, int idx, Color fgC, Color bgC) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                if (idx == 0) { // M8 GREYHOUND
                    g2.fillRect(16, 8, 8, 2);
                    g2.fillRect(14, 10, 10, 4);
                    g2.setColor(bgC);
                    g2.fillRect(15, 11, 2, 2);
                    g2.setColor(fgC);
                    g2.fillRect(24, 11, 12, 2);
                    g2.fillRect(34, 10, 2, 4);
                    g2.fillRect(11, 14, 16, 2);
                    g2.fillRect(9, 16, 20, 2);
                    g2.fillRect(7, 18, 24, 2);
                    g2.fillRect(5, 20, 28, 6);
                    g2.setColor(bgC);
                    for (int x : new int[] { 7, 11, 15, 19, 23, 27 })
                        g2.fillRect(x, 22, 2, 4);
                } else if (idx == 1) { // FLAK 88
                    g2.fillRect(12, 6, 12, 6);
                    g2.setColor(bgC);
                    g2.fillRect(14, 8, 3, 2);
                    g2.setColor(fgC);
                    g2.fillRect(24, 8, 14, 3);
                    g2.fillRect(36, 7, 2, 5);
                    g2.fillRect(8, 12, 20, 4);
                    g2.fillRect(4, 16, 28, 4);
                    g2.fillRect(2, 20, 34, 6);
                    g2.setColor(bgC);
                    for (int x : new int[] { 4, 10, 16, 22, 28 })
                        g2.fillRect(x, 22, 4, 4);
                    g2.fillRect(34, 22, 1, 4);
                } else { // BLACK CAT
                    g2.fillRect(6, 10, 28, 4);
                    g2.fillRect(4, 14, 32, 6);
                    g2.fillRect(14, 4, 12, 6);
                    g2.fillRect(26, 6, 10, 2);
                    g2.fillRect(2, 20, 36, 6);
                    g2.setColor(bgC);
                    for (int x : new int[] { 4, 12, 20, 28 })
                        g2.fillRect(x, 22, 6, 4);
                }
            }

            boolean isReady() {
                return ready;
            }

            String getPlayerName() {
                return nameField.getText().trim().isEmpty() ? "PLAYER_" + playerNum : nameField.getText().trim();
            }

            int getSelectedTankIndex() {
                return selectedTankIndex;
            }
        }
    }
}
