package ch.epfl.sweng.calamar.item;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static junit.framework.Assert.fail;

/**
 * Created by pierre on 12/8/15.
 */
@RunWith(JUnit4.class)
public class ExceptionsTest {

    private final int defaultID = 123;
    private final String defaultPath = "Calamar/1/2/";
    private final Date defaultDate = new Date(1000);
    private final User defaultFrom = new User(13, "bob");
    private final User defaultTo = new User(14, "sponge");
    private final Condition defaultCondition = Condition.trueCondition();
    private final String defaultMessage = "Heeeeyyyyy";

    private static final byte[] defaultData = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
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

    public void assertExceptionInItemCreation(int ID, User from, Recipient to, Date date, Condition condition, String message) {
        try {
            Item item = new SimpleTextItem(ID, from, to, date, condition, message);
            fail();
        } catch(IllegalArgumentException e) {
            // good ... good
        }
    }

    @Test
    public void testItemConstructor() {
        assertExceptionInItemCreation(defaultID, null, defaultTo, defaultDate, defaultCondition, defaultMessage);
        assertExceptionInItemCreation(defaultID, defaultFrom, null, defaultDate, defaultCondition, defaultMessage);
        assertExceptionInItemCreation(defaultID, defaultFrom, defaultTo, null, defaultCondition, defaultMessage);
        assertExceptionInItemCreation(defaultID, defaultFrom, defaultTo, defaultDate, null, defaultMessage);
        // because we use SimpleTextItem
        assertExceptionInItemCreation(defaultID, defaultFrom, defaultTo, defaultDate, defaultCondition, null);
    }

    @Test
    public void testItemFromJSON() throws JSONException {
        try {
            Item.fromJSON(null);
            fail();
        } catch (IllegalArgumentException e) {
            // nice
        }
        Item item = new SimpleTextItem(defaultID, defaultFrom, defaultTo, defaultDate, defaultCondition, defaultMessage);
        try {
            JSONObject jsonObject = item.toJSON();
            jsonObject.remove(Item.JSON_TYPE);
            Item.fromJSON(jsonObject);
            fail();
        } catch (IllegalArgumentException e) {
            // rock it
        }
        try {
            JSONObject jsonObject = item.toJSON();
            jsonObject.put(Item.JSON_TYPE, "wrong type");
            Item.fromJSON(jsonObject);
            fail();
        } catch (IllegalArgumentException e) {
            // oh yeeaaaahh
        }
    }

    @Test
    public void testRecipientFromJSON() throws JSONException {
        try {
            Recipient.fromJSON(null);
            fail();
        } catch (IllegalArgumentException e) {
            // woop woop
        }
        Recipient recipient = defaultFrom;
        try {
            JSONObject jsonObject = recipient.toJSON();
            jsonObject.remove("type");
            Recipient.fromJSON(jsonObject);
            fail();
        } catch (IllegalArgumentException e) {
            // Boum Boum
        }
        try {
            JSONObject jsonObject = recipient.toJSON();
            jsonObject.put("type", "wrong type");
            Recipient.fromJSON(jsonObject);
            fail();
        } catch (IllegalArgumentException e) {
            // yes
        }
    }

    @Test
    public void testRecipient() {
        try {
            Recipient r = new User(defaultID, null);
            fail();
        } catch(IllegalArgumentException e) {
            // smooth
        }
    }

    @Test
    public void testConditionConstructor() {
        try {
            Condition r = new PositionCondition(null, 20);
            fail();
        } catch(IllegalArgumentException e) {
            // smooth
        }
        try {
            Condition.and(null, null);
            fail();
        } catch(IllegalArgumentException e) {
            // yep
        }
        try {
            Condition.or(null, null);
            fail();
        } catch(IllegalArgumentException e) {
            // ja
        }
        try {
            Condition.not(null);
            fail();
        } catch(IllegalArgumentException e) {
            // si
        }
    }
}
