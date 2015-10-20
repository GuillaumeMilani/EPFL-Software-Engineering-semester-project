package ch.epfl.sweng.calamar;


import org.json.JSONException;
import org.json.JSONObject;

public abstract class Recipient {
    private final int ID;
    private final String name;

    protected Recipient(int ID, String name) {
        if (null == name) {
            throw new IllegalArgumentException("field 'name' cannot be null");
        }
        this.ID = ID;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return getName();
    }

    protected void compose(JSONObject o) throws JSONException {
        o.accumulate("ID", ID);
        o.accumulate("name", name);
    }

    public abstract JSONObject toJSON() throws JSONException;

    public static Recipient fromJSON(JSONObject json) throws JSONException, IllegalArgumentException {
        Recipient ret;
        switch (json.getString("type")) {
            case "user":
                ret = User.fromJSON(json);//todo : where is tag type ??..
                break;
            case "group":
            case "public":
            default:
                throw new IllegalArgumentException("Unexpected Recipient type");
        }
        return ret;
    }

    //TODO: hashcode + equals

    protected static class Builder {
        protected int ID = 0;
        protected String name = "default";

        public Builder parse(JSONObject o) throws JSONException {
            ID = o.getInt("ID");
            name = o.getString("name");
            return this;
        }
    }
}
