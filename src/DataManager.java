import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.Component;

/*
 * Name:    DataManager.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Desc:    A utility class that handles saving game data to a file and 
 *          loading it back. Uses Java's built-in file dialogue to let
 *          the user pick where to save/load.
 */

/**
 * Utility class for saving and loading game state to/from disk.
 *
 * LEARNING (Utility / Helper Classes):
 *   Not every class needs to be an object you instantiate. Some classes act 
 *   purely as a collection of related, reusable functions. When all methods are 
 *   'static', you never need to create an instance — just call them directly:
 *       DataManager.saveGame(parent, data);
 *   This pattern is common for I/O helpers, math utilities, etc.
 *
 * LEARNING (File I/O basics in Java):
 *   Reading and writing files is called "I/O" (Input/Output). Java provides:
 *     - FileWriter / BufferedWriter  → for writing text to a file
 *     - FileReader / BufferedReader  → for reading text from a file
 *   Using 'BufferedWriter' wraps a 'FileWriter' to improve performance by 
 *   batching writes rather than hitting the disk on every single character.
 *
 * LEARNING (try-with-resources):
 *   The syntax 'try (BufferedWriter writer = ...) { ... }' is called 
 *   "try-with-resources". It guarantees the writer/reader is ALWAYS closed 
 *   when the block ends — even if an exception is thrown — preventing 
 *   resource leaks (files left open forever).
 */
public class DataManager {

    /**
     * Opens a "Save File" dialogue and writes the provided data array to 
     * the chosen file, one line per entry.
     *
     * @param parent The UI component to attach the dialogue to (for centering).
     * @param data   An array of strings to write. Each element becomes one line.
     *
     * LEARNING (JFileChooser):
     *   JFileChooser is a built-in Swing component that shows the operating 
     *   system's standard open/save dialogue. The return value tells you whether 
     *   the user clicked "Save" (APPROVE_OPTION) or "Cancel".
     *
     * LEARNING (IOException):
     *   Any file operation can fail (disk full, no permission, etc.). Java 
     *   FORCES you to handle these failures by placing the code in a try-catch 
     *   block. If an IOException is thrown, we catch it and show an error message
     *   rather than crashing the whole program.
     */
    public static void saveGame(Component parent, String[] data) {
        // Create a file chooser dialogue with no pre-selected directory
        JFileChooser fileChooser = new JFileChooser();

        // showSaveDialog() blocks until the user picks a file or cancels
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // try-with-resources: writer is automatically closed when the block exits
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write each data entry as a separate line in the file
                for (String line : data) {
                    writer.write(line + "\n"); // '\n' = newline character
                }
                JOptionPane.showMessageDialog(parent, "Game state saved successfully!");
            } catch (IOException ex) {
                // Something went wrong (e.g., permission denied) — inform the user
                JOptionPane.showMessageDialog(parent,
                        "Error saving to file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        // If the user pressed Cancel, fileChooser.showSaveDialog returns CANCEL_OPTION
        // and nothing happens — no file is written.
    }

    /**
     * Opens an "Open File" dialogue, reads the selected file line by line,
     * and returns the contents as a String array.
     *
     * @param parent The UI component to attach the dialogue to.
     * @return       A String[] where each element is one line from the file,
     *               or null if the user cancelled or an error occurred.
     *
     * LEARNING (String vs String[]):
     *   A String holds a single piece of text. A String[] (String array) holds 
     *   MULTIPLE strings. Here we collect each line into a List<String> first 
     *   (because we don't know how many lines the file has in advance), then 
     *   convert it to an array at the end with 'toArray(new String[0])'.
     */
    public static String[] loadGame(Component parent) {
        JFileChooser fileChooser = new JFileChooser();

        // showOpenDialog() blocks until the user picks a file or cancels
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                List<String> lines = new ArrayList<>();
                String line;

                // Read lines one at a time until readLine() returns null (end of file)
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                JOptionPane.showMessageDialog(parent, "Game state loaded successfully!");

                // Convert the dynamically-sized List to a fixed-size String array
                return lines.toArray(new String[0]);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Error opening file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        // Return null if cancelled or failed — callers should always null-check this!
        return null;
    }
}
