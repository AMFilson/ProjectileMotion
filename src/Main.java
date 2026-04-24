/* 
 * Name:    Main.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    March Friday the 13th 2026!
 * Desc:    The entry point for the BIT-REKT application.
 *          This is the first class the JVM (Java Virtual Machine) looks for when 
 *          running the program. It sets up global state and launches the Swing UI.
 */

/**
 * Main entry point for the ProjectileMotion / BIT-REKT application.
 *
 * LEARNING (Class Role):
 *   Every Java program needs exactly one class with a 'public static void main(String[] args)' method.
 *   That method is where execution always begins — think of it like the ON button.
 *   Only one 'main' class per program, but it can create and delegate to any number of other classes.
 */
public class Main {

    // -----------------------------------------------------------------------
    // LEARNING (static fields):
    //   Fields marked 'static' belong to the CLASS itself, not to any specific
    //   instance (object) of the class. This means they exist as a single,
    //   shared copy that any other class can reference using 'LeaderboardPanel.playersList'
    //   or 'Main.gamesPlayed'. Think of them as global variables for the app.
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------

    /**
     * Tracks how many total matches have been completed across the whole session.
     * Incremented by MainWindow whenever a hit lands and a match ends.
     */
    public static int gamesPlayed = 0;

    // -----------------------------------------------------------------------

    /**
     * Application entry point. The JVM calls this method to start the program.
     *
     * @param args Command-line arguments (not used in this application).
     *
     * LEARNING (SwingUtilities.invokeLater):
     *   Swing (Java's GUI library) is NOT thread-safe. This means you should only
     *   create or modify UI components from a specific thread called the
     *   "Event Dispatch Thread" (EDT). 
     *
     *   'SwingUtilities.invokeLater(...)' schedules our MainMenu creation to run
     *   on the EDT, ensuring the GUI renders safely and without race conditions.
     *
     *   The '() -> new MainMenu().setVisible(true)' part is a Lambda Expression —
     *   a shorthand way of passing a small block of code (the Runnable) as an argument.
     *   It's equivalent to writing:
     *
     *       Runnable r = new Runnable() {
     *           public void run() { new MainMenu().setVisible(true); }
     *       };
     *       SwingUtilities.invokeLater(r);
     */
    public static void main(String[] args) {
        /*
         * The original text-based version is preserved here for reference.
         * It was replaced by the animated Swing GUI below.
         * 
         *   Scanner universalInput = new Scanner(System.in);
         *   ArrayList<String> scoreBoard = new ArrayList<>();
         *   Game game = new Game(universalInput, scoreBoard);
         *   game.start();
         */

        // Schedule the creation of our MainMenu window on Swing's safe UI thread.
        javax.swing.SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
