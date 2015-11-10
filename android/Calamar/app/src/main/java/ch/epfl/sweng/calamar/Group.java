package ch.epfl.sweng.calamar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a Group (of users). <br><br>
 *     Group is immutable.
 * Created by LPI on 16.10.2015.
 */
public final class Group extends Recipient {
    private final List<User> users;
    private final static String RECIPIENT_TYPE = "group";

    /**
     * Instantiates a new {@link Group group} with an empty list of users.
     * @param ID the ID of the group
     * @param name the name of the group (e.g. 'work')
     * @see Group#Group(int, String, List)
     */
    public Group(int ID, String name) {
        this(ID, name, new ArrayList<User>());
    }

    /**
     * java equals
     * @param o other Object to compare this with
     * @return true if o is equal in value to this
     */
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof Group) ) return false;
        Group that = (Group)o;
        return super.equals(that) && that.users.equals(users);
    }

    /**
     * java hash function
     * @return hash of the Object
     */
    @Override
    public int hashCode() {
        return super.hashCode()*73+users.hashCode();
    }

    /**
     * Instantiates a new {@link Group group} with the given parameters
     * @param ID the ID of the group
     * @param name the name of the group
     * @param users the list of all the {@link User users} in the group (not null..)
     */
    public Group(int ID, String name, List<User> users) {
        super(ID, name);
        if(null == users) {
            throw new IllegalArgumentException("field 'users' cannot be null");
        }
        this.users = new ArrayList<>(users);//User is immutable
    }

    /**
     * @return a JSONObject representing a {@link Group}
     * @throws JSONException
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        this.compose(json);
        return json;
    }

    /**
     * Appends the fields of Group to the given JSONObject. <br><br>
     *     Should <b>NOT</b> be used alone
     * @param json the json to which we append data
     * @throws JSONException
     */
    @Override
    protected void compose(JSONObject json) throws JSONException {
        super.compose(json);//adds parent fields (Recipient)
        json.accumulate("type", Group.RECIPIENT_TYPE);
        if(users.size() == 1) {
            json.accumulate("users", new JSONArray().put(users.get(0).toJSON()));
        } else {
            for (User user : users) {
                json.accumulate("users", user.toJSON());
                //TODO problem here I wanted to use append !! but not available why ????? FAACK method is hidden....
                //the problem is that if we have a single user accumulate will not generate a jsonarray
                //hence I test size of list and manually wrap the single user in a jsonarray
                //but I find this very ugly... maybe test without but....
            }
        }
    }

    /**
     * Parses a Group from a JSONObject.<br>
     * @param json the well formed {@link JSONObject json} representing the {@link Group group}
     * @return a {@link Group group} parsed from the JSONObject
     * @throws JSONException
     * @see ch.epfl.sweng.calamar.Recipient#fromJSON(JSONObject) Recipient.fromJSON
     */
    public static Group fromJSON(JSONObject json) throws JSONException {
        return new Group.Builder().parse(json).build();
    }

    /**
     * A Builder for {@link Group}, currently only used to parse JSON (little overkill..but ..)
     * @see ch.epfl.sweng.calamar.Recipient.Builder
     */
    private static class Builder extends Recipient.Builder {
        private List<User> users;
        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if(!type.equals(Group.RECIPIENT_TYPE)) {
                throw new IllegalArgumentException("expected "+Group.RECIPIENT_TYPE+" was : " + type);
            }
            //TODO test, maybe problem if group of a single USER...see remark above in toJSON
            JSONArray jsonUsers = json.getJSONArray("users");
            users = new ArrayList<>();
            for(int i = 0; i < jsonUsers.length(); ++i) {
                users.add(User.fromJSON(jsonUsers.getJSONObject(i)));
            }
            return this;
        }
        public Group build() {
            return new Group(super.ID, super.name);
        }
    }
}
