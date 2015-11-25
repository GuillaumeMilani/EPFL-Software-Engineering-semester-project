package ch.epfl.sweng.calamar.condition;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.map.GPSProvider;

/**
 * Created by pierre on 10/27/15.
 */
public class PositionCondition extends Condition {

    private final static double DEFAULT_RADIUS = 20;

    private final Location location;
    private final double radius;

    /**
     * construct a PositionCondition from a location and a radius
     *
     * @param location
     * @param radius
     */
    public PositionCondition(Location location, double radius) {
        this.location = location;
        this.radius = radius;
        GPSProvider.getInstance().addObserver(new GPSProvider.Observer() {
            @Override
            public void update(Location newLocation) {

                setValue(newLocation.distanceTo(PositionCondition.this.location) <
                        PositionCondition.this.radius);
            }
        });
    }


    public PositionCondition(Location location) {
        this(location, DEFAULT_RADIUS);
    }

    /**
     * construct a PositionCondition from a latitude, a longitude and a radius
     *
     * @param latitude
     * @param longitude
     * @param radius
     */
    public PositionCondition(double latitude, double longitude, double radius) {
        this(makeLocation(latitude, longitude), radius);
    }

    /**
     * make a Location from its latitude and longitude
     *
     * @param latitude
     * @param longitude
     * @return Location in this place
     */
    private static Location makeLocation(double latitude, double longitude) {
        Location loc = new Location("calamarTeam");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }


    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean hasLocation() {
        return true;
    }

    /**
     * compose this Condition in the json object
     *
     * @param json jsonObject to put this in
     * @throws JSONException
     */
    @Override
    protected void compose(JSONObject json) throws JSONException {
        json.accumulate("type", getType().name());
        json.accumulate("latitude", location.getLatitude());
        json.accumulate("longitude", location.getLongitude());
        json.accumulate("radius", radius);
    }

    @Override
    public String toString() {
        return "position : (" + location.getLatitude() + " , " + location.getLongitude() + " , " + radius + ")";
    }

    @Override
    public Type getType() {
        return Type.POSITIONCONDITION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionCondition)) return false;
        PositionCondition that = (PositionCondition) o;
        return super.equals(that) && location.equals(that.location) && radius == that.radius;
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

    @Override
    public View getView(Context context) {
        LinearLayout view = (LinearLayout) (super.getView(context));
        Button button = new Button(context);
        button.setText(context.getResources().getString(R.string.condition_position));
        view.addView(button);
        return view;
    }

    /**
     * create a Condition from a JSONObject
     *
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
     *
     * @see Condition.Builder
     */
    public static class Builder extends Condition.Builder {

        private double latitude, longitude;
        private double radius;

        @Override
        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if(Type.valueOf(type) != Type.POSITIONCONDITION) {
                throw new IllegalArgumentException("expected : " + Type.POSITIONCONDITION.name() +
                        " was : "+type);
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
