import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AnimationPanel extends JPanel {

    private final Color foreground = new Color(0, 0, 0);
    private final Color background = new Color(255, 255, 255);

    private TankData p1Tank;
    private TankData p2Tank;
    private int p1Position = 0;
    private int p2Position = 150;
    private int roundNum = 1;
    private String statusText = "";
    
    private boolean shotFired = false;
    private double shotStartX = 0;
    private double shotLandX = 0;
    private boolean isHit = false;
    private boolean wasP1Shot = true;

    private Font pixelFont;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    public AnimationPanel() {
        setBackground(background);
        setPreferredSize(new Dimension(1000, 400));
    }
    
    public void setFont(Font font) {
        this.pixelFont = font;
    }

    public void updateGameState(TankData p1Tank, int p1Pos, TankData p2Tank, int p2Pos, int roundNum, String statusText) {
        this.p1Tank = p1Tank;
        this.p1Position = p1Pos;
        this.p2Tank = p2Tank;
        this.p2Position = p2Pos;
        this.roundNum = roundNum;
        this.statusText = statusText;
        repaint();
    }

    public void setLastShot(double startX, double landX, boolean isHit, boolean isP1Shot) {
        this.shotFired = true;
        this.shotStartX = startX;
        this.shotLandX = landX;
        this.isHit = isHit;
        this.wasP1Shot = isP1Shot;
        repaint();
    }
    
    public void clearShot() {
        this.shotFired = false;
        repaint();
    }

    private BufferedImage loadImage(String imagePath) {
        if (imagePath == null) return null;
        return imageCache.computeIfAbsent(imagePath, path -> {
            try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Dashed Border
        g2d.setColor(foreground);
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0);
        g2d.setStroke(dashed);
        g2d.drawRect(8, 8, w - 16, h - 16);

        // 2. Decorative Dots & Round Counter
        g2d.setStroke(new BasicStroke(1));
        g2d.fillOval(16, 16, 6, 6);
        g2d.fillOval(26, 16, 6, 6);
        
        if (pixelFont != null) {
            g2d.setFont(pixelFont.deriveFont(18f));
            String roundStr = String.format("ROUND %02d", roundNum);
            g2d.drawString(roundStr, 45, 24);
            
            if (statusText != null && !statusText.isEmpty()) {
                g2d.drawString("|", 125, 24);
                g2d.drawString(statusText.toUpperCase(), 145, 24);
            }
            
            g2d.setFont(pixelFont.deriveFont(14f));
            g2d.drawString("GAMES PLAYED: " + Main.gamesPlayed, w - 140, 24);
        }

        // 4. Grid
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.setColor(new Color(0, 0, 0, 25));
        for (int x = 0; x <= w; x += 40) g2d.drawLine(x, 0, x, h);
        for (int y = 0; y <= h; y += 40) g2d.drawLine(0, y, w, y);

        // 5. Terrain
        double scaleX = w / 1000.0;
        double scaleY = h / 400.0;
        GeneralPath terrain = new GeneralPath();
        terrain.moveTo(0 * scaleX, 350 * scaleY);
        terrain.lineTo(150 * scaleX, 350 * scaleY);
        terrain.lineTo(220 * scaleX, 320 * scaleY);
        terrain.lineTo(280 * scaleX, 320 * scaleY);
        terrain.lineTo(350 * scaleX, 350 * scaleY);
        terrain.lineTo(480 * scaleX, 350 * scaleY);
        terrain.lineTo(520 * scaleX, 370 * scaleY);
        terrain.lineTo(580 * scaleX, 350 * scaleY);
        terrain.lineTo(750 * scaleX, 350 * scaleY);
        terrain.lineTo(800 * scaleX, 330 * scaleY);
        terrain.lineTo(880 * scaleX, 330 * scaleY);
        terrain.lineTo(950 * scaleX, 350 * scaleY);
        terrain.lineTo(1000 * scaleX, 350 * scaleY);
        terrain.lineTo(1000 * scaleX, 400 * scaleY);
        terrain.lineTo(0 * scaleX, 400 * scaleY);
        terrain.closePath();

        g2d.setColor(Color.WHITE);
        g2d.fill(terrain);
        g2d.setColor(foreground);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(terrain);

        // 6. Tanks
        double positionScale = (w - 200) / 200.0;
        if (p1Tank != null) {
            int px = 100 + (int)(p1Position * positionScale);
            int py = (int)(290 * scaleY);
            drawTank(g2d, p1Tank, px, py, "P1", false);
        }
        if (p2Tank != null) {
            int px = 100 + (int)(p2Position * positionScale);
            int py = (int)(300 * scaleY);
            drawTank(g2d, p2Tank, px, py, "P2", true);
        }

        // 7. Shot Trajectory
        if (shotFired) {
            int startPx = 100 + (int)(shotStartX * positionScale);
            int landPx = 100 + (int)(shotLandX * positionScale);
            int arcPeakY = (int)(50 * scaleY);
            QuadCurve2D arc = new QuadCurve2D.Float(startPx, (int)(285 * scaleY), (startPx + landPx) / 2f, arcPeakY, landPx, (int)(310 * scaleY));
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0));
            g2d.draw(arc);
            if (pixelFont != null) {
                g2d.setFont(pixelFont.deriveFont(16f));
                g2d.drawString(isHit ? "HIT!" : "MISS", landPx - 15, (int)(280 * scaleY));
            }
        }
        g2d.dispose();
    }
    
    private void drawTank(Graphics2D g2d, TankData tank, int x, int y, String label, boolean flipX) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(x, y);
        g.scale(0.6, 0.6);
        BufferedImage img = loadImage(tank.getImagePath());
        if (img != null) {
            int imgW = img.getWidth();
            int imgH = img.getHeight();
            double scale = 120.0 / Math.max(imgW, imgH);
            int drawW = (int)(imgW * scale);
            int drawH = (int)(imgH * scale);
            if (flipX) g.drawImage(img, drawW, 0, -drawW, drawH, null);
            else g.drawImage(img, 0, 0, drawW, drawH, null);
        } else {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawRect(0, 20, 100, 20);
            g.drawRect(20, 5, 60, 15);
            g.setColor(foreground);
            g.setStroke(new BasicStroke(4));
            if (flipX) g.drawLine(40, 10, -30, -10);
            else g.drawLine(60, 10, 130, -10);
        }
        if (pixelFont != null) {
            g.setFont(pixelFont.deriveFont(16f));
            g.setColor(foreground);
            g.drawString(label, flipX ? 60 : 0, -10);
        }
        g.dispose();
    }
}
