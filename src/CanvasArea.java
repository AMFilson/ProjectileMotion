import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

/**
 * CanvasArea.java
 *
 * The large tank preview canvas shown on BIT-REKT's Home screen.
 * Loads each tank's PNG sprite from the images/ directory and renders it
 * centred in the panel, scaled up with nearest-neighbour interpolation to
 * preserve the pixel-art look.
 *
 * Previously used hand-coded fillRect() calls to draw the tank, which meant
 * the sprite in this panel and the one in CharacterSelectPanel could drift
 * apart over time. By loading the shared PNG files from TankData.getImagePath(),
 * both panels are guaranteed to always show exactly the same image.
 *
 * Dependencies:
 *   - tanks          : the shared list of TankData objects from MainMenu
 *   - tankIndexGetter: a lambda that returns the currently selected index
 *   - vt323          : the loaded VT323 font for the tank name label
 */
public class CanvasArea extends JPanel {

    private final Color bg = new Color(239, 243, 241);
    private final Color fg = new Color(0, 0, 0);

    private final List<TankData>  tanks;
    private final IntSupplier     tankIndexGetter;
    private final Font            vt323;

    /**
     * Cache of loaded BufferedImages so we don't re-read the PNG file on
     * every repaint. The map key is the image path string from TankData.getImagePath().
     *
     * LEARNING (HashMap as a cache):
     *   HashMap.computeIfAbsent(key, loader) checks if the key already has a value.
     *   If it does, it returns it. If not, it calls the loader function to create
     *   one, stores it, and returns it. This means each PNG is loaded exactly once.
     */
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    /**
     * @param tanks           The shared list of all available TankData objects.
     * @param tankIndexGetter A supplier that returns the currently selected tank index.
     * @param font            The VT323 font instance shared from MainMenu.
     */
    public CanvasArea(List<TankData> tanks, IntSupplier tankIndexGetter, Font font) {
        this.tanks           = tanks;
        this.tankIndexGetter = tankIndexGetter;
        this.vt323           = font;
    }

    /**
     * Loads a PNG from disk, caching it after the first load.
     * Returns null if the file doesn't exist or can't be read.
     */
    private BufferedImage loadImage(String path) {
        if (path == null) return null;
        return imageCache.computeIfAbsent(path, p -> {
            try {
                return ImageIO.read(new File(p));
            } catch (Exception e) {
                System.err.println("CanvasArea: could not load image: " + p);
                return null;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int cx = getWidth() / 2;

        int      idx         = tankIndexGetter.getAsInt();
        TankData currentTank = tanks.get(idx);
        String   tankName    = currentTank.getName();

        // --- Draw tank name label (inverted chip at bottom of canvas) ---
        g2.setFont(vt323.deriveFont(32f));
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

        // --- Load and draw the PNG sprite ---
        BufferedImage img = loadImage(currentTank.getImagePath());
        if (img != null) {
            // Use nearest-neighbour interpolation so the pixel art stays crisp
            // when scaled up (no blurring of the hard edges).
            g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // Scale the image to fill roughly 55% of the shorter panel dimension,
            // then centre it in the canvas area.
            int maxDim   = (int) (Math.min(getWidth(), getHeight()) * 0.55);
            int imgW     = img.getWidth();
            int imgH     = img.getHeight();
            double scale = Math.min((double) maxDim / imgW, (double) maxDim / imgH);
            int drawW    = (int) (imgW * scale);
            int drawH    = (int) (imgH * scale);
            int drawX    = cx - drawW / 2;
            // Centre vertically in the space above the name label
            int drawY    = (sy - drawH) / 2;

            g2.drawImage(img, drawX, drawY, drawW, drawH, null);
        }

        g2.dispose();
    }
}
