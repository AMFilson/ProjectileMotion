import java.math.*;

/* 
Name:Player.java (ProjectileMotion)
Author:Andrew Filson
Date: March Friday the 13th 2026!
Desc: A player  in the game Projectile Motion
 */
/**
 * Represents a single player in the Projectile Motion game.
 * Stores player-specific data such as name, selected power, angle, 
 * starting position, and total score over multiple sessions.
 */
public class Player {
  /*
   * The plan is to define a player with the requisite properties, methods,
   * getters and setters
   */
  /* There should be something to track scores between games too */
  /* And a static method to track total amount of games played? */
  // --- FIELD VARIABLES (Encapsulation) ---
  // LEARNING: We use 'private' fields so that external classes can't randomly change 
  // these values. They must go through our public 'getter' and 'setter' methods instead.
  // This concept is known as 'Encapsulation' in Object-Oriented Programming.
  private String name;
  private int power;
  private int angle;
  private int startingPosition;
  private int score;
  /* private double shot; */

  /**
   * Initializes a new Player with the specified name.
   *
   * @param name The display name of the player.
   */
  public Player(String name) {
    this.name = name;

    /*
     * this.shot = shot;
     */
  }

  /**
   * Retrieves the current name of this player.
   *
   * @return Current player name.
   */
  public String getName() {
    return name;
  }

  public void setName(String inputName) {
    name = inputName;
  }

  public int getPower() {
    return power;
  }

  public void setPower(int power) {
    this.power = power;
  }

  public void setAngle(int angle) {
    this.angle = angle;
  }

  public int getAngle() {
    return angle;
  }

  /* added for score tracking */
  public int getScore() {
    return score;
  }

  /* added for score tracking */
  public void setScore(int score) {
    this.score = score;
  }

  public int getStartingPosition() {
    return startingPosition;
  }

  /**
   * Calculates the final X-coordinate where the projectile will land based on 
   * the player's selected power, angle, and starting position using standard projectile 
   * motion physics (ignoring air resistance).
   *
   * @return The final x-coordinate position of the shot's impact.
   */
  public double getShot() {
    // LEARNING: To calculate projectile motion, we need gravity. We declare it as 'final'
    // because gravity is a constant and shouldn't ever be changed while the game runs.
    final double GRAVITY = 9.81;
    
    // LEARNING: Math.sin() and Math.cos() expect radians, not degrees.
    // So we first convert the player's angle into radians using Math.toRadians().
    
    /* determine the time it takes for a shot to land */
    double timeToLand = (2 * getPower() * Math.sin(Math.toRadians(getAngle()))) / GRAVITY;
    
    /* get the x-coordinate of the shot */
    // LEARNING: Distance = velocity * time. We find the horizontal velocity (cos(angle) * power) 
    // and multiply it by the time it spends in the air. Finally, we add their starting position 
    // so we know exactly where it lands on the screen.
    return (getPower() * Math.cos(Math.toRadians(getAngle())) * timeToLand) + getStartingPosition();
  }

  /**
   * Randomly assigns and returns a new starting position on the X-axis for this player.
   * The new position will be randomly generated between 0 and 120 (inclusive).
   *
   * @return the assigned starting position.
   */
  public int setStartingPosition() {
    // LEARNING: Math.random() generates a decimal between 0.0 and 0.999...
    // By multiplying it by 121, we get a range from 0.0 to 120.999...
    // Casting to '(int)' chops off the decimal, giving us a clean integer between 0 and 120.
    return startingPosition = (int) (Math.random() * 121);
  }

}
