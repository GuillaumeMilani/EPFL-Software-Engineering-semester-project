package ch.epfl.sweng.calamar;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/27/15.
 */
public class PositionCondition extends Condition {

    private final static String CONDITION_TYPE = "position";

    private Location location;
    private double radius;

    /**
     * make a Location from its latitude and longitude
     * @param latitude
     * @param longitude
     * @return Location in this place
     */
    private Location makeLocation(double latitude, double longitude)
    {
        Location loc = new Location("calamarTeam");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }

    /**
     * construct a PositionCondition from a location and a radius
     * @param location
     * @param radius
     */
    PositionCondition(Location location, double radius)
    {
        this.location = location;
        this.radius = radius;
    }

    /**
     * construct a PositionCondition from a latitude, a longitude and a radius
     * @param latitude
     * @param longitude
     * @param radius
     */
    PositionCondition(double latitude, double longitude, double radius)
    {
        location = makeLocation(latitude, longitude);
        this.radius = radius;
    }

    /**
     * test if parameters obj satisfy condition
     * @return if obj match condition or not
     */
    @Override
    public boolean matches() {
        return location.distanceTo(GPSProvider.getInstance().getLocation()) < radius;
    }

    /**
     * compose this Condition in the json object
     * @param json jsonObject to put this in
     * @throws JSONException
     */
    @Override
    protected void compose(JSONObject json) throws JSONException {
        json.accumulate("type", CONDITION_TYPE);
        json.accumulate("latitude", location.getLatitude());
        json.accumulate("longitude", location.getLongitude());
        json.accumulate("radius", radius);
    }

    /**
     * create a Condition from a JSONObject
     * @param json Object in JSON format
     * @return the desired condition Condition
     * @throws JSONException
     * @throws IllegalArgumentException
     */
    public static Condition fromJSON(JSONObject json) throws JSONException {
        return new PositionCondition.Builder().parse(json).build();
    }

    /**
     * A Builder for {@link PositionCondition}, currently only used to parse JSON
     * @see ch.epfl.sweng.calamar.Condition.Builder
     */
    public static class Builder extends Condition.Builder {

        private double latitude, longitude;
        private double radius;

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if(!type.equals(PositionCondition.CONDITION_TYPE)) {
                throw new IllegalArgumentException("expected "+ PositionCondition.CONDITION_TYPE +" was : "+type);
            }
            latitude = json.getDouble("latitude");
            longitude = json.getDouble("longitude");
            radius = json.getDouble("radius");
            return this;
        }

        public PositionCondition build() {
            return new PositionCondition(latitude, longitude, radius);
        }
    }
}
