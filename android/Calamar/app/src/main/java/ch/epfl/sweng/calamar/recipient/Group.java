package ch.epfl.sweng.calamar.recipient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;

/**
 * Models a Group (of users). <br><br>
 * Group is immutable.
 * Created by LPI on 16.10.2015.
 */
public final class Group extends Recipient {

    private static final String JSON_USERS = "users";
    private final List<User> users;
    private final static String RECIPIENT_TYPE = "group";

    /**
     * Instantiates a new {@link Group group} with an empty list of users.
     *
     * @param ID   the ID of the group
     * @param name the name of the group (e.g. 'work')
     * @see Group#Group(int, String, List)
     */
    public Group(int ID, String name) {
        this(ID, name, new ArrayList<User>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group that = (Group) o;
        return super.equals(that) && that.users.equals(users);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 73 + users.hashCode();
    }

    /**
     * Instantiates a new {@link Group group} with the given parameters
     *
     * @param ID    the ID of the group
     * @param name  the name of the group
     * @param users the list of all the {@link User users} in the group (not null..)
     */
    public Group(int ID, String name, List<User> users) {
        super(ID, name);
        if (null == users) {
            throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.field_users_null));
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
     * Should <b>NOT</b> be used alone
     *
     * @param json the json to which we append data
     * @throws JSONException
     */
    @Override
    protected void compose(JSONObject json) throws JSONException {
        super.compose(json);//adds parent fields (Recipient)
        json.accumulate(JSON_TYPE, Group.RECIPIENT_TYPE);
        if (users.size() == 1) {
            json.accumulate(JSON_USERS, new JSONArray().put(users.get(0).toJSON()));
        } else {
            for (User user : users) {
                json.accumulate(JSON_USERS, user.toJSON());
                //TODO problem here I wanted to use append !! but not available why ????? FAACK method is hidden....
                //the problem is that if we have a single user accumulate will not generate a jsonarray
                //hence I test size of list and manually wrap the single user in a jsonarray
                //but I find this very ugly... maybe test without but....
            }
        }
    }

    /**
     * Parses a Group from a JSONObject.<br>
     *
     * @param json the well formed {@link JSONObject json} representing the {@link Group group}
     * @return a {@link Group group} parsed from the JSONObject
     * @throws JSONException
     * @see Recipient#fromJSON(JSONObject) Recipient.fromJSON
     */
    public static Group fromJSON(JSONObject json) throws JSONException {
        return new Group.Builder().parse(json).build();
    }

    /**
     * A Builder for {@link Group}, currently only used to parse JSON (little overkill..but ..)
     *
     * @see Recipient.Builder
     */
    private static class Builder extends Recipient.Builder {
        private List<User> users;

        @Override
        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString(JSON_TYPE);
            if (!type.equals(Group.RECIPIENT_TYPE)) {
                throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.expected_but_was, RECIPIENT_TYPE, type));
            }
            //TODO test, maybe problem if group of a single USER...see remark above in toJSON
            JSONArray jsonUsers = json.getJSONArray(JSON_USERS);
            users = new ArrayList<>();
            for (int i = 0; i < jsonUsers.length(); ++i) {
                users.add(User.fromJSON(jsonUsers.getJSONObject(i)));
            }
            return this;
        }

        public Group build() {
            return new Group(super.ID, super.name, users);
        }
    }
}
