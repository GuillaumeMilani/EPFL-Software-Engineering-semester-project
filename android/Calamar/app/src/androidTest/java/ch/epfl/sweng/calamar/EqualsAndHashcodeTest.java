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

    private final String path = "Calamar/1/2/";
    private final Date testDate = new Date(1000);
    private final User testUser1 = new User(13, "bob");
    private final User testUser2 = new User(14, "sponge");
    private final Condition tc = Condition.trueCondition();
    private final String testMessage = "Heeeeyyyyy";

    private static final byte[] testContent = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
            (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x02, (byte) 0x0d, (byte) 0xb1, (byte) 0xb2, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x25, (byte) 0x49, (byte) 0x44, (byte) 0x41,
            (byte) 0x54, (byte) 0x08, (byte) 0x99, (byte) 0x4d, (byte) 0x8a, (byte) 0xb1,
            (byte) 0x0d, (byte) 0x00, (byte) 0x20, (byte) 0x0c, (byte) 0x80, (byte) 0xc0,
            (byte) 0xff, (byte) 0x7f, (byte) 0xc6, (byte) 0xc1, (byte) 0xc4, (byte) 0x96,
            (byte) 0x81, (byte) 0x05, (byte) 0xa8, (byte) 0x80, (byte) 0x67, (byte) 0xe0,
            (byte) 0xb0, (byte) 0xa8, (byte) 0xfc, (byte) 0x65, (byte) 0xba, (byte) 0xaa,
            (byte) 0xce, (byte) 0xb3, (byte) 0x97, (byte) 0x0b, (byte) 0x2b, (byte) 0xd9,
            (byte) 0x11, (byte) 0xfa, (byte) 0xa5, (byte) 0xad, (byte) 0x00, (byte) 0x06,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49, (byte) 0x45,
            (byte) 0x4e, (byte) 0x44, (byte) 0xae, (byte) 0x42, (byte) 0x60, (byte) 0x82};

    @Test
    public void testUser() {
        helperVerifyEqualsAndHashcode(testUser1, new User(13, "bob"));
        helperVerifyNotEqualsAndHashcode(testUser1, new User(13, "bo"));
        helperVerifyNotEqualsAndHashcode(testUser1, new User(12, "bob"));
    }

    @Test
    public void testGroup() {
        List<User> a = new ArrayList<>();
        a.add(testUser1);
        a.add(testUser2);
        List<User> b = new ArrayList<>();
        b.add(new User(13, "bob"));
        b.add(new User(14, "sponge"));
        helperVerifyEqualsAndHashcode(new Group(1000, "Pat", a), new Group(1000, "Pat", b));
    }

    @Test
    public void testSimpleTextItem() {
        SimpleTextItem s = new SimpleTextItem(12, testUser1, testUser2, testDate, tc, testMessage);
        SimpleTextItem s2 = new SimpleTextItem(12, testUser1, testUser2, testDate, tc, testMessage);
        SimpleTextItem s3 = new SimpleTextItem(13, testUser1, testUser2, testDate, tc, testMessage);
        SimpleTextItem s4 = new SimpleTextItem(12, new User(12, "bob"), testUser2, testDate, tc, testMessage);
        SimpleTextItem s5 = new SimpleTextItem(12, testUser1, new User(14, "sp"), testDate, tc, testMessage);
        SimpleTextItem s6 = new SimpleTextItem(12, testUser1, testUser2, new Date(1), tc, testMessage);
        SimpleTextItem s7 = new SimpleTextItem(12, testUser1, testUser2, testDate, Condition.falseCondition(), testMessage);
        SimpleTextItem s8 = new SimpleTextItem(12, testUser1, testUser2, testDate, tc, "Heeeeeeee");
        helperVerifyEqualsAndHashcode(s, s2);
        helperVerifyNotEqualsAndHashcode(s, s3);
        helperVerifyNotEqualsAndHashcode(s, s4);
        helperVerifyNotEqualsAndHashcode(s, s5);
        helperVerifyNotEqualsAndHashcode(s, s6);
        helperVerifyNotEqualsAndHashcode(s, s7);
        helperVerifyNotEqualsAndHashcode(s, s8);
        helperVerifyNotEqualsAndHashcode(s3, s4);
        helperVerifyNotEqualsAndHashcode(s3, s5);
        helperVerifyNotEqualsAndHashcode(s3, s6);
        helperVerifyNotEqualsAndHashcode(s3, s7);
        helperVerifyNotEqualsAndHashcode(s3, s8);
        helperVerifyNotEqualsAndHashcode(s4, s5);
        helperVerifyNotEqualsAndHashcode(s4, s6);
        helperVerifyNotEqualsAndHashcode(s4, s7);
        helperVerifyNotEqualsAndHashcode(s4, s8);
        helperVerifyNotEqualsAndHashcode(s5, s6);
        helperVerifyNotEqualsAndHashcode(s5, s7);
        helperVerifyNotEqualsAndHashcode(s5, s8);
        helperVerifyNotEqualsAndHashcode(s6, s7);
        helperVerifyNotEqualsAndHashcode(s6, s8);
        helperVerifyNotEqualsAndHashcode(s7, s8);
    }

    @Test
    public void testFileItem() {
        // just a random 5x5 pgn pictures to test
        byte[] testContent2 = testContent.clone();
        testContent2[5] = (byte) 0x12;
        FileItem f = new FileItem(12, testUser1, testUser2, testDate, tc, testContent, path + "FileItem");
        FileItem f2 = new FileItem(12, testUser1, testUser2, testDate, tc, testContent, path + "FileItem");
        ImageItem i = new ImageItem(12, testUser1, testUser2, testDate, tc, testContent, path + "FileItem");
        FileItem f3 = new FileItem(12, testUser1, testUser2, testDate, tc, testContent2, path + "FileItem");
        FileItem f4 = new FileItem(12, testUser1, testUser2, testDate, tc, testContent, path + "/3/FileItem");
        helperVerifyEqualsAndHashcode(f, f2);
        helperVerifyEqualsAndHashcode(f, i);
        helperVerifyEqualsAndHashcode(i, f);
        helperVerifyNotEqualsAndHashcode(f, f3);
        helperVerifyNotEqualsAndHashcode(f, f4);
        helperVerifyNotEqualsAndHashcode(f3, f4);
    }

    @Test
    public void testImageItem() {
        // just a random 5x5 pgn pictures to test
        ImageItem i = new ImageItem(12, testUser1, testUser2, testDate, tc, testContent, path + "ImageItem");
        ImageItem i2 = new ImageItem(12, testUser1, testUser2, testDate, tc, testContent, path + "ImageItem");
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

    @Ignore
    private void helperVerifyNotEqualsAndHashcode(Object a, Object b) {
        assertNotEquals(a, b);
        assertNotEquals(b, a);
        assertNotEquals(a.hashCode(), b.hashCode());
    }
}
