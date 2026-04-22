import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * CanvasArea.java
 *
 * The animated tank preview canvas shown on BIT-REKT's Home screen.
 * Draws the currently selected tank as large pixel art, along with its
 * name in an inverted label box at the bottom of the canvas.
 *
 * Previously an inner class of MainMenu. Extracted to keep each screen
 * component in its own file.
 *
 * Dependencies:
 *   - tanks          : the shared list of TankData objects from MainMenu
 *   - tankIndexGetter: a lambda/supplier that always returns the current index
 *                      (avoids needing a direct reference back to MainMenu)
 *   - vt323          : the loaded VT323 font for consistent typography
 */
public class CanvasArea extends JPanel {

    private final Color bg = new Color(239, 243, 241);
    private final Color fg = new Color(0, 0, 0);

    private final List<TankData>  tanks;
    private final IntSupplier     tankIndexGetter;
    private final Font            vt323;

    /**
     * @param tanks           The shared list of all available TankData objects.
     * @param tankIndexGetter A supplier that returns the currently selected tank index.
     *                        Using an IntSupplier (a lambda like '() -> currentTankIndex')
     *                        allows this panel to always read the latest value from
     *                        MainMenu without holding a direct reference to it.
     * @param font            The VT323 font instance shared from MainMenu.
     */
    public CanvasArea(List<TankData> tanks, IntSupplier tankIndexGetter, Font font) {
        this.tanks           = tanks;
        this.tankIndexGetter = tankIndexGetter;
        this.vt323           = font;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int cx = getWidth()  / 2;
        int cy = getHeight() / 2;

        // Fetch the current tank using the supplier — always reflects latest selection
        int      idx         = tankIndexGetter.getAsInt();
        TankData currentTank = tanks.get(idx);
        String   tankName    = currentTank.getName();

        // Draw tank name label (inverted chip at bottom of canvas)
        g2.setFont(vt323.deriveFont(32f));
        FontMetrics sfm = g2.getFontMetrics();
        int sw = sfm.stringWidth(tankName);
        int sh = 40;
        int sy = getHeight() - 60;
        int tx = cx - sw / 2;
        int ty = sy + (sh + sfm.getAscent()) / 2 - 4;
        g2.setColor(fg);
        g2.fillRect(tx - 10, sy + 5, sw + 20, sh - 10); // Black filled rectangle
        g2.setColor(bg);
        g2.drawString(tankName, tx, ty); // White text on top

        // Translate and scale to paint the large centreed tank sprite
        g2.translate(cx - 150, cy - 150);
        g2.scale(4.6875, 4.6875);
        g2.setColor(fg);

        // Draw the appropriate sprite based on the selected tank index
        if (idx == 0) {
            // M8 GREYHOUND
            g2.fillRect(10, 44, 44, 10); g2.fillRect(12, 42, 40, 2);
            g2.setColor(bg);
            g2.fillRect(14, 46, 4, 6); g2.fillRect(22, 46, 4, 6);
            g2.fillRect(30, 46, 4, 6); g2.fillRect(38, 46, 4, 6); g2.fillRect(46, 46, 4, 6);
            g2.setColor(fg);
            g2.fillRect(14, 34, 36, 10); g2.fillRect(18, 32, 28, 2);
            g2.fillRect(22, 24, 20, 8);  g2.fillRect(24, 22, 16, 2);
            g2.fillRect(42, 26, 18, 4);  g2.fillRect(58, 25, 2, 6);  g2.fillRect(26, 20, 8, 2);
            g2.setColor(bg);
            g2.fillRect(24, 24, 2, 2);   g2.fillRect(26, 26, 2, 2);
        } else if (idx == 1) {
            // FLAK 88
            g2.fillRect(24, 48, 16, 6); g2.fillRect(18, 46, 28, 2);
            g2.fillRect(28, 38, 8, 8);  g2.fillRect(30, 32, 4, 6);
            g2.fillRect(32, 20, 4, 16); g2.fillRect(34, 12, 4, 10);
            g2.fillRect(36, 4, 4, 10);  g2.fillRect(38, -4, 2, 8);
        } else {
            // BLACK CAT
            g2.fillRect(6, 46, 52, 8);
            g2.setColor(bg);
            g2.fillRect(10, 48, 6, 4); g2.fillRect(22, 48, 6, 4);
            g2.fillRect(34, 48, 6, 4); g2.fillRect(46, 48, 6, 4);
            g2.setColor(fg);
            g2.fillRect(10, 38, 44, 8); g2.fillRect(14, 34, 34, 4);
            g2.fillRect(16, 24, 24, 10); g2.fillRect(40, 28, 20, 2); g2.fillRect(58, 27, 4, 4);
        }

        g2.dispose();
    }
}
