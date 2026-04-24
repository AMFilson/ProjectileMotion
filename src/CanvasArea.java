/* 
 * Name:    CanvasArea.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 24th 2026
 * Desc:    Custom rendering panel for the animated 3D tank preview on the home screen.
 */

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
 * apart over time. By loading the shared PNG files from
 * TankData.getImagePath(),
 * both panels are guaranteed to always show exactly the same image.
 *
 * Dependencies:
 * - tanks : the shared list of TankData objects from MainMenu
 * - tankIndexGetter: a lambda that returns the currently selected index
 * - vt323 : the loaded VT323 font for the tank name label
 */
public class CanvasArea extends JPanel {

    private final Color foreground = new Color(0, 0, 0);
    private final Color background = new Color(239, 243, 241);

    private final List<TankData> tanks;
    private final IntSupplier tankIndexGetter;
    private final Font pixelFont;

    /**
     * Cache of loaded BufferedImages so we don't re-read the PNG file on
     * every repaint. The map key is the image path string from
     * TankData.getImagePath().\
     *
     * LEARNING (HashMap as a cache):
     * HashMap.computeIfAbsent(key, loader) checks if the key already has a value.
     * If it does, it returns it. If not, it calls the loader function to create
     * one, stores it, and returns it. This means each PNG is loaded exactly once.
     */
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    /**
     * @param tanks           The shared list of all available TankData objects.
     * @param tankIndexGetter A supplier that returns the currently selected tank
     *                        index.
     * @param font            The VT323 font instance shared from MainMenu.
     */
    public CanvasArea(List<TankData> tanks, IntSupplier tankIndexGetter, Font font) {
        this.tanks = tanks;
        this.tankIndexGetter = tankIndexGetter;
        this.pixelFont = font;
    }

    /**
     * Loads a PNG from disk, caching it after the first load.
     * Returns null if the file doesn't exist or can't be read.
     */
    private BufferedImage loadImage(String imagePath) {
        if (imagePath == null)
            return null;
        return imageCache.computeIfAbsent(imagePath, path -> {
            try {
                return ImageIO.read(new File(path));
            } catch (Exception e) {
                System.err.println("CanvasArea: could not load image: " + path);
                return null;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2d = (Graphics2D) graphics.create();

        // Horizontal centre of the canvas — used to position text and image
        int canvasCenterX = getWidth() / 2;

        int selectedTankIndex = tankIndexGetter.getAsInt();
        TankData currentTank = tanks.get(selectedTankIndex);
        String tankName = currentTank.getName();

        // --- Draw tank name label (inverted chip at bottom of canvas) ---
        graphics2d.setFont(pixelFont.deriveFont(32f));
        FontMetrics fontMetrics = graphics2d.getFontMetrics();
        int tankNameTextWidth = fontMetrics.stringWidth(tankName);
        int labelBoxHeight = 40;
        int labelBoxY = getHeight() - 60;
        int tankNameTextX = canvasCenterX - tankNameTextWidth / 2;
        int tankNameTextY = labelBoxY + (labelBoxHeight + fontMetrics.getAscent()) / 2 - 4;
        graphics2d.setColor(foreground);
        graphics2d.fillRect(tankNameTextX - 10, labelBoxY + 5, tankNameTextWidth + 20, labelBoxHeight - 10);
        graphics2d.setColor(background);
        graphics2d.drawString(tankName, tankNameTextX, tankNameTextY);

        // --- Load and draw the PNG sprite ---
        BufferedImage tankImage = loadImage(currentTank.getImagePath());
        if (tankImage != null) {
            // Use nearest-neighbour interpolation so the pixel art stays crisp
            // when scaled up (no blurring of the hard edges).
            graphics2d.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // Scale the image to fill roughly 55% of the shorter panel dimension,
            // then centre it in the canvas area.
            int maxImageDimension = (int) (Math.min(getWidth(), getHeight()) * 0.55);
            int imageWidth = tankImage.getWidth();
            int imageHeight = tankImage.getHeight();
            double imageScale = Math.min((double) maxImageDimension / imageWidth,
                    (double) maxImageDimension / imageHeight);
            int drawWidth = (int) (imageWidth * imageScale);
            int drawHeight = (int) (imageHeight * imageScale);
            int centeredDrawX = canvasCenterX - drawWidth / 2;
            // Centre vertically in the space above the name label
            int centeredDrawY = (labelBoxY - drawHeight) / 2;

            graphics2d.drawImage(tankImage, centeredDrawX, centeredDrawY, drawWidth, drawHeight, null);
        }

        graphics2d.dispose();
    }
}
