package ch.epfl.sweng.calamar;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Group;
import ch.epfl.sweng.calamar.recipient.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by pierre on 10/24/15.
 */
@RunWith(JUnit4.class)
public class EqualsAndHashcodeTest {

    private final Date testDate = new Date(1000);

    @Test
    public void testUser() {
        helperVerifyEqualsAndHashcode(new User(13, "bob"), new User(13, "bob"));
    }

    @Test
    public void testGroup() {
        List<User> a = new ArrayList<>();
        a.add(new User(13, "bob"));
        a.add(new User(14, "sponge"));
        List<User> b = new ArrayList<>();
        b.add(new User(13, "bob"));
        b.add(new User(14, "sponge"));
        helperVerifyEqualsAndHashcode(new Group(1000, "Pat", a), new Group(1000, "Pat", b));
    }

    @Test
    public void testSimpleTextItem() {
        SimpleTextItem s = new SimpleTextItem(12, new User(13, "bob"), new User(14, "sponge"), testDate, Condition.trueCondition(), "Heeeeyyyyy");
        SimpleTextItem s2 = new SimpleTextItem(12, new User(13, "bob"), new User(14, "sponge"), testDate, Condition.trueCondition(), "Heeeeyyyyy");
        helperVerifyEqualsAndHashcode(s, s2);
    }

    @Test
    public void testFileItem() {
        // just a random 5x5 pgn pictures to test
        byte[] bytes = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47, (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x0d, (byte) 0xb1, (byte) 0xb2, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x25, (byte) 0x49, (byte) 0x44, (byte) 0x41, (byte) 0x54, (byte) 0x08, (byte) 0x99, (byte) 0x4d, (byte) 0x8a, (byte) 0xb1, (byte) 0x0d, (byte) 0x00, (byte) 0x20, (byte) 0x0c, (byte) 0x80, (byte) 0xc0, (byte) 0xff, (byte) 0x7f, (byte) 0xc6, (byte) 0xc1, (byte) 0xc4, (byte) 0x96, (byte) 0x81, (byte) 0x05, (byte) 0xa8, (byte) 0x80, (byte) 0x67, (byte) 0xe0, (byte) 0xb0, (byte) 0xa8, (byte) 0xfc, (byte) 0x65, (byte) 0xba, (byte) 0xaa, (byte) 0xce, (byte) 0xb3, (byte) 0x97, (byte) 0x0b, (byte) 0x2b, (byte) 0xd9, (byte) 0x11, (byte) 0xfa, (byte) 0xa5, (byte) 0xad, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49, (byte) 0x45, (byte) 0x4e, (byte) 0x44, (byte) 0xae, (byte) 0x42, (byte) 0x60, (byte) 0x82};
        FileItem f = new FileItem(12, new User(13, "bob"), new User(14, "sponge"), testDate, Condition.trueCondition(), bytes, "FileItem");
        FileItem f2 = new FileItem(12, new User(13, "bob"), new User(14, "sponge"), testDate, Condition.trueCondition(), bytes, "FileItem");
        ImageItem i = new ImageItem(12, new User(13, "bob"), new User(14, "sponge"), testDate, Condition.trueCondition(), bytes, "FileItem");
        helperVerifyEqualsAndHashcode(f, f2);
        helperVerifyEqualsAndHashcode(f, i);
        helperVerifyEqualsAndHashcode(i, f);
    }

    @Test
    public void testImageItem() {
        // just a random 5x5 pgn pictures to test
        byte[] bytes = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47, (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x0d, (byte) 0xb1, (byte) 0xb2, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x25, (byte) 0x49, (byte) 0x44, (byte) 0x41, (byte) 0x54, (byte) 0x08, (byte) 0x99, (byte) 0x4d, (byte) 0x8a, (byte) 0xb1, (byte) 0x0d, (byte) 0x00, (byte) 0x20, (byte) 0x0c, (byte) 0x80, (byte) 0xc0, (byte) 0xff, (byte) 0x7f, (byte) 0xc6, (byte) 0xc1, (byte) 0xc4, (byte) 0x96, (byte) 0x81, (byte) 0x05, (byte) 0xa8, (byte) 0x80, (byte) 0x67, (byte) 0xe0, (byte) 0xb0, (byte) 0xa8, (byte) 0xfc, (byte) 0x65, (byte) 0xba, (byte) 0xaa, (byte) 0xce, (byte) 0xb3, (byte) 0x97, (byte) 0x0b, (byte) 0x2b, (byte) 0xd9, (byte) 0x11, (byte) 0xfa, (byte) 0xa5, (byte) 0xad, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49, (byte) 0x45, (byte) 0x4e, (byte) 0x44, (byte) 0xae, (byte) 0x42, (byte) 0x60, (byte) 0x82};
        ImageItem i = new ImageItem(12, new User(13, "bob"), new User(14, "sponge"), testDate, Condition.trueCondition(), bytes, "ImageItem");
        ImageItem i2 = new ImageItem(12, new User(13, "bob"), new User(14, "sponge"), testDate, Condition.trueCondition(), bytes, "ImageItem");
        helperVerifyEqualsAndHashcode(i, i2);
    }

    @Ignore
    private void helperVerifyEqualsAndHashcode(Object a, Object b) {
        assertEquals(a, b);
        assertEquals(b, a);
        assertNotEquals(a, null);
        assertNotEquals(b, null);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
