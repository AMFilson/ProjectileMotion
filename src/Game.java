import java.util.ArrayList;
import java.util.Scanner;

/**
 * Game Class Blueprint
 * This class handles the menu system, game sessions, and scoreboard.
 */
public class Game {
    // 1. Declare your ArrayList here to store scores between games.
    ArrayList<String> scoreBoard = new ArrayList<>();

    // 2. Declare your two Player objects here so they persist.
    Player playerOne = new Player(null);
    Player playerTwo = new Player(null);
    // 3. Declare a Scanner to be used throughout the class.
    private Scanner universalInput = new Scanner(System.in);

    public Game(Scanner universalInput, ArrayList<String> scoreBoard) {
        // Constructor: Initialize your scanner and lists here.
        this.universalInput = universalInput;
        this.scoreBoard = scoreBoard;

    }

    /**
     * The main entry point for the game logic.
     * Should contain a loop for the menu system.
     */
    public void start() {
        boolean needInput = true;
        int menuChoice = 0;

        while (needInput) {
            // TODO: Ask for player names here (only once per program run).
            System.out.println("Welcome to Bloons Piracy and no GUI edition!");
            System.out.println("Select one of the following menu options below:");
            System.out.println("Enter 1,2,3,4 and press enter:");

            // TODO: Main Menu Loop
            // - Choice 1: Start New Game
            System.out.println("Choice 1: Start New Game");
            // - Choice 2: Scoreboard
            System.out.println("Choice 2: View Scoreboard");
            // - Choice 3: Tutorial
            System.out.println("Choice 3: How to Play");
            // - Choice 4: Exit (Ends program)
            System.out.println("End program and exit");
            String input = universalInput.nextLine(); // takes input as string

            try {
                menuChoice = Integer.parseInt(input); // converts string input to integer if possible

                switch (menuChoice) { // attempting a switch case menu
                    case 1:
                        universalInput.nextLine(); // clear input
                        System.out.println("Please enter in the name of Player one: ");
                        playerOne.setName(universalInput.nextLine()); // takes input and changes player one name
                        universalInput.nextLine(); // clear input
                        System.out.println("Please enter in the name of Player two: ");
                        playerTwo.setName(universalInput.nextLine()); // takes input and changes player two name
                        runGame();
                        break;
                    case 2:
                        showScoreboard();
                        break;
                    case 3:
                        showTutorial();
                        break;
                    case 4:
                        System.exit(0);
                        break;
                    default:
                        break;
                }

            } catch (Exception e) {
                // TODO: handle exception
                System.out.println("Error: Invalid input. Please enter a valid number.");
            }

        }

    }

    /**
     * Logic for a single game session.
     */
    private void runGame() {
        // TODO: Reset player positions to random (0-120).

        // TODO: Turn Loop
        // 1. Get Power/Angle from active player (use try-catch blueprint).
        // 2. Check for 20% Wind trigger.
        // 3. Calculate shot distance.
        // 4. Report distance and how far they missed by.
        // 5. Check if distance < 1 (Hit!).
        // 6. "Wait for Enter" before next turn.

        // TODO: After a winner is found, add the result to the ArrayList.
    }

    private void showScoreboard() {
        // TODO: Iterate through ArrayList and print all past results.
        // TODO: Find and print the best score (closest hit or most wins).
    }

    private void showTutorial() {
        // TODO: Print the instructions and the physics formulas.

    }

}
