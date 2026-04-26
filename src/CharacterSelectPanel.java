/* 
 * Name:    CharacterSelectPanel.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 24th 2026
 * Desc:    The "NEW GAME" screen where players choose their usernames and tanks.
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.text.*;
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

    private final List<TankData> tanks;
    private Font pixelFont;

    private PlayerColumn playerOneColumn;
    private PlayerColumn playerTwoColumn;
    private JLabel battleStatusLabel;
    private JPanel battleBtn;
    private boolean isBlinkVisible = true;

    public CharacterSelectPanel(List<TankData> tanks, Font font) {
        this.tanks = tanks;
        this.pixelFont = font;

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        // --- Two player columns with a centre dividing line ---
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                // Draw a faint vertical divider between the two player columns
                graphics.setColor(new Color(UIComponents.THEME_FOREGROUND.getRed(), UIComponents.THEME_FOREGROUND.getGreen(), UIComponents.THEME_FOREGROUND.getBlue(), 50));
                graphics.drawLine(getWidth() / 2, 20, getWidth() / 2, getHeight() - 20);
            }
        };
        columnsPanel.setOpaque(false);

        playerOneColumn = new PlayerColumn(1, "MIGGY", true);
        playerTwoColumn = new PlayerColumn(2, "", false);
        columnsPanel.add(playerOneColumn);
        columnsPanel.add(playerTwoColumn);
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
            protected void paintComponent(Graphics graphics) {
                isBlinkVisible = (System.currentTimeMillis() / 600) % 2 == 0;
                if (isBlinkVisible || (playerOneColumn.isReady() && playerTwoColumn.isReady())) {
                    super.paintComponent(graphics);
                }
            }
        };
        battleStatusLabel.setFont(pixelFont.deriveFont(20f));
        battleStatusLabel.setForeground(UIComponents.THEME_FOREGROUND);
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
        boolean p1Ready = playerOneColumn.isReady();
        boolean p2Ready = playerTwoColumn.isReady();

        String p1Name = playerOneColumn.getPlayerNameRaw();
        String p2Name = playerTwoColumn.getPlayerNameRaw();
        boolean namesEntered = !p1Name.trim().isEmpty() && !p2Name.trim().isEmpty();

        boolean canBattle = p1Ready && p2Ready && namesEntered;
        battleBtn.setEnabled(canBattle);

        if (canBattle) {
            battleStatusLabel.setText("[ ALL SYSTEMS GO ]");
        } else if (!namesEntered) {
            battleStatusLabel.setText("ENTER NAMES...");
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
        JPanel battleButtonPanel = new JPanel(new BorderLayout()) {
            // Pre-render dither pattern for the disabled/greyed-out state
            private BufferedImage disabledDitherPattern;
            {
                disabledDitherPattern = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics2d = disabledDitherPattern.createGraphics();
                graphics2d.setColor(new Color(UIComponents.THEME_BACKGROUND.getRed(), UIComponents.THEME_BACKGROUND.getGreen(), UIComponents.THEME_BACKGROUND.getBlue(), 80));
                graphics2d.drawLine(0, 2, 2, 0);
                graphics2d.drawLine(2, 4, 4, 2);
                graphics2d.dispose();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                // When disabled, overlay a dithered pattern to signal inactivity
                if (!isEnabled()) {
                    Graphics2D graphics2d = (Graphics2D) graphics.create();
                    graphics2d.setPaint(new TexturePaint(disabledDitherPattern, new Rectangle(0, 0, 4, 4)));
                    graphics2d.fillRect(0, 0, getWidth(), getHeight());
                    graphics2d.dispose();
                }
            }
        };
        battleButtonPanel.setOpaque(false);
        battleButtonPanel.setPreferredSize(new Dimension(220, 54));
        battleButtonPanel.setMaximumSize(new Dimension(220, 54));
        battleButtonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel battleLabel = createLabel("BATTLE", 24f);
        battleLabel.setForeground(UIComponents.THEME_FOREGROUND);
        battleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        battleButtonPanel.add(battleLabel, BorderLayout.CENTER);
        battleButtonPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        battleButtonPanel.setToolTipText("Launch mission sequence (Both players must be READY)");

        Runnable onHover = () -> {
            battleButtonPanel.setBackground(UIComponents.THEME_FOREGROUND);
            battleLabel.setForeground(UIComponents.THEME_BACKGROUND);
            battleButtonPanel.repaint();
        };
        Runnable onUnhover = () -> {
            battleButtonPanel.setBackground(UIComponents.THEME_BACKGROUND);
            battleLabel.setForeground(UIComponents.THEME_FOREGROUND);
            battleButtonPanel.repaint();
        };

        battleButtonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (battleButtonPanel.isEnabled())
                    triggerBattleAction(battleButtonPanel);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (battleButtonPanel.isEnabled()) {
                    battleButtonPanel.setOpaque(true);
                    onHover.run();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!battleButtonPanel.isFocusOwner()) {
                    battleButtonPanel.setOpaque(false);
                    onUnhover.run();
                }
            }
        });

        // --- Keyboard Focus & Accessibility ---
        battleButtonPanel.setFocusable(true);
        battleButtonPanel.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (battleButtonPanel.isEnabled()) {
                    battleButtonPanel.setOpaque(true);
                    onHover.run();
                }
            }

            public void focusLost(FocusEvent e) {
                battleButtonPanel.setOpaque(false);
                onUnhover.run();
            }
        });
        battleButtonPanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE)
                        && battleButtonPanel.isEnabled()) {
                    triggerBattleAction(battleButtonPanel);
                }
            }
        });

        return battleButtonPanel;
    }

    private void triggerBattleAction(JPanel battleButtonPanel) {
        // Collect selected data from both player columns
        String p1Name = playerOneColumn.getPlayerName();
        int p1TankIdx = playerOneColumn.getSelectedTankIndex();
        String p2Name = playerTwoColumn.getPlayerName();
        int p2TankIdx = playerTwoColumn.getSelectedTankIndex();

        // Retrieve the TankData objects and launch the battle window
        TankData t1 = tanks.get(p1TankIdx);
        TankData t2 = tanks.get(p2TankIdx);
        SwingUtilities.invokeLater(() -> {
            MainWindow mw = new MainWindow(p1Name, t1, p1TankIdx, p2Name, t2, p2TankIdx);
            mw.setVisible(true);
            
            // Close the main menu window
            Window mainMenuWindow = SwingUtilities.getWindowAncestor(this);
            if (mainMenuWindow != null) {
                mainMenuWindow.dispose();
            }
        });
    }

    // -------------------------------------------------------------------------
    // SHARED LABEL HELPER
    // -------------------------------------------------------------------------

    private JLabel createLabel(String text, float fontSize) {
        return UIComponents.createLabel(text, pixelFont, fontSize);
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
        private int playerNumber;
        private int selectedTankIndex = 0;
        private boolean ready;
        private JTextField nameField;
        private JPanel tankCanvas;
        private JLabel tankNameLabel;
        // Cache loaded images so each PNG is only read from disk once per column
        private final Map<String, BufferedImage> imageCache = new HashMap<>();
        private JPanel statRowsPanel;
        private JPanel statusBtnWrapper;
        private JPanel statusBtnMain;
        private JPanel statusBtnShadow;
        private JLabel statusLbl;
        private JLabel statusVal;

        PlayerColumn(int playerNumber, String defaultName, boolean startReady) {
            this.playerNumber = playerNumber;
            this.ready = startReady;
            setLayout(new BorderLayout(0, 0));
            setOpaque(false);

            JPanel columnContentPanel = new JPanel();
            columnContentPanel.setLayout(new BoxLayout(columnContentPanel, BoxLayout.Y_AXIS));
            columnContentPanel.setOpaque(false);
            columnContentPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

            // --- Header row: PLAYER label + tank name chip ---
            JPanel playerHeaderRow = new JPanel(new BorderLayout());
            playerHeaderRow.setOpaque(false);
            playerHeaderRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.THEME_FOREGROUND));

            JLabel playerLabel = createLabel(String.format("PLAYER %02d", playerNumber), 36f);
            if (playerNumber == 1) {
                // P1: inverted (white text on black background)
                playerLabel.setOpaque(true);
                playerLabel.setBackground(UIComponents.THEME_FOREGROUND);
                playerLabel.setForeground(UIComponents.THEME_BACKGROUND);
                playerLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            } else {
                // P2: outlined
                playerLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 2),
                        BorderFactory.createEmptyBorder(2, 10, 2, 10)));
            }
            playerHeaderRow.add(playerLabel, BorderLayout.WEST);

            tankNameLabel = createLabel(tanks.get(0).getName(), 24f);
            tankNameLabel.setOpaque(true);
            tankNameLabel.setBackground(UIComponents.THEME_FOREGROUND);
            tankNameLabel.setForeground(UIComponents.THEME_BACKGROUND);
            tankNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            tankNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            playerHeaderRow.add(tankNameLabel, BorderLayout.EAST);

            playerHeaderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
            columnContentPanel.add(playerHeaderRow);
            columnContentPanel.add(Box.createVerticalStrut(20));

            // --- Identification row: NAME label + text field + STATUS button ---
            JPanel nameAndStatusRow = new JPanel();
            nameAndStatusRow.setLayout(new BoxLayout(nameAndStatusRow, BoxLayout.X_AXIS));
            nameAndStatusRow.setOpaque(false);
            nameAndStatusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            nameAndStatusRow.setPreferredSize(new Dimension(0, 50));
            nameAndStatusRow.add(createLabel("NAME:", 24f));
            nameAndStatusRow.add(Box.createHorizontalStrut(5));

            // Name input field (styled with DashedBorder)
            nameField = new JTextField(defaultName, 8);
            ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (isValid(string)) super.insertString(fb, offset, string, attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (isValid(text)) super.replace(fb, offset, length, text, attrs);
                }

                private boolean isValid(String text) {
                    // Disallow commas and most punctuation to preserve CSV integrity
                    return text != null && text.matches("[a-zA-Z0-9 ]*");
                }
            });
            nameField.setFont(pixelFont.deriveFont(24f));
            nameField.setForeground(UIComponents.THEME_FOREGROUND);
            nameField.setBackground(UIComponents.THEME_BACKGROUND);
            nameField.setOpaque(false);
            nameField.setBorder(new DashedBorder(UIComponents.THEME_FOREGROUND, 1, 4));
            nameField.setCaretColor(UIComponents.THEME_FOREGROUND);
            nameField.setMaximumSize(new Dimension(140, 44));
            nameField.setPreferredSize(new Dimension(140, 44));
            nameField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    nameField.setOpaque(true);
                    nameField.setBackground(UIComponents.THEME_FOREGROUND);
                    nameField.setForeground(UIComponents.THEME_BACKGROUND);
                    nameField.setCaretColor(UIComponents.THEME_BACKGROUND);
                }

                public void focusLost(FocusEvent e) {
                    nameField.setOpaque(false);
                    nameField.setBackground(UIComponents.THEME_BACKGROUND);
                    nameField.setForeground(UIComponents.THEME_FOREGROUND);
                    nameField.setCaretColor(UIComponents.THEME_FOREGROUND);
                }
            });
            nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    onStatusChanged();
                }

                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    onStatusChanged();
                }

                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    onStatusChanged();
                }
            });
            nameField.setToolTipText("Enter your Username");
            nameAndStatusRow.add(nameField);
            nameAndStatusRow.add(Box.createHorizontalStrut(5));

            // --- READY/STANDBY status button (shadow-offset button style) ---
            statusBtnWrapper = new JPanel(null); // null layout for absolute positioning
            statusBtnWrapper.setOpaque(false);
            statusBtnWrapper.setPreferredSize(new Dimension(144, 44));
            statusBtnWrapper.setMaximumSize(new Dimension(144, 44));

            // Shadow panel sits behind and offset from the main button
            statusBtnShadow = new JPanel();
            statusBtnShadow.setBackground(UIComponents.THEME_FOREGROUND);
            statusBtnShadow.setBounds(4, 4, 140, 40);

            // Main button surface — slightly wider to cover shadow's top-right corner
            statusBtnMain = new JPanel(new BorderLayout());
            statusBtnMain.setBorder(BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 2));
            statusBtnMain.setBounds(0, 0, 144, 40);

            statusLbl = createLabel("STATUS", 14f);
            statusVal = createLabel(ready ? "[ READY ]" : "[ STANDBY ]", 14f);
            statusLbl.setVerticalAlignment(SwingConstants.CENTER);
            statusVal.setVerticalAlignment(SwingConstants.CENTER);
            statusVal.setHorizontalAlignment(SwingConstants.RIGHT);
            statusBtnMain.add(statusLbl, BorderLayout.WEST);
            statusBtnMain.add(statusVal, BorderLayout.EAST);
            statusBtnMain.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            statusBtnMain.setToolTipText("Toggle readiness state (READY / STANDBY)");

            statusBtnWrapper.add(statusBtnMain);
            statusBtnWrapper.add(statusBtnShadow);

            statusBtnMain.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            statusBtnMain.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pressStatusBtn();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    releaseStatusBtn();
                }
            });

            // --- Keyboard Focus & Accessibility ---
            statusBtnMain.setFocusable(true);
            statusBtnMain.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    // Visual focus state matches READY state aesthetic
                    statusBtnMain.setOpaque(true);
                    statusBtnMain.setBackground(UIComponents.THEME_FOREGROUND);
                    statusLbl.setForeground(UIComponents.THEME_BACKGROUND);
                    statusVal.setForeground(UIComponents.THEME_BACKGROUND);
                }

                public void focusLost(FocusEvent e) {
                    applyStatusStyle(); // Revert to based on state
                }
            });
            statusBtnMain.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                        pressStatusBtn();
                        // Brief delay for animation feel? No, just release immediately for simplicity
                        Timer timer = new Timer(100, ex -> releaseStatusBtn());
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            });

            nameAndStatusRow.add(statusBtnWrapper);
            columnContentPanel.add(nameAndStatusRow);
            columnContentPanel.add(Box.createVerticalStrut(20));

            // --- Tank carousel (< prev | pixel art | next >) ---
            JPanel tankCarousel = new JPanel(new BorderLayout(0, 0)) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    super.paintComponent(graphics);
                    Graphics2D graphics2d = (Graphics2D) graphics.create();
                    graphics2d.setColor(UIComponents.THEME_FOREGROUND);
                    float[] dashPattern = { 4f };
                    graphics2d.setStroke(
                            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                    // Draw only top and bottom dashed lines
                    graphics2d.drawLine(0, 0, getWidth() - 1, 0);
                    graphics2d.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
                    graphics2d.dispose();
                }
            };
            tankCarousel.setOpaque(false);
            tankCarousel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
            tankCarousel.setPreferredSize(new Dimension(260, 260));

            tankCarousel.add(createCarouselBtn("<", () -> {
                selectedTankIndex = (selectedTankIndex - 1 + tanks.size()) % tanks.size();
                refreshTankView();
            }, true), BorderLayout.WEST);

            tankCanvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics graphics) {
                    super.paintComponent(graphics);
                    Graphics2D graphics2d = (Graphics2D) graphics.create();
                    // Use nearest-neighbour so pixel-art edges stay sharp at any scale
                    graphics2d.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    TankData selectedTank = tanks.get(selectedTankIndex);
                    String imagePath = selectedTank.getImagePath();
                    if (imagePath != null) {
                        BufferedImage tankImage = imageCache.computeIfAbsent(imagePath, path -> {
                            try {
                                return ImageIO.read(new File(path));
                            } catch (Exception ex) {
                                return null;
                            }
                        });
                        if (tankImage != null) {
                            // Scale image to fill 90% of the canvas, then centre it
                            int maxImageDimension = (int) (Math.min(getWidth(), getHeight()) * 0.90);
                            double imageScale = Math.min((double) maxImageDimension / tankImage.getWidth(),
                                    (double) maxImageDimension / tankImage.getHeight());
                            int drawWidth = (int) (tankImage.getWidth() * imageScale);
                            int drawHeight = (int) (tankImage.getHeight() * imageScale);
                            int centeredDrawX = getWidth() / 2 - drawWidth / 2;
                            int centeredDrawY = getHeight() / 2 - drawHeight / 2;
                            graphics2d.drawImage(tankImage, centeredDrawX, centeredDrawY, drawWidth, drawHeight, null);
                        }
                    }
                    graphics2d.dispose();
                }
            };
            tankCanvas.setOpaque(false);
            tankCarousel.add(tankCanvas, BorderLayout.CENTER);

            tankCarousel.add(createCarouselBtn(">", () -> {
                selectedTankIndex = (selectedTankIndex + 1) % tanks.size();
                refreshTankView();
            }, false), BorderLayout.EAST);

            columnContentPanel.add(tankCarousel);
            columnContentPanel.add(Box.createVerticalStrut(20));

            // --- Stats panel (populated by refreshTankView) ---
            statRowsPanel = new JPanel();
            statRowsPanel.setLayout(new BoxLayout(statRowsPanel, BoxLayout.Y_AXIS));
            statRowsPanel.setOpaque(false);
            columnContentPanel.add(statRowsPanel);
            refreshTankView();

            columnContentPanel.add(Box.createVerticalStrut(20));
            applyStatusStyle();
            add(columnContentPanel, BorderLayout.CENTER);
        }

        // --- State management ---

        /**
         * Re-reads the selected tank and updates the name chip, canvas, and stat rows.
         */
        private void refreshTankView() {
            TankData selectedTank = tanks.get(selectedTankIndex);
            tankNameLabel.setText(selectedTank.getName());
            tankCanvas.repaint();
            statRowsPanel.removeAll();
            statRowsPanel.add(buildFidelityStatRow("OFFENSIVE POWER", selectedTank.getOffensivePower(), false));
            statRowsPanel.add(Box.createVerticalStrut(10));
            statRowsPanel.add(buildFidelityStatRow("MOBILITY INDEX", selectedTank.getMobilityIndex(), false));
            statRowsPanel.revalidate();
            statRowsPanel.repaint();
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

        /** Helper for status button press animation */
        private void pressStatusBtn() {
            statusBtnMain.setLocation(2, 2);
            statusBtnShadow.setVisible(false);
            toggleStatus();
        }

        /** Helper for status button release animation */
        private void releaseStatusBtn() {
            statusBtnMain.setLocation(0, 0);
            if (ready)
                statusBtnShadow.setVisible(true);
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
                statusBtnMain.setBackground(UIComponents.THEME_FOREGROUND);
                statusLbl.setForeground(UIComponents.THEME_BACKGROUND);
                statusVal.setForeground(UIComponents.THEME_BACKGROUND);
                statusVal.setText("[ READY ]");
                statusBtnMain.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 2),
                        BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                statusBtnShadow.setVisible(true);
            } else {
                // STANDBY: transparent background, dashed border, hidden shadow
                statusBtnMain.setOpaque(false);
                statusBtnMain.setBackground(UIComponents.THEME_BACKGROUND);
                statusLbl.setForeground(UIComponents.THEME_FOREGROUND);
                statusVal.setForeground(UIComponents.THEME_FOREGROUND);
                statusVal.setText("[ STANDBY ]");
                statusBtnMain.setBorder(BorderFactory.createCompoundBorder(
                        new DashedBorder(UIComponents.THEME_FOREGROUND, 1, 4),
                        BorderFactory.createEmptyBorder(0, 4, 0, 4)));
                statusBtnShadow.setVisible(false);
            }
        }

        // --- Component builders ---

        /** Creates one stat row (label + numeric value + progress bar). */
        private JPanel buildFidelityStatRow(String labelText, double value, boolean dithered) {
            JPanel statPanel = new JPanel();
            statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.Y_AXIS));
            statPanel.setOpaque(true);
            statPanel.setBackground(UIComponents.THEME_BACKGROUND);
            statPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            statPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            JPanel statHeaderRow = new JPanel(new BorderLayout());
            statHeaderRow.setOpaque(false);
            statHeaderRow.add(createLabel(labelText, 16f), BorderLayout.WEST);
            statHeaderRow.add(createLabel(String.format("%.1f", value), 16f), BorderLayout.EAST);
            statPanel.add(statHeaderRow);
            statPanel.add(Box.createVerticalStrut(4));

            JPanel progressBarContainer = new JPanel(new BorderLayout());
            progressBarContainer.setOpaque(false);
            progressBarContainer.setPreferredSize(new Dimension(0, 18));
            progressBarContainer.setBorder(BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 1));
            DitheredBar progressBar = new DitheredBar((int) value, dithered);
            progressBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            progressBarContainer.add(progressBar);
            statPanel.add(progressBarContainer);
            statPanel.setToolTipText("Current unit performance metric: " + labelText);
            return statPanel;
        }

        /** Creates a < or > navigation button for the tank carousel. */
        private JPanel createCarouselBtn(String label, Runnable action, boolean left) {
            JPanel carouselButtonPanel = new JPanel(new BorderLayout());
            carouselButtonPanel.setOpaque(false);
            carouselButtonPanel.setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
            carouselButtonPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            carouselButtonPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIComponents.THEME_FOREGROUND, 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            carouselButtonPanel.setToolTipText("View " + (left ? "previous" : "next") + " tank in roster");
            JLabel carouselArrowLabel = createLabel(label, 28f);
            carouselArrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
            carouselButtonPanel.add(carouselArrowLabel, BorderLayout.CENTER);
            carouselButtonPanel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    action.run();
                }

                Runnable onHover = () -> {
                    carouselButtonPanel.setOpaque(true);
                    carouselButtonPanel.setBackground(UIComponents.THEME_FOREGROUND);
                    carouselArrowLabel.setForeground(UIComponents.THEME_BACKGROUND);
                    carouselButtonPanel.repaint();
                };
                Runnable onUnhover = () -> {
                    carouselButtonPanel.setOpaque(false);
                    carouselArrowLabel.setForeground(UIComponents.THEME_FOREGROUND);
                    carouselButtonPanel.repaint();
                };

                public void mouseEntered(MouseEvent e) {
                    onHover.run();
                }

                public void mouseExited(MouseEvent e) {
                    onUnhover.run();
                }
            });

            // --- Keyboard Focus & Accessibility ---
            carouselButtonPanel.setFocusable(true);
            carouselButtonPanel.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    carouselButtonPanel.setOpaque(true);
                    carouselButtonPanel.setBackground(UIComponents.THEME_FOREGROUND);
                    carouselArrowLabel.setForeground(UIComponents.THEME_BACKGROUND);
                }

                public void focusLost(FocusEvent e) {
                    carouselButtonPanel.setOpaque(false);
                    carouselArrowLabel.setForeground(UIComponents.THEME_FOREGROUND);
                }
            });
            carouselButtonPanel.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                        action.run();
                    }
                }
            });

            return carouselButtonPanel;
        }

        // --- Public accessors used by CharacterSelectPanel ---

        boolean isReady() {
            return ready;
        }

        String getPlayerName() {
            return nameField.getText().trim().isEmpty() ? "PLAYER_" + playerNumber : nameField.getText().trim();
        }

        String getPlayerNameRaw() {
            return nameField.getText();
        }

        int getSelectedTankIndex() {
            return selectedTankIndex;
        }
    }
}
