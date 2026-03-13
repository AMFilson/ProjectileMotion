import java.util.ArrayList;
import java.util.Scanner;

/**
 * Game Class Blueprint
 * This class handles the menu system, game sessions, and scoreboard.
 */
public class Game {
    // 1. Declare your ArrayList here to store scores between games.
    // 2. Declare your two Player objects here so they persist.
    // 3. Declare a Scanner to be used throughout the class.

    public Game() {
        // Constructor: Initialize your scanner and lists here.
    }

    /**
     * The main entry point for the game logic.
     * Should contain a loop for the menu system.
     */
    public void start() {
        // TODO: Ask for player names here (only once per program run).
        
        // TODO: Main Menu Loop
        // - Choice 1: Start New Game
        // - Choice 2: Scoreboard
        // - Choice 3: Tutorial
        // - Choice 4: Exit (Ends program)
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

    // TIP: Create a helper method for the Try-Catch input validation 
    // to keep your code clean and reusable!
}
