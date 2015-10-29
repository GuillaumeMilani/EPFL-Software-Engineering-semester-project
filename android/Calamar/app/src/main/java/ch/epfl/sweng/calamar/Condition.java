package ch.epfl.sweng.calamar;

/**
 * Created by pierre on 10/27/15.
 */
public abstract class Condition {
    /**
     * test if parameters obj satisfy condition
     * @return if obj match condition or not
     */
    public abstract boolean matches();

    public static Condition and(final Condition c1, final Condition c2)
    {
        return new Condition() {
            @Override
            public boolean matches() {
                return c1.matches() && c2.matches();
            }
        };
    }

    public static Condition or(final Condition c1, final Condition c2)
    {
        return new Condition() {
            @Override
            public boolean matches() {
                return c1.matches() || c2.matches();
            }
        };
    }

    public static Condition not(final Condition c)
    {
        return new Condition() {
            @Override
            public boolean matches() {
                return !c.matches();
            }
        };
    }
}
