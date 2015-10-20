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

    //TODO add support for other items (now assuming only SimpleTextItem)

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CalamarDB";

    private static final String MESSAGES_TABLE = "tb_Items";
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

    /**
     * Create a databasehandler for managing stored informations on the user phone.
     * @param context The context of the application
     */
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

    /**
     * Basically deletes the database
     */
    public void drop(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.rawQuery("DROP TABLE IF EXISTS " + MESSAGES_TABLE, null);
        db.rawQuery("DROP TABLE IF EXISTS " + USERS_TABLE, null);
    }

    /**
     * Deletes all messages in the database
     */
    public void deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGES_TABLE, MESSAGES_KEY_ID + " =  ?", null);
    }

    /**
     *
     * Deletes a message given itself
     * @param message the item to delete
     */
    public void deleteMessage(Item message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGES_TABLE, MESSAGES_KEY_ID + " = " + message.getID(), null);
    }

    /**
     *
     * Deletes a message given an id
     * @param id the id of the message
     */
    public void deleteMessage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGES_TABLE, MESSAGES_KEY_ID + " = " + id, null);
    }

    /**
     *
     * Deletes several messages
     * @param ids the ids of the messages to delete
     */
    public void deleteMessages(List<Integer> ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Integer id : ids) {
            db.delete(MESSAGES_TABLE, MESSAGES_KEY_ID + " = " + id, null);
        }
    }

    /**
     *
     * Adds a message
     * @param message the message to add
     */
    public void addMessage(Item message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", message.getID());
        values.put("text", ((SimpleTextItem) message).getMessage());
        values.put("from", message.getFrom().getID());
        values.put("to", message.getTo().getID());
        db.replace(MESSAGES_TABLE, null, values);
    }

    /**
     *
     * Adds all messages given a list
     * @param messages the list of items to add
     */
    public void addMessages(List<Item> messages) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Item message : messages) {
            ContentValues values = new ContentValues();
            values.put("id", message.getID());
            values.put("text", ((SimpleTextItem) message).getMessage());
            values.put("from", message.getFrom().getID());
            values.put("to", message.getTo().getID());
            db.replace(MESSAGES_TABLE, null, values);
        }
    }


    /**
     * Updates a message
     * @param message the message to update
     */
    public void updateMessage(Item message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", message.getID());
        values.put("text", ((SimpleTextItem) message).getMessage());
        values.put("from", message.getFrom().getID());
        values.put("to", message.getTo().getID());
        db.update(MESSAGES_TABLE, values, MESSAGES_KEY_ID + " = " + message.getID(), null);
    }

    /**
     *
     * Updates several messages given a list
     * @param messages the list of items to update
     */
    public void updateMessages(List<Item> messages) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Item message : messages) {
            ContentValues values = new ContentValues();
            values.put("id", message.getID());
            values.put("text", ((SimpleTextItem) message).getMessage());
            values.put("from", message.getFrom().getID());
            values.put("to", message.getTo().getID());
            db.update(MESSAGES_TABLE, values, MESSAGES_KEY_ID + " = " + message.getID(), null);
        }
    }

    /**
     *
     * Returns a message given an id
     * @param id the id of the message to retrieve
     * @return the message
     */
    public Item getMessage(int id) {
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

    /**
     *
     * Returns a list of messages given a list of ids
     * @param ids the ids corresponding to the messages
     * @return the messages
     */
    public List<Item> getMessages(List<Integer> ids){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> messages = new ArrayList<>();
        for (Integer id : ids){
        Cursor cursor = db.query(MESSAGES_TABLE, MESSAGES_COLUMNS, MESSAGES_KEY_ID + " = " + id, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {

            String text = cursor.getString(1);
            User from = getUser(cursor.getInt(2));
            Recipient to = getUser(cursor.getInt(3));
            Date time = new Date(cursor.getInt(4));
            cursor.close();
            messages.add(new SimpleTextItem(id, from, to, time, text));
        }
        }
        return messages;
    }

    /**
     *
     * Returns all messages
     * @return the list of Item
     */
    public List<Item> getAllMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MESSAGES_TABLE, null);
        boolean hasNext = true;
        List<Item> messages = new ArrayList<>();
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                int id = cursor.getInt(0);
                String text = cursor.getString(1);
                User from = getUser(cursor.getInt(2));
                Recipient to = getUser(cursor.getInt(3));
                Date time = new Date(cursor.getInt(4));
                messages.add(new SimpleTextItem(id, from, to, time, text));
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        return messages;
    }

    /**
     *
     * Adds an user to the database
     * @param user the user to add
     */
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERS_KEY_ID, user.getID());
        values.put(USERS_KEY_NAME, user.getName());
        db.replace(USERS_TABLE, null, values);
    }

    /**
     *
     * Adds several users
     * @param users the list of users to add.
     */
    public void addUsers(List<User> users){
        SQLiteDatabase db = this.getWritableDatabase();
        for (User user : users){
            ContentValues values = new ContentValues();
            values.put(USERS_KEY_ID, user.getID());
            values.put(USERS_KEY_NAME, user.getName());
            db.replace(USERS_TABLE, null, values);
        }
    }

    /**
     *
     * Updates an user
     * @param user the user to update
     */
    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERS_KEY_ID, user.getID());
        values.put(USERS_KEY_NAME, user.getName());
        db.update(USERS_TABLE, values, USERS_KEY_ID + " = " + user.getID(), null);
    }

    /**
     *
     * Updates all users given in the list
     * @param users the list of users to update
     */
    public void updateUsers(List<User> users){
        SQLiteDatabase db = this.getWritableDatabase();
        for (User user : users){
            ContentValues values = new ContentValues();
            values.put(USERS_KEY_ID, user.getID());
            values.put(USERS_KEY_NAME, user.getName());
            db.update(USERS_TABLE, values, USERS_KEY_ID + " = " + user.getID(), null);
        }
    }

    /**
     *
     * Deletes the user given in argument
     * @param user the user to delete
     */
    public void deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USERS_TABLE, USERS_KEY_ID + " = " + user.getID(), null);
    }

    /**
     *
     * Deletes the user corresponding to an id
     * @param id the id
     */
    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USERS_TABLE, USERS_KEY_ID + " = " + id, null);
    }

    /**
     *
     * Deletes all users given as argument.
     * @param users a list of users to delete.
     */
    public void deleteUsers(List<User> users){
        SQLiteDatabase db = this.getWritableDatabase();
        for (User user : users){
            db.delete(USERS_TABLE, USERS_KEY_ID + " = " + user.getID(), null);
        }
    }

    /**
     *
     * Returns a user corresponding to an id.
     * @param id the id of the user
     * @return the user
     */
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

    /**
     *
     * Returns the users corresponding to the ids
     * @param ids a list of Integers
     * @return a list of Users
     */
    public List<User> getUsers(List<Integer> ids){
        SQLiteDatabase db = this.getReadableDatabase();
        List<User> users = new ArrayList<>();
        for (Integer id: ids){
            Cursor cursor = db.query(USERS_TABLE, USERS_COLUMNS, USERS_KEY_ID + " = " + id, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {

                String name = cursor.getString(1);
                cursor.close();
                users.add(new User(id, name));
            }
        }
        return users;
    }

    /**
     *
     * Returns all users.
     * @return all users as a List
     */
    public List<User> getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USERS_TABLE, null);
        boolean hasNext = true;
        List<User> users = new ArrayList<>();
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                users.add(new User(id, name));
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        return users;
    }
}
