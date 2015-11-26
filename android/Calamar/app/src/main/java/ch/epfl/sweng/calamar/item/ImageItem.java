package ch.epfl.sweng.calamar.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Created by pierre on 11/12/15.
 */
public final class ImageItem extends FileItem {

    protected final static Type ITEM_TYPE = Type.IMAGEITEM;

    private final Bitmap bitmap;

    /**
     * Instantiates a new ImageItem with the following parameters
     *
     * @param ID        the id
     * @param from      the 'from' field of the Item (sender)
     * @param to        the 'to' field of the Item (recipient)
     * @param date      the creation/posting date of the Item
     * @param condition the content (text message)
     * @param data      the image
     * @see Item#Item(int, User, Recipient, Date, Condition)
     */
    public ImageItem(int ID, User from, Recipient to, Date date, Condition condition, byte[] data, String name) {
        super(ID, from, to, date, condition, data, name);
        this.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * Instantiates a new ImageItem with the following parameters (condition will be always true)
     *
     * @param ID     the id
     * @param from   the 'from' field of the Item (sender)
     * @param to     the 'to' field of the Item (recipient)
     * @param date   the creation/posting date of the Item
     * @param bitmap the image
     * @see Item#Item(int, User, Recipient, Date)
     */
    public ImageItem(int ID, User from, Recipient to, Date date, byte[] bitmap, String name) {
        super(ID, from, to, date, bitmap, name);
        this.bitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
    }

    /**
     * gets type of this ImageItem (always returns IMAGEITEM)
     *
     * @return IMAGEITEM
     */
    @Override
    public Type getType() {
        return ITEM_TYPE;
    }

    @Override
    public View getItemView(Context context) {
        ImageView view = new ImageView(context);
        view.setImageBitmap(bitmap);
        return view;
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
        json.put("type", ITEM_TYPE.name());
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
     * Returns the image of the item
     *
     * @return a bitmap
     */
    public Bitmap getBitmap() {
        return Bitmap.createBitmap(bitmap);
    }


    /**
     * used to transform a string (containing an array of bytes representing the png image) to a bitmap
     *
     * @param str png image as byte[]
     * @return bitmap corresponding to given byte[] input
     */
    public static Bitmap string2Bitmap(String str) {
        byte[] bytes2 = Base64.decode(str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);
    }

    /**
     * used to transform a bitmap to a string containing an array of bytes representing the png image
     *
     * @param bitmap the bitmap to convert
     * @return string representation as array of byte in png format
     */
    public static String bitmap2String(Bitmap bitmap) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, blob);
        return Base64.encodeToString(blob.toByteArray(), Base64.DEFAULT);
    }

    /**
     * A Builder for {@link ImageItem}, currently only used to parse JSON (little overkill..but ..)
     *
     * @see Item.Builder
     */
    public static class Builder extends FileItem.Builder {

        public Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if (!type.equals(ImageItem.ITEM_TYPE.name())) {
                throw new IllegalArgumentException("expected " + ImageItem.ITEM_TYPE.name() + " was : " + type);
            }
            return this;
        }

        public ImageItem build() {
            return new ImageItem(super.ID, super.from, super.to, super.date, super.condition, super.data, super.path);
        }
    }
}
