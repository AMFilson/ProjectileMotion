/* 
Name:Main.java (ProjectileMotion)
Author:Andrew Filson
Date: March Friday the 13th 2026!
Desc: A game of shoot at a target
 */
/**
 * Main entry point for the ProjectileMotion application.
 * Launches the BIT-REKT Swing graphical user interface.
 */
public class Main {

  // Simulated global players list for Leaderboard access
  public static java.util.List<Player> playersList = new java.util.ArrayList<>();
  public static int gamesPlayed = 0;

  static {
      // Add mock data until real game stores players here
      Player p1 = new Player("VON_NEUMANN");
      p1.setScore(999999);
      p1.setSelectedTankIndex(1); // FLAK 88

      Player p2 = new Player("CYBER_PUNK_88");
      p2.setScore(842550);
      p2.setSelectedTankIndex(0); // M8 GREYHOUND

      Player p3 = new Player("YOU // USER_772");
      p3.setScore(760042);
      p3.setSelectedTankIndex(2); // BLACK CAT

      playersList.add(p1);
      playersList.add(p2);
      playersList.add(p3);
  }

  public static void main(String[] args) {
    /*
     * Appended to convert to animated UI game
     * Scanner universalInput = new Scanner(System.in);
     * ArrayList<String> scoreBoard = new ArrayList<>();
     * 
     * Game game = new Game(universalInput, scoreBoard);
     * game.start();
     */
    // Launch the Swing GUI
    javax.swing.SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
  }
}
