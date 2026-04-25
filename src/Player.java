
/*
 * Name:    Player.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    March Friday the 13th 2026!
 * Desc:    Represents a single player in the Projectile Motion game.
 *          Stores all player-specific state: name, shot parameters, 
 *          starting position, tank selection, and running score.
 */

/**
 * Represents one human player in the game.
 *
 * LEARNING (What is a Class?):
 * A class is a BLUEPRINT for creating objects. Once you define Player here,
 * you can create as many Player objects as you need anywhere in the program:
 * Player p1 = new Player("Ghost");
 * Player p2 = new Player("Reaper");
 * Each has its OWN copy of name, score, angle, etc. — they're independent.
 *
 * LEARNING (Object-Oriented Design):
 * The plan is to define a player with the requisite properties, methods,
 * getters and setters. There should also be something to track scores
 * between games and potentially a static method to track total games played.
 */
public class Player {

    // -----------------------------------------------------------------------
    // LEARNING (Encapsulation — private fields):
    // We use 'private' access on all fields so that external classes can't
    // randomly assign invalid values (like a negative power or a name of null).
    // All access must go through specific 'getter' and 'setter' methods below,
    // which is where you could add validation logic in the future.
    // -----------------------------------------------------------------------

    /** The player's display name, set during character selection. */
    private String name;

    /**
     * Shot power entered by the player (1–100, capped by tank's offensivePower).
     * Used in the projectile physics calculation.
     */
    private int power;

    /**
     * Launch angle in degrees (0–180).
     * - 0° = horizontal left
     * - 90° = straight up
     * - 180° = horizontal right
     */
    private int angle;

    /**
     * The player's X-coordinate current position on the battlefield (0–200).
     */
    private int position;

    /** The tank data associated with this player */
    private TankData tank;

    /**
     * Cumulative wins across multiple rounds in a session.
     * Displayed on the Leaderboard.
     */
    private int score;

    /**
     * The tank the player has selected from the carousel (0 = M8 Greyhound,
     * 1 = Flak 88, 2 = Black Cat). Default is index 0.
     */
    private int selectedTankIndex = 0;

    // -----------------------------------------------------------------------
    // LEARNING (Constructor):
    // A constructor is a special method that runs when you create a new object
    // with 'new Player("name")'. It sets up the initial state of the object.
    // It has the same name as the class and no return type.
    // -----------------------------------------------------------------------

    /**
     * Creates a new Player with the given display name.
     * All other stats start at their default zero values.
     *
     * @param name The player's display name (e.g., "GHOST", "REAPER").
     */
    public Player(String name) {
        this.name = name;
        // 'this.name' → the field on THIS object
        // 'name' → the parameter passed in by the caller
        // The 'this.' prefix distinguishes them when they share the same identifier.
    }

    // -----------------------------------------------------------------------
    // LEARNING (Getters and Setters):
    // These short methods are the controlled "doorways" into and out of
    // the private fields. The naming convention in Java is:
    // - getFieldName() → returns the value
    // - setFieldName(value) → assigns a new value
    // IDE tools (like VS Code) can generate these for you automatically.
    // -----------------------------------------------------------------------

    /** @return The index of the tank selected by this player (0–2). */
    public int getSelectedTankIndex() {
        return selectedTankIndex;
    }

    /**
     * Sets this player's chosen tank by its position in the tanks list.
     * 
     * @param index 0 = M8 Greyhound, 1 = Flak 88, 2 = Black Cat.
     */
    public void setSelectedTankIndex(int index) {
        this.selectedTankIndex = index;
    }

    /** @return The TankData assigned to this player */
    public TankData getTank() {
        return tank;
    }

    /** @param tank The TankData to assign */
    public void setTank(TankData tank) {
        this.tank = tank;
    }

    /** @return This player's current display name. */
    public String getName() {
        return name;
    }

    /** @param newName The new name to assign to this player. */
    public void setName(String newName) {
        name = newName;
    }

    /** @return This player's currently configured shot power (1–100). */
    public int getPower() {
        return power;
    }

    /**
     * @param power New power value. Should be between 1 and the tank's
     *              offensivePower cap.
     */
    public void setPower(int power) {
        this.power = power;
    }

    /** @param angle New launch angle in degrees (0–180). */
    public void setAngle(int angle) {
        this.angle = angle;
    }

    /** @return This player's current launch angle in degrees. */
    public int getAngle() {
        return angle;
    }

    /** @return This player's total cumulative score (number of hits/wins). */
    public int getScore() {
        return score;
    }

    /**
     * @param score The new score to assign (typically incremented by 1 on a win).
     */
    public void setScore(int score) {
        this.score = score;
    }

    /** @return This player's current X-axis position. */
    public int getPosition() {
        return position;
    }

    /** @param position The new position to assign. */
    public void setPosition(int position) {
        this.position = position;
    }

    // -----------------------------------------------------------------------
    // LEARNING (Projectile Motion Physics):
    // The formula used here is derived from kinematics. In the absence of
    // air resistance, a projectile's trajectory is determined by:
    // - Its initial velocity (power)
    // - Its launch angle (angle, converted to radians)
    // - Gravity (a constant downward acceleration: 9.81 m/s²)
    //
    // TIME IN AIR: t = (2 * v * sin(θ)) / g
    // RANGE (X): x = v * cos(θ) * t
    // FINAL POS: landX = startingPosition + x
    // -----------------------------------------------------------------------

    /**
     * Calculates and returns the X-coordinate where this player's shot will land,
     * using standard projectile motion physics (no air resistance or wind).
     *
     * @return The X-position of the projectile's impact point.
     */
    public double getShot() {
        // Gravity is a constant (it never changes mid-game), so we mark it 'final'.
        // LEARNING (final keyword): A 'final' local variable can only be assigned once.
        // This is useful for constants to make your intent clear to other developers.
        final double GRAVITY = 9.81;

        // LEARNING (Radians vs Degrees):
        // Java's Math.sin() and Math.cos() functions expect angles in RADIANS,
        // not degrees. One full circle = 2π radians = 360°.
        // Math.toRadians(degrees) performs the conversion for us.

        // Step 1: Calculate how long the projectile stays in the air.
        // Formula: time = (2 * initialVelocity * sin(angle)) / gravity
        double timeToLand = (2 * getPower() * Math.sin(Math.toRadians(getAngle()))) / GRAVITY;

        // Step 2: Calculate the total horizontal distance travelled, then add
        // the player's starting position to get the absolute X-coordinate of impact.
        // Formula: distance = velocity * cos(angle) * time
        // Then: landX = position + distance
        return (getPower() * Math.cos(Math.toRadians(getAngle())) * timeToLand) + getPosition();
    }

    public int randomizePosition(int offset) {
        return position = offset + (int) (Math.random() * 100);
    }

}
