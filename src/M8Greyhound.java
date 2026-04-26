/*
 * Name:    M8Greyhound.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 25th 2026
 * Desc:    Implementation of the M8 Greyhound light tank.
 */

/* 
 * =========================================================================
 * LEARNING: INHERITANCE & THE 'SUPER' KEYWORD
 * =========================================================================
 * 
 * Subclasses:
 * M8Greyhound 'extends' TankData. This means M8Greyhound is a specialized 
 * version of a TankData object. It inherits all the methods and fields 
 * (like 'name', 'health', 'mobilityIndex') from its parent class.
 * 
 * The 'super' Keyword:
 * In the constructor, we call 'super("M8-GREYHOUND", pwr, mob)'. This tells 
 * Java to immediately invoke the constructor of the parent class (TankData) 
 * so that all the base attributes are set up properly before we do anything 
 * else in the subclass.
 * =========================================================================
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
