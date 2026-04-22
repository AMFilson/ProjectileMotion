import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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
 *   component.setBorder(new DashedBorder(Color.BLACK, 1, 4));
 *
 * Parameters:
 *   color     - The colour of the dashes.
 *   thickness - Stroke width in pixels (1 = hairline).
 *   dashLength - Length of each dash segment in pixels.
 */
class DashedBorder extends javax.swing.border.AbstractBorder {
    private Color color;
    private int thickness;
    private int dashLength;

    public DashedBorder(Color color, int thickness, int dashLength) {
        this.color      = color;
        this.thickness  = thickness;
        this.dashLength = dashLength;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        // dash[] = {dashLength} means: dashLength pixels ON, dashLength pixels OFF (repeating)
        float[] dash = { dashLength };
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        // Draw rect subtracting 1 to stay inside the component's bounds
        g2.drawRect(x, y, width - 1, height - 1);
        g2.dispose();
    }
}

// =============================================================================
// DITHERED BAR
// =============================================================================

/**
 * A custom JPanel that renders a horizontal progress bar.
 *
 * When dithered=false: fills a solid black rectangle proportional to percent.
 * When dithered=true:  fills with a diagonal hatch pattern (TexturePaint).
 *
 * Usage:
 *   DitheredBar bar = new DitheredBar(75, false); // 75% solid fill
 *   DitheredBar bar = new DitheredBar(45, true);  // 45% dithered/hatch fill
 */
class DitheredBar extends JPanel {
    private int     percent;
    private boolean dithered;

    // fg/bg colours are hardcoded to match the BIT-REKT palette
    private static final Color FG = new Color(0, 0, 0);

    public DitheredBar(int percent, boolean dithered) {
        this.percent  = percent;
        this.dithered = dithered;
        setBorder(BorderFactory.createLineBorder(FG, 1));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Calculate how many pixels wide the filled portion should be
        int width = (int) (getWidth() * (percent / 100.0));
        Graphics2D g2 = (Graphics2D) g.create();
        if (dithered) {
            // Create a 4x4 tile image with a diagonal line for the hatch pattern
            BufferedImage fillImg = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
            Graphics2D fG = fillImg.createGraphics();
            fG.setColor(FG);
            fG.drawLine(0, 2, 2, 0); // top diagonal
            fG.drawLine(2, 4, 4, 2); // bottom diagonal
            fG.dispose();
            g2.setPaint(new TexturePaint(fillImg, new Rectangle(0, 0, 4, 4)));
        } else {
            g2.setColor(FG);
        }
        g2.fillRect(0, 0, width, getHeight());
        g2.dispose();
    }
}

// =============================================================================
// MAIN FRAME PANEL
// =============================================================================

/**
 * A custom JPanel that draws BIT-REKT's signature decorative border frame.
 *
 * Renders:
 *   - Filled background in the app's off-white (#eff3f1)
 *   - Outer thin hairline rectangle
 *   - Inner 4px thick rectangle (inset by 4px on all sides)
 *   - L-shaped corner bracket accents at each corner
 *   - Optional opacity (used for the flicker animation in MainMenu)
 *
 * Usage:
 *   MainFramePanel frame = new MainFramePanel();
 *   frame.setPreferredSize(new Dimension(900, 600));
 *   frame.setLayout(new BorderLayout());
 */
class MainFramePanel extends JPanel {
    private final Color fg = new Color(0, 0, 0);
    private final Color bg = new Color(239, 243, 241);

    // Opacity support for the flicker animation effect in MainMenu
    private float opacity = 1.0f;

    public void setOpacity(float o) {
        this.opacity = o;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Apply opacity — allows the whole frame to fade slightly for flicker effect
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        // Fill background
        g2.setColor(bg);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Inner thick border (4px stroke, inset 4px from edges)
        g2.setColor(fg);
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(4, 4, getWidth() - 8, getHeight() - 8);

        // Outer thin hairline border (1px, at the very edge)
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        // Corner bracket decorations
        int bSize = 15;
        drawBracket(g2, -10,                   -10,                   bSize, true,  true);  // Top-left
        drawBracket(g2, getWidth() + 10 - bSize, -10,                 bSize, true,  false); // Top-right
        drawBracket(g2, -10,                   getHeight() + 10 - bSize, bSize, false, true);  // Bottom-left
        drawBracket(g2, getWidth() + 10 - bSize, getHeight() + 10 - bSize, bSize, false, false); // Bottom-right

        g2.dispose();
    }

    /**
     * Draws a single L-shaped corner bracket.
     *
     * @param g    Graphics2D context
     * @param x    Left X of the bounding box
     * @param y    Top Y of the bounding box
     * @param size Arm length in pixels
     * @param top  If true, horizontal arm goes along the top; otherwise bottom
     * @param left If true, vertical arm goes along the left; otherwise right
     */
    private void drawBracket(Graphics2D g, int x, int y, int size, boolean top, boolean left) {
        g.setColor(fg);
        if (top && left) {
            g.fillRect(x, y, size, 2);           // Horizontal arm (top)
            g.fillRect(x, y, 2, size);           // Vertical arm (left)
        } else if (top && !left) {
            g.fillRect(x, y, size, 2);           // Horizontal arm (top)
            g.fillRect(x + size - 2, y, 2, size); // Vertical arm (right)
        } else if (!top && left) {
            g.fillRect(x, y + size - 2, size, 2); // Horizontal arm (bottom)
            g.fillRect(x, y, 2, size);           // Vertical arm (left)
        } else {
            g.fillRect(x, y + size - 2, size, 2); // Horizontal arm (bottom)
            g.fillRect(x + size - 2, y, 2, size); // Vertical arm (right)
        }
    }
}
