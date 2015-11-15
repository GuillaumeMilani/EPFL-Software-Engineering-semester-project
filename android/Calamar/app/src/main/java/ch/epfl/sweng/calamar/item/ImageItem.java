package ch.epfl.sweng.calamar.item;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Created by pierre on 11/12/15.
 */
public final class ImageItem extends Item {

    private final static Type ITEM_TYPE = Type.IMAGEITEM;

    private final Bitmap bitmap;

    /**
     * Instantiates a new ImageItem with the following parameters
     * @param ID the id
     * @param from the 'from' field of the Item (sender)
     * @param to the 'to' field of the Item (recipient)
     * @param date the creation/posting date of the Item
     * @param condition the content (text message)
     * @param bitmap the image
     * @see Item#Item(int, User, Recipient, long, Condition)
     */
    public ImageItem(int ID, User from, Recipient to, Date date, Condition condition, Bitmap bitmap) {
        super(ID, from, to, date.getTime(), condition);
        this.bitmap = bitmap;
    }

    /**
     * Instantiates a new ImageItem with the following parameters (condition will be always true)
     * @param ID the id
     * @param from the 'from' field of the Item (sender)
     * @param to the 'to' field of the Item (recipient)
     * @param date the creation/posting date of the Item
     * @param bitmap the image
     * @see Item#Item(int, User, Recipient, long)
     */
    public ImageItem(int ID, User from, Recipient to, Date date, Bitmap bitmap) {
        super(ID, from, to, date.getTime());
        this.bitmap = bitmap;
    }

    /**
     * gets type of this ImageItem (always return IMAGEITEM
     * @return IMAGEITEM
     */
    @Override
    public Type getType() {
        return ITEM_TYPE;
    }

    /**
     * Appends the fields of ImageItem to the given JSONObject. <br><br>
     * Should <b>NOT</b> be used alone
     *
     * @param json the json to which we append data
     * @throws JSONException
     */
    @Override
    public void compose(JSONObject json) throws JSONException {
        super.compose(json);
        json.accumulate("image", bitmap2String(bitmap));
        json.accumulate("type", ITEM_TYPE.name());
    }

    /**
     * Parses a ImageItem from a JSONObject.<br>
     *
     * @param json the well formed {@link JSONObject json} representing the {@link ImageItem item}
     * @return a {@link ImageItem} parsed from the JSONObject
     * @throws JSONException
     * @see Item#fromJSON(JSONObject) Recipient.fromJSON
     */
    public static ImageItem fromJSON(JSONObject json) throws JSONException {
        return new ImageItem.Builder().parse(json).build();
    }

    /**
     * @return a JSONObject representing a {@link ImageItem}
     * @throws JSONException
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject ret = new JSONObject();
        this.compose(ret);
        return ret;
    }

    /**
     * test if this is equals to other Object, true when object is an ImageItem and content truly equals
     *
     * @param o Obect to compare this with
     * @return true if two ImageItems are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageItem)) return false;
        ImageItem that = (ImageItem) o;
        return super.equals(that) && bitmap.sameAs(that.bitmap);
    }

    /**
     * hash the ImageItem
     *
     * @return hash of the ImageItem
     */
    @Override
    public int hashCode() {
        if(bitmap != null) {
            return super.hashCode() * 73;
        }
        else {
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, blob);
            return super.hashCode() * 73 + Arrays.hashCode(blob.toByteArray());
        }
    }

    /**
     * used to transform a string (containing an array of bytes representing the png image) to a bitmap
     * @param str png image as byte[]
     * @return bitmap corresponding to given byte[] input
     */
    public static Bitmap string2Bitmap(String str)
    {
        byte[] bytes2 = str.getBytes(Charset.forName("UTF-8"));
        return BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);
    }

    /**
     * used to transform a bitmap to a string containing an array of bytes representing the png image
     * @param bitmap the bitmap to convert
     * @return string representation as array of byte in png format
     */
    public static String bitmap2String(Bitmap bitmap)
    {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, blob);
        return new String(blob.toByteArray(), Charset.forName("UTF-8"));
    }

    /**
     * A Builder for {@link ImageItem}, currently only used to parse JSON (little overkill..but ..)
     *
     * @see Item.Builder
     */
    public static class Builder extends Item.Builder {

        private Bitmap bitmap;

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if (!type.equals(ImageItem.ITEM_TYPE.name())) {
                throw new IllegalArgumentException("expected " + ImageItem.ITEM_TYPE.name() + " was : " + type);
            }
            bitmap = string2Bitmap(json.getString("image"));
            return this;
        }

        public ImageItem build() {
            return new ImageItem(super.ID, super.from, super.to, new Date(super.date), super.condition, bitmap);
        }
    }
}
