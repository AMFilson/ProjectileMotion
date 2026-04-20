import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Creates and manages the main graphical user interface for the Projectile Motion game.
 * 
 * Functions as a robust data entry and state-saving form, using components such as
 * JTextFields to capture player state (names, positions, power, and angles).
 * It uses JFileChooser to save and load game state configurations to/from files.
 */
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

    // 2. JLabel player1PositionField
    private JLabel player1PositionField = new JLabel("0");

    // 3. JTextField player1PowerField
    private JTextField player1PowerField = new JTextField(20);

    // 4. JTextField player1AngleField
    private JTextField player1AngleField = new JTextField(20);

    // 5. JLabel player1ScoreField
    private JLabel player1ScoreField = new JLabel("0");

    // 6. JTextField player2NameField
    private JTextField player2NameField = new JTextField(20);

    // 6. JLabel player2PositionField
    private JLabel player2PositionField = new JLabel("0");

    // 7. JTextField player2PowerField
    private JTextField player2PowerField = new JTextField(20);

    // 8. JTextField player2AngleField
    private JTextField player2AngleField = new JTextField(20);

    // 9. JLabel player2ScoreField
    private JLabel player2ScoreField = new JLabel("0");

    // 10. JLabel gamesPlayedField
    private JLabel gamesPlayedField = new JLabel("0");

    // 11. JLabel currentRoundField
    private JLabel currentRoundField = new JLabel("0");

    // Buttons:
    // 1. JButton buttonSave: "Save to File"
    private JButton buttonSave = new JButton("Save to File");

    // 2. JButton buttonOpen: "Open File"
    private JButton buttonOpen = new JButton("Open File");

    // Player 1 Directional Buttons
    private JButton player1Up = new JButton("▲");
    private JButton player1Down = new JButton("▼");
    private JButton player1Left = new JButton("◄");
    private JButton player1Right = new JButton("►");

    // Player 2 Directional Buttons
    private JButton player2Up = new JButton("▲");
    private JButton player2Down = new JButton("▼");
    private JButton player2Left = new JButton("◄");
    private JButton player2Right = new JButton("►");

    // Animation Panel
    private AnimationPanel animationPanel = new AnimationPanel();

    /* The primary JFrame Constructor */
    public MainWindow() {
        // Set up the frame
        setTitle("Projectile Motion Data Entry");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // LEARNING: LayoutManagers decide how components are arranged inside a container.
        // BorderLayout uses compass directions (NORTH, SOUTH, EAST, WEST, CENTER).
        setLayout(new BorderLayout(10, 10));

        // LEARNING: GridLayout arranges components in a strict grid of rows and columns.
        // GridLayout(0, 2) means "as many rows as needed, but exactly 2 columns".
        JPanel player1Panel = new JPanel(new GridLayout(0, 2, 5, 5));
        player1Panel.setBorder(BorderFactory.createTitledBorder("Player 1"));

        JPanel player2Panel = new JPanel(new GridLayout(0, 2, 5, 5));
        player2Panel.setBorder(BorderFactory.createTitledBorder("Player 2"));

        // Add P1 components
        player1Panel.add(new JLabel("Name:"));
        player1Panel.add(player1NameField);
        player1Panel.add(new JLabel("Position:"));
        player1Panel.add(player1PositionField);
        player1Panel.add(new JLabel("Power:"));
        player1Panel.add(player1PowerField);
        player1Panel.add(new JLabel("Angle:"));
        player1Panel.add(player1AngleField);
        player1Panel.add(new JLabel("Games Won:"));
        player1Panel.add(player1ScoreField);

        // Add P2 components
        player2Panel.add(new JLabel("Name:"));
        player2Panel.add(player2NameField);
        player2Panel.add(new JLabel("Position:"));
        player2Panel.add(player2PositionField);
        player2Panel.add(new JLabel("Power:"));
        player2Panel.add(player2PowerField);
        player2Panel.add(new JLabel("Angle:"));
        player2Panel.add(player2AngleField);
        player2Panel.add(new JLabel("Games Won:"));
        player2Panel.add(player2ScoreField);

        // Stats Panel for the very bottom
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 5));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statsPanel.add(new JLabel("Games Played:"));
        statsPanel.add(gamesPlayedField);
        statsPanel.add(new JLabel("Current Round:"));
        statsPanel.add(currentRoundField);

        // Assemble the input panel
        JPanel playersGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        playersGrid.add(player1Panel);
        playersGrid.add(player2Panel);

        // Arrow Key Panels for each player
        JPanel player1ArrowPad = createArrowKeyPanel(player1Up, player1Down, player1Left, player1Right);
        JPanel player2ArrowPad = createArrowKeyPanel(player2Up, player2Down, player2Left, player2Right);

        // Wrap pads to center them under their respective player columns
        JPanel player1PadWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player1PadWrapper.add(player1ArrowPad);
        JPanel player2PadWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player2PadWrapper.add(player2ArrowPad);

        // Grid for the directional pads
        JPanel directionalGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        directionalGrid.add(player1PadWrapper);
        directionalGrid.add(player2PadWrapper);

        // Combined data and directional controls
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(playersGrid, BorderLayout.CENTER);
        mainContentPanel.add(directionalGrid, BorderLayout.SOUTH);

        // Panel for footer buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buttonSave);
        buttonPanel.add(buttonOpen);

        // Lower UI Assembly
        JPanel lowerUI = new JPanel(new BorderLayout());
        lowerUI.add(mainContentPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(statsPanel, BorderLayout.NORTH);
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);

        lowerUI.add(footerPanel, BorderLayout.SOUTH);

        // Add panels to frame
        add(animationPanel, BorderLayout.CENTER); // Animation takes the top/center space
        add(lowerUI, BorderLayout.SOUTH); // Controls move to the bottom

        // LEARNING: Button clicks are handled by 'ActionListeners'.
        // We use an "anonymous inner class" here (new ActionListener() {...}) to quickly
        // define what should happen (calling saveToFile()) when the user clicks 'Save'.
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile();
            }
        });

        // Action Listener for Open button
        buttonOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFromFile();
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

    /**
     * Opens a file selection dialog and populates the GUI fields with data from
     * the selected text file. Expected format is 12 lines of data.
     */
    private void openFromFile() {
        // Create a new JFileChooser instance for file selection
        JFileChooser fileChooser = new JFileChooser();

        // Show the 'Open' dialog window and check if the user approved the selection
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Get the File object representing the location chosen by the user
            File file = fileChooser.getSelectedFile();

            /*
             * use a try-with-resources statement to ensure the BufferedReader
             * is closed automatically.
             */
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Read each field value from the file in the specific saved order
                player1NameField.setText(reader.readLine());
                player1PositionField.setText(reader.readLine());
                player1PowerField.setText(reader.readLine());
                player1AngleField.setText(reader.readLine());
                player1ScoreField.setText(reader.readLine());

                player2NameField.setText(reader.readLine());
                player2PositionField.setText(reader.readLine());
                player2PowerField.setText(reader.readLine());
                player2AngleField.setText(reader.readLine());
                player2ScoreField.setText(reader.readLine());

                gamesPlayedField.setText(reader.readLine());
                currentRoundField.setText(reader.readLine());

                // Notify user of success
                JOptionPane.showMessageDialog(this, "Game state loaded successfully!");
            } catch (IOException ex) {
                // If an error occurs (e.g., file not found), show an error dialog
                JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Helper method to create a standardized D-Pad layout for arrow keys.
     */
    private JPanel createArrowKeyPanel(JButton up, JButton down, JButton left, JButton right) {
        JPanel panel = new JPanel(new GridLayout(2, 3, 5, 5));
        panel.add(new JLabel("")); // Spacer
        panel.add(up);
        panel.add(new JLabel("")); // Spacer
        panel.add(left);
        panel.add(down);
        panel.add(right);
        return panel;
    }

}
