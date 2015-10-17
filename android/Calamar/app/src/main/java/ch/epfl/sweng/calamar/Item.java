package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

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

    protected void compose(JSONObject o) throws JSONException {
        o.accumulate("ID", ID);
        o.accumulate("from", from.toJSON());
        o.accumulate("to", to.toJSON()); //TODO how to infer type ?
        o.accumulate("date", date);
    }

    public static Item fromJSON(JSONObject json) throws JSONException, IllegalArgumentException {
        Item item;
        switch(json.getString("type")) {
            case "simpleText":
                item = SimpleTextItem.fromJSON(json);
                break;
            default:
                throw new IllegalArgumentException("Unexpected Item type");
        }
        return item;
    }

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
