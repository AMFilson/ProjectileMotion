import java.util.Random;

public class TankData {
    private String name;
    private double offensivePower;
    private double mobilityIndex;
    private Random random;

    public TankData(String name, double op, double mi) {
        this.name = name;
        this.offensivePower = op;
        this.mobilityIndex = mi;
        this.random = new Random();
    }
    
    // For random generation
    public TankData(String name) {
        this.name = name;
        this.random = new Random();
        this.rerollStats();
    }

    public void rerollStats() {
        // Generate random double between 0 and 100
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
