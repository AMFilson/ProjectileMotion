/*
 * Name:    Flak88.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 25th 2026
 * Desc:    Represents the Flak 88 tank unit.
 */

public class Flak88 extends TankData {

    public Flak88() {
        super("FLAK 88");
    }

    public Flak88(double offensivePower, double mobilityIndex) {
        super("FLAK 88", offensivePower, mobilityIndex);
    }

    @Override
    public String getImagePath() {
        return "images/flak_88.png";
    }
}
