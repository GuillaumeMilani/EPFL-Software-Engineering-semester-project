package ch.epfl.sweng.calamar.item;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Models an Item, superclass of all the possibly many kind of 'item' the app manage. <br><br>
 * known subclasses : <li>
 * <ul>{@link SimpleTextItem},</ul>
 * </li>
 * Item is immutable
 */
public abstract class Item {
    private final int ID;
    private final User from;
    private final Recipient to;
    private final long date; //posix date
    private final Condition condition;

    public enum Type {SIMPLETEXTITEM, IMAGEITEM}
    //TODO date d'expiration ?

    private Set<Observer> observers = new HashSet<>();


    private final Condition.Observer conditionObserver = new Condition.Observer() {
        @Override
        public void update(Condition condition) {
            for(Observer o : observers){
                o.update(Item.this);
            }
        }
    };

    protected Item(int ID, User from, Recipient to, long date, Condition condition) {
        if (null == from || null == to || null == condition) {
            throw new IllegalArgumentException("field 'from' and/or 'to' and/or 'condition' cannot be null");
        }
        this.ID = ID;
        this.from = from; //User is immutable
        this.to = to;     //Recipient is immutable
        this.date = date;
        this.condition = condition;

        condition.addObserver(conditionObserver);
    }

    protected Item(int ID, User from, Recipient to, long date) {
        this(ID, from, to, date, Condition.trueCondition());
    }

    public abstract Type getType();

    protected abstract View getItemView(Context context);


    /**
     * Get the complete view of the item. ( With condition(s) )
     *
     * @param context
     * @return the view of the item.
     */
    public View getView(final Context context)
    {
        final LinearLayout view = (LinearLayout)getPreView(context);

        setConditionView(view,context);

        //When the condition is okay, we update the item description
        final Condition.Observer previewConditionObserver = new Condition.Observer() {
            @Override
            public void update(Condition condition) {
                view.removeAllViews();
                setItemView(view, context);
                setConditionView(view,context);
            }
        };

        condition.addObserver(previewConditionObserver);
        //TODO : When to remove this observer ?
        
        return view;
    }

    /**
     * Get a simple pre view of the item
     *
     * @param context
     * @return the preview of the item
     */
    public View getPreView(final Context context){
        final LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        setItemView(view, context);

        return view;
    }

    private void setItemView(View v, Context context){
        LinearLayout preview = (LinearLayout)v;
        if(condition.getValue()){
            preview.addView(getItemView(context), 0);
        } else {
            TextView lockMessage = new TextView(context);
            lockMessage.setText(R.string.item_is_locked_getview);
            preview.addView(lockMessage, 0);
        }
    }

    private void setConditionView(View v, Context context){
        LinearLayout view = (LinearLayout)v;
        TextView titleCondition = new TextView(context);
        titleCondition.setText(R.string.item_getView_condition_title);
        titleCondition.setTextSize(25);
        view.addView(titleCondition,1);
        view.addView(condition.getView(context), 2);
    }

    /**
     * @return the 'condition' field of the Item
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * @return the 'from' field of the Item (sender)
     */
    public User getFrom() {
        return from;
    }

    /**
     * @return the 'to' field of the Item (recipient)
     */
    public Recipient getTo() {
        return to;
    }

    /**
     * @return the creation/posting date of the Item
     */
    public Date getDate() {
        return new Date(this.date);
    }

    public int getID() {
        return ID;
    }

    /**
     * Appends the fields of {@link Item} to a {@link JSONObject} representing the Item.<br>
     * is called by the {@link #compose(JSONObject)} method of the child classes in
     * a chain where each compose method append the field of its class to the object.<br>
     * The chain begins by a call to {@link #toJSON()} in an instantiable child class.<br><br>
     * Should <b>NOT</b> be used alone.
     *
     * @param json the json to which we append (using {@link JSONObject#accumulate(String, Object)} ) data
     * @throws JSONException
     */
    protected void compose(JSONObject json) throws JSONException {
        json.accumulate("ID", ID);
        json.accumulate("from", from.toJSON());
        json.accumulate("to", to.toJSON());
        json.accumulate("date", date);
        json.accumulate("condition", condition.toJSON());
    }

    /**
     * @return a JSONObject representing a {@link Item)
     * @throws JSONException
     */
    //must remains abstract if class not instantiable
    public abstract JSONObject toJSON() throws JSONException;

    /**
     * Parses an Item from a JSONObject.<br>
     * To instantiate the correct Item ({@link SimpleTextItem}, etc ...)
     * the JSON must have a 'type' field indicating the type...('simpleText', ...)
     *
     * @param json the well formed {@link JSONObject json} representing the {@link Item item}
     * @return a {@link Item item} parsed from the JSONObject
     * @throws JSONException
     */
    public static Item fromJSON(JSONObject json) throws JSONException, IllegalArgumentException {
        if (null == json || json.isNull("type")) {
            throw new IllegalArgumentException("malformed json, either null or no 'type' value");
        }
        Item item;
        String type = json.getString("type");
        switch (Type.valueOf(type)) {
            case SIMPLETEXTITEM:
                item = SimpleTextItem.fromJSON(json);
                break;
            case IMAGEITEM:
                item = ImageItem.fromJSON(json);
                break;
            default:
                throw new IllegalArgumentException("Unexpected Item type (" + type + ")");
        }
        return item;
    }

    /**
     * java equals
     *
     * @param o other Object to compare this with
     * @return true if o is equal in value to this
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Item)) return false;
        Item that = (Item) o;
        return that.ID == ID && that.from.equals(from) && that.to.equals(to) &&
                that.date == date && this.getType().equals(that.getType()) && this.condition.equals(that.condition);
    }

    /**
     * java hash function
     *
     * @return hash of the Object
     */
    @Override
    public int hashCode() {
        return ID + from.hashCode() * 89 + to.hashCode() * 197 + ((int) date) * 479;
    }

    @Override
    public String toString(){
        return "id : "+ID+" , from : ("+from+") , to : ("+to+") , at : "+new Date(date);
    }

    /**
     * A Builder for {@link Item}, has no build() method since Item isn't instantiable,
     * is used by the child builders (in {@link SimpleTextItem} or...) to build the "Item
     * part of the object". currently only used to parse JSON (little overkill..but ..)
     */
    protected static class Builder {
        protected int ID;
        protected User from;
        protected Recipient to;
        protected long date;
        protected Condition condition;

        public Builder parse(JSONObject o) throws JSONException {
            ID = o.getInt("ID");
            from = User.fromJSON(o.getJSONObject("from"));
            to = Recipient.fromJSON(o.getJSONObject("to"));
            date = o.getLong("date");
            //TODO to delete when server ready to send true condition when there is no condition
            // and replace by just fromJSON etc..
            if(o.has("condition")) {
                condition = Condition.fromJSON(o.getJSONObject("condition"));
            } else {
                condition = Condition.trueCondition();
            }
            return this;
        }
    }

    public void addObserver(Item.Observer observer) {
        this.observers.add(observer);
    }

    public boolean removeObserver(Item.Observer observer) {
        return this.observers.remove(observer);
    }

    public abstract static class Observer {
        public abstract void update(Item item);
    }
}
