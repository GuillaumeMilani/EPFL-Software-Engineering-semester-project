package ch.epfl.sweng.calamar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by pierre on 10/24/15.
 */
@RunWith(JUnit4.class)
public class EqualsAndHashcodeTest {

    @Test
    public void testUser() {
        testVerifyEqualsAndHashcode(new User(13, "bob"), new User(13, "bob"));
    }

    @Test
    public void testGroup() {
        List<User> a = new ArrayList<>();
        a.add(new User(13, "bob"));
        a.add(new User(14, "sponge"));
        List<User> b = new ArrayList<>();
        b.add(new User(13, "bob"));
        b.add(new User(14, "sponge"));
        testVerifyEqualsAndHashcode(new Group(1000, "Pat", a), new Group(1000, "Pat", b));
    }

    @Test
    public void testSimpleTextItem() {
        testVerifyEqualsAndHashcode(new SimpleTextItem(12, new User(13, "bob"), new User(14, "sponge"), new Date(1000), "Heeeeyyyyy"),
                new SimpleTextItem(12, new User(13, "bob"), new User(14, "sponge"), new Date(1000), "Heeeeyyyyy"));
    }

    private void testVerifyEqualsAndHashcode(Object a, Object b) {
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
