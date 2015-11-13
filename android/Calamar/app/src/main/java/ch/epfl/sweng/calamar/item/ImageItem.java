package ch.epfl.sweng.calamar.item;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Created by pierre on 11/12/15.
 */
public final class ImageItem extends Item {

    private final static Type ITEM_TYPE = Type.IMAGEITEM;

    protected ImageItem(int ID, User from, Recipient to, Date date, Condition condition) {
        super(ID, from, to, date.getTime(), condition);
    }

    @Override
    public Type getType() {
        return ITEM_TYPE;
    }

    @Override
    public void compose(JSONObject json) throws JSONException {

    }

    public static ImageItem fromJSON(JSONObject json) throws JSONException {
        return new ImageItem.Builder().parse(json).build();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageItem)) return false;
        ImageItem that = (ImageItem) o;
        return super.equals(that);
    }

    private static Bitmap String2Bitmap(String str)
    {
        byte[] bytes = str.getBytes();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private static String Bitmap2String(Bitmap bitmap)
    {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, blob);
        return new String(blob.toByteArray());
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 73;
    }

    public static class Builder extends Item.Builder {

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if (!type.equals(ImageItem.ITEM_TYPE.name())) {
                throw new IllegalArgumentException("expected " + ImageItem.ITEM_TYPE.name() + " was : " + type);
            }
            return this;
        }

        public ImageItem build() {
            return new ImageItem(super.ID, super.from, super.to, new Date(super.date), super.condition);
        }
    }
}
