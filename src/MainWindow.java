import javax.swing.*;

public class MainWindow extends JFrame {

    /*
     * UserInterface.java
     * Andrew Filson
     * April 17th 2026
     * A Swing GUI for the Projectile motion game
     */

    /*
     * Swing UI implementation Plan
     * 
     * OUTCOME:
     * The objective is to create a Java Swing GUI that serves as a robust data
     * entry
     * and state-saving form for the Projectile Motion game.
     * 
     * - This GUI will include multiple JTextField components to capture the game's
     * state
     * (player names, positions, power, and angles) alongside general game data like
     * the round number.
     * - It will use JButtons to perform file I/O operations using JFileChooser,
     * allowing the user to seamlessly save the data to a text file and
     * subsequently load it back into the interface.
     */
    // DESIGN:
    // utilize a GridLayout or GridBagLayout to cleanly organize JLabel and
    // JTextField pairs.

    // Components:
    // 1. JTextField player1NameField
    private JTextField player1NameField = new JTextField(20);

    // 2. JTextField player1PositionField
    private JTextField player1PositionField = new JTextField(20);

    // 3. JTextField player1PowerField
    private JTextField player1PowerField = new JTextField(20);

    // 4. JTextField player1AngleField
    private JTextField player1AngleField = new JTextField(20);

    // 5. JTextField player2NameField
    private JTextField player2NameField = new JTextField(20);

    // 6. JTextField player2PositionField
    private JTextField player2PositionField = new JTextField(20);

    // 7. JTextField player2PowerField
    private JTextField player2PowerField = new JTextField(20);

    // 8. JTextField player2AngleField
    private JTextField player2AngleField = new JTextField(20);

    // 9. JTextField gamesPlayedField
    private JTextField gamesPlayedField = new JTextField(20);

    // 10. JTextField currentRoundField
    private JTextField currentRoundField = new JTextField(20);

    // Buttons:
    // 1. JButton btnSave: "Save to File"
    private JButton btnSave = new JButton("Save to File");

    // 2. JButton btnOpen: "Open File"
}
