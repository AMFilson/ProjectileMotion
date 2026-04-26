/* 
 * Name:    UIComponents.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 24th 2026
 * Desc:    Shared custom Swing components and borders used throughout the application.
 */

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/* 
 * =========================================================================
 * LEARNING: INHERITANCE & STATIC UTILITIES
 * =========================================================================
 * 
 * Inheritance (extends):
 * You'll see classes here using the 'extends' keyword (e.g., 'class MainFramePanel 
 * extends JPanel'). Inheritance is a core OOP concept where a new class takes on 
 * (inherits) the properties and methods of an existing class. By extending JPanel, 
 * MainFramePanel *is* a JPanel, but we can add our own custom drawing logic on 
 * top of it while keeping all of JPanel's default behavior!
 * 
 * Static Utility Methods:
 * The 'createLabel' method is marked as 'public static'. The 'static' keyword 
 * means the method belongs to the UIComponents class itself, rather than to any 
 * specific instance of it. This allows us to call UIComponents.createLabel(...) 
 * from anywhere in our project without having to create a 'new UIComponents()' 
 * object first. It's a great way to build a library of shared tools!
 * =========================================================================
 */

public class UIComponents {
    public static final Color THEME_BACKGROUND = new Color(239, 243, 241);
    public static final Color THEME_FOREGROUND = new Color(0, 0, 0);
    public static final Color THEME_ERROR = new Color(200, 0, 0);
    public static final Color THEME_HIGHLIGHT = new Color(100, 100, 100);
    public static final Color THEME_PANEL_BG = new Color(245, 245, 245);

    /**
     * Creates a standardized JLabel with the specified text, font, and size.
     * It sets the foreground color to THEME_FOREGROUND and makes it transparent.
     */
    public static JLabel createLabel(String text, Font font, float fontSize) {
        JLabel label = new JLabel(text);
        if (font != null) {
            label.setFont(font.deriveFont(fontSize));
        }
        label.setForeground(THEME_FOREGROUND);
        label.setOpaque(false);
        return label;
    }

    /**
     * A custom Icon class that draws pixel-art style icons.
     */
    public static class PixelIcon implements Icon {
        private String type;
        private int size = 48;

        public PixelIcon(String type) {
            this.type = type;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Draw pixelated border box
            g2.setColor(THEME_FOREGROUND);
            g2.fillRect(x, y, size, size);
            g2.setColor(THEME_BACKGROUND);
            g2.fillRect(x + 4, y + 4, size - 8, size - 8);

            g2.setColor(THEME_FOREGROUND);
            if (type.equals("?")) {
                int[][] pixels = {
                        { 0, 0, 1, 1, 1, 0, 0 },
                        { 0, 1, 0, 0, 0, 1, 0 },
                        { 0, 0, 0, 0, 0, 1, 0 },
                        { 0, 0, 0, 1, 1, 0, 0 },
                        { 0, 0, 1, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 1, 0, 0, 0, 0 }
                };
                drawPixels(g2, x + 10, y + 10, pixels, 4);
            } else if (type.equals("!")) {
                int[][] pixels = {
                        { 0, 1, 1, 0 },
                        { 0, 1, 1, 0 },
                        { 0, 1, 1, 0 },
                        { 0, 1, 1, 0 },
                        { 0, 0, 0, 0 },
                        { 0, 1, 1, 0 }
                };
                drawPixels(g2, x + 14, y + 10, pixels, 5);
            }
            g2.dispose();
        }

        private void drawPixels(Graphics2D g, int x, int y, int[][] pixels, int scale) {
            for (int r = 0; r < pixels.length; r++) {
                for (int c = 0; c < pixels[r].length; c++) {
                    if (pixels[r][c] == 1) {
                        g.fillRect(x + c * scale, y + r * scale, scale, scale);
                    }
                }
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    /**
     * Displays a customized, themed dialog box that matches the BIT-REKT aesthetic.
     */
    public static int showThemedDialog(Component parent, String message, String title, String[] options,
            String iconType, Font font) {
        final int[] result = { -1 };
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(parentWindow, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(THEME_BACKGROUND);
        root.setBorder(BorderFactory.createLineBorder(THEME_FOREGROUND, 4));

        // Custom Header/Title Bar
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        header.setBackground(THEME_FOREGROUND);
        JLabel titleLbl = createLabel(title.toUpperCase(), font, 14f);
        titleLbl.setForeground(THEME_BACKGROUND);
        header.add(titleLbl);
        root.add(header, BorderLayout.NORTH);

        // Body Content
        JPanel body = new JPanel(new BorderLayout(20, 10));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        if (iconType != null) {
            body.add(new JLabel(new PixelIcon(iconType)), BorderLayout.WEST);
        }

        JLabel msgLbl = createLabel("<html><div style='text-align: center;'>" + message + "</div></html>", font, 18f);
        msgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(msgLbl, BorderLayout.CENTER);

        // Custom Button Area
        JPanel btnArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnArea.setOpaque(false);
        btnArea.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        for (int i = 0; i < options.length; i++) {
            final int index = i;
            JPanel btn = createThemedButton(options[i].toUpperCase(), () -> {
                result[0] = index;
                dialog.dispose();
            }, font);
            btnArea.add(btn);
        }
        body.add(btnArea, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);
        dialog.add(root);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    private static JPanel createThemedButton(String label, Runnable action, Font font) {
        JPanel btn = new JPanel(new BorderLayout());
        btn.setOpaque(true);
        btn.setBackground(THEME_FOREGROUND);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(THEME_FOREGROUND, 2),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = createLabel(label, font, 18f);
        lbl.setForeground(THEME_BACKGROUND);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        btn.add(lbl, BorderLayout.CENTER);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(THEME_BACKGROUND);
                lbl.setForeground(THEME_FOREGROUND);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(THEME_FOREGROUND);
                lbl.setForeground(THEME_BACKGROUND);
            }
        };
        btn.addMouseListener(ma);

        return btn;
    }
}

/**
 * UIComponents.java
 *
 * A shared library of reusable low-level Swing components used across
 * BIT-REKT's screens (MainMenu, MainWindow, CharacterSelectPanel, etc.).
 *
 * Extracting these prevents code duplication — previously DashedBorder and
 * MainFramePanel existed as private copies in both MainMenu and MainWindow.
 *
 * Classes defined here:
 *   - DashedBorder    : Custom AbstractBorder that draws a dashed rectangle.
 *   - DitheredBar     : A JPanel progress bar with an optional diagonal hatch fill.
 *   - MainFramePanel  : A JPanel that draws BIT-REKT's decorative outer frame.
 */

// =============================================================================
// DASHED BORDER
// =============================================================================

/**
 * A custom Swing border that draws a single dashed/dotted rectangle outline.
 *
 * Usage:
 * component.setBorder(new DashedBorder(Color.BLACK, 1, 4));
 *
 * Parameters:
 * color - The colour of the dashes.
 * thickness - Stroke width in pixels (1 = hairline).
 * dashLength - Length of each dash segment in pixels.
 */
class DashedBorder extends javax.swing.border.AbstractBorder {
    private Color color;
    private int thickness;
    private int dashLength;

    public DashedBorder(Color color, int thickness, int dashLength) {
        this.color = color;
        this.thickness = thickness;
        this.dashLength = dashLength;
    }

    /**
     * Paints the dashed border around the component's bounds.
     *
     * @param component The component whose border is being painted.
     * @param graphics  The Graphics context provided by Swing's paint system.
     * @param originX   The left edge of the border area.
     * @param originY   The top edge of the border area.
     * @param width     The total width of the border area.
     * @param height    The total height of the border area.
     */
    @Override
    public void paintBorder(Component component, Graphics graphics, int originX, int originY, int width, int height) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setColor(color);
        // dashPattern = {dashLength} means: dashLength pixels ON, dashLength pixels OFF
        // (repeating)
        float[] dashPattern = { dashLength };
        graphics2d.setStroke(
                new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        // Draw rect subtracting 1 to stay inside the component's bounds
        graphics2d.drawRect(originX, originY, width - 1, height - 1);
        graphics2d.dispose();
    }
}

// =============================================================================
// DITHERED BAR
// =============================================================================

/**
 * A custom JPanel that renders a horizontal progress bar.
 *
 * When dithered=false: fills a solid black rectangle proportional to percent.
 * When dithered=true: fills with a diagonal hatch pattern (TexturePaint).
 *
 * Usage:
 * DitheredBar bar = new DitheredBar(75, false); // 75% solid fill
 * DitheredBar bar = new DitheredBar(45, true); // 45% dithered/hatch fill
 */
class DitheredBar extends JPanel {
    private int percent;
    private boolean dithered;

    // FILL_COLOR is the colour used to draw the filled portion of the bar.
    // Hardcoded to match the BIT-REKT palette (pure black).
    private static final Color FILL_COLOR = UIComponents.THEME_FOREGROUND;

    public DitheredBar(int percent, boolean dithered) {
        this.percent = percent;
        this.dithered = dithered;
        setBorder(BorderFactory.createLineBorder(FILL_COLOR, 1));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        // Calculate how many pixels wide the filled portion should be
        int fillWidth = (int) (getWidth() * (percent / 100.0));
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        if (dithered) {
            // Create a 4x4 tile image with a diagonal line for the hatch pattern
            BufferedImage hatchTileImage = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
            Graphics2D hatchGraphics = hatchTileImage.createGraphics();
            hatchGraphics.setColor(FILL_COLOR);
            hatchGraphics.drawLine(0, 2, 2, 0); // top diagonal
            hatchGraphics.drawLine(2, 4, 4, 2); // bottom diagonal
            hatchGraphics.dispose();
            graphics2d.setPaint(new TexturePaint(hatchTileImage, new Rectangle(0, 0, 4, 4)));
        } else {
            graphics2d.setColor(FILL_COLOR);
        }
        graphics2d.fillRect(0, 0, fillWidth, getHeight());
        graphics2d.dispose();
    }
}

// =============================================================================
// MAIN FRAME PANEL
// =============================================================================

/**
 * A custom JPanel that draws BIT-REKT's signature decorative border frame.
 *
 * Renders:
 * - Filled background in the app's off-white (#eff3f1)
 * - Outer thin hairline rectangle
 * - Inner 4px thick rectangle (inset by 4px on all sides)
 * - L-shaped corner bracket accents at each corner
 * - Optional opacity (used for the flicker animation in MainMenu)
 *
 * Usage:
 * MainFramePanel frame = new MainFramePanel();
 * frame.setPreferredSize(new Dimension(900, 600));
 * frame.setLayout(new BorderLayout());
 */
class MainFramePanel extends JPanel {
    // Opacity support for the flicker animation effect in MainMenu
    private float opacity = 1.0f;

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2d = (Graphics2D) graphics.create();

        // Apply opacity — allows the whole frame to fade slightly for flicker effect
        graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        // Fill background
        graphics2d.setColor(UIComponents.THEME_BACKGROUND);
        graphics2d.fillRect(0, 0, getWidth(), getHeight());

        // Inner thick border (4px stroke, inset 4px from edges)
        graphics2d.setColor(UIComponents.THEME_FOREGROUND);
        graphics2d.setStroke(new BasicStroke(4));
        graphics2d.drawRect(4, 4, getWidth() - 8, getHeight() - 8);

        // Outer thin hairline border (1px, at the very edge)
        graphics2d.setStroke(new BasicStroke(1));
        graphics2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        // Corner bracket decorations — L-shaped accents at each corner
        int bracketSize = 15;
        drawBracket(graphics2d, -10, -10, bracketSize, true, true); // Top-left
        drawBracket(graphics2d, getWidth() + 10 - bracketSize, -10, bracketSize, true, false); // Top-right
        drawBracket(graphics2d, -10, getHeight() + 10 - bracketSize, bracketSize, false, true); // Bottom-left
        drawBracket(graphics2d, getWidth() + 10 - bracketSize, getHeight() + 10 - bracketSize, bracketSize, false,
                false); // Bottom-right

        graphics2d.dispose();
    }

    /**
     * Draws a single L-shaped corner bracket.
     *
     * @param graphics2d The Graphics2D context to draw into.
     * @param originX    Left X of the bounding box for this bracket.
     * @param originY    Top Y of the bounding box for this bracket.
     * @param armLength  Length (in pixels) of each arm of the L-shape.
     * @param armAtTop   If true, the horizontal arm goes along the top; otherwise
     *                   bottom.
     * @param armAtLeft  If true, the vertical arm goes along the left; otherwise
     *                   right.
     */
    private void drawBracket(Graphics2D graphics2d, int originX, int originY, int armLength, boolean armAtTop,
            boolean armAtLeft) {
        graphics2d.setColor(UIComponents.THEME_FOREGROUND);
        if (armAtTop && armAtLeft) {
            graphics2d.fillRect(originX, originY, armLength, 2); // Horizontal arm (top)
            graphics2d.fillRect(originX, originY, 2, armLength); // Vertical arm (left)
        } else if (armAtTop && !armAtLeft) {
            graphics2d.fillRect(originX, originY, armLength, 2); // Horizontal arm (top)
            graphics2d.fillRect(originX + armLength - 2, originY, 2, armLength); // Vertical arm (right)
        } else if (!armAtTop && armAtLeft) {
            graphics2d.fillRect(originX, originY + armLength - 2, armLength, 2); // Horizontal arm (bottom)
            graphics2d.fillRect(originX, originY, 2, armLength); // Vertical arm (left)
        } else {
            graphics2d.fillRect(originX, originY + armLength - 2, armLength, 2); // Horizontal arm (bottom)
            graphics2d.fillRect(originX + armLength - 2, originY, 2, armLength); // Vertical arm (right)
        }
    }
}

// =============================================================================
// BIT-REKT SCROLLBAR UI
// =============================================================================

/**
 * A custom ScrollBarUI that gives scrollbars a high-contrast terminal look.
 *
 * Features:
 * - Pure black thumb (solid rectangle)
 * - Off-white track with a thin black outline
 * - No arrow buttons (minimalist look)
 */
class BitRektScrollBarUI extends BasicScrollBarUI {
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(UIComponents.THEME_BACKGROUND);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g.setColor(UIComponents.THEME_FOREGROUND);
        g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width - 1, trackBounds.height - 1);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
            return;
        g.setColor(UIComponents.THEME_FOREGROUND);
        // Fill the thumb with a solid color, inset slightly for better aesthetics
        g.fillRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    /**
     * Creates a dummy button with zero size to hide the scrollbar arrows.
     */
    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }
}
