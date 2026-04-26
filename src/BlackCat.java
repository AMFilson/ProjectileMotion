/*
 * Name:    BlackCat.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 25th 2026
 * Desc:    Implementation of the Black Cat tank destroyer unit.
 */

/* 
 * =========================================================================
 * LEARNING: METHOD OVERRIDING (Polymorphism)
 * =========================================================================
 * 
 * Method Overriding (the @Override annotation):
 * While inheritance gives us all the parent's methods for free, sometimes we 
 * want a subclass to behave differently! If BlackCat had a special armor type, 
 * we could write:
 * 
 * @Override
 * public void takeDamage(double amount) { ... custom logic ... }
 * 
 * This overrides the parent's default behavior, allowing the BlackCat to 
 * respond to damage differently. This ability to treat different subclasses 
 * as the same base type (TankData) while they behave uniquely is called 
 * Polymorphism!
 * =========================================================================
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
