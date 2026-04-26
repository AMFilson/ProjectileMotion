/*
 * Name:    Flak88.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 25th 2026
 * Desc:    Implementation of the Flak 88 anti-aircraft artillery unit.
 */

/* 
 * =========================================================================
 * LEARNING: INHERITANCE IN ACTION
 * =========================================================================
 * 
 * Flak88 is another example of Inheritance. By extending TankData, we create 
 * a completely different unit with different stats (high power, low mobility) 
 * but we don't have to rewrite any of the health tracking, damage calculation, 
 * or getter methods. They are all provided for free by the parent class!
 * =========================================================================
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
