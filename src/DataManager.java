import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.Component;

/**
 * Utility class to handle saving and loading game data.
 */
public class DataManager {

    public static void saveGame(Component parent, String[] data) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String line : data) {
                    writer.write(line + "\n");
                }
                JOptionPane.showMessageDialog(parent, "Game state saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Error saving to file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static String[] loadGame(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                JOptionPane.showMessageDialog(parent, "Game state loaded successfully!");
                return lines.toArray(new String[0]);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
}
