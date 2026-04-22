import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CharacterSelectPanel.java
 *
 * The "NEW GAME" character selection screen for BIT-REKT.
 * Displays two PlayerColumn panels side-by-side (P1 left, P2 right),
 * a BATTLE button in the footer, and a live status label reflecting
 * readiness state.
 *
 * Previously an inner class of MainMenu (lines 1090–1614).
 * Extracted to its own file to reduce MainMenu.java's size.
 *
 * Contains one inner class:
 * - PlayerColumn : handles one player's name entry, tank carousel, and
 * status (READY / STANDBY) toggle button.
 *
 * Constructor parameters (injected from MainMenu):
 * 
 * @param tanks The shared list of TankData objects (same reference as MainMenu)
 * @param font  The loaded VT323 font instance
 */
public class CharacterSelectPanel extends JPanel {

    private final Color bg = new Color(239, 243, 241);
    private final Color fg = new Color(0, 0, 0);

    private final List<TankData> tanks;
    private final Font vt323;

    private PlayerColumn p1Col;
    private PlayerColumn p2Col;
    private JLabel battleStatusLabel;
    private JPanel battleBtn;
    private boolean blinkOn = true;

    public CharacterSelectPanel(List<TankData> tanks, Font font) {
        this.tanks = tanks;
        this.vt323 = font;

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        // --- Two player columns with a centre dividing line ---
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw a faint vertical divider between the two player columns
                g.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 50));
                g.drawLine(getWidth() / 2, 20, getWidth() / 2, getHeight() - 20);
            }
        };
        columnsPanel.setOpaque(false);

        p1Col = new PlayerColumn(1, "MIGGY", true);
        p2Col = new PlayerColumn(2, "", false);
        columnsPanel.add(p1Col);
        columnsPanel.add(p2Col);
        add(columnsPanel, BorderLayout.CENTER);

        // --- Footer: BATTLE button + status label ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel footerLeft = new JPanel();
        footerLeft.setOpaque(false);
        footer.add(footerLeft, BorderLayout.WEST);

        // Status label blinks when players aren't ready
        battleStatusLabel = new JLabel("AWAITING P2...") {
            @Override
            protected void paintComponent(Graphics g) {
                blinkOn = (System.currentTimeMillis() / 600) % 2 == 0;
                if (blinkOn || (p1Col.isReady() && p2Col.isReady())) {
                    super.paintComponent(g);
                }
            }
        };
        battleStatusLabel.setFont(vt323.deriveFont(20f));
        battleStatusLabel.setForeground(fg);
        battleStatusLabel.setPreferredSize(new Dimension(200, 40));
        battleStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        battleBtn = createBattleButton();
        battleBtn.setEnabled(false);

        JPanel battleWrapper = new JPanel(new BorderLayout());
        battleWrapper.setOpaque(false);
        battleWrapper.add(battleBtn, BorderLayout.CENTER);
        battleWrapper.add(battleStatusLabel, BorderLayout.EAST);

        // West placeholder keeps the BATTLE button visually centred
        JPanel westPlaceholder = new JPanel();
        westPlaceholder.setOpaque(false);
        westPlaceholder.setPreferredSize(new Dimension(200, 10));
        battleWrapper.add(westPlaceholder, BorderLayout.WEST);

        footer.add(battleWrapper, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        onStatusChanged(); // Initialise the status label text
    }

    /**
     * Called by PlayerColumn whenever a player toggles READY/STANDBY.
     * Updates the BATTLE button's enabled state and the status label text.
     */
    void onStatusChanged() {
        boolean p1Ready = p1Col.isReady();
        boolean p2Ready = p2Col.isReady();
        boolean bothReady = p1Ready && p2Ready;

        battleBtn.setEnabled(bothReady);

        if (bothReady) {
            battleStatusLabel.setText("[ ALL SYSTEMS GO ]");
        } else if (!p1Ready && !p2Ready) {
            battleStatusLabel.setText("AWAITING PLAYERS...");
        } else if (!p1Ready) {
            battleStatusLabel.setText("AWAITING P1...");
        } else {
            battleStatusLabel.setText("AWAITING P2...");
        }

        repaint();
    }

    // -------------------------------------------------------------------------
    // BATTLE BUTTON FACTORY
    // -------------------------------------------------------------------------

    private JPanel createBattleButton() {
        JPanel mainBtn = new JPanel(new BorderLayout()) {
            // Pre-render dither pattern for the disabled/greyed-out state
            private BufferedImage ditherPattern;
            {
                ditherPattern = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = ditherPattern.createGraphics();
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 80));
                g2.drawLine(0, 2, 2, 0);
                g2.drawLine(2, 4, 4, 2);
                g2.dispose();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // When disabled, overlay a dithered pattern to signal inactivity
                if (!isEnabled()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(new TexturePaint(ditherPattern, new Rectangle(0, 0, 4, 4)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            }
        };
        mainBtn.setOpaque(false);
        mainBtn.setPreferredSize(new Dimension(220, 54));
        mainBtn.setMaximumSize(new Dimension(220, 54));
        mainBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel lbl = createLabel("BATTLE", 24f);
        lbl.setForeground(fg);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        mainBtn.add(lbl, BorderLayout.CENTER);
        mainBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        mainBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!mainBtn.isEnabled())
                    return;

                // Collect selected data from both player columns
                String p1Name = p1Col.getPlayerName();
                int p1TankIdx = p1Col.getSelectedTankIndex();
                String p2Name = p2Col.getPlayerName();
                int p2TankIdx = p2Col.getSelectedTankIndex();

                // Sync back to the global players list so Leaderboard stays current
                if (Main.playersList.size() >= 2) {
                    Main.playersList.get(0).setName(p1Name);
                    Main.playersList.get(0).setSelectedTankIndex(p1TankIdx);
                    Main.playersList.get(1).setName(p2Name);
                    Main.playersList.get(1).setSelectedTankIndex(p2TankIdx);
                }

                // Retrieve the TankData objects and launch the battle window
                TankData t1 = tanks.get(p1TankIdx);
                TankData t2 = tanks.get(p2TankIdx);
                SwingUtilities.invokeLater(() -> {
                    MainWindow mw = new MainWindow(p1Name, t1, p2Name, t2);
                    mw.setVisible(true);
                });
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (mainBtn.isEnabled()) {
                    mainBtn.setOpaque(true);
                    mainBtn.setBackground(fg);
                    lbl.setForeground(bg);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mainBtn.setOpaque(false);
                lbl.setForeground(fg);
                lbl.setText("BATTLE");
            }
        });

        return mainBtn;
    }

    // -------------------------------------------------------------------------
    // SHARED LABEL HELPER
    // -------------------------------------------------------------------------

    private JLabel createLabel(String txt, float fontSize) {
        JLabel label = new JLabel(txt);
        label.setFont(vt323.deriveFont(fontSize));
        label.setForeground(fg);
        label.setOpaque(false);
        return label;
    }

    // =========================================================================
    // PLAYER COLUMN (inner class)
    // =========================================================================

    /**
     * Represents one player's section of the character select screen.
     *
     * Contains:
     * - PLAYER 01/02 heading + tank name chip
     * - Name entry field + READY/STANDBY status button
     * - Tank carousel (< / > navigation with pixel-art preview)
     * - Offensive Power and Mobility Index stat rows
     */
    class PlayerColumn extends JPanel {
        private int playerNum;
        private int selectedTankIndex = 0;
        private boolean ready;
        private JTextField nameField;
        private JPanel tankCanvas;
        private JLabel tankNameLabel;
        // Cache loaded images so each PNG is only read from disk once per column
        private final Map<String, BufferedImage> imageCache = new HashMap<>();
        private JPanel statsPanel;
        private JPanel statusBtnWrapper;
        private JPanel statusBtnMain;
        private JPanel statusBtnShadow;
        private JLabel statusLbl;
        private JLabel statusVal;

        PlayerColumn(int num, String defaultName, boolean startReady) {
            this.playerNum = num;
            this.ready = startReady;
            setLayout(new BorderLayout(0, 0));
            setOpaque(false);

            JPanel inner = new JPanel();
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setOpaque(false);
            inner.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

            // --- Header row: PLAYER label + tank name chip ---
            JPanel headerRow = new JPanel(new BorderLayout());
            headerRow.setOpaque(false);
            headerRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fg));

            JLabel playerLabel = createLabel(String.format("PLAYER %02d", num), 36f);
            if (num == 1) {
                // P1: inverted (white text on black background)
                playerLabel.setOpaque(true);
                playerLabel.setBackground(fg);
                playerLabel.setForeground(bg);
                playerLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            } else {
                // P2: outlined
                playerLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(fg, 2),
                        BorderFactory.createEmptyBorder(2, 10, 2, 10)));
            }
            headerRow.add(playerLabel, BorderLayout.WEST);

            tankNameLabel = createLabel(tanks.get(0).getName(), 24f);
            tankNameLabel.setOpaque(true);
            tankNameLabel.setBackground(fg);
            tankNameLabel.setForeground(bg);
            tankNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            tankNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            headerRow.add(tankNameLabel, BorderLayout.EAST);

            headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
            inner.add(headerRow);
            inner.add(Box.createVerticalStrut(20));

            // --- Identification row: NAME label + text field + STATUS button ---
            JPanel identRow = new JPanel();
            identRow.setLayout(new BoxLayout(identRow, BoxLayout.X_AXIS));
            identRow.setOpaque(false);
            identRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            identRow.setPreferredSize(new Dimension(0, 50));
            identRow.add(createLabel("NAME:", 24f));
            identRow.add(Box.createHorizontalStrut(5));

            // Name input field (styled with DashedBorder)
            nameField = new JTextField(defaultName, 8);
            nameField.setFont(vt323.deriveFont(24f));
            nameField.setForeground(fg);
            nameField.setBackground(bg);
            nameField.setOpaque(false);
            nameField.setBorder(new DashedBorder(fg, 1, 4));
            nameField.setCaretColor(fg);
            nameField.setMaximumSize(new Dimension(140, 44));
            nameField.setPreferredSize(new Dimension(140, 44));
            nameField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    nameField.setOpaque(true);
                    nameField.setBackground(fg);
                    nameField.setForeground(bg);
                    nameField.setCaretColor(bg);
                }

                public void focusLost(FocusEvent e) {
                    nameField.setOpaque(false);
                    nameField.setBackground(bg);
                    nameField.setForeground(fg);
                    nameField.setCaretColor(fg);
                }
            });
            identRow.add(nameField);
            identRow.add(Box.createHorizontalStrut(5));

            // --- READY/STANDBY status button (shadow-offset button style) ---
            statusBtnWrapper = new JPanel(null); // null layout for absolute positioning
            statusBtnWrapper.setOpaque(false);
            statusBtnWrapper.setPreferredSize(new Dimension(144, 44));
            statusBtnWrapper.setMaximumSize(new Dimension(144, 44));

            // Shadow panel sits behind and offset from the main button
            statusBtnShadow = new JPanel();
            statusBtnShadow.setBackground(fg);
            statusBtnShadow.setBounds(4, 4, 140, 40);

            // Main button surface — slightly wider to cover shadow's top-right corner
            statusBtnMain = new JPanel(new BorderLayout());
            statusBtnMain.setBorder(BorderFactory.createLineBorder(fg, 2));
            statusBtnMain.setBounds(0, 0, 144, 40);

            statusLbl = createLabel("STATUS", 14f);
            statusVal = createLabel(ready ? "[ READY ]" : "[ STANDBY ]", 14f);
            statusLbl.setVerticalAlignment(SwingConstants.CENTER);
            statusVal.setVerticalAlignment(SwingConstants.CENTER);
            statusVal.setHorizontalAlignment(SwingConstants.RIGHT);
            statusBtnMain.add(statusLbl, BorderLayout.WEST);
            statusBtnMain.add(statusVal, BorderLayout.EAST);
            statusBtnMain.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

            statusBtnWrapper.add(statusBtnMain);
            statusBtnWrapper.add(statusBtnShadow);

            statusBtnMain.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            statusBtnMain.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Press-down animation: move main button 2px toward the shadow
                    statusBtnMain.setLocation(2, 2);
                    statusBtnShadow.setVisible(false);
                    toggleStatus();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // Release animation: return to original position
                    statusBtnMain.setLocation(0, 0);
                    if (ready)
                        statusBtnShadow.setVisible(true);
                }
            });

            identRow.add(statusBtnWrapper);
            inner.add(identRow);
            inner.add(Box.createVerticalStrut(20));

            // --- Tank carousel (< prev | pixel art | next >) ---
            JPanel carousel = new JPanel(new BorderLayout(0, 0));
            carousel.setOpaque(false);
            carousel.setBorder(new DashedBorder(fg, 1, 4));
            carousel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
            carousel.setPreferredSize(new Dimension(260, 260));

            carousel.add(createCarouselBtn("<", () -> {
                selectedTankIndex = (selectedTankIndex - 1 + tanks.size()) % tanks.size();
                refreshTankView();
            }, true), BorderLayout.WEST);

            tankCanvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    // Use nearest-neighbour so pixel-art edges stay sharp at any scale
                    g2.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    TankData t = tanks.get(selectedTankIndex);
                    String path = t.getImagePath();
                    if (path != null) {
                        BufferedImage img = imageCache.computeIfAbsent(path, p -> {
                            try {
                                return ImageIO.read(new File(p));
                            } catch (Exception ex) {
                                return null;
                            }
                        });
                        if (img != null) {
                            // Scale image to fill 70% of the canvas, then centre it
                            int maxDim = (int) (Math.min(getWidth(), getHeight()) * 0.70);
                            double scale = Math.min((double) maxDim / img.getWidth(),
                                    (double) maxDim / img.getHeight());
                            int dw = (int) (img.getWidth() * scale);
                            int dh = (int) (img.getHeight() * scale);
                            int dx = getWidth() / 2 - dw / 2;
                            int dy = getHeight() / 2 - dh / 2;
                            g2.drawImage(img, dx, dy, dw, dh, null);
                        }
                    }
                    g2.dispose();
                }
            };
            tankCanvas.setOpaque(false);
            carousel.add(tankCanvas, BorderLayout.CENTER);

            carousel.add(createCarouselBtn(">", () -> {
                selectedTankIndex = (selectedTankIndex + 1) % tanks.size();
                refreshTankView();
            }, false), BorderLayout.EAST);

            inner.add(carousel);
            inner.add(Box.createVerticalStrut(20));

            // --- Stats panel (populated by refreshTankView) ---
            statsPanel = new JPanel();
            statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
            statsPanel.setOpaque(false);
            inner.add(statsPanel);
            refreshTankView();

            inner.add(Box.createVerticalStrut(20));
            applyStatusStyle();
            add(inner, BorderLayout.CENTER);
        }

        // --- State management ---

        /**
         * Re-reads the selected tank and updates the name chip, canvas, and stat rows.
         */
        private void refreshTankView() {
            TankData t = tanks.get(selectedTankIndex);
            tankNameLabel.setText(t.getName());
            tankCanvas.repaint();
            statsPanel.removeAll();
            statsPanel.add(buildFidelityStatRow("OFFENSIVE POWER", t.getOffensivePower(), false));
            statsPanel.add(Box.createVerticalStrut(10));
            statsPanel.add(buildFidelityStatRow("MOBILITY INDEX", t.getMobilityIndex(), false));
            statsPanel.revalidate();
            statsPanel.repaint();
        }

        /**
         * Flips the ready state and notifies CharacterSelectPanel to update the BATTLE
         * button.
         */
        private void toggleStatus() {
            ready = !ready;
            applyStatusStyle();
            onStatusChanged(); // Calls the outer CharacterSelectPanel method
        }

        /**
         * Updates the status button's visual style to reflect READY or STANDBY state.
         */
        private void applyStatusStyle() {
            statusLbl.setVerticalAlignment(SwingConstants.CENTER);
            statusVal.setVerticalAlignment(SwingConstants.CENTER);
            if (ready) {
                // READY: solid black background, white text, visible shadow
                statusBtnMain.setOpaque(true);
                statusBtnMain.setBackground(fg);
                statusLbl.setForeground(bg);
                statusVal.setForeground(bg);
                statusVal.setText("[ READY ]");
                statusBtnMain.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(fg, 2),
                        BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                statusBtnShadow.setVisible(true);
            } else {
                // STANDBY: transparent background, dashed border, hidden shadow
                statusBtnMain.setOpaque(false);
                statusBtnMain.setBackground(bg);
                statusLbl.setForeground(fg);
                statusVal.setForeground(fg);
                statusVal.setText("[ STANDBY ]");
                statusBtnMain.setBorder(BorderFactory.createCompoundBorder(
                        new DashedBorder(fg, 1, 4),
                        BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                statusBtnShadow.setVisible(false);
            }
        }

        // --- Component builders ---

        /** Creates one stat row (label + numeric value + progress bar). */
        private JPanel buildFidelityStatRow(String label, double val, boolean dithered) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(true);
            p.setBackground(bg);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(createLabel(label, 16f), BorderLayout.WEST);
            top.add(createLabel(String.format("%.1f", val), 16f), BorderLayout.EAST);
            p.add(top);
            p.add(Box.createVerticalStrut(4));

            JPanel barContainer = new JPanel(new BorderLayout());
            barContainer.setOpaque(false);
            barContainer.setPreferredSize(new Dimension(0, 18));
            barContainer.setBorder(BorderFactory.createLineBorder(fg, 1));
            DitheredBar bar = new DitheredBar((int) val, dithered);
            bar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            barContainer.add(bar);
            p.add(barContainer);
            return p;
        }

        /** Creates a < or > navigation button for the tank carousel. */
        private JPanel createCarouselBtn(String label, Runnable action, boolean left) {
            JPanel btn = new JPanel(new BorderLayout());
            btn.setOpaque(false);
            btn.setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            JLabel lbl = createLabel(label, 28f);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            btn.add(lbl, BorderLayout.CENTER);
            btn.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    action.run();
                }

                public void mouseEntered(MouseEvent e) {
                    btn.setOpaque(true);
                    btn.setBackground(fg);
                    lbl.setForeground(bg);
                    btn.repaint();
                }

                public void mouseExited(MouseEvent e) {
                    btn.setOpaque(false);
                    lbl.setForeground(fg);
                    btn.repaint();
                }
            });
            return btn;
        }

        // --- Public accessors used by CharacterSelectPanel ---

        boolean isReady() {
            return ready;
        }

        String getPlayerName() {
            return nameField.getText().trim().isEmpty() ? "PLAYER_" + playerNum : nameField.getText().trim();
        }

        int getSelectedTankIndex() {
            return selectedTankIndex;
        }
    }
}
