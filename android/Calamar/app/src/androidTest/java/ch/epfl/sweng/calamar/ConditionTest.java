package ch.epfl.sweng.calamar;

import android.location.Location;
import android.test.ActivityInstrumentationTestCase2;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.map.GPSProvider;

import static junit.framework.Assert.assertEquals;

/**
 * Created by pierre on 11/6/15.
 */

@RunWith(JUnit4.class)
public class ConditionTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    ConditionTest() {
        super(ChatActivity.class);
    }

    /**
     * helps defining observers that test Condition
     * TO for TestingObserver
     */
    class TO extends Condition.Observer {

        private int triggered = 0;
        private final Condition c;

        TO(Condition c) {
            this.c = c;
            this.c.addObserver(this);
        }

        @Override
        public void update(Condition c) {
            triggered++;
        }

        public void assertValue(boolean value) {
            assertEquals(value, c.getValue());
        }

        public void assertTriggered(int n) {
            assertEquals(triggered, n);
        }

        public void assertAll(boolean value, int n) {
            assertValue(value);
            assertTriggered(n);
        }
    }

    /**
     * used to make setValue accessible
     * TC for TestingCondition
     */
    class TC extends Condition {

        TC() {
            setValue(false);
        }

        //don't really care
        @Override
        protected void compose(JSONObject json) throws JSONException {

        }

        @Override
        public String toString() {
            return "";
        }

        void set(boolean value) {
            setValue(value);
        }

        @Override
        public String type() {
            return "test";
        }
    }

    @Test
    public void testConditionTrueFalse() {
        TO o1 = new TO(Condition.trueCondition());
        o1.assertAll(true, 0);
        TO o2 = new TO(Condition.falseCondition());
        o2.assertAll(false, 0);
    }

    @Test
    public void testConditionAnd() {
        TC a = new TC(), b = new TC();
        TO o = new TO(Condition.and(a, b));
        o.assertAll(false, 0);
        a.set(false);
        b.set(false);
        o.assertAll(false, 0);
        a.set(true);
        b.set(true);
        o.assertAll(true, 1);
        a.set(false);
        o.assertAll(false, 2);
        b.set(false);
        o.assertAll(false, 2);
    }

    @Test
    public void testConditionOr() {
        TC a = new TC(), b = new TC();
        TO o = new TO(Condition.or(a, b));
        o.assertAll(false, 0);
        a.set(false);
        b.set(false);
        o.assertAll(false, 0);
        a.set(true);
        o.assertAll(true, 1);
        b.set(true);
        o.assertAll(true, 1);
        a.set(false);
        b.set(false);
        o.assertAll(false, 2);
    }

    @Test
    public void testConditionNot() {
        TC a = new TC();
        TO o = new TO(Condition.not(a));
        o.assertAll(true, 0);
        a.set(false);
        o.assertAll(true, 0);
        a.set(true);
        o.assertAll(false, 1);
        a.set(true);
        o.assertAll(false, 1);
        a.set(false);
        o.assertAll(true, 2);
    }

    public class MockPositionCondition extends PositionCondition {
        public MockPositionCondition(double latitude, double longitude, double radius)
        {
            super(latitude, longitude, radius);
            MockGPSProvider.getInstance().addObserver(new GPSProvider.Observer() {

                @Override
                public void update(Location newLocation) {

                    setValue(newLocation.distanceTo(getLocation()) < getRadius());
                }
            });
        }
    }

    @Test
    public void testPositionCondition() {
        
    }
}
