package ch.epfl.sweng.calamar.item;

import android.content.Context;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.Compresser;

public class FileItem extends Item {

    private final String name;
    private final static Type ITEM_TYPE = Type.FILEITEM;
    private final byte[] data;
    private final int hash;

    public FileItem(int ID, User from, Recipient to, Date date, Condition condition, byte[] data, String name) {
        super(ID, from, to, date, condition);
        this.data = Compresser.compress(data.clone());
        this.name = name;
        hash = computeHash();
    }

    public FileItem(int ID, User from, Recipient to, Date date, byte[] data, String name) {
        super(ID, from, to, date);
        this.name = name;
        this.data = Compresser.compress(data.clone());
        hash = computeHash();
    }

    /**
     * Returns the copy of the data of the file.
     *
     * @return a byte array
     */
    public byte[] getData() {
        return data.clone();
    }

    /**
     * Returns the name of the file
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return ITEM_TYPE;
    }

    @Override
    protected View getItemView(Context context) {
        TextView view = new TextView(context);
        view.setText(name);
        return view;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject ret = new JSONObject();
        this.compose(ret);
        return ret;
    }

    public static FileItem fromJSON(JSONObject json) throws JSONException {
        return new FileItem.Builder().parse(json).build();
    }

    @Override
    public void compose(JSONObject object) throws JSONException {
        super.compose(object);
        object.accumulate("data", byteArrayToString(data));
        object.accumulate("name", name);
        object.accumulate("type", ITEM_TYPE.name());
    }

    public static String byteArrayToString(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static byte[] stringToByteArray(String str) {
        return Base64.decode(str, Base64.DEFAULT);
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
        if (!(o instanceof FileItem)) return false;
        FileItem that = (FileItem) o;
        return super.equals(that) && Arrays.equals(data, that.data) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private int computeHash() {
        if (data == null) {
            return 0;
        } else {
            return super.hashCode() * 73 + Arrays.hashCode(data);
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", filename : " + name;
    }

    protected static class Builder extends Item.Builder {

        protected byte[] data;
        protected String name;

        @Override
        protected FileItem build() {
            return new FileItem(super.ID, super.from, super.to, super.date, super.condition, data, name);
        }

        @Override
        protected FileItem.Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if (!(type.equals(FileItem.ITEM_TYPE.name()) || type.equals(ImageItem.ITEM_TYPE.name()))) {
                throw new IllegalArgumentException("expected " + FileItem.ITEM_TYPE.name() + " was : " + type);
            }
            data = Compresser.decompress(stringToByteArray(json.getString("data")));
            name = json.getString("name");
            return this;
        }

        protected FileItem.Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        protected FileItem.Builder setData(String str) {
            this.data = stringToByteArray(str);
            return this;
        }

        protected FileItem.Builder setName(String name) {
            this.name = name;
            return this;
        }
    }
}
