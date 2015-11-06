package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Date;

import dalvik.annotation.TestTargetClass;

/**
 * Created by pierre on 10/20/15.
 */
public class ParsingTest {
    void testItemParsing(Item i) throws JSONException {
        assert(i.equals(Item.fromJSON(i.toJSON())));
    }

    void testRecipientParsing(Recipient r) throws JSONException {
        assert(r.equals(Recipient.fromJSON(r.toJSON())));
    }

    @Test
    void testUserParsing() throws JSONException {
        testRecipientParsing(new User(69, "Jean Luc"));
        testRecipientParsing(new User(666, "Albert Danton"));
    }

    @Test
    void testSimpleTextItemParsing() throws JSONException {
        testItemParsing(new SimpleTextItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), new Date(2012, 12, 21), Condition.trueCondition(), "hey, How are you ?"));
    }
}
