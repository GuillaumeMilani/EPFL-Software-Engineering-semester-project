package ch.epfl.sweng.calamar.item;

import android.content.Context;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.Compresser;

/**
 * Class representing a File Item, without a particular extension. For example, *.png will be ImageItem
 * instead of FileItem.
 */
public class FileItem extends Item {

    private final String name;
    private final static Type ITEM_TYPE = Type.FILEITEM;
    private final byte[] data;
    private final int hash;

    /**
     * Instantiates a new FileItem with the given parameters
     *
     * @param ID        The ID of the item
     * @param from      The user who sent the item
     * @param to        The recipient of the item
     * @param date      The date of creation of the item
     * @param condition The condition for unlocking the item
     * @param data      The content of the file
     * @param name      The name of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, Condition condition, byte[] data, String name, String message) {
        super(ID, from, to, date, condition, message);
        this.data = Compresser.compress(data.clone());
        this.name = name;
        hash = computeHash();
    }

    /**
     * Instantiates a new FileItem with the given parameters
     *
     * @param ID   The ID of the item
     * @param from The user who sent the item
     * @param to   The recipient of the item
     * @param date The date of creation of the item
     * @param data The content of the file
     * @param name The name of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, byte[] data, String name, String message) {
        super(ID, from, to, date, message);
        this.name = name;
        this.data = Compresser.compress(data.clone());
        hash = computeHash();
    }

    /**
     * Instantiates a new FileItem with the given parameters
     *
     * @param ID        The ID of the item
     * @param from      The user who sent the item
     * @param to        The recipient of the item
     * @param date      The date of creation of the item
     * @param condition The condition for unlocking the item
     * @param data      The content of the file
     * @param name      The name of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, Condition condition, byte[] data, String name) {
        super(ID, from, to, date, condition, "");
        this.data = Compresser.compress(data.clone());
        this.name = name;
        hash = computeHash();
    }

    /**
     * Instantiates a new FileItem with the given parameters
     *
     * @param ID   The ID of the item
     * @param from The user who sent the item
     * @param to   The recipient of the item
     * @param date The date of creation of the item
     * @param data The content of the file
     * @param name The name of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, byte[] data, String name) {
        super(ID, from, to, date, "");
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
        object.accumulate("data", byteArrayToBase64String(data));
        object.accumulate("name", name);
        object.accumulate("type", ITEM_TYPE.name());
    }

    /**
     * Encodes data to a Base64 String to allow easier transmission through network.
     *
     * @param data The data to be encoded
     * @return the String created
     */
    public static String byteArrayToBase64String(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    /**
     * Decodes data from a Base64 String
     *
     * @param str The string to be decoded
     * @return The byte array created
     */
    public static byte[] base64StringToByteArray(String str) {
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
            return super.hashCode() * 73 + Arrays.hashCode(data) * 107 + name.hashCode();
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
            return new FileItem(super.ID, super.from, super.to, super.date, super.condition, data, name, message);
        }

        @Override
        protected FileItem.Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if (!(type.equals(FileItem.ITEM_TYPE.name()) || type.equals(ImageItem.ITEM_TYPE.name()))) {
                throw new IllegalArgumentException("expected " + FileItem.ITEM_TYPE.name() + " was : " + type);
            }
            data = Compresser.decompress(base64StringToByteArray(json.getString("data")));
            name = json.getString("name");
            return this;
        }

        /**
         * Sets the data of the FileItem to be created
         *
         * @param data an array of byte
         * @return the builder
         */
        protected FileItem.Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the data of the FileItem to be created
         *
         * @param str a string representing bytes
         * @return the builder
         */
        protected FileItem.Builder setData(String str) {
            this.data = str.getBytes(Charset.forName("UTF-8"));
            return this;
        }

        /**
         * Sets the name of the FileItem to be created
         *
         * @param name a string
         * @return the builder
         */
        protected FileItem.Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the name and data of the FileItem to be created by using a File
         *
         * @param f the file
         * @return the builder
         * @throws IOException If there is a problem converting the file to an array of byte
         */
        protected FileItem.Builder setFile(File f) throws IOException {
            this.name = f.getName();
            this.data = FileUtils.toByteArray(f);
            return this;
        }
    }
}
