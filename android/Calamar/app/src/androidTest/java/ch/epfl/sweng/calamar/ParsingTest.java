package ch.epfl.sweng.calamar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static junit.framework.Assert.assertEquals;


/**
 * Created by pierre on 10/20/15.
 */

@RunWith(JUnit4.class)
public class ParsingTest {

    private final Date testDate = new Date(22313);
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
    public void testUserParsing() throws JSONException {
        helperRecipientParsing(new User(69, "Jean Luc"));
        helperRecipientParsing(new User(666, "Albert Danton"));
    }

    @Test
    public void testSimpleTextItemParsing() throws JSONException {
        helperItemParsing(new SimpleTextItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), testDate, Condition.falseCondition(), "hey, How are you ?"));
        helperItemParsing(new SimpleTextItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), testDate, Condition.trueCondition(), "hey, How are you ?"));
    }

    @Test
    public void testImageItemParsing() throws JSONException {
        helperItemParsing(new ImageItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), testDate, Condition.trueCondition(), testContent, "ImageItem"));
    }

    @Test
    public void testBitmapToFromString() {
        Bitmap b1 = BitmapFactory.decodeByteArray(testContent, 0, testContent.length);
        assertEquals(ImageItem.bitmap2String(b1), ImageItem.bitmap2String(ImageItem.string2Bitmap(ImageItem.bitmap2String(b1))));
    }

    @Test
    public void testFileItemParsing() throws JSONException {
        helperItemParsing(new FileItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), testDate, Condition.trueCondition(), testContent, "FileItem"));
    }

    @Test
    public void helperConditionParsing() throws JSONException {
        Condition a = Condition.trueCondition(), b = Condition.falseCondition();
        helperConditionParsing(Condition.and(a, b));
        helperConditionParsing(Condition.or(a, b));
        helperConditionParsing(Condition.not(a));
        helperConditionParsing(Condition.falseCondition());
        helperConditionParsing(Condition.trueCondition());
    }

    //TODO Enable once ConstantGPSProvider is done
    @Ignore
    public void testPositionConditionParsing() throws JSONException {
        helperConditionParsing(new PositionCondition(10., 20., 15.));
    }

    @Ignore
    private void helperItemParsing(Item i) throws JSONException {
        assertEquals(i, Item.fromJSON(i.toJSON()));
    }

    @Ignore
    private void helperRecipientParsing(Recipient r) throws JSONException {
        assertEquals(r, Recipient.fromJSON(r.toJSON()));
    }

    @Ignore
    private void helperConditionParsing(Condition c) throws JSONException {
        assertEquals(c, Condition.fromJSON(c.toJSON()));
    }
}
