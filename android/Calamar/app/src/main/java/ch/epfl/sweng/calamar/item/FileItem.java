package ch.epfl.sweng.calamar.item;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.Compresser;

/**
 * Class representing a File Item, without a particular extension. For example, *.png will be ImageItem
 * instead of FileItem.
 */
public class FileItem extends Item {

    private final String path;
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
     * @param path      The path of the file
     * @param message   The message of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, Condition condition, byte[] data, String path, String message) {
        super(ID, from, to, date, condition, message);
        if (data != null) {
            this.data = Compresser.compress(data.clone());
        } else {
            this.data = new byte[0];
        }
        final int idx = path.lastIndexOf('/');
        if (idx == -1) {
            this.name = path;
            this.path = '/' + path;
            Log.d("Path", "Bad path of file : " + path);
        } else {
            this.path = path;
            this.name = path.substring(idx + 1);
        }
        this.hash = computeHash();
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
     * @param path      The path of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, Condition condition, byte[] data, String path) {
        this(ID, from, to, date, condition, data, path, "");
    }

    /**
     * Instantiates a new FileItem with the given parameters
     *
     * @param ID      The ID of the item
     * @param from    The user who sent the item
     * @param to      The recipient of the item
     * @param date    The date of creation of the item
     * @param data    The content of the file
     * @param path    The path of the file
     * @param message The message of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, byte[] data, String path, String message) {
        this(ID, from, to, date, Condition.trueCondition(), data, path, message);
    }

    /**
     * Instantiates a new FileItem with the given parameters
     *
     * @param ID   The ID of the item
     * @param from The user who sent the item
     * @param to   The recipient of the item
     * @param date The date of creation of the item
     * @param data The content of the file
     * @param path The path of the file
     */
    public FileItem(int ID, User from, Recipient to, Date date, byte[] data, String path) {
        this(ID, from, to, date, Condition.trueCondition(), data, path, "");
    }

    /**
     * Returns a copy of the (COMPRESSED!) data of the file.
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

    /**
     * Returns the path of the file
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    @Override
    public Type getType() {
        return ITEM_TYPE;
    }

    @Override
    public View getItemView(Context context) {
        TextView view = new TextView(context);
        view.setText(name);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForFile(FileItem.this);
            }
        });
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
        object.accumulate("path", path);
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
        return super.equals(that) && Arrays.equals(data, that.data) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private int computeHash() {
        if (data == null) {
            return 0;
        } else {
            return super.hashCode() * 73 + Arrays.hashCode(data) * 107 + path.hashCode();
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", filepath : " + path;
    }

    protected static void startActivityForFile(FileItem f) {
        File file = new File(f.getPath());
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = FileUtils.getMimeType(file);
        newIntent.setDataAndType(Uri.fromFile(file), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            CalamarApplication.getInstance().startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(CalamarApplication.getInstance(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    public static class Builder extends Item.Builder {

        protected byte[] data;
        protected String path;

        @Override
        public FileItem build() {
            return new FileItem(super.ID, super.from, super.to, super.date, super.condition, data, path, message);
        }

        @Override
        public FileItem.Builder parse(JSONObject json) throws JSONException {
            super.parse(json);
            String type = json.getString("type");
            if (!(type.equals(FileItem.ITEM_TYPE.name()) || type.equals(ImageItem.ITEM_TYPE.name()))) {
                throw new IllegalArgumentException("expected " + FileItem.ITEM_TYPE.name() + " was : " + type);
            }
            data = Compresser.decompress(base64StringToByteArray(json.getString("data")));
            path = json.getString("path");
            return this;
        }

        /**
         * Sets the data of the FileItem to be created
         *
         * @param data an array of byte
         * @return the builder
         */
        public FileItem.Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the data of the FileItem to be created
         *
         * @param str a string representing bytes
         * @return the builder
         */
        public FileItem.Builder setData(String str) {
            this.data = str.getBytes(Charset.forName("UTF-8"));
            return this;
        }

        /**
         * Sets the path of the FileItem to be created
         *
         * @param path a string
         * @return the builder
         */
        public FileItem.Builder setPath(String path) {
            this.path = path;
            final int idx = path.lastIndexOf('/');
            if (idx == -1) {
                this.path = '/' + path;
                Log.d("PATH", "Bad path for FileItem :" + path);
            } else {
                this.path = path;
            }
            return this;
        }

        /**
         * Sets the name and data of the FileItem to be created by using a File
         *
         * @param f the file
         * @return the builder
         * @throws IOException If there is a problem converting the file to an array of byte
         */
        public FileItem.Builder setFile(File f) throws IOException {
            this.path = f.getAbsolutePath();
            this.data = FileUtils.toByteArray(f);
            return this;
        }
    }
}
