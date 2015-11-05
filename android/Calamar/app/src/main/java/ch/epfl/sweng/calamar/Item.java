package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Models an Item, superclass of all the possibly many kind of 'item' the app manage. <br><br>
 *     known subclasses : <li>
 *                          <ul>{@link SimpleTextItem},</ul>
 *                          <ul>{@link LocalizedItem}</ul>
 *                        </li>
 *     Item is immutable
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

    /**
     * @return the 'from' field of the Item (sender)
     */
    public User getFrom() {
        return from;
    }

    /**
     * @return the 'to' field of the Item (recipient)
     */
    public Recipient getTo() {
        return to;
    }

    /**
     * @return the creation/posting date of the Item
     */
    public Date getDate() {
        return new Date(this.date);
    }

    public int getID(){
        return ID;
    }

    /**
     * Appends the fields of {@link Item} to a {@link JSONObject} representing the Item.<br>
     *     is called by the {@link #compose(JSONObject)} method of the child classes in
     *     a chain where each compose method append the field of its class to the object.<br>
     *         The chain begins by a call to {@link #toJSON()} in an instantiable child class.<br><br>
     * Should <b>NOT</b> be used alone.
     * @param json the json to which we append (using {@link JSONObject#accumulate(String, Object)} ) data
     * @throws JSONException
     */
    protected void compose(JSONObject json) throws JSONException {
        json.accumulate("ID", ID);
        json.accumulate("from", from.toJSON());
        json.accumulate("to", to.toJSON());
        json.accumulate("date", date);
    }

    /**
     * @return a JSONObject representing a {@link Item)
     * @throws JSONException
     */
    //must remains abstract if class not instantiable
    public abstract JSONObject toJSON() throws JSONException;

    /**
     * Parses an Item from a JSONObject.<br>
     * To instantiate the correct Item ({@link SimpleTextItem}, etc ...)
     * the JSON must have a 'type' field indicating the type...('simpleText', ...)
     * @param json the well formed {@link JSONObject json} representing the {@link Item item}
     * @return a {@link Item item} parsed from the JSONObject
     * @throws JSONException
     */
    public static Item fromJSON(JSONObject json) throws JSONException, IllegalArgumentException {
        if (null == json || json.isNull("type")) {
            throw new IllegalArgumentException("malformed json, either null or no 'type' value");
        }
        Item item;
        String type = json.getString("type");
        switch(type) {
            case "simpleText":
                item = SimpleTextItem.fromJSON(json);
                break;
            default:
                throw new IllegalArgumentException("Unexpected Item type (" + type + ")");
        }
        return item;
    }


    /**
     * A Builder for {@link Item}, has no build() method since Item isn't instantiable,
     * is used by the child builders (in {@link SimpleTextItem} or...) to build the "Item
     * part of the object". currently only used to parse JSON (little overkill..but ..)
     */
    protected static class Builder {
        protected int ID;
        protected User from;
        protected Recipient to;
        protected long date;

        public Builder parse(JSONObject o) throws JSONException {
            ID = o.getInt("ID");
            from = User.fromJSON(o.getJSONObject("from"));
            to = Recipient.fromJSON(o.getJSONObject("to"));
            date = o.getLong("date");
            return this;
        }
    }
}
