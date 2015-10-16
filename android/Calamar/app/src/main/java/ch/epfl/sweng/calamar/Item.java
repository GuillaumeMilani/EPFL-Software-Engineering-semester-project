package ch.epfl.sweng.calamar;

import java.util.Date;

/**
 * Created by LPI on 16.10.2015.
 */
public abstract class Item {
    private final int ID;
    private final User from;
    private final Recipient to;
    private final long date; //posix date


    public User getFrom() {
        return from;
    }

    public Recipient getTo() {
        return to;
    }

    public Date getDate() {
        return new Date(this.date);
    }
}
