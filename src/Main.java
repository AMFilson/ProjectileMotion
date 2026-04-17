
/* 
Name:Main.java (ProjectileMotion)
Author:Andrew Filson
Date: March Friday the 13th 2026!
Desc: A game of shoot at a target
 */
public class Main {

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
    javax.swing.SwingUtilities.invokeLater(() -> new MainWindow());
  }
}
