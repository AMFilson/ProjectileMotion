import java.math.*;

public class Player {
  /*
   * The plan is to define a player with the requisite properties, methods,
   * getters and setters
   */
  /* There should be something to track scores between games too */
  /* And a static method to track total amount of games played? */
  private String name;
  private int power;
  private int angle;
  private int startingPosition;
  static private int gamesPlayed = 0;
  private int score = 0;
  /* private double shot; */

  public Player(String name) {
    this.name = name;

    /*
     * this.shot = shot;
     */
  }

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

  public int getStartingPosition() {
    return startingPosition;
  }

  public double getShot() {
    final double GRAVITY = 9.81;
    /* determine the time it takes for a shot to land */
    double timeToLand = (2 * getPower() * Math.sin(Math.toRadians(getAngle()))) / GRAVITY;
    /* get the x-coordinate of the shot */
    return (getPower() * Math.cos(Math.toRadians(getAngle())) * timeToLand) + getStartingPosition();
  }

  public int setStartingPosition() {
    return startingPosition = (int) (Math.random() * 121);
    // has to be 121 because int rounds down 120.99 to 120;
  }

  public void addWin() {
    // Save details of won game here
    int finalScore = score;
    int winningGame = gamesPlayed + 1;
    // append these details to an array list inside of game.java
  }

}
