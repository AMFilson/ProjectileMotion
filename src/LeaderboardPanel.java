/* 
 * Name:    LeaderboardPanel.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 24th 2026
 * Desc:    Displays the high scores and career stats for BIT-REKT pilots.
 */

import javax.swing.*;
import java.awt.*;

/**
 * LeaderboardPanel.java
 *
 * A self-contained JPanel that displays the BIT-REKT leaderboard.
 * It reads player data from its own playersList (a static field) and game stats
 * from Main.gamesPlayed.
 *
 * Shown when the user clicks "LEADERBOARD" in the MainMenu sidebar.
 * Added to the CardLayout in MainMenu under the key "LEADERBOARD".
 */
public class LeaderboardPanel extends JPanel {

    /**
     * Shared roster of all players — referenced by CharacterSelectPanel and this
     * panel.
     */
    public static java.util.List<Player> playersList = new java.util.ArrayList<>();
    public static java.util.List<MatchRecord> sessionHistory = new java.util.ArrayList<>();

    /** Aggregates historical match data into a ranked player list. */
    public static void refreshLeaderboardData() {
        if (sessionHistory.isEmpty()) {
            playersList.clear();
            return;
        }

        java.util.Map<String, Player> bestPerformances = new java.util.HashMap<>();

        for (MatchRecord record : sessionHistory) {
            // Process Player 1
            updateBest(bestPerformances, record.getP1Name(), record.getP1TankIndex(), record.getP1Score());
            // Process Player 2
            updateBest(bestPerformances, record.getP2Name(), record.getP2TankIndex(), record.getP2Score());
        }

        playersList = new java.util.ArrayList<>(bestPerformances.values());
        java.util.Collections.sort(playersList, (a, b) -> b.getScore() - a.getScore());
    }

    private static void updateBest(java.util.Map<String, Player> map, String name, int tankIdx, int score) {
        Player p = map.get(name);
        if (p == null || score > p.getScore()) {
            Player newBest = new Player(name);
            newBest.setScore(score);
            newBest.setSelectedTankIndex(tankIdx);
            map.put(name, newBest);
        }
    }

    private final Color foreground = new Color(0, 0, 0);
    private final Color background = new Color(239, 243, 241);
    private Font pixelFont;

    /**
     * Builds the leaderboard UI.
     *
     * @param font The VT323 font loaded by MainMenu, passed in to ensure
     *             consistent typography across all panels.
     */
    public LeaderboardPanel(Font font) {
        this.pixelFont = font;
        refreshLeaderboardData(); // Reload latest stats whenever panel is created

        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.fill = GridBagConstraints.BOTH;

        // --- LEFT: Leaderboard Table ---
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new DashedBorder(foreground, 1, 4));

        // Table header row
        JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, foreground),
                BorderFactory.createEmptyBorder(0, 20, 8, 20)));
        headerRow.add(createLabel("RANK", 20f));
        headerRow.add(createLabel("UNIT", 20f));
        headerRow.add(createLabel("PLAYER", 20f));
        JLabel gamesWonLabel = createLabel("GAMES WON", 20f);
        gamesWonLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerRow.add(gamesWonLabel);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tablePanel.add(headerRow);
        tablePanel.add(Box.createVerticalStrut(10));

        // Player rows — reads from the shared static list
        java.util.List<Player> players = playersList;
        if (players == null)
            players = new java.util.ArrayList<>();

        for (int rankIndex = 0; rankIndex < players.size() && rankIndex < 5; rankIndex++) {
            Player player = players.get(rankIndex);
            tablePanel.add(
                    createRankRow(rankIndex + 1, player.getName(), player.getSelectedTankIndex(), player.getScore()));
        }

        tablePanel.add(Box.createVerticalGlue());

        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1.0;
        layoutConstraints.weighty = 1.0;
        add(tablePanel, layoutConstraints);

        // --- RIGHT: Stats sidebar ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, foreground),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel gamesPlayedBox = createLeaderboardStatBox("GAMES PLAYED", String.valueOf(Main.gamesPlayed), 0, false);
        gamesPlayedBox.setToolTipText("Total combat encounters completed in the current session");
        rightPanel.add(gamesPlayedBox);
        rightPanel.add(Box.createVerticalStrut(20));

        // Legacy status chip (inverted colours)
        JPanel legacyBox = new JPanel();
        legacyBox.setLayout(new BoxLayout(legacyBox, BoxLayout.Y_AXIS));
        legacyBox.setOpaque(true);
        legacyBox.setBackground(foreground);
        legacyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foreground, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        legacyBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        legacyBox.setPreferredSize(new Dimension(160, 75));

        JLabel legacyLbl = createLabel("LEGACY STATUS", 12f);
        legacyLbl.setForeground(background);
        legacyLbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, background));
        legacyLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyBox.add(legacyLbl);

        JLabel legacyVal = createLabel("PLATINUM TIER", 18f);
        legacyVal.setForeground(background);
        legacyVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyVal.setFont(legacyVal.getFont().deriveFont(Font.BOLD));
        legacyBox.add(legacyVal);

        JLabel unlockLbl = createLabel("UNLOCKED: X-CALIBER_SKIN", 12f);
        unlockLbl.setForeground(background);
        unlockLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        legacyBox.add(unlockLbl);
        legacyBox.setToolTipText("Player achievement tier");

        rightPanel.add(legacyBox);
        rightPanel.add(Box.createVerticalGlue());

        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 0.0;
        layoutConstraints.weighty = 1.0;
        rightPanel.setPreferredSize(new Dimension(185, 0));
        add(rightPanel, layoutConstraints);
    }

    /** Builds one leaderboard row showing rank badge, tank art, name, and score. */
    private JPanel createRankRow(int rank, String name, int tankIndex, int score) {
        JPanel rankRow = new JPanel(new GridLayout(1, 4, 10, 0));
        rankRow.setOpaque(true);
        rankRow.setBackground(new Color(0, 0, 0, 0));
        rankRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0, 50)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        rankRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Highlight the "YOU" entry
        if (name.contains("YOU")) {
            rankRow.setBackground(new Color(0, 0, 0, 15));
            rankRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(foreground, 1),
                    BorderFactory.createEmptyBorder(11, 19, 11, 19)));
        }

        // Rank badge
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrapper.setOpaque(false);
        JLabel rankBadge = createLabel(String.format("#%02d", rank), 24f);
        rankBadge.setOpaque(true);
        if (rank == 1 || rank == 3) {
            rankBadge.setBackground(foreground);
            rankBadge.setForeground(background);
        } else {
            rankBadge.setBackground(new Color(0, 0, 0, 0));
            rankBadge.setForeground(foreground);
            rankBadge.setBorder(
                    rank == 2 ? BorderFactory.createLineBorder(foreground, 2) : new DashedBorder(foreground, 1, 4));
        }
        rankBadge.setPreferredSize(new Dimension(40, 40));
        rankBadge.setHorizontalAlignment(SwingConstants.CENTER);
        badgeWrapper.add(rankBadge);
        rankRow.add(badgeWrapper);

        // Small tank pixel-art preview
        JPanel unitPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D graphics2d = (Graphics2D) graphics.create();
                graphics2d.translate(0, -10);
                graphics2d.scale(0.8, 0.8);
                graphics2d.setColor(foreground);
                if (tankIndex == 0) { // M8 GREYHOUND
                    graphics2d.fillRect(10, 44, 44, 10);
                    graphics2d.fillRect(12, 42, 40, 2);
                    graphics2d.setColor(background);
                    graphics2d.fillRect(14, 46, 4, 6);
                    graphics2d.fillRect(22, 46, 4, 6);
                    graphics2d.fillRect(30, 46, 4, 6);
                    graphics2d.fillRect(38, 46, 4, 6);
                    graphics2d.fillRect(46, 46, 4, 6);
                    graphics2d.setColor(foreground);
                    graphics2d.fillRect(14, 34, 36, 10);
                    graphics2d.fillRect(18, 32, 28, 2);
                    graphics2d.fillRect(22, 24, 20, 8);
                    graphics2d.fillRect(24, 22, 16, 2);
                    graphics2d.fillRect(42, 26, 18, 4);
                    graphics2d.fillRect(58, 25, 2, 6);
                    graphics2d.fillRect(26, 20, 8, 2);
                    graphics2d.setColor(background);
                    graphics2d.fillRect(24, 24, 2, 2);
                    graphics2d.fillRect(26, 26, 2, 2);
                } else if (tankIndex == 1) { // FLAK 88
                    graphics2d.fillRect(24, 48, 16, 6);
                    graphics2d.fillRect(18, 46, 28, 2);
                    graphics2d.fillRect(28, 38, 8, 8);
                    graphics2d.fillRect(30, 32, 4, 6);
                    graphics2d.fillRect(32, 20, 4, 16);
                    graphics2d.fillRect(34, 12, 4, 10);
                    graphics2d.fillRect(36, 4, 4, 10);
                    graphics2d.fillRect(38, -4, 2, 8);
                } else { // BLACK CAT
                    graphics2d.fillRect(6, 46, 52, 8);
                    graphics2d.setColor(background);
                    graphics2d.fillRect(10, 48, 6, 4);
                    graphics2d.fillRect(22, 48, 6, 4);
                    graphics2d.fillRect(34, 48, 6, 4);
                    graphics2d.fillRect(46, 48, 6, 4);
                    graphics2d.setColor(foreground);
                    graphics2d.fillRect(10, 38, 44, 8);
                    graphics2d.fillRect(14, 34, 34, 4);
                    graphics2d.fillRect(16, 24, 24, 10);
                    graphics2d.fillRect(40, 28, 20, 2);
                    graphics2d.fillRect(58, 27, 4, 4);
                }
                graphics2d.dispose();
            }
        };
        unitPanel.setOpaque(false);
        rankRow.add(unitPanel);

        // Player name
        rankRow.add(createLabel(name, 24f));

        // Score (formatted with commas)
        JLabel scoreLbl = createLabel(String.format("%,d", score), 24f);
        scoreLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        rankRow.add(scoreLbl);

        return rankRow;
    }

    /**
     * Creates a stat display box with label, value, and optional dithered progress
     * bar.
     */
    private JPanel createLeaderboardStatBox(String labelText, String valueText, int percentage, boolean dithered) {
        JPanel statBox = new JPanel();
        statBox.setLayout(new BoxLayout(statBox, BoxLayout.Y_AXIS));
        statBox.setOpaque(false);
        statBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        statBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foreground, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        statBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        statBox.setPreferredSize(new Dimension(160, 75));

        JLabel statLabel = createLabel(labelText, 12f);
        statLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, foreground));
        statLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statBox.add(statLabel);

        JLabel statValueLabel = createLabel(valueText, 24f);
        statValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statValueLabel.setFont(statValueLabel.getFont().deriveFont(Font.BOLD));
        statBox.add(statValueLabel);

        return statBox;
    }

    /** Shared label factory — matches BIT-REKT typography. */
    private JLabel createLabel(String text, float fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(pixelFont.deriveFont(fontSize));
        label.setForeground(foreground);
        label.setOpaque(false);
        return label;
    }
}
