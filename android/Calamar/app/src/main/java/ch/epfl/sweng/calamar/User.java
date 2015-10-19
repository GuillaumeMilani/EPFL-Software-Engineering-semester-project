package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

public final class User extends Recipient {
    //type = user, virtual field ^^

    public User(int ID, String name) {
        super(ID, name);
    }

    @Override
    public void compose(JSONObject o) throws JSONException {
        super.compose(o);
        o.accumulate("type", "user");
    }

    public static User fromJSON(JSONObject json) throws JSONException {
        return new User.Builder().parse(json).build();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        this.compose(json);
        return json;
    }

    private static class Builder extends Recipient.Builder {
        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            return this;
        }
        public User build() {
            return new User(super.ID, super.name);
        }
    }
}
