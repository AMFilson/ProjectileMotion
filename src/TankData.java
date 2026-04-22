import java.util.Random;

/**
 * Represents a tank unit with fixed or randomized combat statistics.
 * offensivePower caps the player's max shot power during battle.
 * mobilityIndex caps how far a player can reposition each round.
 */
public class TankData {
    private String name;
    private double offensivePower;
    private double mobilityIndex;
    private Random random;

    /** Constructor with explicit fixed stats. */
    public TankData(String name, double op, double mi) {
        this.name = name;
        this.offensivePower = op;
        this.mobilityIndex = mi;
        this.random = new Random();
    }

    /** Constructor that randomizes stats on creation. */
    public TankData(String name) {
        this.name = name;
        this.random = new Random();
        this.rerollStats();
    }

    public void rerollStats() {
        this.offensivePower = random.nextDouble() * 100;
        this.mobilityIndex = random.nextDouble() * 100;
    }

    public String getName() {
        return name;
    }

    public double getOffensivePower() {
        return offensivePower;
    }

    public double getMobilityIndex() {
        return mobilityIndex;
    }
}
