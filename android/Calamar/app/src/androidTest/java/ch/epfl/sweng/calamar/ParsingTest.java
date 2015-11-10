package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by pierre on 10/20/15.
 */
public class ParsingTest {

    @Test
    public void testUserParsing() throws JSONException {
        testRecipientParsing(new User(69, "Jean Luc"));
        testRecipientParsing(new User(666, "Albert Danton"));
    }

    @Test
    public void testSimpleTextItemParsing() throws JSONException {
        testItemParsing(new SimpleTextItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), new Date(2223232), "hey, How are you ?"));
    }

    private void testItemParsing(Item i) throws JSONException {
        assertEquals(i, Item.fromJSON(i.toJSON()));
    }

    private void testRecipientParsing(Recipient r) throws JSONException {
        assertEquals(r, Recipient.fromJSON(r.toJSON()));
    }
}
