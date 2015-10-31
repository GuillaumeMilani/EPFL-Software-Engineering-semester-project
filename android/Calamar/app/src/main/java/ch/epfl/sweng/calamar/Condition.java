package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/27/15.
 */
public abstract class Condition {
    /**
     * test if parameters obj satisfy condition
     * @return if obj match condition or not
     */
    public abstract boolean matches();

    protected abstract void compose(JSONObject json) throws JSONException;

    public JSONObject toJSON() throws JSONException {
        JSONObject ret = new JSONObject();
        this.compose(ret);
        return ret;
    }

    public static Condition fromJSON(JSONObject json) throws JSONException, IllegalArgumentException {
        if (null == json || json.isNull("type")) {
            throw new IllegalArgumentException("malformed json, either null or no 'type' value");
        }
        Condition cond;
        String type = json.getString("type");
        switch(type) {
            case "position":
                cond = PositionCondition.fromJSON(json);
                break;
            case "and":
                cond = and(fromJSON(json.getJSONObject("a")), fromJSON(json.getJSONObject("b")));
                break;
            case "or":
                cond = or(fromJSON(json.getJSONObject("a")), fromJSON(json.getJSONObject("b")));
                break;
            case "not":
                cond = not(fromJSON(json.getJSONObject("val")));
                break;
            default:
                throw new IllegalArgumentException("Unexpected Item type (" + type + ")");
        }
        return cond;
    }


    public static Condition and(final Condition c1, final Condition c2)
    {
        return new Condition() {
            @Override
            public boolean matches() {
                return c1.matches() && c2.matches();
            }

            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate("type", "and");
                json.accumulate("a" , c1.toJSON());
                json.accumulate("b", c2.toJSON());
            }
        };
    }

    public static Condition or(final Condition c1, final Condition c2)
    {
        return new Condition() {
            @Override
            public boolean matches() {
                return c1.matches() || c2.matches();
            }

            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate("type", "or");
                json.accumulate("a" , c1.toJSON());
                json.accumulate("b" , c2.toJSON());
            }
        };
    }

    public static Condition not(final Condition c)
    {
        return new Condition() {
            @Override
            public boolean matches() {
                return !c.matches();
            }

            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate("type", "not");
                json.accumulate("val" , c.toJSON());
            }
        };
    }

    protected static class Builder {

        public Builder parse(JSONObject o) throws JSONException {
            return this;
        }
    }
}
