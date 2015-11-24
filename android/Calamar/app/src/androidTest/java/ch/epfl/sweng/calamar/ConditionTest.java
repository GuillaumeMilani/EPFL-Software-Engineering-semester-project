package ch.epfl.sweng.calamar;

import android.location.Location;
import android.test.ActivityInstrumentationTestCase2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.map.GPSProvider;

/**
 * Created by pierre on 11/6/15.
 */

@RunWith(JUnit4.class)
public class ConditionTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public ConditionTest() {
        super(MainActivity.class);
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

    public static Location makeLocation(double latitude, double longitude)
    {
        Location loc = new Location("calamarTestingTeam");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }

    //TODO: missing google play services Ignore while not fixed (position condition visually works)

    @Ignore
    public void testPositionCondition() throws InterruptedException {
        GPSProvider gps = GPSProvider.getInstance();
        gps.startLocationUpdates(getActivity());
        // BC
        gps.setMockLocation(makeLocation(46.518568, 6.561926));
        Thread.sleep(5000);
        // ~ Rolex
        PositionCondition c1 = new PositionCondition(46.518388, 6.568313, 20);
        // ~ BC
        PositionCondition c2 = new PositionCondition(46.518568, 6.561926, 20);
        TO o1 = new TO(c1);
        TO o2 = new TO(c2);
        o1.assertAll(false, 0);
        o2.assertAll(true, 0);
        // Amphimax
        gps.setMockLocation(makeLocation(46.521783, 6.575507));
        Thread.sleep(5000);
        o1.assertAll(false, 0);
        o2.assertAll(false, 1);
        // in the Rolex
        gps.setMockLocation(makeLocation(46.518313, 6.567804));
        Thread.sleep(5000);
        o1.assertAll(true, 1);
        o2.assertAll(false, 1);
        // back to BC
        gps.setMockLocation(makeLocation(46.518568, 6.561926));
        Thread.sleep(5000);
        o1.assertAll(false, 2);
        o2.assertAll(true, 2);
    }


    @Ignore
    public void testMetadataTrueCondition() throws JSONException {
        Condition a = Condition.trueCondition();
        JSONArray ja = new JSONArray();
        assertEquals(ja, a.getMetadata());
    }

    @Ignore
    public void testMetadataPositionCondition() throws JSONException {
        Condition pc = new PositionCondition(123.4, 432.1, 10);
        JSONObject jo = new JSONObject();
        jo.accumulate("type", "position");
        jo.accumulate("latitude", 123.4);
        jo.accumulate("longitude", 432.1);
        jo.accumulate("radius", 10);
        JSONArray ja = (new JSONArray()).put(jo);
        assertEquals(ja.toString(), pc.getMetadata().toString());
    }

    @Ignore
    public void testMetadataMultiplePositionCondition() throws JSONException {
        Condition pc1 = new PositionCondition(123.4, 432.1, 10);
        Condition pc2 = new PositionCondition(432.1, 123.4, 20);
        Condition pc1AndPc2 = Condition.and(pc1, pc2);
        JSONObject jo1 = new JSONObject();
        jo1.accumulate("type", "position");
        jo1.accumulate("latitude", 123.4);
        jo1.accumulate("longitude", 432.1);
        jo1.accumulate("radius", 10);
        JSONObject jo2 = new JSONObject();
        jo2.accumulate("type", "position");
        jo2.accumulate("latitude", 432.1);
        jo2.accumulate("longitude", 123.4);
        jo2.accumulate("radius", 20);
        JSONArray ja = new JSONArray();
        ja.put(jo1);
        ja.put(jo2);
        assertEquals(ja.toString(), pc1AndPc2.getMetadata().toString());
    }

    @Ignore
    public void testToJSONMetadata() throws JSONException {
        Condition pc1 = new PositionCondition(123.4, 432.1, 10);
        JSONObject jo1 = new JSONObject();
        jo1.accumulate("type", "position");
        jo1.accumulate("latitude", 123.4);
        jo1.accumulate("longitude", 432.1);
        jo1.accumulate("radius", 10);
        JSONArray ja = new JSONArray();
        ja.put(jo1);
        jo1.accumulate("metadata", ja);

        assertEquals(jo1.toString(), pc1.toJSON().toString());
    }
}
