package ch.epfl.sweng.calamar.recipient;


import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;

/**
 * Models a recipient (name and ID).<br>
 * known subclasses : <li>
 * <ul>{@link User},</ul>
 * <ul>{@link Group}</ul>
 * </li>
 * Recipient is immutable
 */
public abstract class Recipient {

    public final static int DEFAULT_ID = -1;
    protected final static String JSON_TYPE = "type";
    private final static String JSON_ID = "ID";
    private final static String JSON_NAME = "name";
    private final static String TYPE_USER = "user";
    /**
     * the id of the Recipient
     */
    private final int ID;
    /**
     * the name of the Recipient ("Bob" ...)
     */
    private final String name;
    /**
     * if the recipient is highlight
     */
    private Boolean highlight = false;

    protected Recipient(int ID, String name) {
        if (null == name) {
            throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.field_name_null));
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

    public Boolean getHighlight() {
        return highlight;
    }

    public void setHighlight(Boolean highlight) {
        this.highlight = highlight;
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
     * is called by the {@link #compose(JSONObject)} method of the child classes in
     * a chain where each compose method append the field of its class to the objects.<br>
     * The chain begins by a call to {@link #toJSON()} in an instantiable child class.<br><br>
     * Should <b>NOT</b> be used alone.
     *
     * @param json the json to which we append (using {@link JSONObject#accumulate(String, Object)} ) data
     * @throws JSONException
     */
    protected void compose(JSONObject json) throws JSONException {
        json.accumulate(JSON_ID, ID);
        json.accumulate(JSON_NAME, name);
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
     *
     * @param json the well formed {@link JSONObject json} representing the {@link Recipient recipient}
     * @return a {@link Recipient recipient} parsed from the JSONObject
     * @throws JSONException
     */
    public static Recipient fromJSON(JSONObject json) throws JSONException {
        if (null == json || json.isNull(JSON_TYPE)) {
            throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.malformed_json));
        }
        Recipient ret;
        String type = json.getString(JSON_TYPE);
        switch (type) {
            case TYPE_USER:
                ret = User.fromJSON(json);
                break;
            //case "group":
            default:
                throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.unexpected_recipient_type, type));
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Recipient)) return false;
        Recipient that = (Recipient) o;
        return that.name.equals(name) && that.ID == ID;
    }

    @Override
    public int hashCode() {
        return ID + name.hashCode() * 89;
    }


    /**
     * A Builder for {@link Recipient}, has no build() method since Recipient isn't instantiable,
     * is used by the child builders (in {@link User} or {@link Group} or..) to build the "Recipient
     * part of the object". currently only used to parse JSON (little overkill..but ..)
     */
    protected static class Builder {
        protected int ID = 0;
        protected String name = "default";

        public Builder parse(JSONObject o) throws JSONException {
            ID = o.getInt(JSON_ID);
            name = o.getString(JSON_NAME);
            return this;
        }
    }
}
