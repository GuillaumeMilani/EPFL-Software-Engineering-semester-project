package ch.epfl.sweng.calamar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.test.InstrumentationTestCase;
import android.util.Log;

import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;


/**
 * Created by pierre on 10/20/15.
 */

@RunWith(JUnit4.class)
public class ParsingTest extends InstrumentationTestCase {
    private static final String TAG = "ParsingTest";

    public ParsingTest() throws Exception {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @Override
    protected void setUp() throws Exception {
        Log.d(TAG, "setUp(): " + getName());
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG, "tearDown(): " + getName());
        super.tearDown();
    }

    @Test
    public void testUserParsing() throws JSONException {
        helperRecipientParsing(new User(69, "Jean Luc"));
        helperRecipientParsing(new User(666, "Albert Danton"));
    }

    @Test
    public void testSimpleTextItemParsing() throws JSONException {
        helperItemParsing(new SimpleTextItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), new Date(22313), Condition.trueCondition(), "hey, How are you ?"));
    }

    @Test
    public void testImageItemParsing() throws JSONException {
        Bitmap bitmap = BitmapFactory.decodeResource(this.getInstrumentation().getContext().getResources(), R.drawable.in_message_bg);
        assertNotNull(bitmap);
        helperItemParsing(new ImageItem(42, new User(69, "Jean Luc"), new User(666, "Albert Danton"), new Date(22313), Condition.trueCondition(), bitmap));
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
