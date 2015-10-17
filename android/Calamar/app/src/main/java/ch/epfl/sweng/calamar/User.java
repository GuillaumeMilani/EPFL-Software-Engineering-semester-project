package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/17/15.
 */
public class User extends Recipient {

    @Override
    public void parse(JSONObject o) throws JSONException {
        super.parse(o);
    }

    @Override
    public void compose(JSONObject o) throws JSONException {
        super.compose(o);
        o.accumulate("type", "user");
    }
}
