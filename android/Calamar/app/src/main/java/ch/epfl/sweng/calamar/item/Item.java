package ch.epfl.sweng.calamar.item;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    private final Date date; //posix date
    private final Condition condition;
    private final String message;


    public enum Type {SIMPLETEXTITEM, IMAGEITEM, FILEITEM}

    private Set<Item.Observer> observers = new HashSet<>();


    private final Condition.Observer conditionObserver = new Condition.Observer() {
        @Override
        public void update(Condition condition) {
            for (Observer o : observers) {
                o.update(Item.this);
            }
        }
    };


    protected Item(int ID, User from, Recipient to, Date date, Condition condition, String message) {
        if (null == from || null == to || null == condition) {
            throw new IllegalArgumentException("field 'from' and/or 'to' and/or 'condition' cannot be null");
        }
        if (message == null) {
            this.message = "";
        } else {
            this.message = message;
        }
        this.ID = ID;
        this.from = from; //User is immutable
        this.to = to;     //Recipient is immutable
        this.date = date;
        this.condition = condition; //TODO Condition is not immutable...

        condition.addObserver(conditionObserver);
    }

    protected Item(int ID, User from, Recipient to, Date date, String message) {
        this(ID, from, to, date, Condition.trueCondition(), message);
    }

    public abstract Type getType();

    protected abstract View getItemView(Context context);

    /**
     * @return the text content (message) of the Item
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the complete view of the item. ( With condition(s) )
     *
     * @param context
     * @return the view of the item.
     */
    public View getView(final Activity context) {
        //Inflate a basic layout
        LayoutInflater li = LayoutInflater.from(context);
        View baseView = li.inflate(R.layout.item_details_base_layout, null);

        //FIll the layout
        TextView dateText = (TextView) baseView.findViewById(R.id.ItemDetailsDate);
        dateText.setText(date.toString());

        TextView fromText = (TextView) baseView.findViewById(R.id.ItemDetailsUserFrom);
        fromText.setText(from.toString());

        TextView toText = (TextView) baseView.findViewById(R.id.ItemDetailsUserTo);
        toText.setText(to.toString());

        LinearLayout previewLayout = (LinearLayout) baseView.findViewById(R.id.ItemDetailsItemPreview);
        previewLayout.addView(getPreView(context));

        LinearLayout conditionLayout = (LinearLayout) baseView.findViewById(R.id.ItemDetailsConditionLayout);
        conditionLayout.addView(condition.getView(context));

        return baseView;
    }

    /**
     * Get a simple pre view of the item
     *
     * @param context
     * @return the preview of the item
     */
    public View getPreView(final Context context) {
        final LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);

        if (!message.equals("")) {
            TextView text = new TextView(context);
            text.setText(message);
            view.addView(text, 0);
        }
        if (condition.getValue()) {
            view.addView(getItemView(context), 1);
        } else {
            TextView lockMessage = new TextView(context);
            lockMessage.setText(R.string.item_is_locked_getview);
            view.addView(lockMessage, 1);
        }

        return view;
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
        return date;
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
        json.accumulate("date", date.getTime());
        json.accumulate("condition", condition.toJSON());
        json.accumulate("message", message);
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
            case FILEITEM:
                item = FileItem.fromJSON(json);
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
                that.date.getTime() == date.getTime() && this.condition.equals(that.condition) &&
                that.message.equals(this.message);
    }

    /**
     * java hash function
     *
     * @return hash of the Object
     */
    @Override
    public int hashCode() {
        return ID + from.hashCode() * 89 + to.hashCode() * 197 + ((int) date.getTime()) * 479 +
                condition.hashCode() * 503 + (message != null ? message.hashCode() : 0) * 701;
    }

    @Override
    public String toString() {
        return "id : " + ID + " , from : (" + from + ") , to : (" + to + ") , at : " + date
                + " message : " + message;
    }

    /**
     * A Builder for {@link Item}, has no build() method since Item isn't instantiable,
     * is used by the child builders (in {@link SimpleTextItem} or...) to build the "Item
     * part of the object". currently only used to parse JSON (little overkill..but ..)
     */
    protected abstract static class Builder {
        protected int ID;
        protected User from;
        protected Recipient to;
        protected Date date;
        protected Condition condition = Condition.trueCondition();
        protected String message;

        protected Builder parse(JSONObject o) throws JSONException {
            ID = o.getInt("ID");
            from = User.fromJSON(o.getJSONObject("from"));
            if (o.isNull("to")) {
                to = new User(User.PUBLIC_ID, User.PUBLIC_NAME);
            } else {
                to = Recipient.fromJSON(o.getJSONObject("to"));
            }
            message = o.getString("message");
            date = new Date(o.getLong("date"));

            if (o.isNull("condition")) {
                condition = Condition.trueCondition();
            } else {
                condition = Condition.fromJSON(new JSONObject(o.getString("condition")));
            }

            return this;
        }

        protected Builder setID(int ID) {
            this.ID = ID;
            return this;
        }

        protected Builder setFrom(User from) {
            this.from = from;
            return this;
        }

        protected Builder setTo(Recipient to) {
            this.to = to;
            return this;
        }

        protected Builder setDate(long date) {
            this.date = new Date(date);
            return this;
        }

        protected Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        protected Builder setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        protected void setMessage(String message) {
            this.message = message;
        }

        protected abstract Item build();
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
