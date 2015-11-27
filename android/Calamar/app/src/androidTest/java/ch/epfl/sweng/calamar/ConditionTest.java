package ch.epfl.sweng.calamar;

import android.location.Location;
import android.support.test.rule.ActivityTestRule;
import android.test.ActivityInstrumentationTestCase2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.map.GPSProvider;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by pierre on 11/6/15.
 */

@RunWith(JUnit4.class)
public class ConditionTest {


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
        public Type getType() {
            return Type.TESTCONDITION;
        }
    }

    class TestCondPosition extends Condition {

        Location location;
        double radius;

        public TestCondPosition(Location location, double radius) {
            this.location = location;
            this.radius = radius;
        }

        public TestCondPosition(double latitude, double longitude, double radius) {
            this.location = new Location("test");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            this.radius = radius;
        }

        @Override
        protected void compose(JSONObject json) throws JSONException {
            json.accumulate("type", getType().name());
            json.accumulate("latitude", location.getLatitude());
            json.accumulate("longitude", location.getLongitude());
            json.accumulate("radius", radius);
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public Type getType() {
            return Type.TESTCONDITION;
        }

        @Override
        public Location getLocation() {
            return location;
        }

        @Override
        public boolean hasLocation() {
            return true;
        }


        @Override
        public JSONArray getMetadata() throws JSONException {
            JSONArray array = new JSONArray();
            JSONObject jObject = new JSONObject();
            jObject.accumulate("type", getType().name());
            jObject.accumulate("latitude", location.getLatitude());
            jObject.accumulate("longitude", location.getLongitude());
            array.put(jObject);
            return array;
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

    @Test
    public void testPositionCondition() throws InterruptedException {
        GPSProvider gps = GPSProvider.getInstance();
        // BC
        gps.setMockLocation(makeLocation(46.518568, 6.561926));
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
        o1.assertAll(false, 0);
        o2.assertAll(false, 1);
        // in the Rolex
        gps.setMockLocation(makeLocation(46.518313, 6.567804));
        o1.assertAll(true, 1);
        o2.assertAll(false, 1);
        // back to BC
        gps.setMockLocation(makeLocation(46.518568, 6.561926));
        o1.assertAll(false, 2);
        o2.assertAll(true, 2);
    }

    @Test
    public void testGetLocation() {
        // setup
        Location loc = new Location("test");
        loc.setLatitude(69);
        loc.setLongitude(42);

        Location loc2 = new Location("test");
        loc2.setLatitude(78);
        loc2.setLongitude(93);

        TestCondPosition posCond = new TestCondPosition(loc, 20);
        TestCondPosition posCond2 = new TestCondPosition(loc2, 20);

        // simple
        assertEquals(loc, posCond.getLocation());
        assertFalse(Condition.trueCondition().hasLocation());
        assertFalse(Condition.falseCondition().hasLocation());

        try {
            Condition.trueCondition().getLocation();
            fail("getLocation on condition without location didn't throw an exception");
        } catch (UnsupportedOperationException e) {}

        try {
            Condition.falseCondition().getLocation();
            fail("getLocation on condition without location didn't throw an exception");
        } catch (UnsupportedOperationException e) {}

        // and
        Condition and1 = Condition.and(Condition.trueCondition(), posCond);
        Condition and2 = Condition.and(posCond, Condition.trueCondition());
        Condition and3 = Condition.and(posCond, posCond2);
        Condition and4 = Condition.and(Condition.trueCondition(),
                Condition.not(Condition.trueCondition()));

        assertEquals(loc, and1.getLocation());
        assertEquals(loc, and2.getLocation());

        assertTrue(and3.getLocation().equals(loc) || and3.getLocation().equals(loc2));
        assertTrue(and3.hasLocation());

        assertFalse(and4.hasLocation());

        // or
        Condition or1 = Condition.or(Condition.trueCondition(), posCond);
        Condition or2 = Condition.or(posCond, Condition.falseCondition());
        Condition or3 = Condition.or(posCond, posCond2);
        Condition or4 = Condition.or(Condition.not(or2), and4);
        Condition or5 = Condition.and(Condition.not(and4), Condition.not(and4));


        assertEquals(loc, or1.getLocation());
        assertEquals(loc, or2.getLocation());
        assertEquals(loc, or4.getLocation());

        assertTrue(or3.getLocation().equals(loc) || or3.getLocation().equals(loc2));
        assertTrue(or3.hasLocation());

        assertFalse(or5.hasLocation());
        try {
            or5.getLocation();
            fail("getLocation on condition without location didn't throw an exception");
        } catch (UnsupportedOperationException e) {}
    }


    @Test
    public void testMetadataTrueCondition() throws JSONException {
        Condition a = Condition.trueCondition();
        JSONArray ja = new JSONArray();
        assertEquals(ja, a.getMetadata());
    }

    @Test
    public void testMetadataPositionCondition() throws JSONException {
        Condition pc = new TestCondPosition(123.4, 432.1, 10);
        JSONObject jo = new JSONObject();
        jo.accumulate("type", Condition.Type.TESTCONDITION);
        jo.accumulate("latitude", 123.4);
        jo.accumulate("longitude", 432.1);
        JSONArray ja = (new JSONArray()).put(jo);
        assertEquals(ja.toString(), pc.getMetadata().toString());
    }

    @Test
    public void testMetadataMultiplePositionCondition() throws JSONException {
        Condition pc1 = new TestCondPosition(123.4, 432.1, 10);
        Condition pc2 = new TestCondPosition(432.1, 123.4, 20);
        Condition pc1AndPc2 = Condition.and(pc1, pc2);
        JSONObject jo1 = new JSONObject();
        jo1.accumulate("type", Condition.Type.TESTCONDITION);
        jo1.accumulate("latitude", 123.4);
        jo1.accumulate("longitude", 432.1);
        JSONObject jo2 = new JSONObject();
        jo2.accumulate("type", Condition.Type.TESTCONDITION);
        jo2.accumulate("latitude", 432.1);
        jo2.accumulate("longitude", 123.4);
        JSONArray ja = new JSONArray();
        ja.put(jo1);
        ja.put(jo2);
        assertEquals(ja.toString(), pc1AndPc2.getMetadata().toString());
    }

    @Test
    public void testToJSONMetadata() throws JSONException {
        Condition pc1 = new TestCondPosition(123.4, 432.1, 10);

        JSONObject jo1 = new JSONObject();
        jo1.accumulate("type", Condition.Type.TESTCONDITION);
        jo1.accumulate("latitude", 123.4);
        jo1.accumulate("longitude", 432.1);
        jo1.accumulate("radius", 10);


        JSONObject met = new JSONObject();
        met.accumulate("type", Condition.Type.TESTCONDITION);
        met.accumulate("latitude", 123.4);
        met.accumulate("longitude", 432.1);

        JSONArray ja = new JSONArray();
        ja.put(met);

        jo1.accumulate("metadata", ja);

        assertEquals(jo1.toString(), pc1.toJSON().toString());
    }
}
