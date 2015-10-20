package ch.epfl.sweng.calamar;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Models a recipient (name and ID).<br>
 *     known subclasses : <li>
 *                          <ul>{@link User},</ul>
 *                          <ul>{@link Group}</ul>
 *                        </li>
 *     Recipient is immutable
 */
public abstract class Recipient {
    /**
     * the id of the Recipient
     */
    private final int ID;
    /**
     * the name of the Recipient ("Bob" ...)
     */
    private final String name;

    protected Recipient(int ID, String name) {
        if (null == name) {
            throw new IllegalArgumentException("field 'name' cannot be null");
        }
        this.ID = ID;
        this.name = name;
    }

    /**
     * @return the name of the {@link Recipient recipient}
     */
    public String getName() {
        return name;
    }

    /**
     * @return the ID of the {@link Recipient recipient}
     */
    public int getID() {
        return ID;
    }

    /**
     * @return the name of the recipient... using {@link #getName()}
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Appends the fields of {@link Recipient} to a {@link JSONObject} representing the Recipient.<br>
     *     is called by the {@link #compose(JSONObject)} method of the child classes in
     *     a chain where each compose method append the field of its class to the objects.<br>
     *         The chain begins by a call to {@link #toJSON()} in an instantiable child class.<br>
     * Should <b>NOT</b> be used alone.
     * @param json the json to which we append data
     * @throws JSONException
     */
    protected void compose(JSONObject json) throws JSONException {
        json.accumulate("ID", ID);
        json.accumulate("name", name);
    }

    /**
     * @return a JSONObject representing a {@link Recipient)
     * @throws JSONException
     */
    //must remains abstract if class not instantiable
    public abstract JSONObject toJSON() throws JSONException;

    /**
     * Parses a Recipient from a JSONObject.<br>
     * To instantiate the correct Recipient ({@link User}, {@link Group}, ...)
     * the JSON must have a 'type' field indicating the type...('user', 'group')
     * @param json the well formed {@link JSONObject json} representing the {@link Recipient recipient}
     * @return a {@link Recipient recipient} parsed from the JSONObject
     * @throws JSONException
     */
    public static Recipient fromJSON(JSONObject json) throws JSONException {
        if (null == json || json.isNull("type")) {
            throw new IllegalArgumentException("malformed json, either null or no 'type' value");
        }
        Recipient ret;
        String type = json.getString("type");
        switch (type) {
            case "user":
                ret = User.fromJSON(json);
                break;
            case "group":
            case "public":
            default:
                throw new IllegalArgumentException("Unexpected Recipient type (" + type + ")");
        }
        return ret;
    }

    //TODO: hashcode + equals

    /**
     * A Builder for {@link Recipient}, has no build() method since Recipient isn't instantiable,
     * is used by the child builders (in {@link User} or {@link Group} or..) to build the "Recipient
     * part of the object". currently only used to parse JSON (little overkill..but ..)
     */
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
