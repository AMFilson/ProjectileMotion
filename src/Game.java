import java.util.ArrayList;
import java.util.Scanner;

/*
 * Name:    Game.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    March Friday the 13th 2026!
 * Desc:    The original TEXT-BASED game engine for Projectile Motion.
 *          Handles the menu system, individual game sessions, scoreboard,
 *          and tutorial for the console version of the game.
 *          NOTE: This class was the original prototype. The game has since
 *          been ported to a GUI (MainMenu → MainWindow), but this class 
 *          is preserved for reference and future integration.
 */

/**
 * The core text-based game engine.
 *
 * LEARNING (Class Responsibilities):
 *   Notice how this single class handles several distinct things:
 *     1. The menu loop  (start method)
 *     2. One game session  (runGame)
 *     3. Displaying the scoreboard  (showScoreboard)
 *     4. The tutorial  (showTutorial)
 *     5. Input validation  (getValidatedDouble)
 *
 *   In a larger project, you'd typically split these into separate classes.
 *   For a small prototype, keeping everything in one class is fine and common.
 */
public class Game {

    // -----------------------------------------------------------------------
    // LEARNING (Instance Fields vs Local Variables):
    //   Instance fields are declared here (inside the class, but outside methods).
    //   They belong to each Game OBJECT and persist for the entire lifetime of 
    //   that object. Local variables inside methods are temporary — they only 
    //   exist while that method is running, then they're discarded.
    // -----------------------------------------------------------------------

    /**
     * Stores a log of completed game results (e.g., "Winner: GHOST Game: 2 Round: 4").
     * This persists between multiple calls to runGame() within one session.
     */
    private ArrayList<String> scoreBoard = new ArrayList<>();

    /**
     * The two persistent player objects. They're created once and reused across 
     * multiple game rounds so scores and names are preserved.
     * LEARNING: 'null' means no name yet — names are set during the game loop.
     */
    Player playerOne = new Player(null);
    Player playerTwo = new Player(null);

    /**
     * A Scanner attached to System.in (the keyboard).
     * LEARNING (Scanner):
     *   Scanner is Java's standard way to read text input from the console.
     *   We declare it as an instance field so every method in this class can 
     *   use the same Scanner — creating multiple Scanners on System.in can 
     *   cause conflicts.
     */
    private Scanner universalInput = new Scanner(System.in);

    /** Tracks how many complete game sessions have been played in this run. */
    private int gamesPlayed = 0;

    /**
     * Tracks which round within the current game session we're on.
     * Resets to 0 when a new game starts? (TODO: currently it doesn't reset — future bug)
     */
    private int currentRound = 0;

    // -----------------------------------------------------------------------
    // LEARNING (Constructor):
    //   Even though the fields above already have default values assigned inline,
    //   the constructor re-assigns them from the parameters passed in by Main.
    //   This makes the class more flexible — whoever creates a Game can inject 
    //   their own Scanner and ArrayList into it. This design pattern is called 
    //   "Dependency Injection" — you inject dependencies from outside rather 
    //   than creating them internally.
    // -----------------------------------------------------------------------

    /**
     * Constructs a new Game instance using the provided Scanner and scoreboard.
     *
     * @param universalInput A Scanner already attached to System.in.
     * @param scoreBoard     An ArrayList that will collect game result strings.
     */
    public Game(Scanner universalInput, ArrayList<String> scoreBoard) {
        // 'this.x' = the field on this object.  'x' (no 'this.') = the parameter.
        this.universalInput = universalInput;
        this.scoreBoard     = scoreBoard;
    }

    // -----------------------------------------------------------------------
    // MAIN MENU LOOP
    // -----------------------------------------------------------------------

    /**
     * Starts and runs the main menu loop. Presents options to the player,
     * reads their choice, and dispatches to the appropriate sub-routine.
     *
     * LEARNING (while loop for game/menu loops):
     *   A 'while (condition)' loop keeps running as long as 'condition' is true.
     *   For menus, we want to keep showing the options until the user chooses 
     *   to quit (choice 4). The flag 'needInput' would be set to false at that 
     *   point, but here we use System.exit(0) inside the case instead.
     *
     * LEARNING (try-catch for input safety):
     *   Calling Integer.parseInt("hello") throws a NumberFormatException.
     *   Wrapping it in try-catch prevents the program from crashing — instead,
     *   we display a friendly error and loop back to ask again.
     */
    public void start() {
        boolean needInput = true;
        int menuChoice    = 0;

        while (needInput) {
            // Display the menu every iteration so the user always sees it
            System.out.println("Welcome to Bloons Piracy and no GUI edition!");
            System.out.println("Select one of the following menu options below:");
            System.out.println("Enter 1,2,3,4 and press enter:");
            System.out.println("Choice 1: Start New Game");
            System.out.println("Choice 2: View Scoreboard");
            System.out.println("Choice 3: How to Play");
            System.out.println("Choice 4: End program and exit");

            // Read input as a String first to avoid Scanner buffer issues with nextInt()
            String input = universalInput.nextLine();

            // LEARNING (try-catch block):
            //   'try' → attempt to execute this block
            //   'catch (Exception e)' → run this if ANY exception is thrown inside try
            try {
                menuChoice = Integer.parseInt(input); // Throws if input isn't a number

                switch (menuChoice) {
                    case 1:
                        System.out.println("Please enter in the name of Player one: ");
                        playerOne.setName(universalInput.nextLine());
                        System.out.println("Please enter in the name of Player two: ");
                        playerTwo.setName(universalInput.nextLine());
                        runGame();
                        break;
                    case 2:
                        showScoreboard();
                        break;
                    case 3:
                        showTutorial();
                        break;
                    case 4:
                        System.out.println("Ending Program...");
                        System.exit(0);
                        break;
                    default:
                        break;
                }

            } catch (Exception menuParseException) {
                System.out.println("Error: Invalid input. Please enter a valid number.");
            }
        }
    }

    // -----------------------------------------------------------------------
    // GAME SESSION LOGIC
    // -----------------------------------------------------------------------

    /**
     * Runs a single complete game session between playerOne and playerTwo.
     * Each "round" both players enter their angle, the physics are simulated,
     * and the session continues until one player scores a hit (miss < 1 unit).
     *
     * LEARNING (do-while vs while):
     *   This uses 'while (!hit)' — we only enter the loop if nobody has hit yet.
     *   An alternative would be a do-while loop which always runs at least once.
     *
     * LEARNING (for-each loop over arrays):
     *   Instead of iterating with 'for (int i = 0; i < players.length; i++)',
     *   the enhanced for-each 'for (Player eachPlayer : players)' is cleaner and 
     *   less error-prone. It automatically handles the index for you.
     */
    private void runGame() {
        // Randomize starting positions for this session
        playerOne.setStartingPosition();
        playerTwo.setStartingPosition();

        boolean hit = false;

        while (!hit) {
            currentRound++;
            System.out.println("\n" + playerOne.getName() + " is at " + playerOne.getStartingPosition());
            System.out.println(playerTwo.getName() + " is at " + playerTwo.getStartingPosition());
            System.out.printf("Round: %d \n", currentRound);

            // Put both players in an array so we can loop over them cleanly
            Player[] players = { playerOne, playerTwo };

            // LEARNING (Parallel arrays):
            //   We pair prompts[] with minAndMax[][] by index. This is a simpler 
            //   alternative to a custom class, but can become hard to maintain at scale.
            String[] inputPrompts = {
                "Please enter in a power between 1 - 100: ",
                "Please enter an angle between 0 and 180: "
            };
            double[][] inputRanges = { { 1, 100 }, { 0, 180 } };

            // Get input for each player using the enhanced for-each loop
            for (Player currentPlayer : players) {
                System.out.println("\n" + currentPlayer.getName() + "'s turn:");
                double power = getValidatedDouble(inputPrompts[0], inputRanges[0][0], inputRanges[0][1]);
                double angle = getValidatedDouble(inputPrompts[1], inputRanges[1][0], inputRanges[1][1]);
                currentPlayer.setPower((int) power);
                currentPlayer.setAngle((int) angle);
            }

            // Calculate where each shot lands using Player.getShot()
            double playerOneShotX = playerOne.getShot();
            double playerTwoShotX = playerTwo.getShot();

            double playerOneMissDistance = Math.abs(playerOneShotX - playerTwo.getStartingPosition());
            double playerTwoMissDistance = Math.abs(playerTwoShotX - playerOne.getStartingPosition());

            // Print miss report for each player (only if they didn't hit)
            if (playerOneMissDistance > 1) {
                System.out.printf(
                    "\n%s's shot landed at %.2f (Missed by %.2f)\n",
                    playerOne.getName(), playerOneShotX, playerOneMissDistance
                );
            }
            if (playerTwoMissDistance > 1) {
                System.out.printf(
                    "%s's shot landed at %.2f (Missed by %.2f)\n",
                    playerTwo.getName(), playerTwoShotX, playerTwoMissDistance
                );
            }

            // Hit threshold: within 1 unit is considered a direct hit
            if (playerOneMissDistance < 1 || playerTwoMissDistance < 1) {
                hit = true;
                gamesPlayed++;

                if (playerOneMissDistance < 1 && playerTwoMissDistance < 1) {
                    System.out.println("It's a tie! Both hit!");
                    playerOne.setScore(playerOne.getScore() + 1);
                    playerTwo.setScore(playerTwo.getScore() + 1);
                    scoreBoard.add(
                        "Tie: " + playerOne.getName() + " and " + playerTwo.getName() +
                        " Game: " + gamesPlayed + " Round: " + currentRound
                    );
                } else if (playerOneMissDistance < 1) {
                    // Player one's shot connected
                    System.out.println(playerOne.getName() + " wins!");
                    playerOne.setScore(playerOne.getScore() + 1);
                    scoreBoard.add(
                        "Winner: " + playerOne.getName() +
                        " Game: " + gamesPlayed + " Round: " + currentRound
                    );
                } else {
                    // Player two's shot connected
                    System.out.println(playerTwo.getName() + " wins!");
                    playerTwo.setScore(playerTwo.getScore() + 1);
                    scoreBoard.add(
                        "Winner: " + playerTwo.getName() +
                        " Game: " + gamesPlayed + " Round: " + currentRound
                    );
                }
            } else {
                // Nobody hit — wait for a keypress before starting the next round
                System.out.println("No hit! Press enter to continue to the next turn.");
                universalInput.nextLine();
            }
        }
    }

    // -----------------------------------------------------------------------
    // SCOREBOARD DISPLAY
    // -----------------------------------------------------------------------

    /**
     * Prints all past game results stored in the scoreBoard ArrayList.
     * If no games have been played, prompts the user to play first.
     *
     * LEARNING (ArrayList):
     *   ArrayList is a resizable array. Unlike a regular 'int[]', it can grow 
     *   and shrink dynamically. We use it here because we don't know in advance 
     *   how many rounds will be played.
     *   - 'isEmpty()' → true if the list has no elements
     *   - for-each loop → iterates over every element
     */
    private void showScoreboard() {
        if (scoreBoard.isEmpty()) {
            System.out.println("There is no scoreboard! Play a game first!");
            System.out.println("Press enter to go back to main menu");
            universalInput.nextLine();
        } else {
            // Print every logged result one line at a time
            for (String scoreEntry : scoreBoard) {
                System.out.println(scoreEntry);
            }

            /*
             * TODO: Sorting the scoreboard by round count or score is still pending.
             * The challenge is that the result is one compound String (e.g., 
             * "Winner: GHOST Game: 3 Round: 2"), not a structured object.
             * A cleaner approach would be to store a custom Result class instead.
             *
             *   ArrayList<String> bestScore = new ArrayList<>(scoreBoard);
             *   bestScore.sort(null); // Alphabetical sort — not useful here
             */

            System.out.println("Press enter to go back to main menu");
            universalInput.nextLine();
        }
    }

    // -----------------------------------------------------------------------
    // TUTORIAL
    // -----------------------------------------------------------------------

    /**
     * Displays the game rules and physics explanation to the console.
     */
    private void showTutorial() {
        System.out.println("This is a two player game.");
        System.out.println("To play simply enter in the usernames for your players.");
        System.out.println("The objective of the game is to hit the other player with your shot");
        System.out.println("Each player is placed at a random spot between 0 and 120");
        System.out.println("Then each player sets the power and angle of their shot and attempts to hit the other player");
        System.out.println("First person to strike the other player wins");
        System.out.println("Press enter to go back to main menu");
        universalInput.nextLine();
    }

    // -----------------------------------------------------------------------
    // INPUT VALIDATION
    // -----------------------------------------------------------------------

    /**
     * Prompts the user for a decimal number and keeps asking until a valid 
     * value within the specified range is entered.
     *
     * @param prompt The message to display to the player.
     * @param min    The minimum allowed value (inclusive).
     * @param max    The maximum allowed value (inclusive).
     * @return       A validated double value within [min, max].
     *
     * LEARNING (Input Validation Pattern):
     *   This "ask → parse → validate → loop" pattern appears in almost every 
     *   console application. Key ideas:
     *   1. Always read input as a String (avoids Scanner buffer bugs with nextInt/nextDouble).
     *   2. Attempt to parse it with try-catch to handle non-numeric input gracefully.
     *   3. After parsing, range-check the actual value.
     *   4. Only exit the loop once the value passes ALL checks.
     *
     * LEARNING (boolean flag pattern):
     *   'isValid = false' at the start, then we loop until 'isValid' becomes true.
     *   This is a common, readable pattern for "keep trying until it works".
     */
    private double getValidatedDouble(String prompt, double min, double max) {
        double  parsedValue = 0;
        boolean isValid     = false;

        while (!isValid) {
            System.out.println(prompt + " (" + min + "-" + max + "): ");
            String rawInput = universalInput.nextLine(); // Always read as String first

            try {
                parsedValue = Double.parseDouble(rawInput); // Convert String → double

                if (parsedValue >= min && parsedValue <= max) {
                    isValid = true; // Passes both requirements — exit the loop
                } else {
                    System.out.println("Error: Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException parseException) {
                // User typed letters or symbols — parse failed; loop asks again
                System.out.println("Error: Invalid input. Please enter a valid number.");
            }
        }

        return parsedValue;
    }

}
