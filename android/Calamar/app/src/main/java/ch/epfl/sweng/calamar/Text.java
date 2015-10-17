package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/17/15.
 */
public class Text extends Item {
    private String data;

    @Override
    public void parse(JSONObject o) throws JSONException {
        super.parse(o);
        if(!o.getString("type").equals("text")) throw new IllegalArgumentException("expected text Item");
        data = o.getString("data");
    }
}
