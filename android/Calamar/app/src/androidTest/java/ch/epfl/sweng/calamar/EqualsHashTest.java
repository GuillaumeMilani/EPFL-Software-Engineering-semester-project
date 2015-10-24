package ch.epfl.sweng.calamar;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pierre on 10/24/15.
 */
public class EqualsHashTest {
    void testEqualsHash(Object a, Object b) {
        assert(a.equals(b));
        assert(b.equals(a));
        assert(a.hashCode() == b.hashCode());
    }

    @Test
    void testUser() {
        testEqualsHash(new User(13, "bob"), new User(13, "bob"));
    }

    @Test
    void testGroup() {
        List<User> a = new ArrayList<User>();
        a.add(new User(13, "bob"));
        a.add(new User(14, "sponge"));
        List<User> b = new ArrayList<User>();
        b.add(new User(13, "bob"));
        b.add(new User(14, "sponge"));
        testEqualsHash(new Group(1000, "Pat", a), new Group(1000, "Pat", b));
    }

    @Test
    void testSimpleTextItem() {
        testEqualsHash(new SimpleTextItem(12, new User(13, "bob"), new User(14, "sponge"), new Date(1000), "Heeeeyyyyy"),
                new SimpleTextItem(12, new User(13, "bob"), new User(14, "sponge"), new Date(1000), "Heeeeyyyyy"));
    }
}
