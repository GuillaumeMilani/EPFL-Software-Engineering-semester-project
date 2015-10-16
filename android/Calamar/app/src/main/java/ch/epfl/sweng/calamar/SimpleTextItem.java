package ch.epfl.sweng.calamar;

import java.util.Date;

/**
 * Created by LPI on 16.10.2015.
 */
public final class SimpleTextItem extends Item {
    private final String message;

    public SimpleTextItem(int ID, User from, Recipient to, Date date, String message) {
        super(ID, from, to, date.getTime());
        if(null == message) {
            throw new IllegalArgumentException("field 'message' cannor be null");
        }
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
