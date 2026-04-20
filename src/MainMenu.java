import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The initial landing menu of the 'BIT-REKT' application, implemented in a retro,
 * pixel-art style using native Java 2D Graphics and Swing Components.
 * 
 * It manages the primary navigation options (e.g., New Game, How to Play, Leaderboard)
 * and controls transitioning into the main game window state.
 */
public class MainMenu extends JFrame {

    private Font vt323_base;

    private Color bg = new Color(239, 243, 241); // #eff3f1
    private Color fg = new Color(0, 0, 0); // #000000

    public MainMenu() {
        setTitle("BIT-REKT");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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

        // Dither Background Overlay Panel
        // LEARNING: By overriding 'paintComponent', we can draw custom graphics 
        // directly onto the panel before any standard Swing components are drawn.
        JPanel rootPanel = new JPanel(new GridBagLayout()) { // Use GridBag to center the mainframe
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(bg);
                g.fillRect(0, 0, getWidth(), getHeight());

                // Radial dither bg (opacity 0.05, 4px scale)
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
        // LEARNING: GridBagConstraints dictate exactly how a component should behave 
        // within a GridBagLayout (e.g., should it stretch? anchor left? take up 2 columns?).
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // Tell components to fill their available grid space
        gbc.insets = new Insets(6, 6, 6, 6); // Add a 6-pixel margin around components

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, fg));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel subTitle = createLabel("HEAVY ARMORED DIVISION", 12f);
        subTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JLabel title = createLabel("PANZER-BIT", 48f);
        // Reduce spacing between lines to match CSS line-height 0.8
        titleBlock.add(subTitle);
        titleBlock.add(Box.createVerticalStrut(-5));
        titleBlock.add(title);

        JLabel systemStatus = createLabel(
                "<html><p align='right' style='line-height:1'>LOC: SECTOR_G4<br>NET: ENCRYPTED<br>VER: 1.0.4-STABLE</p></html>",
                14f);
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
                BorderFactory.createEmptyBorder(10, 0, 10, 12)));

        String[] navNames = {"NEW GAME", "HOW TO PLAY", "LEADERBOARD", "BONUS", "TERMINATE"};
        String[] navIds = { "01", "02", "03", "04", "05" };

        for (int i = 0; i < navNames.length; i++) {
            JPanel navItem = createNavItem(navNames[i], navIds[i]);
            sidebar.add(navItem);
            sidebar.add(Box.createVerticalStrut(8));
        }

        // CPU block bottom of sidebar
        sidebar.add(Box.createVerticalGlue());
        JPanel cpuLoadBlock = new JPanel(new BorderLayout());
        cpuLoadBlock.setOpaque(false);
        cpuLoadBlock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JPanel cpuLabels = new JPanel(new BorderLayout());
        cpuLabels.setOpaque(false);
        cpuLabels.add(createLabel("CPU_LOAD", 12f), BorderLayout.WEST);
        cpuLabels.add(createLabel("42%", 12f), BorderLayout.EAST);
        cpuLoadBlock.add(cpuLabels, BorderLayout.NORTH);

        DitheredBar cpuBar = new DitheredBar(42, true);
        cpuBar.setPreferredSize(new Dimension(200, 12));
        cpuLoadBlock.add(cpuBar, BorderLayout.SOUTH);
        sidebar.add(cpuLoadBlock);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        // Fix grid size to exactly 240px (- offsets)
        sidebar.setPreferredSize(new Dimension(220, 0));
        mainFrame.add(sidebar, gbc);

        // CENTER CANVAS
        CanvasArea canvas = new CanvasArea();
        canvas.setOpaque(false);
        canvas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                new DashedBorder(fg, 1, 4) // Emulating 1px dashed
        ));

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        mainFrame.add(canvas, gbc);

        // RIGHT INFO PANEL
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, fg),
                BorderFactory.createEmptyBorder(10, 12, 10, 0)));

        infoPanel.add(createStatBox("OFFENSIVE POWER", "88.4", 88, false));
        infoPanel.add(Box.createVerticalStrut(16));
        infoPanel.add(createStatBox("ARMOR DENSITY", "62.1", 62, true));
        infoPanel.add(Box.createVerticalStrut(16));
        infoPanel.add(createStatBox("MOBILITY INDEX", "45.9", 45, false));

        infoPanel.add(Box.createVerticalGlue());

        JPanel sysLogs = new JPanel(new BorderLayout());
        sysLogs.setOpaque(false);
        JLabel sysLogsLabel = createLabel("SYSTEM LOGS", 10f);
        sysLogsLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        String logText = "<html>&gt; CALIBRATING OPTICS<br>&gt; FUEL CELL: OPTIMAL<br>&gt; RADAR: ACTIVE</html>";
        JLabel logsContent = createLabel(logText, 10f);
        sysLogs.add(sysLogsLabel, BorderLayout.NORTH);
        sysLogs.add(logsContent, BorderLayout.CENTER);
        infoPanel.add(sysLogs);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        infoPanel.setPreferredSize(new Dimension(185, 0));
        mainFrame.add(infoPanel, gbc);

        // FOOTER
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, fg));
        footerPanel.add(createLabel("CREATED BY ANDREW FILSON", 14f), BorderLayout.WEST);

        JLabel rightFooterLabel = createLabel("PRESS [ENTER] TO INITIALIZE", 14f);
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

        // Setup ticking for System Status Time
        Timer t = new Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            systemStatus
                    .setText("<html><p align='right' style='line-height:0.8'>LOC: SECTOR_G4<br>NET: ENCRYPTED<br>TME: "
                            + time + "</p></html>");
        });
        t.start();

        Timer flicker = new Timer(4000, null); // 4s animation step
        flicker.addActionListener(new ActionListener() {
            int step = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                step = (step + 1) % 4; // Emulate flicker steps
                float alpha = 1.0f;
                if (step == 1)
                    alpha = 0.95f;
                else if (step == 3)
                    alpha = 0.9f;
                mainFrame.setOpacity(alpha);
                mainFrame.repaint();
            }
        });
        flicker.start();

        // Add Enter Key Functionality
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    setVisible(false);
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainWindow());
                }
            }
        });

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
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel leftLbl = createLabel("□ " + title, 24f);
        JLabel rightLbl = createLabel(num, 24f);
        panel.add(leftLbl, BorderLayout.WEST);
        panel.add(rightLbl, BorderLayout.EAST);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setOpaque(true);
                panel.setBackground(fg);
                leftLbl.setForeground(bg);
                leftLbl.setText("■ " + title);
                rightLbl.setForeground(bg);
                panel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setOpaque(false);
                leftLbl.setForeground(fg);
                leftLbl.setText("□ " + title);
                rightLbl.setForeground(fg);
                panel.repaint();
            }

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

    private JPanel createStatBox(String labelTxt, String valTxt, int percentage, boolean dithered) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        JLabel lbl = createLabel(labelTxt, 12f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        // Force left alignment
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(lbl);

        JLabel val = createLabel(valTxt, 24f);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        val.setFont(val.getFont().deriveFont(Font.BOLD)); // CSS font-weight: bold
        box.add(val);

        DitheredBar bar = new DitheredBar(percentage, dithered);
        bar.setPreferredSize(new Dimension(150, 12));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(bar);

        return box;
    }

    // A panel replacing the standard outline and corner brackets
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
            g2.drawRect(4, 4, getWidth() - 8, getHeight() - 8); // inner border

            g2.setStroke(new BasicStroke(1));
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1); // outline

            // Corner brackets
            g2.setStroke(new BasicStroke(2));
            int bSize = 15;
            // top-left (left and top borders extending inward)
            g2.drawLine(-10, -10, bSize - 10, -10);
            g2.drawLine(-10, -10, -10, bSize - 10);

            // Replicating actual HTML offsets strictly
            // Top-Left: top -10, left -10
            drawBracket(g2, -10, -10, bSize, true, true);
            // Top-Right: top -10, right -10 => w - bSize + 10
            drawBracket(g2, getWidth() + 10 - bSize, -10, bSize, true, false);
            // Bottom-Left
            drawBracket(g2, -10, getHeight() + 10 - bSize, bSize, false, true);
            // Bottom-Right
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

            // Center of Canvas
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            // -- Draw Shadow --
            // conic-gradient opacity 0.4
            int sw = 220;
            int sh = 40;
            int sx = cx - sw / 2;
            int sy = getHeight() - 60; // bottom 40px equivalent inside canvas

            BufferedImage shadowDither = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg = shadowDither.createGraphics();
            sg.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 102)); // ~0.4 opacity
            sg.fillRect(0, 0, 2, 2);
            sg.fillRect(2, 2, 2, 2);
            sg.dispose();

            g2.setPaint(new TexturePaint(shadowDither, new Rectangle(0, 0, 4, 4)));
            g2.fillRect(sx, sy, sw, sh);

            // -- Draw SVG path Tank (Size 300x300, native 64x64 format)
            g2.translate(cx - 150, cy - 150); // Center the 300px tank
            g2.scale(4.6875, 4.6875);
            g2.setColor(fg);

            // Transcribing SVG exact coordinates:
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

            g2.dispose();

            // -- Absolute Element overlays --
            Graphics2D gText = (Graphics2D) g.create();
            gText.setColor(fg);
            gText.setFont(vt323_base.deriveFont(10f));

            // Top Right
            String sText = "SCANNING...";
            String idText = "ID: TKN-88";
            FontMetrics fm = gText.getFontMetrics();
            int topRy = 20;
            gText.drawString(sText, getWidth() - 10 - fm.stringWidth(sText), topRy);
            gText.fillRect(getWidth() - 10 - 40, topRy + 4, 40, 1);
            gText.drawString(idText, getWidth() - 10 - fm.stringWidth(idText), topRy + 15);

            // Bottom Left
            int btLx = 20;
            int btLy = getHeight() - 30; // Equivalent to bottom 20px CSS
            gText.drawString("ENGINE_CORE", btLx, btLy);
            gText.fillRect(btLx, btLy + 2, fm.stringWidth("ENGINE_CORE"), 1); // border bottom
            gText.drawString("[|||||||||||.....]", btLx, btLy + 15);
            gText.dispose();
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
                // Repeating linear gradient emulation (45deg lines)
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

    // Helper border to emulate dashed CSS lines
    class DashedBorder extends javax.swing.border.AbstractBorder {
        private Color color;
        private int thickness;
        private int dashLength;

        public DashedBorder(Color c, int t, int l) {
            this.color = c;
            this.thickness = t;
            this.dashLength = l;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                    new float[] { dashLength }, 0));
            g2.drawRect(x, y, width - thickness, height - thickness);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = thickness;
            return insets;
        }
    }
}
