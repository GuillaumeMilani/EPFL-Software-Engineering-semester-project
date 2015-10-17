package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/17/15.
 */
public abstract class Recipient {
    private int ID;

    public void parse(JSONObject o) throws JSONException {
        ID = o.getInt("ID");
    }

    public static Recipient fromJSON(JSONObject o) throws JSONException, IllegalArgumentException {
        Recipient ret;
        switch(o.getString("type")) {
            case "user":
                ret = new User();
                ret.parse(o);
                break;
            case "group":
            case "public":
            default:
                throw new IllegalArgumentException("Unexpected Recipient type");
        }
        return ret;
    }
}
