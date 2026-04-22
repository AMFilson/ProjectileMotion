import javax.swing.*;
import java.awt.*;

/**
 * LeaderboardPanel.java
 *
 * A self-contained JPanel that displays the BIT-REKT leaderboard.
 * It reads player data from Main.playersList (a static field) and game stats
 * from Main.gamesPlayed — no data needs to be injected at construction time.
 *
 * Shown when the user clicks "LEADERBOARD" in the MainMenu sidebar.
 * Added to the CardLayout in MainMenu under the key "LEADERBOARD".
 */
public class LeaderboardPanel extends JPanel {

    private final Color bg = new Color(239, 243, 241);
    private final Color fg = new Color(0, 0, 0);
    private Font vt323;

    /**
     * Builds the leaderboard UI.
     *
     * @param font The VT323 font loaded by MainMenu, passed in to ensure
     *             consistent typography across all panels.
     */
    public LeaderboardPanel(Font font) {
        this.vt323 = font;
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // --- LEFT: Leaderboard Table ---
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new DashedBorder(fg, 1, 4));

        // Table header row
        JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, fg),
                BorderFactory.createEmptyBorder(0, 20, 8, 20)));
        headerRow.add(createLabel("RANK", 20f));
        headerRow.add(createLabel("UNIT", 20f));
        headerRow.add(createLabel("PLAYER", 20f));
        JLabel hsLabel = createLabel("HIGH_SCORE", 20f);
        hsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerRow.add(hsLabel);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tablePanel.add(headerRow);
        tablePanel.add(Box.createVerticalStrut(10));

        // Player rows — reads from the shared static list in Main
        java.util.List<Player> players = Main.playersList;
        if (players == null) players = new java.util.ArrayList<>();

        for (int i = 0; i < players.size() && i < 5; i++) {
            Player p = players.get(i);
            tablePanel.add(createRankRow(i + 1, p.getName(), p.getSelectedTankIndex(), p.getScore()));
        }

        tablePanel.add(Box.createVerticalGlue());

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        add(tablePanel, gbc);

        // --- RIGHT: Stats sidebar ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, fg),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        rightPanel.add(createLeaderboardStatBox("GAMES PLAYED", String.valueOf(Main.gamesPlayed), 0, false));
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(createLeaderboardStatBox("SEASON PROGRESS", "WEEK 04", 45, true));
        rightPanel.add(Box.createVerticalStrut(20));

        // Legacy status chip (inverted colours)
        JPanel legacyBox = new JPanel();
        legacyBox.setLayout(new BoxLayout(legacyBox, BoxLayout.Y_AXIS));
        legacyBox.setOpaque(true);
        legacyBox.setBackground(fg);
        legacyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        JLabel legacyLbl = createLabel("LEGACY STATUS", 12f);
        legacyLbl.setForeground(bg);
        legacyLbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, bg));
        legacyLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyBox.add(legacyLbl);

        JLabel legacyVal = createLabel("PLATINUM TIER", 18f);
        legacyVal.setForeground(bg);
        legacyVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyVal.setFont(legacyVal.getFont().deriveFont(Font.BOLD));
        legacyBox.add(legacyVal);

        JLabel unlockLbl = createLabel("UNLOCKED: X-CALIBER_SKIN", 12f);
        unlockLbl.setForeground(bg);
        unlockLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyBox.add(unlockLbl);

        rightPanel.add(legacyBox);
        rightPanel.add(Box.createVerticalGlue());

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.0; gbc.weighty = 1.0;
        rightPanel.setPreferredSize(new Dimension(185, 0));
        add(rightPanel, gbc);
    }

    /** Builds one leaderboard row showing rank badge, tank art, name, and score. */
    private JPanel createRankRow(int rank, String name, int tankIndex, int score) {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(true);
        row.setBackground(new Color(0, 0, 0, 0));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0, 50)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Highlight the "YOU" entry
        if (name.contains("YOU")) {
            row.setBackground(new Color(0, 0, 0, 15));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1),
                    BorderFactory.createEmptyBorder(11, 19, 11, 19)));
        }

        // Rank badge
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrapper.setOpaque(false);
        JLabel badge = createLabel(String.format("#%02d", rank), 24f);
        badge.setOpaque(true);
        if (rank == 1 || rank == 3) {
            badge.setBackground(fg);
            badge.setForeground(bg);
        } else {
            badge.setBackground(new Color(0, 0, 0, 0));
            badge.setForeground(fg);
            badge.setBorder(rank == 2 ? BorderFactory.createLineBorder(fg, 2) : new DashedBorder(fg, 1, 4));
        }
        badge.setPreferredSize(new Dimension(40, 40));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badgeWrapper.add(badge);
        row.add(badgeWrapper);

        // Small tank pixel-art preview
        JPanel unitPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(0, -10);
                g2.scale(0.8, 0.8);
                g2.setColor(fg);
                if (tankIndex == 0) { // M8 GREYHOUND
                    g2.fillRect(10, 44, 44, 10); g2.fillRect(12, 42, 40, 2);
                    g2.setColor(bg);
                    g2.fillRect(14, 46, 4, 6); g2.fillRect(22, 46, 4, 6);
                    g2.fillRect(30, 46, 4, 6); g2.fillRect(38, 46, 4, 6); g2.fillRect(46, 46, 4, 6);
                    g2.setColor(fg);
                    g2.fillRect(14, 34, 36, 10); g2.fillRect(18, 32, 28, 2);
                    g2.fillRect(22, 24, 20, 8); g2.fillRect(24, 22, 16, 2);
                    g2.fillRect(42, 26, 18, 4); g2.fillRect(58, 25, 2, 6); g2.fillRect(26, 20, 8, 2);
                    g2.setColor(bg);
                    g2.fillRect(24, 24, 2, 2); g2.fillRect(26, 26, 2, 2);
                } else if (tankIndex == 1) { // FLAK 88
                    g2.fillRect(24, 48, 16, 6); g2.fillRect(18, 46, 28, 2);
                    g2.fillRect(28, 38, 8, 8); g2.fillRect(30, 32, 4, 6);
                    g2.fillRect(32, 20, 4, 16); g2.fillRect(34, 12, 4, 10);
                    g2.fillRect(36, 4, 4, 10); g2.fillRect(38, -4, 2, 8);
                } else { // BLACK CAT
                    g2.fillRect(6, 46, 52, 8);
                    g2.setColor(bg);
                    g2.fillRect(10, 48, 6, 4); g2.fillRect(22, 48, 6, 4);
                    g2.fillRect(34, 48, 6, 4); g2.fillRect(46, 48, 6, 4);
                    g2.setColor(fg);
                    g2.fillRect(10, 38, 44, 8); g2.fillRect(14, 34, 34, 4);
                    g2.fillRect(16, 24, 24, 10); g2.fillRect(40, 28, 20, 2); g2.fillRect(58, 27, 4, 4);
                }
                g2.dispose();
            }
        };
        unitPanel.setOpaque(false);
        row.add(unitPanel);

        // Player name
        row.add(createLabel(name, 24f));

        // Score (formatted with commas)
        JLabel scoreLbl = createLabel(String.format("%,d", score), 24f);
        scoreLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(scoreLbl);

        return row;
    }

    /** Creates a stat display box with label, value, and optional dithered progress bar. */
    private JPanel createLeaderboardStatBox(String labelTxt, String valTxt, int percentage, boolean dithered) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        JLabel lbl = createLabel(labelTxt, 12f);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(lbl);

        JLabel val = createLabel(valTxt, 24f);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        val.setFont(val.getFont().deriveFont(Font.BOLD));
        box.add(val);

        DitheredBar bar = new DitheredBar(percentage, dithered);
        bar.setPreferredSize(new Dimension(160, 12));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(bar);

        return box;
    }

    /** Shared label factory — matches BIT-REKT typography. */
    private JLabel createLabel(String txt, float fontSize) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(vt323.deriveFont(fontSize));
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        return lbl;
    }
}
