package ch.epfl.sweng.calamar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CalamarDB";

    private static final String MESSAGES_TABLE = "tb_Messages";
    private static final String MESSAGES_KEY_ID = "id";
    private static final String MESSAGES_KEY_TEXT = "text";
    private static final String MESSAGES_KEY_FROM = "from";
    private static final String MESSAGES_KEY_TO = "to";
    private static final String MESSAGES_KEY_TIME = "time";
    private static final String[] MESSAGES_COLUMNS = {MESSAGES_KEY_ID, MESSAGES_KEY_TEXT, MESSAGES_KEY_FROM, MESSAGES_KEY_TO, MESSAGES_KEY_TIME};

    private static final String USERS_TABLE = "tb_Users";
    private static final String USERS_KEY_ID = "id";
    private static final String USERS_KEY_NAME = "name";
    private static final String[] USERS_COLUMNS = {USERS_KEY_ID, USERS_KEY_NAME};

    public SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createMessagesTable = "CREATE TABLE " + MESSAGES_TABLE + " ( "
                + MESSAGES_KEY_ID + " INTEGER PRIMARY, " + MESSAGES_KEY_TEXT + " TEXT, " + MESSAGES_KEY_FROM + " INTEGER, " + MESSAGES_KEY_TO + " INTEGER, " + MESSAGES_KEY_TIME + " INTEGER )";
        db.execSQL(createMessagesTable);
        final String createUsersTable = "CREATE TABLE " + USERS_TABLE + " ( " + USERS_KEY_ID + " INTEGER PRIMARY, " + USERS_KEY_NAME + " TEXT )";
        db.execSQL(createUsersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO does nothing at the moment
    }

    public void deleteMessage(SimpleTextItem message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGES_TABLE, MESSAGES_KEY_ID + " = " + message.getID(), null);
    }

    public void deleteMessage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGES_TABLE, MESSAGES_KEY_ID + " = " + id, null);
    }

    public void addMessage(SimpleTextItem message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", message.getID());
        values.put("text", message.getMessage());
        values.put("from", message.getFrom().getID());
        values.put("to", message.getTo().getID());
        db.insert(MESSAGES_TABLE, null, values);
    }


    public void updateMessage(SimpleTextItem message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", message.getID());
        values.put("text", message.getMessage());
        values.put("from", message.getFrom().getID());
        values.put("to", message.getTo().getID());
        db.update(MESSAGES_TABLE, values, MESSAGES_KEY_ID + " = " + message.getID(), null);
    }

    public SimpleTextItem getMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MESSAGES_TABLE, MESSAGES_COLUMNS, MESSAGES_KEY_ID + " = " + id, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {

            String text = cursor.getString(1);
            User from = getUser(cursor.getInt(2));
            Recipient to = getUser(cursor.getInt(3));
            Date time = new Date(cursor.getInt(4));
            cursor.close();
            return new SimpleTextItem(id, from, to, time, text);
        }
        return null;
    }

    public List<SimpleTextItem> getAllMessages(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MESSAGES_TABLE, null);
        boolean hasNext=true;
        if (cursor!=null){
            hasNext=cursor.moveToFirst();
            List<SimpleTextItem> messages = new ArrayList<>();
            while (hasNext){
                int id = cursor.getInt(0);
                String text = cursor.getString(1);
                User from = getUser(cursor.getInt(2));
                Recipient to = getUser(cursor.getInt(3));
                Date time = new Date(cursor.getInt(4));
                messages.add(new SimpleTextItem(id,from,to,time,text));
                hasNext=cursor.moveToNext();
            }
            cursor.close();
            return messages;
        }
       return null;
    }

    public void addUser(User u) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERS_KEY_ID, u.getID());
        values.put(USERS_KEY_NAME, u.getName());
        db.insert(USERS_TABLE, null, values);
    }

    public void updateUser(User u) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERS_KEY_ID, u.getID());
        values.put(USERS_KEY_NAME, u.getName());
        db.update(USERS_TABLE, values, USERS_KEY_ID + " = " + u.getID(), null);
    }

    public void deleteUser(User u) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USERS_TABLE, USERS_KEY_ID + " = " + u.getID(), null);
    }

    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USERS_TABLE, USERS_KEY_ID + " = " + id, null);
    }

    public User getUser(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USERS_TABLE, USERS_COLUMNS, USERS_KEY_ID + " = " + id, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {

            String name = cursor.getString(1);
            cursor.close();
            return new User(id, name);
        }
        return null;
    }

    public List<User> getAllUsers(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE, null);
        boolean hasNext=true;
        if (cursor!=null) {
            hasNext=cursor.moveToFirst();
            List<User> users=new ArrayList<>();
            while (hasNext){
                int id=cursor.getInt(0);
                String name=cursor.getString(1);
                users.add(new User(id,name));
                hasNext=cursor.moveToNext();
            }
            cursor.close();
            return users;
        }
        return null;
    }
}
