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
    //TODO date d'expiration ?

    protected Item(int ID, User from, Recipient to, long date) {
        if(null == from || null == to) {
            throw new IllegalArgumentException("field 'from' and/or 'to' cannot be null");
        }
        this.ID = ID;
        this.from = from; //User is immutable
        this.to = to;     //Recipient is immutable
        this.date = date;
    }

    //TODO: maybe better field names in java...
    public User getFrom() {
        return from;
    }

    public Recipient getTo() {
        return to;
    }

    public Date getDate() {
        return new Date(this.date);
    }

    //TODO: hashcode equals
}
