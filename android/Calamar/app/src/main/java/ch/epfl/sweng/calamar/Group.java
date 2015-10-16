package ch.epfl.sweng.calamar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LPI on 16.10.2015.
 */
public final class Group extends Recipient {
    private final List<User> users;

    public Group(int ID, String name) {
        this(ID, name, new ArrayList<User>());
    }

    public Group(int ID, String name, List<User> users) {
        super(ID, name);
        if(null == users) {
            throw new IllegalArgumentException("field 'users' cannot be null");
        }
        this.users = new ArrayList<>(users);//User is immutable
    }
}
