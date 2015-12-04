package ch.epfl.sweng.calamar.recipient;

import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;

/**
 * Models an user, (kind of {@link Recipient}).<br><br>
 * User is immutable.
 */
public final class User extends Recipient {
    public static final int PUBLIC_ID = -1;
    public static final String PUBLIC_NAME = "Public";
    private final static String DUMMY_PASSWORD = "password";
    private final static String RECIPIENT_TYPE = "user";


    /**
     * Instantiates a new User with <i>ID</i> and <i>name</i>
     *
     * @param ID   , the id
     * @param name , the name of the user ("Bob"..)
     */
    public User(int ID, String name) {
        super(ID, name);
    }

    /**
     * Appends the fields of User (currently nothing but the "virtual" field type = RECIPIENT_TYPE)
     * to the given JSONObject. <br><br>
     * Should <b>NOT</b> be used alone
     *
     * @param json the json to which we append data
     * @throws JSONException
     */
    @Override
    protected void compose(JSONObject json) throws JSONException {
        super.compose(json);//adds parent fields
        json.accumulate(JSON_TYPE, User.RECIPIENT_TYPE);
    }

    /**
     * Parses a User from a JSONObject.<br>
     *
     * @param json the well formed {@link JSONObject json} representing the {@link User user}
     * @return a {@link User user} parsed from the JSONObject
     * @throws JSONException
     * @see Recipient#fromJSON(JSONObject) Recipient.fromJSON
     */
    public static User fromJSON(JSONObject json) throws JSONException {
        return new User.Builder().parse(json).build();
    }

    /**
     * @return a JSONObject representing a {@link User}
     * @throws JSONException
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        this.compose(json);
        return json;
    }

    /**
     * java equals
     *
     * @param o other Object to compare this with
     * @return true if o is equal in value to this
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return super.equals(that);
    }

    /**
     * Returns the password of the user to decrypt data on the phone.
     *
     * @return a constant String "password" for now.
     */
    public String getPassword() {
        //TODO Change this method once a better encoding is found
        return DUMMY_PASSWORD;
    }

    /**
     * A Builder for {@link User}, currently only used to parse JSON (little overkill..but ..)
     *
     * @see Recipient.Builder
     */
    private static class Builder extends Recipient.Builder {
        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString(JSON_TYPE);
            if (!type.equals(User.RECIPIENT_TYPE)) {
                throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.expected_but_was, RECIPIENT_TYPE, type));
            }
            return this;
        }

        public User build() {
            return new User(super.ID, super.name);
        }
    }
}
