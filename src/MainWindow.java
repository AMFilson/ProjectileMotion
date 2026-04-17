import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
    /*
     * There should be a window added for Animations later on
     * this will require JPanel so a seperate file from this atm
     */

    // Components:
    // 1. JTextField player1NameField
    private JTextField player1NameField = new JTextField(20);

    // 2. JTextField player1PositionField
    private JTextField player1PositionField = new JTextField(20);

    // 3. JTextField player1PowerField
    private JTextField player1PowerField = new JTextField(20);

    // 4. JTextField player1AngleField
    private JTextField player1AngleField = new JTextField(20);

    // 5. JTextField player1ScoreField
    private JTextField player1ScoreField = new JTextField(20);

    // 6. JTextField player2NameField
    private JTextField player2NameField = new JTextField(20);

    // 6. JTextField player2PositionField
    private JTextField player2PositionField = new JTextField(20);

    // 7. JTextField player2PowerField
    private JTextField player2PowerField = new JTextField(20);

    // 8. JTextField player2AngleField
    private JTextField player2AngleField = new JTextField(20);

    // 9. JTextField player2ScoreField
    private JTextField player2ScoreField = new JTextField(20);

    // 10. JTextField gamesPlayedField
    private JTextField gamesPlayedField = new JTextField(20);

    // 11. JTextField currentRoundField
    private JTextField currentRoundField = new JTextField(20);

    // Buttons:
    // 1. JButton btnSave: "Save to File"
    private JButton btnSave = new JButton("Save to File");

    // 2. JButton btnOpen: "Open File"
    private JButton btnOpen = new JButton("Open File");

    /* The primary JFrame Constructor */
    public MainWindow() {
        // Set up the frame
        setTitle("Projectile Motion Data Entry");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel for input fields using GridLayout
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add components with labels for player 1
        inputPanel.add(new JLabel("Player 1 Name:"));
        inputPanel.add(player1NameField);
        inputPanel.add(new JLabel("Player 1 Position:"));
        inputPanel.add(player1PositionField);
        inputPanel.add(new JLabel("Player 1 Power:"));
        inputPanel.add(player1PowerField);
        inputPanel.add(new JLabel("Player 1 Angle:"));
        inputPanel.add(player1AngleField);
        /* need to include score tracking to as well */
        inputPanel.add(new JLabel("Player 1 Score:"));
        inputPanel.add(player1ScoreField);

        // Add components with labels for player 2
        inputPanel.add(new JLabel("Player 2 Name:"));
        inputPanel.add(player2NameField);
        inputPanel.add(new JLabel("Player 2 Position:"));
        inputPanel.add(player2PositionField);
        inputPanel.add(new JLabel("Player 2 Power:"));
        inputPanel.add(player2PowerField);
        inputPanel.add(new JLabel("Player 2 Angle:"));
        inputPanel.add(player2AngleField);
        /* need to include score tracking to as well */
        inputPanel.add(new JLabel("Player 2 Score:"));
        inputPanel.add(player2ScoreField);

        // Panel for Games Played
        inputPanel.add(new JLabel("Games Played:"));
        inputPanel.add(gamesPlayedField);
        // Panel for Current Round
        inputPanel.add(new JLabel("Current Round:"));
        inputPanel.add(currentRoundField);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSave);
        buttonPanel.add(btnOpen);

        // Add panels to frame
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listener for Save button
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile();
            }
        });

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Gathers all data from the JTextFields and exports them to a text file.
     * This method triggers a file selection dialog and handles the file writing
     * process.
     */
    private void saveToFile() {
        // Create a new JFileChooser instance for file selection
        JFileChooser fileChooser = new JFileChooser();

        // Show the 'Save' dialog window. This returns JFileChooser.APPROVE_OPTION
        // if the user clicks the "Save" button after selecting a file path.
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Get the File object representing the location chosen by the user
            File file = fileChooser.getSelectedFile();

            /*
             * We use a try-with-resources statement to ensure the BufferedWriter
             * and FileWriter are closed automatically, even if an exception occurs.
             * BufferedReader/FileWriter are used for efficient character output.
             */
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

                /*
                 * The following write operations output the text from each field
                 * followed by a newline (\n). THE ORDER IS CRITICAL:
                 * it must match the order expected by the 'Open File' logic.
                 */

                // Line 1-5: Player 1 Data (Name, Pos, Power, Angle, Score)
                writer.write(player1NameField.getText() + "\n");
                writer.write(player1PositionField.getText() + "\n");
                writer.write(player1PowerField.getText() + "\n");
                writer.write(player1AngleField.getText() + "\n");
                writer.write(player1ScoreField.getText() + "\n");

                // Line 6-10: Player 2 Data (Name, Pos, Power, Angle, Score)
                writer.write(player2NameField.getText() + "\n");
                writer.write(player2PositionField.getText() + "\n");
                writer.write(player2PowerField.getText() + "\n");
                writer.write(player2AngleField.getText() + "\n");
                writer.write(player2ScoreField.getText() + "\n");

                // Line 11-12: General Game Stats
                writer.write(gamesPlayedField.getText() + "\n");
                writer.write(currentRoundField.getText() + "\n");

                // Show a confirmation dialog to the user upon successful save
                JOptionPane.showMessageDialog(this, "Game state saved successfully!");
            } catch (IOException ex) {
                // If an error occurs (e.g., permission denied, disk full), show an error dialog
                JOptionPane.showMessageDialog(this, "Error saving to file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Test to see if this works will relink back to main
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}
