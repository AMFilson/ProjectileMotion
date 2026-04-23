import javax.swing.*;
import java.awt.*;

/*
 * Name:    AnimationPanel.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 17th 2026
 * Desc:    A custom drawing canvas (JPanel subclass) intended to display 
 *          the projectile battle animations within the MainWindow game screen.
 */

/**
 * A placeholder canvas for rendering battle animations in BIT-REKT.
 *
 * LEARNING (Extending a Class / Inheritance):
 *   'extends JPanel' means AnimationPanel INHERITS everything JPanel already 
 *   knows how to do (draw itself, respond to events, handle resizing, etc.).
 *   We can then ADD our own behaviour on top of that foundation. This concept 
 *   is called "Inheritance" — one of the four pillars of OOP.
 *
 *   Think of it like this: JPanel is a blank canvas. AnimationPanel is a 
 *   specialised version that we've customized for our game's visual needs.
 *
 * LEARNING (Custom Painting in Swing — for future use):
 *   To draw custom graphics (e.g., arcs for projectiles, tank sprites, terrain),
 *   you would OVERRIDE the 'paintComponent(Graphics g)' method inherited from 
 *   JPanel:
 *
 *       @Override
 *       protected void paintComponent(Graphics graphics) {
 *           super.paintComponent(graphics);  // ALWAYS call this first to clear the panel
 *           Graphics2D graphics2d = (Graphics2D) graphics;
 *           graphics2d.setColor(Color.RED);
 *           graphics2d.drawArc(x, y, width, height, startAngle, arcAngle); // draws a curve
 *       }
 *
 *   Call 'animationPanel.repaint()' from your game logic whenever the visual
 *   state changes and the panel needs to redraw itself.
 */
public class AnimationPanel extends JPanel {

    /**
     * Default constructor. Sets up the initial appearance of the animation canvas.
     *
     * LEARNING (JPanel sizing):
     *   Swing layout managers decide the final size of components, but 'setPreferredSize'
     *   tells the layout manager how large THIS component WANTS to be. The manager may 
     *   respect it or override it depending on the layout type.
     */
    public AnimationPanel() {
        // Give the canvas a light background — will be visible as the battlefield area.
        setBackground(Color.WHITE);

        // Suggest a default size. MainWindow may override this via layout constraints.
        setPreferredSize(new Dimension(600, 400));
    }

}
