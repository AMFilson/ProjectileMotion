import javax.swing.*;
import java.awt.*;

/* 
Name:AnimationPanel.java (ProjectileMotion)
Author:Andrew Filson
Date: April 17th 2026
Desc: A panel to display animations for the ProjectileMotion game
 */
/**
 * A custom Swing JPanel intended to handle custom rendering and graphics 
 * animations for the Projectile Motion game canvas.
 */
public class AnimationPanel extends JPanel {

    public AnimationPanel() {
        // Set a default background color for the animation area
        setBackground(Color.WHITE);
        // Set a preferred size (can be adjusted during MainWindow integration)
        setPreferredSize(new Dimension(600, 400));
    }

}
