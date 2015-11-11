package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by pierre on 10/20/15.
 */

@RunWith(JUnit4.class)
public class ParsingTest {


    @Test
    public void testUserParsing() throws JSONException {
        testRecipientParsing(new User(69, "Jean Luc"));
        testRecipientParsing(new User(666, "Albert Danton"));
    }

    @Test
    public void testSimpleTextItemParsing() throws JSONException {
        testItemParsing(new SimpleTextItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), new Date(22313), Condition.trueCondition(), "hey, How are you ?"));
    }

    @Test
    public void testConditionParsing() throws JSONException {
        Condition a = Condition.trueCondition(), b = Condition.falseCondition();
        testConditionParsing(Condition.and(a, b));
        testConditionParsing(Condition.or(a, b));
        testConditionParsing(Condition.not(a));
        testConditionParsing(Condition.falseCondition());
        testConditionParsing(Condition.trueCondition());
    }

    @Test
    public void testPositionConditionParsing() throws JSONException {
        testConditionParsing(new PositionCondition(10., 20., 15.));
    }

    @Ignore
    private void testItemParsing(Item i) throws JSONException {
        assertEquals(i, Item.fromJSON(i.toJSON()));
    }

    @Ignore
    private void testRecipientParsing(Recipient r) throws JSONException {
        assertEquals(r, Recipient.fromJSON(r.toJSON()));
    }

    @Ignore
    private void testConditionParsing(Condition c) throws JSONException {
        assertEquals(c, Condition.fromJSON(c.toJSON()));
    }
}
