package ch.epfl.sweng.calamar;

import android.location.Location;

import java.util.Date;

/**
 * Created by LPI on 16.10.2015.
 */
public abstract class LocalizedItem extends Item {
    private final Location location;

    protected LocalizedItem(int ID, User from, Recipient to, Date date, Location location) {
        super(ID, from, to, date.getTime());
        if(null == location) {
            throw new IllegalArgumentException("field 'location' cannot be null");
        }
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
