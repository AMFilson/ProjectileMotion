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
    //   shared copy that any other class can reference using 'Main.playersList'
    //   or 'Main.gamesPlayed'. Think of them as global variables for the app.
    // -----------------------------------------------------------------------

    /** Shared roster of all players — referenced by the Leaderboard panel in MainMenu. */
    public static java.util.List<Player> playersList = new java.util.ArrayList<>();

    /**
     * Tracks how many total rounds have been completed across the whole session.
     * Incremented by MainWindow whenever a hit lands or a round ends.
     */
    public static int gamesPlayed = 0;

    // -----------------------------------------------------------------------
    // LEARNING (static initializer block):
    //   A 'static { ... }' block runs exactly ONCE, the very first time this 
    //   class is loaded by the JVM — before main() is even called. It's the 
    //   right place to populate static data that the rest of the app needs 
    //   to exist before anything else happens.
    //
    //   Here we pre-populate the playersList with placeholder/mock leaderboard 
    //   entries so the Leaderboard screen isn't empty on first launch.
    // -----------------------------------------------------------------------
    static {
        // Mock leaderboard entry #1
        Player p1 = new Player("VON_NEUMANN");
        p1.setScore(999999);
        p1.setSelectedTankIndex(1); // Index 1 = FLAK 88

        // Mock leaderboard entry #2
        Player p2 = new Player("CYBER_PUNK_88");
        p2.setScore(842550);
        p2.setSelectedTankIndex(0); // Index 0 = M8 GREYHOUND

        // Mock leaderboard entry #3
        Player p3 = new Player("YOU // USER_772");
        p3.setScore(760042);
        p3.setSelectedTankIndex(2); // Index 2 = BLACK CAT

        // Add them all to the shared roster
        playersList.add(p1);
        playersList.add(p2);
        playersList.add(p3);
    }

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
