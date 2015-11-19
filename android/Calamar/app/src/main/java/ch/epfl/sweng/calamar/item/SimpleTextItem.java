package ch.epfl.sweng.calamar.item;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Models a simple text {@link Item}, (no special properties, just a text message).<br><br>
 * SimpleTextItem is immutable
 */
public final class SimpleTextItem extends Item {
    private final String message;
    private final static Type ITEM_TYPE = Type.SIMPLETEXTITEM;

    /**
     * Instantiates a new SimpleTextItem with the following parameters
     *
     * @param ID        the ID
     * @param from      the 'from' field of the Item (sender)
     * @param to        the 'to' field of the Item (recipient)
     * @param date      the creation/posting date of the Item
     * @param message   the content (text message)
     * @param condition the condition
     * @see Item#Item(int, User, Recipient, long)
     */

    public SimpleTextItem(int ID, User from, Recipient to, Date date, Condition condition, String message) {
        super(ID, from, to, date.getTime(), condition);
        if (null == message) {
            throw new IllegalArgumentException("field 'message' cannot be null");
        }
        this.message = message;
    }

    public SimpleTextItem(int ID, User from, Recipient to, Date date, String message) {
        this(ID, from, to, date, Condition.trueCondition(), message);
    }

    /**
     * @return the text content (message) of the Item
     */
    public String getMessage() {
        return this.message;
    }

    @Override
    public Type getType() {
        return ITEM_TYPE;
    }

    @Override
    public View getView(Context context) {
        TextView res = new TextView(context);
        res.setText(message);
        return res;
    }

    /**
     * Appends the fields of SimpleTextItem to the given JSONObject. <br><br>
     * Should <b>NOT</b> be used alone
     *
     * @param json the json to which we append data
     * @throws JSONException
     */
    @Override
    public void compose(JSONObject json) throws JSONException {
        super.compose(json);
        json.accumulate("text", message);
        json.accumulate("type", ITEM_TYPE.name());
    }

    /**
     * Parses a SimpleTextItem from a JSONObject.<br>
     *
     * @param json the well formed {@link JSONObject json} representing the {@link SimpleTextItem item}
     * @return a {@link SimpleTextItem} parsed from the JSONObject
     * @throws JSONException
     * @see Item#fromJSON(JSONObject) Recipient.fromJSON
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

    /**
     * test if this is equals to other Object, true when object is a SimpleTextItem and content truly equals
     *
     * @param o Obect to compare this with
     * @return true if two SimpleTextItems are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleTextItem)) return false;
        SimpleTextItem that = (SimpleTextItem) o;
        return super.equals(that) && that.message.equals(message);
    }

    /**
     * hash the SimpleTextItem
     *
     * @return hash of the SimpleTextItem
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 73 + (message != null ? message.hashCode() : 0);
    }

    /**
     * A Builder for {@link SimpleTextItem}, currently only used to parse JSON (little overkill..but ..)
     *
     * @see Item.Builder
     */
    public static class Builder extends Item.Builder {
        private String message = "default message";

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if (!type.equals(SimpleTextItem.ITEM_TYPE.name())) {
                throw new IllegalArgumentException("expected " + SimpleTextItem.ITEM_TYPE.name() + " was : " + type);
            }
            message = json.getString("text");
            return this;
        }

        public SimpleTextItem build() {
            return new SimpleTextItem(super.ID, super.from, super.to, new Date(super.date), super.condition, message);
        }
    }
}
