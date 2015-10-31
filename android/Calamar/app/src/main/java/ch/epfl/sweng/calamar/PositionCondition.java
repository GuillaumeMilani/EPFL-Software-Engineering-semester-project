package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pierre on 10/27/15.
 */
public class PositionCondition extends Condition {

    private final static String CONDITION_TYPE = "poisition";

    //position + radius

    @Override
    public boolean matches() {
        return false;
    }

    @Override
    protected void compose(JSONObject json) throws JSONException {

    }

    public static Condition fromJSON(JSONObject json) throws JSONException {
        return new PositionCondition.Builder().parse(json).build();
    }

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
