import java.util.ArrayList;
import java.util.Scanner;

/* 
Name:Game.java (ProjectileMotion)
Author:Andrew Filson
Date: March Friday the 13th 2026!
Desc:This class handles the menu system, game sessions, and scoreboard.
*/
public class Game {
    // 1. Declare your ArrayList here to store scores between games.
    private ArrayList<String> scoreBoard = new ArrayList<>();

    // 2. Declare your two Player objects here so they persist.
    Player playerOne = new Player(null);
    Player playerTwo = new Player(null);

    /*
     * This is actually redundant I could have just set these to
     * private ArrayList<String> scoreBoard;
     * private Scanner universalInput;
     * this is because these variables are overriden with the ones
     * via the Game Constructor grabbing the variables with the values
     * from Main
     */
    // 3. Declare a Scanner to be used throughout the class.
    private Scanner universalInput = new Scanner(System.in);

    // declare games played counter
    private int gamesPlayed = 0;

    // declare currentRound counter
    private int currentRound = 0;

    public Game(Scanner universalInput, ArrayList<String> scoreBoard) {
        // Constructor: Initialize your scanner and lists here.
        /*
         * Takes the value from the parameter on the right
         * saves it into the class variable on the left
         */
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
            System.out.println("Choice 4: End program and exit");
            /* Ensures I always clear the buffer vs using nextInt */
            String input = universalInput.nextLine(); // takes input as string

            try {
                menuChoice = Integer.parseInt(input); // converts string input to integer if possible

                switch (menuChoice) { // attempting a switch case menu
                    case 1:
                        System.out.println("Please enter in the name of Player one: ");
                        playerOne.setName(universalInput.nextLine()); // takes input and changes player one name
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
                        System.out.println("Ending Program...");
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
        playerOne.setStartingPosition();
        playerTwo.setStartingPosition();

        boolean hit = false;
        while (!hit) {
            currentRound++;
            System.out.println("\n" + playerOne.getName() + " is at " + playerOne.getStartingPosition());
            System.out.println(playerTwo.getName() + " is at " + playerTwo.getStartingPosition());
            System.out.printf("Round: %d \n", currentRound);

            Player[] players = { playerOne, playerTwo };
            String[] promptList = {
                    "Please enter in a power between 1 - 1000: ",
                    "Please enter an angle between 0 and 180: "
            };
            double[][] minAndMax = { { 1, 1000 }, { 0, 180 } };

            /* currentPlayer would be better known as eachPlayer */
            for (Player currentPlayer : players) {
                System.out.println("\n" + currentPlayer.getName() + "'s turn:");
                double power = getValidatedDouble(promptList[0], minAndMax[0][0], minAndMax[0][1]);
                double angle = getValidatedDouble(promptList[1], minAndMax[1][0], minAndMax[1][1]);
                currentPlayer.setPower((int) power);
                currentPlayer.setAngle((int) angle);
            }

            // .20% wind scrapped for now until I figure out how to build or get tired

            // 3. Calculate shot distance.
            double shotOne = playerOne.getShot();
            double shotTwo = playerTwo.getShot();

            // 4. Report distance and how far they missed by.
            /* Absolute to avoid negative numbers and false wins */
            double missOne = Math.abs(shotOne - playerTwo.getStartingPosition());
            double missTwo = Math.abs(shotTwo - playerOne.getStartingPosition());

            if (missOne > 1) {
                System.out.printf("\n%s's shot landed at %.2f (Missed by %.2f)\n", playerOne.getName(), shotOne,
                        missOne);
            }
            if (missTwo > 1) {
                System.out.printf("%s's shot landed at %.2f (Missed by %.2f)\n", playerTwo.getName(), shotTwo, missTwo);
            }
            // 5. Check if distance < 1 (Hit!).
            if (missOne < 1 || missTwo < 1) {
                hit = true;
                gamesPlayed++; // Increment for any win or tie
                if (missOne < 1 && missTwo < 1) {
                    System.out.println("It's a tie! Both hit!");
                    playerOne.setScore(playerOne.getScore() + 1);
                    playerTwo.setScore(playerTwo.getScore() + 1);
                    scoreBoard.add(
                            "Tie: " + playerOne.getName() + " and " + playerTwo.getName() + " Game: " + gamesPlayed
                                    + " Round: " + currentRound);
                } else if (missOne < 1) {
                    System.out.println(playerOne.getName() + " wins!");
                    playerOne.setScore(playerOne.getScore() + 1);
                    scoreBoard.add(
                            "Winner: " + playerOne.getName() + " Game: " + gamesPlayed + " Round: " + currentRound);
                } else {
                    System.out.println(playerTwo.getName() + " wins!");
                    playerTwo.setScore(playerTwo.getScore() + 1);
                    scoreBoard.add(
                            "Winner: " + playerTwo.getName() + " Game: " + gamesPlayed + " Round: " + currentRound);
                }
            } else {
                // 6. "Wait for Enter" before next turn.
                System.out.println("No hit! Press enter to continue to the next turn.");
                universalInput.nextLine();
                /* I want to add an option to return to main Menu for GUI version */
            }
        }
    }

    // TODO: After a winner is found, add the result to the ArrayList.

    private void showScoreboard() {
        if (scoreBoard.isEmpty()) {
            System.out.println("There is no scoreboard! Play a game first!");
            System.out.println("Press enter to go back to main menu");
            universalInput.nextLine();

        } else {
            // TODO: Iterate through ArrayList and print all past results.
            for (String eachsScore : scoreBoard) {
                System.out.println(eachsScore);
            }
            /*
             * Ammended this for now, no idea how to sort the arraylist based on
             * a specific parameter from the scoreboard string
             */
            // TODO: Find and print the best score (closest hit or most wins).
            // ArrayList<String> bestScore = new ArrayList<>(scoreBoard);
            // bestScore.sort(null);
            // System.out.println("Top Score: " + bestScore.get(0));

            System.out.println("Press enter to go back to main menu");
            universalInput.nextLine();
        }

    }

    private void showTutorial() {
        // TODO: Print the instructions and the physics formulas.
        System.out.println("This is a two player game.");
        System.out.println("To play simply enter in the usernames for your players.");
        System.out.println("The objective of the game is to hit the other player with your shot");
        System.out.println("Each player is placed at a random spot between 0 and 120");
        System.out.println(
                "Then each player sets the power and angle of their shot and attempts to hit the other player");
        System.out.println("First person to strike the other player wins");
        System.out.println("Press enter to go back to main menu");
        universalInput.nextLine();
    }

    private double getValidatedDouble(String prompt, double min, double max) {
        double value = 0;
        boolean isValid = false;
        while (!isValid) {
            System.out.println(prompt + " (" + min + "-" + max + "): ");
            String input = universalInput.nextLine(); // Always read as a string first
            try {
                value = Double.parseDouble(input); // Try to convert string to number
                // Check if it's within the required range
                if (value >= min && value <= max) {
                    isValid = true;
                } else {
                    System.out.println("Error: Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                // This runs if parseDouble() fails (e.g. user typed "abc")
                System.out.println("Error: Invalid input. Please enter a valid number.");
            }

        }
        return value;
    }

}
