import javax.swing.*;
import java.awt.*;

public class HowToPlayPanel extends JPanel {

    private Font pixelFont;
    private Color background = new Color(239, 243, 241);
    private Color foreground = new Color(0, 0, 0);

    public HowToPlayPanel(Font font) {
        this.pixelFont = font;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // Add glue at the top if we want them centered vertically as well, 
        // but typically a scrollable list starts at the top.
        
        container.add(createInstructionBox("HOW TO PLAY", 
            "• Select 'NEW GAME'\n" +
            "• Select your unit\n" +
            "• Enter your username\n" +
            "• Set your status to ready and begin the match\n" +
            "• Your fire power is fixed but you can adjust your SHOT ANGLE (0-180) and POSITION\n" +
            "• Use MOBILITY to reposition\n" +
            "• Press FIRE to launch a shot\n" +
            "• First player to hit the other wins"));
        
        container.add(Box.createVerticalStrut(20));
        
        container.add(createInstructionBox("LEADERBOARD", 
            "View the performance of all units. Statistics are loaded from the global database (CSV) " +
            "and updated after every match."));
            
        container.add(Box.createVerticalStrut(20));

        container.add(createInstructionBox("SAVE / LOAD", 
            "Select 'SAVE' to persist your current session history to a file. " +
            "Select 'LOAD' to import match records from a previously saved file back into the simulator."));
            
        container.add(Box.createVerticalStrut(20));

        container.add(createInstructionBox("TERMINATE", 
            "Cleanly exit the program."));

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Apply custom BitRekt ScrollBar UI
        scrollPane.getVerticalScrollBar().setUI(new BitRektScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createInstructionBox(String title, String body) {
        JPanel box = new JPanel();
        box.setLayout(new BorderLayout());
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(foreground, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Centering in BoxLayout
        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.setMaximumSize(new Dimension(585, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(pixelFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(foreground);
        titleLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, foreground));
        box.add(titleLabel, BorderLayout.NORTH);

        JTextArea bodyArea = new JTextArea(body);
        bodyArea.setFont(pixelFont.deriveFont(18f));
        bodyArea.setForeground(foreground);
        bodyArea.setOpaque(false);
        bodyArea.setEditable(false);
        bodyArea.setFocusable(false);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        box.add(bodyArea, BorderLayout.CENTER);

        return box;
    }
}
