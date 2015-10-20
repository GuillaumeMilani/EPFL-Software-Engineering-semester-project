package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public final class SimpleTextItem extends Item {
    private final String message;

    public SimpleTextItem(int ID, User from, Recipient to, Date date, String message) {
        super(ID, from, to, date.getTime());
        if(null == message) {
            throw new IllegalArgumentException("field 'message' cannot be null");
        }
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public void compose(JSONObject o) throws JSONException {
        super.compose(o);
        o.accumulate("message", message);
        o.accumulate("type", "simpleText");
    }

    public static SimpleTextItem fromJSON(JSONObject json) throws JSONException {
        return new SimpleTextItem.Builder().parse(json).build();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject ret = new JSONObject();
        this.compose(ret);
        return ret;
    }

    public static class Builder extends Item.Builder {
        private String message = "default message";

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            if(!json.getString("type").equals("simpleText")) {
                throw new IllegalArgumentException("expected text Item");
            }
            message = json.getString("text"); // Text or message ?
            return this;
        }

        public SimpleTextItem build() {
            return new SimpleTextItem(super.ID, super.from, super.to, new Date(super.date), message);
        }
    }
}
