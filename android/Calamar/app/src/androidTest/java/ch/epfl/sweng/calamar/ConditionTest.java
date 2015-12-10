package ch.epfl.sweng.calamar;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.map.GPSProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;

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

    @Before
    public void setUp() {
        // to shut his mouth
        CalamarApplication.getInstance().setGoogleApiClient(mock(GoogleApiClient.class));
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
        o.assertAll(true, 1);
        b.set(false);
        o.assertAll(true, 1);
        try {
            Condition.and(null, a);
            fail("Condition shouldn't be null");
        } catch (IllegalArgumentException e) {
        }
        try {
            Condition.and(a, null);
            fail("Condition shouldn't be null");
        } catch (IllegalArgumentException e) {
        }
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
        o.assertAll(true, 1);
        try {
            Condition.or(null, a);
            fail("Condition shouldn't be null");
        } catch (IllegalArgumentException e) {
        }
        try {
            Condition.or(a, null);
            fail("Condition shouldn't be null");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testConditionNot() {
        TC a = new TC();
        TO o = new TO(Condition.not(a));
        o.assertAll(true, 0);
        a.set(false);
        o.assertAll(true, 0);
        a.set(true);
        o.assertAll(true, 0);
        a.set(true);
        o.assertAll(true, 0);
        a.set(false);
        o.assertAll(true, 0);
    }

    public static Location makeLocation(double latitude, double longitude) {
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
        o2.assertAll(false, 0); // because at begin it is always false
        // Amphimax
        gps.setMockLocation(makeLocation(46.521783, 6.575507));
        o1.assertAll(false, 0);
        o2.assertAll(false, 0);
        // in the Rolex
        gps.setMockLocation(makeLocation(46.518388, 6.568313));
        o1.assertAll(true, 1);
        o2.assertAll(false, 0);
        // back to BC
        gps.setMockLocation(makeLocation(46.518568, 6.561926));
        o1.assertAll(true, 1);
        o2.assertAll(true, 1);
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

        PositionCondition posCond = new PositionCondition(loc, 20);
        PositionCondition posCond2 = new PositionCondition(loc2, 20);

        // simple
        assertEquals(loc, posCond.getLocation());
        assertFalse(Condition.trueCondition().hasLocation());
        assertFalse(Condition.falseCondition().hasLocation());

        try {
            Condition.trueCondition().getLocation();
            fail("getLocation on condition without location didn't throw an exception");
        } catch (UnsupportedOperationException e) {
        }

        try {
            Condition.falseCondition().getLocation();
            fail("getLocation on condition without location didn't throw an exception");
        } catch (UnsupportedOperationException e) {
        }

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
        } catch (UnsupportedOperationException e) {
        }
    }


    @Test
    public void testMetadataTrueCondition() throws JSONException {
        Condition a = Condition.trueCondition();
        JSONArray ja = new JSONArray();
        assertEquals(ja, a.getMetadata());
    }

    @Test
    public void testMetadataPositionCondition() throws JSONException {
        Condition pc = new PositionCondition(12.3, 43.2, 10);
        JSONObject jo = new JSONObject();
        jo.accumulate("type", Condition.Type.POSITIONCONDITION);
        jo.accumulate("latitude", 12.3);
        jo.accumulate("longitude", 43.2);
        JSONArray ja = (new JSONArray()).put(jo);
        assertEquals(ja.toString(), pc.getMetadata().toString());
    }

    @Test
    public void testMetadataMultiplePositionCondition() throws JSONException {
        Condition pc1 = new PositionCondition(12.3, 43.2, 10);
        Condition pc2 = new PositionCondition(43.2, 12.3, 20);
        Condition pc1AndPc2 = Condition.and(pc1, pc2);
        JSONObject jo1 = new JSONObject();
        jo1.accumulate("type", Condition.Type.POSITIONCONDITION);
        jo1.accumulate("latitude", 12.3);
        jo1.accumulate("longitude", 43.2);
        JSONObject jo2 = new JSONObject();
        jo2.accumulate("type", Condition.Type.POSITIONCONDITION);
        jo2.accumulate("latitude", 43.2);
        jo2.accumulate("longitude", 12.3);
        JSONArray ja = new JSONArray();
        ja.put(jo1);
        ja.put(jo2);
        assertEquals(ja.toString(), pc1AndPc2.getMetadata().toString());
    }

    @Test
    public void testToJSONMetadata() throws JSONException {
        Condition pc1 = new PositionCondition(12.3, 43.2, 10);

        JSONObject jo1 = new JSONObject();
        jo1.accumulate("type", Condition.Type.POSITIONCONDITION);
        jo1.accumulate("latitude", 12.3);
        jo1.accumulate("longitude", 43.2);
        jo1.accumulate("radius", 10);


        JSONObject met = new JSONObject();
        met.accumulate("type", Condition.Type.POSITIONCONDITION);
        met.accumulate("latitude", 12.3);
        met.accumulate("longitude", 43.2);

        JSONArray ja = new JSONArray();
        ja.put(met);

        jo1.accumulate("metadata", ja);

        assertEquals(jo1.toString(), pc1.toJSON().toString());
    }

    @Test
    public void testConditionFromJSONExceptions() throws JSONException {
        try {
            Condition.fromJSON(null);
            fail();
        } catch (IllegalArgumentException e) {
            // yikiyik
        }
        Condition condition = new PositionCondition(2, 2, 20);
        try {
            JSONObject jsonObject = condition.toJSON();
            jsonObject.remove("type");
            Condition.fromJSON(jsonObject);
            fail();
        } catch (IllegalArgumentException e) {
            // Boum Boum
        }
        try {
            JSONObject jsonObject = condition.toJSON();
            jsonObject.put("type", "wrong type");
            Condition.fromJSON(jsonObject);
            fail();
        } catch (IllegalArgumentException e) {
            // yes
        }
    }

    @Test
    public void testBadLongitudeOrLatitude() {
        try {
            new PositionCondition(-91, 140, 20);
            fail("Latitude shouldn't be smaller than -90");
        } catch (IllegalArgumentException e) {

        }
        try {
            new PositionCondition(91, 140, 20);
            fail("Latitude shouldn't be greater than 90");
        } catch (IllegalArgumentException e) {

        }
        try {
            new PositionCondition(45, -181, 20);
            fail("Longitude shouldn't be smaller than -180");
        } catch (IllegalArgumentException e) {

        }
        try {
            new PositionCondition(45, 181, 20);
            fail("Longitude shouldn't be greater than 180");
        } catch (IllegalArgumentException e) {

        }
    }
}
