/*
 * Name:    M8Greyhound.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 25th 2026
 * Desc:    Represents the M8 Greyhound tank unit.
 */

public class M8Greyhound extends TankData {

    public M8Greyhound() {
        super("M8 GREYHOUND");
    }

    public M8Greyhound(double offensivePower, double mobilityIndex) {
        super("M8 GREYHOUND", offensivePower, mobilityIndex);
    }

    @Override
    public String getImagePath() {
        return "images/m8_greyhound.png";
    }
}
