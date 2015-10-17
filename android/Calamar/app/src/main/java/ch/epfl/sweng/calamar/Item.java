package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/17/15.
 */
public abstract class Item {
    private int ID;
    private User from;
    private Recipient to;
    private long date;

    public void parse(JSONObject o) throws JSONException {
        ID = o.getInt("ID");
        from = new User();
        from.parse(o.getJSONObject("from"));
        to = Recipient.fromJSON(o.getJSONObject("to"));
        date = o.getLong("date");
    }

    public void compose(JSONObject o) throws JSONException {
        o.accumulate("ID", ID);
        o.accumulate("from", Recipient.toJSON(from));
        o.accumulate("to", Recipient.toJSON(to));
        o.accumulate("date", date);
    }

    public static Item fromJSON(JSONObject o) throws JSONException, IllegalArgumentException {
        Item item;
        switch(o.getString("type")) {
            case "text":
                item = new Text();
                item.parse(o);
                break;
            default:
                throw new IllegalArgumentException("Unexpected Item type");
        }
        return item;
    }

    public static JSONObject toJSON(Item i) throws JSONException {
        JSONObject ret = new JSONObject();
        i.compose(ret);
        return ret;
    }
}
