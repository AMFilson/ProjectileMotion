/*
 * Name:    BlackCat.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 25th 2026
 * Desc:    Represents the Black Cat tank unit.
 */

public class BlackCat extends TankData {

    public BlackCat() {
        super("BLACK CAT");
    }

    public BlackCat(double offensivePower, double mobilityIndex) {
        super("BLACK CAT", offensivePower, mobilityIndex);
    }

    @Override
    public String getImagePath() {
        return "images/black_cat.png";
    }
}
