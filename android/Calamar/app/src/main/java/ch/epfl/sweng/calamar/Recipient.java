package ch.epfl.sweng.calamar;

/**
 * Created by LPI on 16.10.2015.
 */
public abstract class Recipient {
    private final int ID;
    private final String name;

    protected Recipient(int ID, String name) {
        if(null == name) {
            throw new IllegalArgumentException("field 'name' cannot be null");
        }
        this.ID = ID;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return getName();
    }

    //TODO: hashcode + equals
}
