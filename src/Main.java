
/* 
Name:Main.java (ProjectileMotion)
Author:Andrew Filson
Date: March Friday the 13th 2026!
Desc: A game of shoot at a target
 */

/* Appended for now need to convert to a UI based game 
import java.util.ArrayList;
import java.util.Scanner; */

public class Main {

  public static void main(String[] args) {

    /*
     * Appended for now pending UI conversion
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
