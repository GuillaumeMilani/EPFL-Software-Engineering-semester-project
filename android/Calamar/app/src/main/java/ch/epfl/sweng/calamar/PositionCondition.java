package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/27/15.
 */
public class PositionCondition extends Condition {

    private final static String CONDITION_TYPE = "poisition";

    //position + radius

    /**
     * test if parameters obj satisfy condition
     * @return if obj match condition or not
     */
    @Override
    public boolean matches() {
        return false;
    }

    /**
     * compose this Condition in the json object
     * @param json jsonObject to put this in
     * @throws JSONException
     */
    @Override
    protected void compose(JSONObject json) throws JSONException {

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

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if(!type.equals(PositionCondition.CONDITION_TYPE)) {
                throw new IllegalArgumentException("expected "+ PositionCondition.CONDITION_TYPE +" was : "+type);
            }
            return this;
        }

        public PositionCondition build() {
            return new PositionCondition();
        }
    }
}
