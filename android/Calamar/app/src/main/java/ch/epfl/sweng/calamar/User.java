package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

public final class User extends Recipient {
    //type = user, virtual field ^^

    public User(int ID, String name) {
        super(ID, name);
    }

    @Override
    protected void compose(JSONObject json) throws JSONException {
        super.compose(json);
        json.accumulate("type", "user");
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

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof User) ) return false;
        User that = (User)o;
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
