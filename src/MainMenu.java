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
 *   - HOME          : CanvasArea tank preview + stats sidebar
 *   - LEADERBOARD   : LeaderboardPanel
 *   - NEW_GAME      : CharacterSelectPanel
 *
 * All inner panel classes have been extracted to their own files:
 *   UIComponents.java         -> DashedBorder, DitheredBar, MainFramePanel
 *   CanvasArea.java           -> Home-screen tank preview canvas
 *   LeaderboardPanel.java     -> Leaderboard card
 *   CharacterSelectPanel.java -> NEW GAME card + PlayerColumn
 */
public class MainMenu extends JFrame {

    private Font  vt323_base;
    private Color bg = new Color(239, 243, 241);
    private Color fg = new Color(0, 0, 0);

    // Shared tank roster — passed by reference to CanvasArea and CharacterSelectPanel
    private java.util.List<TankData> tanks = new java.util.ArrayList<>();
    private int currentTankIndex = 0;

    // Home-screen components updated by the arrow buttons in updateInfoPanel()
    private JPanel     infoPanel;
    private CanvasArea canvas;
    private int        flickerStep = 0;

    // CardLayout controls which screen is currently shown
    private CardLayout cardLayout;
    private JPanel     cardContentPanel;

    // Nav item helpers — allow resetting all highlights when changing cards
    private java.util.List<JPanel>   navItemsList          = new java.util.ArrayList<>();
    private java.util.List<Runnable> navHighlightResetters = new java.util.ArrayList<>();

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
        Timer blinkTimer = new Timer(600, e -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof MainMenu) w.repaint();
            }
        });
        blinkTimer.start();

        // Load the VT323 pixel font (fallback: system Monospaced)
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
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(bg);
                g.fillRect(0, 0, getWidth(), getHeight());
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(fg);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
                for (int y = 0; y < getHeight(); y += 4)
                    for (int x = 0; x < getWidth(); x += 4)
                        g2.fillRect(x, y, 1, 1);
                g2.dispose();
            }
        };

        // Outer decorative frame (shared component from UIComponents.java)
        MainFramePanel mainFrame = new MainFramePanel();
        mainFrame.setPreferredSize(new Dimension(900, 600));
        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);

        // =====================================================================
        // HEADER
        // =====================================================================
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
                "<html><p align='right' style='line-height:0.8'>LOCATION: CAMP 30<br>TIME: 19:04:25</p></html>", 16f);
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(systemStatus, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        mainFrame.add(headerPanel, gbc);

        // =====================================================================
        // LEFT SIDEBAR NAV
        // =====================================================================
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, fg),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        String[] navNames = { "NEW GAME", "HOW TO PLAY", "LEADERBOARD", "SAVE/LOAD", "TERMINATE" };
        String[] navIds   = { "01",       "02",          "03",          "04",        "05" };

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
        navItemsList.add(homeBtn);
        sidebar.add(homeBtn);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.weightx = 0.0; gbc.weighty = 1.0;
        sidebar.setPreferredSize(new Dimension(220, 0));
        mainFrame.add(sidebar, gbc);

        // =====================================================================
        // HOME PANEL (canvas + info sidebar) — first card in CardLayout
        // =====================================================================
        canvas = new CanvasArea(tanks, () -> currentTankIndex, vt323_base);
        canvas.setOpaque(false);
        canvas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                new DashedBorder(fg, 1, 4)));

        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, fg),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        updateInfoPanel();
        infoPanel.setPreferredSize(new Dimension(185, 0));

        JPanel homePanel = new JPanel(new GridBagLayout());
        homePanel.setOpaque(false);
        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.fill = GridBagConstraints.BOTH;
        hgbc.gridx = 0; hgbc.gridy = 0; hgbc.weightx = 1.0; hgbc.weighty = 1.0;
        homePanel.add(canvas, hgbc);
        hgbc.gridx = 1; hgbc.weightx = 0.0;
        homePanel.add(infoPanel, hgbc);

        // =====================================================================
        // CARD LAYOUT (HOME / LEADERBOARD / NEW_GAME)
        // =====================================================================
        cardLayout = new CardLayout();
        cardContentPanel = new JPanel(cardLayout);
        cardContentPanel.setOpaque(false);

        cardContentPanel.add(homePanel,                                    "HOME");
        cardContentPanel.add(new LeaderboardPanel(vt323_base),             "LEADERBOARD");
        cardContentPanel.add(new CharacterSelectPanel(tanks, vt323_base),  "NEW_GAME");

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        mainFrame.add(cardContentPanel, gbc);

        // =====================================================================
        // FOOTER
        // =====================================================================
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, fg),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        footerPanel.add(createLabel("CREATED BY ANDREW FILSON", 16f), BorderLayout.WEST);

        JLabel rightFooterLabel = createLabel("PRESS [ENTER] TO INITIALIZE", 16f);
        rightFooterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        footerPanel.add(rightFooterLabel, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        mainFrame.add(footerPanel, gbc);

        rootPanel.add(mainFrame);
        add(rootPanel);

        // Live clock (updates system status label every second)
        Timer timeTaker = new Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            systemStatus.setText("<html><p align='right' style='line-height:0.8'>LOCATION: CAMP 30<br>TIME: "
                    + time + "</p></html>");
        });
        timeTaker.start();

        // Subtle opacity flicker animation on the main frame
        Timer flickerAct = new Timer(100, e -> {
            flickerStep = (flickerStep + 1) % 40;
            float alpha = 1.0f;
            if (flickerStep == 5)  alpha = 0.95f;
            if (flickerStep == 7)  alpha = 0.98f;
            mainFrame.setOpacity(alpha);
            mainFrame.repaint();
        });
        flickerAct.start();
    }

    // =========================================================================
    // LABEL FACTORY
    // =========================================================================

    private JLabel createLabel(String txt, float fontSize) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(vt323_base.deriveFont(fontSize));
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        return lbl;
    }

    // =========================================================================
    // NAV ITEM FACTORY
    // =========================================================================

    private JPanel createNavItem(String title, String num) {
        JPanel panel = new JPanel(new BorderLayout());

        // Tooltip for each nav item
        if      (title.equals("NEW GAME"))    panel.setToolTipText("Start a new game session");
        else if (title.equals("HOW TO PLAY")) panel.setToolTipText("View gameplay instructions");
        else if (title.equals("LEADERBOARD")) panel.setToolTipText("View global high scores");
        else if (title.equals("SAVE/LOAD"))   panel.setToolTipText("Save or load your game state");
        else if (title.equals("TERMINATE"))   panel.setToolTipText("Exit the application");

        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel leftLbl  = createLabel("□ " + title, 24f);
        JLabel rightLbl = createLabel(num, 24f);
        panel.add(leftLbl,  BorderLayout.WEST);
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
            public void focusGained(FocusEvent e) { highlight.run(); }
            public void focusLost(FocusEvent e)   { unhighlight.run(); }
        });

        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { handleNavClick(title); }
        });

        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { highlight.run(); }
            public void mouseExited(MouseEvent e)  { if (!isNavActive(title)) unhighlight.run(); }
            public void mousePressed(MouseEvent e) { handleNavClick(title); }
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
        for (Runnable r : navHighlightResetters) r.run();

        if      (title.equals("NEW GAME"))    cardLayout.show(cardContentPanel, "NEW_GAME");
        else if (title.equals("LEADERBOARD")) cardLayout.show(cardContentPanel, "LEADERBOARD");
        else if (title.equals("SAVE/LOAD"))   handleSaveLoad();
        else if (title.equals("TERMINATE"))   System.exit(0);
        else                                  cardLayout.show(cardContentPanel, "HOME");
    }

    private void handleSaveLoad() {
        Object[] options = { "Save", "Load", "Cancel" };
        int n = JOptionPane.showOptionDialog(this,
                "Would you like to Save or Load a game file?",
                "SAVE/LOAD",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[2]);

        if (n == JOptionPane.YES_OPTION) {
            DataManager.saveGame(this, new String[]{ "No actual game data to save from menu yet" });
        } else if (n == JOptionPane.NO_OPTION) {
            DataManager.loadGame(this);
        }
    }

    // =========================================================================
    // HOME / INFO PANEL BUILDERS
    // =========================================================================

    private void updateInfoPanel() {
        if (infoPanel == null) return;
        infoPanel.removeAll();

        TankData currentTank = tanks.get(currentTankIndex);
        infoPanel.add(createStatBox("OFFENSIVE POWER", String.format("%.1f", currentTank.getOffensivePower()), (int) currentTank.getOffensivePower(), false));
        infoPanel.add(Box.createVerticalStrut(16));
        infoPanel.add(createStatBox("MOBILITY INDEX",  String.format("%.1f", currentTank.getMobilityIndex()),  (int) currentTank.getMobilityIndex(),  false));
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
            for (TankData t : tanks) t.rerollStats();
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

        Runnable hl  = () -> { panel.setOpaque(true); panel.setBackground(fg); lbl.setForeground(bg); panel.repaint(); };
        Runnable uhl = () -> { panel.setOpaque(false); lbl.setForeground(fg); panel.repaint(); };

        panel.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { hl.run(); }
            public void focusLost(FocusEvent e)   { uhl.run(); }
        });
        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (action != null) action.run(); }
        });
        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hl.run(); }
            public void mouseExited(MouseEvent e)  { if (!panel.hasFocus()) uhl.run(); }
            public void mousePressed(MouseEvent e) { if (action != null) action.run(); }
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
                        BorderFactory.createLineBorder(fg, 1),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)));
                setToolTipText("Randomize tank unit specifications");
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFocusable(true);

                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { isHovered = true; setOpaque(true); setBackground(fg); repaint(); }
                    public void focusLost(FocusEvent e)   { isHovered = false; setOpaque(false); repaint(); }
                });
                getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
                getActionMap().put("onEnter", new AbstractAction() {
                    public void actionPerformed(ActionEvent e) { if (action != null) action.run(); }
                });
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { isHovered = true; setOpaque(true); setBackground(fg); repaint(); }
                    public void mouseExited(MouseEvent e)  { if (!hasFocus()) { isHovered = false; setOpaque(false); repaint(); } }
                    public void mousePressed(MouseEvent e) { if (action != null) action.run(); }
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
                // Refresh icon (two curved arrow shapes)
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
}
