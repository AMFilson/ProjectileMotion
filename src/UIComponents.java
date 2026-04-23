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
 *   color      - The colour of the dashes.
 *   thickness  - Stroke width in pixels (1 = hairline).
 *   dashLength - Length of each dash segment in pixels.
 */
class DashedBorder extends javax.swing.border.AbstractBorder {
    private Color color;
    private int   thickness;
    private int   dashLength;

    public DashedBorder(Color color, int thickness, int dashLength) {
        this.color      = color;
        this.thickness  = thickness;
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
        // dashPattern = {dashLength} means: dashLength pixels ON, dashLength pixels OFF (repeating)
        float[] dashPattern = { dashLength };
        graphics2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
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
 * When dithered=true:  fills with a diagonal hatch pattern (TexturePaint).
 *
 * Usage:
 *   DitheredBar bar = new DitheredBar(75, false); // 75% solid fill
 *   DitheredBar bar = new DitheredBar(45, true);  // 45% dithered/hatch fill
 */
class DitheredBar extends JPanel {
    private int     percent;
    private boolean dithered;

    // FILL_COLOR is the colour used to draw the filled portion of the bar.
    // Hardcoded to match the BIT-REKT palette (pure black).
    private static final Color FILL_COLOR = new Color(0, 0, 0);

    public DitheredBar(int percent, boolean dithered) {
        this.percent  = percent;
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
    private final Color foreground = new Color(0, 0, 0);
    private final Color background = new Color(239, 243, 241);

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
        graphics2d.setColor(background);
        graphics2d.fillRect(0, 0, getWidth(), getHeight());

        // Inner thick border (4px stroke, inset 4px from edges)
        graphics2d.setColor(foreground);
        graphics2d.setStroke(new BasicStroke(4));
        graphics2d.drawRect(4, 4, getWidth() - 8, getHeight() - 8);

        // Outer thin hairline border (1px, at the very edge)
        graphics2d.setStroke(new BasicStroke(1));
        graphics2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        // Corner bracket decorations — L-shaped accents at each corner
        int bracketSize = 15;
        drawBracket(graphics2d, -10,                        -10,                        bracketSize, true,  true);  // Top-left
        drawBracket(graphics2d, getWidth() + 10 - bracketSize, -10,                     bracketSize, true,  false); // Top-right
        drawBracket(graphics2d, -10,                        getHeight() + 10 - bracketSize, bracketSize, false, true);  // Bottom-left
        drawBracket(graphics2d, getWidth() + 10 - bracketSize, getHeight() + 10 - bracketSize, bracketSize, false, false); // Bottom-right

        graphics2d.dispose();
    }

    /**
     * Draws a single L-shaped corner bracket.
     *
     * @param graphics2d  The Graphics2D context to draw into.
     * @param originX     Left X of the bounding box for this bracket.
     * @param originY     Top Y of the bounding box for this bracket.
     * @param armLength   Length (in pixels) of each arm of the L-shape.
     * @param armAtTop    If true, the horizontal arm goes along the top; otherwise bottom.
     * @param armAtLeft   If true, the vertical arm goes along the left; otherwise right.
     */
    private void drawBracket(Graphics2D graphics2d, int originX, int originY, int armLength, boolean armAtTop, boolean armAtLeft) {
        graphics2d.setColor(foreground);
        if (armAtTop && armAtLeft) {
            graphics2d.fillRect(originX, originY, armLength, 2);                    // Horizontal arm (top)
            graphics2d.fillRect(originX, originY, 2, armLength);                    // Vertical arm (left)
        } else if (armAtTop && !armAtLeft) {
            graphics2d.fillRect(originX, originY, armLength, 2);                    // Horizontal arm (top)
            graphics2d.fillRect(originX + armLength - 2, originY, 2, armLength);    // Vertical arm (right)
        } else if (!armAtTop && armAtLeft) {
            graphics2d.fillRect(originX, originY + armLength - 2, armLength, 2);   // Horizontal arm (bottom)
            graphics2d.fillRect(originX, originY, 2, armLength);                    // Vertical arm (left)
        } else {
            graphics2d.fillRect(originX, originY + armLength - 2, armLength, 2);   // Horizontal arm (bottom)
            graphics2d.fillRect(originX + armLength - 2, originY, 2, armLength);   // Vertical arm (right)
        }
    }
}
