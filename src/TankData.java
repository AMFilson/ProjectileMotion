import java.util.Random;

/*
 * Name:    TankData.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 22nd 2026
 * Desc:    A simple data class that 
 *          stores the name and statistics for a tank unit in the game.
 */

/**
 * Represents a tank unit with randomized or fixed combat statistics.
 *
 * LEARNING (Data / Model Classes):
 * In Object-Oriented Programming it's good practice to separate your DATA from
 * your LOGIC from your DISPLAY. TankData is a pure data class — it just holds
 * values and lets you read them. It knows nothing about drawing itself or
 * making game decisions. This separation of concerns makes code easier to
 * maintain and test.
 *
 * How stats are used in-game:
 * - offensivePower: Sets the maximum shot power a player can use per turn.
 * - mobilityIndex: Sets how far a player can reposition their tank each round.
 */
public abstract class TankData {

    // -----------------------------------------------------------------------
    // LEARNING (private fields):
    // By marking fields 'private', we prevent other classes from reaching in
    // and changing them directly. All access goes through our public methods
    // (getters). This is called "Encapsulation" — one of the four pillars of OOP.
    // -----------------------------------------------------------------------

    /** The display name of this tank unit (e.g., "M8 GREYHOUND"). */
    private String name;

    /**
     * The offensive power stat. Controls the maximum projectile launch power.
     * A value between 0.0 and 100.0.
     */
    private double offensivePower;

    /**
     * The mobility index stat. Controls maximum positional shift per round.
     * A value between 0.0 and 100.0.
     */
    private double mobilityIndex;

    /**
     * Used internally to generate random stat values.
     * LEARNING (Random class): java.util.Random generates pseudo-random numbers.
     * 'nextDouble()' gives a value between 0.0 (inclusive) and 1.0 (exclusive).
     * Multiplying by 100 scales that to a 0–100 range.
     */
    private Random random;

    // -----------------------------------------------------------------------
    // LEARNING (Constructor Overloading):
    // Java allows multiple constructors on the same class as long as they have
    // different parameter lists. This is called "overloading". It lets the caller
    // choose how much information to provide when creating an object.
    // -----------------------------------------------------------------------

    /**
     * Constructs a TankData with EXPLICIT, fixed stats.
     * Use this when you need a specific, reproducible configuration
     * (e.g., testing, the legacy MainWindow constructor).
     *
     * @param name The tank's display name.
     * @param op   Offensive Power value (0–100).
     * @param mi   Mobility Index value (0–100).
     */
    public TankData(String name, double offensivePower, double mobilityIndex) {
        this.name = name;
        this.offensivePower = offensivePower;
        this.mobilityIndex = mobilityIndex;
        this.random = new Random();
    }

    /**
     * Constructs a TankData with RANDOMIZED stats.
     * This is the version used in the character selection screen so each
     * session feels different. Stats are rolled immediately by calling
     * rerollStats().
     *
     * @param name The tank's display name.
     */
    public TankData(String name) {
        this.name = name;
        this.random = new Random();
        this.rerollStats(); // Immediately generate random values on creation
    }

    /**
     * Re-randomizes both stats using the existing Random instance.
     *
     * LEARNING (random.nextDouble()):
     * 'random.nextDouble()' returns a value in [0.0, 1.0).
     * Multiplying by 100 scales the range to [0.0, 100.0).
     * This is a common technique to generate a random percentage.
     */
    public void rerollStats() {
        this.offensivePower = random.nextDouble() * 100;
        this.mobilityIndex = random.nextDouble() * 100;
    }

    // -----------------------------------------------------------------------
    // LEARNING (Getter Methods):
    // These are simple public methods that return the value of a private field.
    // Because the fields are private, this is the only way external code can
    // READ the data — which is intentional. If we wanted to allow external code
    // to CHANGE the data too, we'd add a corresponding 'setter' method.
    // -----------------------------------------------------------------------

    /** @return The display name of this tank unit. */
    public String getName() {
        return name;
    }

    /** @return The Offensive Power stat (max shot power, 0–100). */
    public double getOffensivePower() {
        return offensivePower;
    }

    /** @return The Mobility Index stat (max position shift per round, 0–100). */
    public double getMobilityIndex() {
        return mobilityIndex;
    }

    /**
     * Returns the relative file path to this tank's pixel-art PNG sprite.
     * All three tanks have matching PNGs in the images/ directory.
     * Using a centralised mapping here ensures every UI panel (CanvasArea,
     * CharacterSelectPanel, LeaderboardPanel etc.) loads the SAME image file,
     * so the sprites are always visually consistent.
     *
     * LEARNING (switch expression):
     * The '->' syntax is a Java 14+ switch expression. Unlike the old switch
     * statement it is an expression (produces a value) and doesn't fall through.
     *
     * @return Relative path to the PNG, e.g. "images/m8_greyhound.png"
     */
    public abstract String getImagePath();
}
