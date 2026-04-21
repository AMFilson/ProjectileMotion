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

    public MainMenu() {
        setTitle("BIT-REKT");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tanks.add(new TankData("M8 GREYHOUND"));
        tanks.add(new TankData("FLAK 88"));
        tanks.add(new TankData("BLACK CAT"));

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

        String[] navNames = { "NEW GAME", "HOW TO PLAY", "LEADERBOARD", "BONUS", "TERMINATE" };
        String[] navIds = { "01", "02", "03", "04", "05" };

        for (int i = 0; i < navNames.length; i++) {
            JPanel navItem = createNavItem(navNames[i], navIds[i]);
            if (i == 0) {
                // Request focus for NEW GAME on launch
                SwingUtilities.invokeLater(() -> navItem.requestFocusInWindow());
            }
            sidebar.add(navItem);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());
        JPanel expBlock = new JPanel(new BorderLayout());
        expBlock.setOpaque(false);
        expBlock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        expBlock.setToolTipText("Your current level and pilot proficiency");
        JPanel expLabels = new JPanel(new BorderLayout());
        expLabels.setOpaque(false);
        expLabels.add(createLabel("CURRENT LVL", 12f), BorderLayout.WEST);
        expLabels.add(createLabel("L_12", 12f), BorderLayout.EAST);
        expBlock.add(expLabels, BorderLayout.NORTH);

        DitheredBar expBar = new DitheredBar(65, true);
        expBar.setPreferredSize(new Dimension(200, 12));
        expBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        expBlock.add(expBar, BorderLayout.SOUTH);
        sidebar.add(expBlock);

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

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        mainFrame.add(canvas, gbc);

        // RIGHT INFO PANEL
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, fg),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        updateInfoPanel();

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        infoPanel.setPreferredSize(new Dimension(185, 0));
        mainFrame.add(infoPanel, gbc);

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
            systemStatus.setText("<html><p align='right' style='line-height:0.8'>LOCATION: CAMP 30<br>TIME: " + time + "</p></html>");
        });
        timeTaker.start();

        Timer flickerAct = new Timer(100, e -> {
            flickerStep = (flickerStep + 1) % 40;
            float alpha = 1.0f;
            if (flickerStep == 5) alpha = 0.95f;
            if (flickerStep == 7) alpha = 0.98f;
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
        if (title.equals("NEW GAME")) panel.setToolTipText("Start a new game session");
        else if (title.equals("HOW TO PLAY")) panel.setToolTipText("View gameplay instructions");
        else if (title.equals("LEADERBOARD")) panel.setToolTipText("View global high scores");
        else if (title.equals("BONUS")) panel.setToolTipText("Unlockable content and extras");
        else if (title.equals("TERMINATE")) panel.setToolTipText("Exit the application");

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

        panel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { highlight.run(); }
            @Override
            public void focusLost(FocusEvent e) { unhighlight.run(); }
        });

        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (title.equals("NEW GAME")) {
                    setVisible(false);
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainWindow());
                } else if (title.equals("TERMINATE")) {
                    System.exit(0);
                }
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { highlight.run(); }
            @Override
            public void mouseExited(MouseEvent e) { if (!panel.hasFocus()) unhighlight.run(); }
            @Override
            public void mousePressed(MouseEvent e) {
                if (title.equals("NEW GAME")) {
                    setVisible(false);
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainWindow());
                } else if (title.equals("TERMINATE")) {
                    System.exit(0);
                }
            }
        });
        return panel;
    }

    private void updateInfoPanel() {
        if (infoPanel == null) return;
        infoPanel.removeAll();
        
        TankData currentTank = tanks.get(currentTankIndex);
        
        String opStr = String.format("%.1f", currentTank.getOffensivePower());
        infoPanel.add(createStatBox("OFFENSIVE POWER", opStr, (int)currentTank.getOffensivePower(), false));
        infoPanel.add(Box.createVerticalStrut(16));
        
        String miStr = String.format("%.1f", currentTank.getMobilityIndex());
        infoPanel.add(createStatBox("MOBILITY INDEX", miStr, (int)currentTank.getMobilityIndex(), false));
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

        Runnable hl = () -> { panel.setOpaque(true); panel.setBackground(fg); lbl.setForeground(bg); panel.repaint(); };
        Runnable uhl = () -> { panel.setOpaque(false); lbl.setForeground(fg); panel.repaint(); };

        panel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { hl.run(); }
            @Override
            public void focusLost(FocusEvent e) { uhl.run(); }
        });

        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
        panel.getActionMap().put("onEnter", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (action != null) action.run(); }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hl.run(); }
            @Override public void mouseExited(MouseEvent e) { if (!panel.hasFocus()) uhl.run(); }
            @Override public void mousePressed(MouseEvent e) { if (action != null) action.run(); }
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
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(fg, 1), BorderFactory.createEmptyBorder(8, 16, 8, 16)));
                setToolTipText("Randomize tank unit specifications");
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFocusable(true);

                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { isHovered = true; setOpaque(true); setBackground(fg); repaint(); }
                    @Override public void focusLost(FocusEvent e) { isHovered = false; setOpaque(false); repaint(); }
                });

                getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "onEnter");
                getActionMap().put("onEnter", new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent e) { if (action != null) action.run(); }
                });

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; setOpaque(true); setBackground(fg); repaint(); }
                    @Override public void mouseExited(MouseEvent e) { if (!hasFocus()) { isHovered = false; setOpaque(false); repaint(); } }
                    @Override public void mousePressed(MouseEvent e) { if (action != null) action.run(); }
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
                int[] x1 = {3, 3, 2, 2, 1, 1, 3, 3, 8, 8, 4, 4, 6, 6, 5, 5, 4, 4, 3};
                int[] y1 = {2, 3, 3, 4, 4, 5, 5, 13, 13, 12, 12, 5, 5, 4, 4, 3, 3, 2, 2};
                g2.fillPolygon(x1, y1, 19);
                int[] x2 = {7, 7, 12, 12, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 13, 13, 7};
                int[] y2 = {3, 4, 4, 11, 11, 12, 12, 13, 13, 14, 14, 13, 13, 12, 12, 11, 11, 3, 3};
                g2.fillPolygon(x2, y2, 19);
                g2.dispose();
            }
        };
        return panel;
    }

    class MainFramePanel extends JPanel {
        private float opacity = 1.0f;
        public void setOpacity(float o) { this.opacity = o; }
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
            if (top && left) { g.fillRect(x, y, size, 2); g.fillRect(x, y, 2, size); }
            else if (top && !left) { g.fillRect(x, y, size, 2); g.fillRect(x + size - 2, y, 2, size); }
            else if (!top && left) { g.fillRect(x, y + size - 2, size, 2); g.fillRect(x, y, 2, size); }
            else if (!top && !left) { g.fillRect(x, y + size - 2, size, 2); g.fillRect(x + size - 2, y, 2, size); }
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
                g2.fillRect(10, 44, 44, 10); g2.fillRect(12, 42, 40, 2);
                g2.setColor(bg);
                g2.fillRect(14, 46, 4, 6); g2.fillRect(22, 46, 4, 6); g2.fillRect(30, 46, 4, 6); g2.fillRect(38, 46, 4, 6); g2.fillRect(46, 46, 4, 6);
                g2.setColor(fg);
                g2.fillRect(14, 34, 36, 10); g2.fillRect(18, 32, 28, 2); g2.fillRect(22, 24, 20, 8); g2.fillRect(24, 22, 16, 2); g2.fillRect(42, 26, 18, 4); g2.fillRect(58, 25, 2, 6); g2.fillRect(26, 20, 8, 2);
                g2.setColor(bg); g2.fillRect(24, 24, 2, 2); g2.fillRect(26, 26, 2, 2);
            } else if (currentTankIndex == 1) {
                // FLAK 88
                g2.fillRect(24, 48, 16, 6); g2.fillRect(18, 46, 28, 2); g2.fillRect(28, 38, 8, 8); g2.fillRect(30, 32, 4, 6); g2.fillRect(32, 20, 4, 16); g2.fillRect(34, 12, 4, 10); g2.fillRect(36, 4, 4, 10); g2.fillRect(38, -4, 2, 8);
            } else if (currentTankIndex == 2) {
                // BLACK CAT
                g2.fillRect(6, 46, 52, 8);
                g2.setColor(bg);
                g2.fillRect(10, 48, 6, 4); g2.fillRect(22, 48, 6, 4); g2.fillRect(34, 48, 6, 4); g2.fillRect(46, 48, 6, 4);
                g2.setColor(fg);
                g2.fillRect(10, 38, 44, 8); g2.fillRect(14, 34, 34, 4); g2.fillRect(16, 24, 24, 10); g2.fillRect(40, 28, 20, 2); g2.fillRect(58, 27, 4, 4);
            }
            g2.dispose();
        }
    }

    class DitheredBar extends JPanel {
        private int percent;
        private boolean dithered;
        public DitheredBar(int percent, boolean dithered) { this.percent = percent; this.dithered = dithered; setBorder(BorderFactory.createLineBorder(fg, 1)); setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = (int) (getWidth() * (percent / 100.0));
            Graphics2D g2 = (Graphics2D) g.create();
            if (dithered) {
                BufferedImage fillImg = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
                Graphics2D fG = fillImg.createGraphics();
                fG.setColor(fg); fG.drawLine(0, 2, 2, 0); fG.drawLine(2, 4, 4, 2); fG.dispose();
                g2.setPaint(new TexturePaint(fillImg, new Rectangle(0, 0, 4, 4)));
            } else { g2.setColor(fg); }
            g2.fillRect(0, 0, width, getHeight());
            g2.dispose();
        }
    }

    class DashedBorder extends javax.swing.border.AbstractBorder {
        private Color color;
        private int thickness;
        private int dashLength;
        public DashedBorder(Color color, int thickness, int dashLength) { this.color = color; this.thickness = thickness; this.dashLength = dashLength; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            float[] dash = {dashLength};
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2.drawRect(x, y, width - 1, height - 1);
            g2.dispose();
        }
    }
}
