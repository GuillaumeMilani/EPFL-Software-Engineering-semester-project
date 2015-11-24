package ch.epfl.sweng.calamar.item;

import android.content.Context;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.Compresser;

public class FileItem extends Item {

    private final String name;
    private final static Type ITEM_TYPE = Type.FILEITEM;
    private final byte[] data;
    private final int hash;

    protected FileItem(int ID, User from, Recipient to, long date, Condition condition, byte[] data, String name) throws IOException {
        super(ID, from, to, date, condition);
        this.name = name;
        this.data = Compresser.compress(data.clone());
        hash = computeHash();
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

    public static FileItem fromJSON(JSONObject json) throws JSONException, IOException, DataFormatException {
        return new FileItem.Builder().parse(json).build();
    }

    @Override
    public void compose(JSONObject object) throws JSONException {
        super.compose(object);
        object.accumulate("data", arrayToString(data));
        object.accumulate("name", name);
    }

    private static String arrayToString(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    private static byte[] stringToArray(String str) {
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

    protected static class Builder extends Item.Builder {

        private byte[] data;
        private String name;

        @Override
        protected FileItem build() throws IOException {
            return new FileItem(super.ID, super.from, super.to, super.date, super.condition, data, name);
        }

        @Override
        protected FileItem.Builder parse(JSONObject json) throws JSONException, IOException, DataFormatException {
            super.parse(json);
            String type = json.getString("type");
            if (!type.equals(FileItem.ITEM_TYPE.name())) {
                throw new IllegalArgumentException("expected " + FileItem.ITEM_TYPE.name() + " was : " + type);
            }
            data = Compresser.decompress(stringToArray(json.getString("data")));
            name = json.getString("name");
            return this;
        }

        protected FileItem.Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        protected FileItem.Builder setData(String str) {
            this.data = stringToArray(str);
            return this;
        }

        protected FileItem.Builder setName(String name) {
            this.name = name;
            return this;
        }
    }
}
