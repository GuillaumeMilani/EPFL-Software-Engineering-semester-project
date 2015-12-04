package ch.epfl.sweng.calamar.condition;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;

/**
 * Created by pierre on 10/27/15.
 */
public abstract class Condition {

    // TODO make tostring usable ^^

    protected static final String JSON_TYPE = "type";
    private static final String JSON_METADATA = "metadata";
    private static final String JSON_VALUE = "val";
    private static final String JSON_LEFT = "a";
    private static final String JSON_RIGHT = "b";
    private static final int GREEN = Color.rgb(0, 155, 0);
    private static final int RED = Color.rgb(155, 0, 0);

    public enum Type {
        POSITIONCONDITION, TRUECONDITION, FALSECONDITION,
        ANDCONDITION, ORCONDITION, NOTCONDITION, TESTCONDITION
    }

    private Boolean value = false;
    private final Set<Observer> observers = new HashSet<>();

    private static JSONArray concatArray(JSONArray... arrays)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray array : arrays) {
            for (int i = 0; i < array.length(); i++) {
                result.put(array.get(i));
            }
        }
        return result;
    }

    /**
     * compose this Condition in the json object
     *
     * @param json jsonObject to put this in
     * @throws JSONException
     */
    protected abstract void compose(JSONObject json) throws JSONException;

    /**
     * get a JSON description of this
     *
     * @return JSONObject describing this
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject ret = new JSONObject();
        this.compose(ret);
        ret.accumulate(JSON_METADATA, getMetadata());
        return ret;
    }

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Condition)) return false;
        Condition that = (Condition) o;
        return value == that.value && getType().equals(that.getType());
    }

    public abstract Type getType();


    /**
     * @return the location in the condition if any
     * @throws UnsupportedOperationException if {@link #hasLocation} returns false
     */
    public Location getLocation() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CalamarApplication.getInstance().getString(R.string.condition_getlocation_unsupported));
    }

    /**
     * @return true if condition contains at least one location, false otherwise
     */
    public boolean hasLocation() {
        return false;
    }

    public View getView(Activity context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        //TODO : Add padding ?
        // Good, but when we have and(and(c1,c2),c3), c3 is not in the right place.
        //layout.setPadding(10,10,10,10);

        layout.setBackgroundColor(getValue() ? GREEN : RED);
        return layout;

        //TODO : Try to make it work to see if it's better looking
        /*
        return new FrameLayout(context) {
            Paint paint = new Paint();

            {
                addObserver(new Observer() {
                    @Override
                    public void update(Condition condition) {
                        invalidate();
                    }
                });
            }

            @Override
            public void onDraw(Canvas canvas) {
                paint.setColor(getValue() ? Color.GREEN : Color.RED);
                paint.setStrokeWidth(3);
                canvas.drawRect(3, 3, getWidth()-3, getHeight()-3, paint);
            }
        };*/
    }


    /**
     * set a value for this condition. If newValue differs from old, notify observers
     *
     * @param newValue new value to set
     */
    protected void setValue(Boolean newValue) {
        if (!value && newValue) {
            value = true;
            for (Observer o : observers) {
                o.update(this);
            }
        }
    }

    /**
     * @return the value of the condition, true if condition fulfilled false otherwise
     */
    public boolean getValue() {
        return value;
    }

    public JSONArray getMetadata() throws JSONException {
        return new JSONArray();
    }

    /**
     * create a Condition from a JSONObject
     *
     * @param json Object in JSON format
     * @return the desired condition Condition
     * @throws JSONException
     * @throws IllegalArgumentException
     */
    public static Condition fromJSON(JSONObject json) throws JSONException, IllegalArgumentException {
        if (null == json || json.isNull(JSON_TYPE)) {
            throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.malformed_json));
        }
        Condition cond;
        String type = json.getString(JSON_TYPE);
        switch (Type.valueOf(type)) {
            case POSITIONCONDITION:
                cond = PositionCondition.fromJSON(json);
                break;
            case ANDCONDITION:
                cond = and(fromJSON(json.getJSONObject(JSON_LEFT)), fromJSON(json.getJSONObject(JSON_RIGHT)));
                break;
            case ORCONDITION:
                cond = or(fromJSON(json.getJSONObject(JSON_LEFT)), fromJSON(json.getJSONObject(JSON_RIGHT)));
                break;
            case NOTCONDITION:
                cond = not(fromJSON(json.getJSONObject(JSON_VALUE)));
                break;
            case TRUECONDITION:
                cond = trueCondition();
                break;
            case FALSECONDITION:
                cond = falseCondition();
                break;
            default:
                throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.unexpected_item_type, type));
        }
        return cond;
    }

    /**
     * Create an always true condition
     *
     * @return true condition
     */
    public static Condition trueCondition() {
        return new Condition() {
            {
                this.setValue(true);
            }

            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate(JSON_TYPE, getType().name());
            }

            @Override
            public String toString() {
                return "(true)";
            }

            @Override
            public Type getType() {
                return Type.TRUECONDITION;
            }

            @Override
            public View getView(Activity context) {
                LinearLayout view = (LinearLayout) (super.getView(context));
                TextView tv = new TextView(context);
                tv.setText(context.getResources().getString(R.string.condition_true)); // TODO differs from name in enum...
                view.addView(tv);
                return view;
            }
        };
    }


    /**
     * Create an always false condition
     *
     * @return false condition
     */
    public static Condition falseCondition() {
        return new Condition() {
            {
                setValue(false);
            }

            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate(JSON_TYPE, getType().name());
            }

            @Override
            public String toString() {
                return "(false)";
            }

            @Override
            public Type getType() {
                return Type.FALSECONDITION;
            }

            @Override
            public View getView(Activity context) {
                LinearLayout view = (LinearLayout) (super.getView(context));
                TextView tv = new TextView(context);
                tv.setText(context.getResources().getString(R.string.condition_false));
                view.addView(tv);
                return view;
            }
        };
    }

    /**
     * create a condition that represent the intersection of two conditions
     *
     * @param c1 first condition
     * @param c2 second condition
     * @return a new condition that is the intersection of c1 and c2
     */
    public static Condition and(final Condition c1, final Condition c2) {
        return new Condition() {
            //constructor
            {
                // usefull but some tests in ChatActivityBasicTest doesn't pass
                /*if (c1.getType() == Type.FALSECONDITION || c2.getType() == Type.FALSECONDITION) {
                    throw new IllegalArgumentException("... && false will always be false..");
                }*/
                setValue(c1.value && c2.value);
                Condition.Observer o = new Observer() {

                    @Override
                    public void update(Condition c) {
                        setValue(c1.value && c2.value);
                        if (getValue()) {
                            c1.removeObserver(this);
                            c2.removeObserver(this);
                        }
                    }
                };
                c1.addObserver(o);
                c2.addObserver(o);
            }


            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate(JSON_TYPE, getType().name());
                json.accumulate(JSON_LEFT, c1.toJSON());
                json.accumulate(JSON_RIGHT, c2.toJSON());
            }

            @Override
            public String toString() {
                return "(" + c1.toString() + " && " + c2.toString() + ")";
            }

            @Override
            public Type getType() {
                return Type.ANDCONDITION;
            }

            @Override

            public Location getLocation() {
                return Condition.getLocation(c1, c2);
            }

            @Override
            public boolean hasLocation() {
                return c1.hasLocation() || c2.hasLocation();
            }

            @Override
            public JSONArray getMetadata() throws JSONException {
                return concatArray(c1.getMetadata(), c2.getMetadata());
            }

            @Override
            public View getView(Activity context) {
                LinearLayout view = (LinearLayout) (super.getView(context));
                LinearLayout LL = new LinearLayout(context);
                LL.setOrientation(LinearLayout.VERTICAL);
                TextView tv = new TextView(context);
                tv.setText(context.getResources().getString(R.string.condition_and));
                LL.addView(c1.getView(context), 0);
                LL.addView(tv, 1);
                LL.addView(c2.getView(context), 2);
                view.addView(LL);
                return view;
            }
        };
    }

    /**
     * create a condition that represent the union of two conditions
     *
     * @param c1 first condition
     * @param c2 second condition
     * @return a new condition that is the union of c1 and c2
     */
    public static Condition or(final Condition c1, final Condition c2) {
        return new Condition() {
            //constructor
            {
                setValue(c1.value || c2.value);
                Condition.Observer o = new Observer() {

                    @Override
                    public void update(Condition c) {
                        setValue(c1.value || c2.value);
                        if (getValue()) {
                            c1.removeObserver(this);
                            c2.removeObserver(this);
                        }
                    }
                };
                c1.addObserver(o);
                c2.addObserver(o);
            }

            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate(JSON_TYPE, getType().name());
                json.accumulate(JSON_LEFT, c1.toJSON());
                json.accumulate(JSON_RIGHT, c2.toJSON());
            }

            @Override
            public String toString() {
                return "(" + c1.toString() + " || " + c2.toString() + ")";
            }

            @Override
            public Type getType() {
                return Type.ORCONDITION;
            }

            @Override
            public Location getLocation() {
                return Condition.getLocation(c1, c2);
            }

            @Override
            public boolean hasLocation() {
                return c1.hasLocation() || c2.hasLocation();
            }

            @Override
            public JSONArray getMetadata() throws JSONException {
                return concatArray(c1.getMetadata(), c2.getMetadata());
            }

            public View getView(Activity context) {
                LinearLayout view = (LinearLayout) (super.getView(context));
                LinearLayout LL = new LinearLayout(context);
                LL.setOrientation(LinearLayout.VERTICAL);
                TextView tv = new TextView(context);
                tv.setText(context.getResources().getString(R.string.condition_or));
                LL.addView(c1.getView(context), 0);
                LL.addView(tv, 1);
                LL.addView(c2.getView(context), 2);
                view.addView(LL);
                return view;
            }
        };
    }

    /**
     * negats a condition
     *
     * @param c condition to negate
     * @return a condition that is true when c is false and false when c is true
     */
    public static Condition not(final Condition c) {
        return new Condition() {
            //constructor
            {
                setValue(!c.value);
                Condition.Observer o = new Observer() {

                    @Override
                    public void update(Condition c) {
                        setValue(!c.value);
                        if (getValue()) {
                            c.removeObserver(this);
                        }
                    }
                };
                c.addObserver(o);
            }

            @Override
            protected void compose(JSONObject json) throws JSONException {
                json.accumulate(JSON_TYPE, getType().name());
                json.accumulate(JSON_VALUE, c.toJSON());
            }

            @Override
            public String toString() {
                return "(!" + c.toString() + ")";
            }

            @Override
            public Type getType() {
                return Type.NOTCONDITION;
            }

            @Override
            public JSONArray getMetadata() throws JSONException {
                return c.getMetadata();
            }

            @Override
            public Location getLocation() {
                return c.getLocation();
            }

            @Override
            public boolean hasLocation() {
                return c.hasLocation();
            }

            @Override
            public View getView(Activity context) {
                LinearLayout view = (LinearLayout) (super.getView(context));
                LinearLayout LL = new LinearLayout(context);
                LL.setOrientation(LinearLayout.VERTICAL);
                TextView tv = new TextView(context);
                tv.setText(context.getResources().getString(R.string.condition_not));
                LL.addView(tv, 0);
                LL.addView(c.getView(context), 1);
                view.addView(LL);
                return view;
            }
        };
    }

    /**
     * A Builder for {@link Condition}, has no build() method since Item isn't instantiable,
     * is used by the child builders (in {@link PositionCondition} or...) to build the "Condition
     * part of the object". currently only used to parse JSON
     */
    public static class Builder {

        public Builder parse(JSONObject o) throws JSONException {
            return this;
        }
    }

    public void addObserver(Condition.Observer observer) {
        this.observers.add(observer);
    }

    public boolean removeObserver(Condition.Observer observer) {
        return this.observers.remove(observer);
    }

    private static Location getLocation(Condition c1, Condition c2) {
        if (c1.hasLocation()) {
            return c1.getLocation();
        }
        return c2.getLocation(); // if c2 hasn't any location -> default throw exception
    }

    public abstract static class Observer {
        public abstract void update(Condition condition);
    }
}
