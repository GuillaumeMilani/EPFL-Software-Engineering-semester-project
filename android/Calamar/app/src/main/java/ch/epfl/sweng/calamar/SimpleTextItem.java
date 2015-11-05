package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Models a simple text {@link Item}, (no special properties, just a text message).<br><br>
 *     SimpleTextItem is immutable
 */
public final class SimpleTextItem extends Item {
    private final String message;
    private final static String ITEM_TYPE = "simpleText";

    /**
     * Instantiates a new SimpleTextItem with the following parameters
     * @param ID the ID
     * @param from the 'from' field of the Item (sender)
     * @param to the 'to' field of the Item (recipient)
     * @param date the creation/posting date of the Item
     * @param message the content (text message)
     * @see Item#Item(int, User, Recipient, long)
     */
    public SimpleTextItem(int ID, User from, Recipient to, Date date, String message) {
        super(ID, from, to, date.getTime());
        if(null == message) {
            throw new IllegalArgumentException("field 'message' cannot be null");
        }
        this.message = message;
    }

    /**
     * @return the text content (message) of the Item
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Appends the fields of SimpleTextItem to the given JSONObject. <br><br>
     *     Should <b>NOT</b> be used alone
     * @param json the json to which we append data
     * @throws JSONException
     */
    @Override
    public void compose(JSONObject json) throws JSONException {
        super.compose(json);
        json.accumulate("message", message);
        json.accumulate("type", ITEM_TYPE);
    }

    /**
     * Parses a SimpleTextItem from a JSONObject.<br>
     * @param json the well formed {@link JSONObject json} representing the {@link SimpleTextItem item}
     * @return a {@link SimpleTextItem} parsed from the JSONObject
     * @throws JSONException
     * @see ch.epfl.sweng.calamar.Item#fromJSON(JSONObject) Recipient.fromJSON
     */
    public static SimpleTextItem fromJSON(JSONObject json) throws JSONException {
        return new SimpleTextItem.Builder().parse(json).build();
    }

    /**
     * @return a JSONObject representing a {@link SimpleTextItem}
     * @throws JSONException
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject ret = new JSONObject();
        this.compose(ret);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof SimpleTextItem) ) return false;
        SimpleTextItem that = (SimpleTextItem)o;
        return super.equals(that) && that.message.equals(message);
    }

    @Override
    public int hashCode() {
        return super.hashCode()*73+(message != null ? message.hashCode() : 0);
    }

    /**
     * A Builder for {@link SimpleTextItem}, currently only used to parse JSON (little overkill..but ..)
     * @see ch.epfl.sweng.calamar.Item.Builder
     */
    public static class Builder extends Item.Builder {
        private String message = "default message";

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if(!type.equals(SimpleTextItem.ITEM_TYPE)) {
                throw new IllegalArgumentException("expected "+ SimpleTextItem.ITEM_TYPE +" was : "+type);
            }
            message = json.getString("text");
            return this;
        }

        public SimpleTextItem build() {
            return new SimpleTextItem(super.ID, super.from, super.to, new Date(super.date), message);
        }
    }
}
