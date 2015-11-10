package ch.epfl.sweng.calamar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    //TODO add support for other items (now assuming only SimpleTextItem)

    private final CalamarApplication app;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CalamarDB";

    private static final String ITEMS_TABLE = "tb_Items";
    private static final String ITEMS_KEY_TYPE = "type";
    private static final String ITEMS_KEY_ID = "id";
    private static final String ITEMS_KEY_TEXT = "text";
    private static final String ITEMS_KEY_FROM = "from_id";
    private static final String ITEMS_KEY_TO = "to_id";
    private static final String ITEMS_KEY_TIME = "time";
    private static final String ITEMS_KEY_CONDITION = "condition";
    private static final String[] ITEMS_COLUMNS = {ITEMS_KEY_TYPE,ITEMS_KEY_ID, ITEMS_KEY_FROM, ITEMS_KEY_TO, ITEMS_KEY_TIME, ITEMS_KEY_CONDITION, ITEMS_KEY_TEXT};

    private static final String RECIPIENTS_TABLE = "tb_Recipients";
    private static final String RECIPIENTS_KEY_ID = "id";
    private static final String RECIPIENTS_KEY_NAME = "name";
    private static final String[] RECIPIENTS_COLUMN = {RECIPIENTS_KEY_ID, RECIPIENTS_KEY_NAME};

    /**
     * Creates a databasehandler for managing stored informations on the user phone.
     *
     * @param app The application
     */
    public SQLiteDatabaseHandler(CalamarApplication app) {
        super(CalamarApplication.getInstance(), DATABASE_NAME, null, DATABASE_VERSION);
        this.app = CalamarApplication.getInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createMessagesTable = "CREATE TABLE " + ITEMS_TABLE + " ("
                + ITEMS_KEY_TYPE + " TEXT NOT NULL,"
                + ITEMS_KEY_ID + " INTEGER PRIMARY KEY NOT NULL,"
                + ITEMS_KEY_FROM + " INTEGER NOT NULL,"
                + ITEMS_KEY_TO + " INTEGER NOT NULL,"
                + ITEMS_KEY_TIME + " INTEGER NOT NULL,"
                + ITEMS_KEY_CONDITION + " TEXT NOT NULL, "
                + ITEMS_KEY_TEXT + " TEXT)";
        db.execSQL(createMessagesTable);
        final String createRecipientsTable = "CREATE TABLE " + RECIPIENTS_TABLE + " ("
                + RECIPIENTS_KEY_ID + " INTEGER PRIMARY KEY NOT NULL,"
                + RECIPIENTS_KEY_NAME + " TEXT NOT NULL)";
        db.execSQL(createRecipientsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO does nothing at the moment
        onCreate(db);
    }

    /**
     * Deletes all items in the database
     */
    public void deleteAllItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ITEMS_TABLE, null, null);
        db.close();
    }

    /**
     * Deletes an item given itself
     *
     * @param message the item to delete
     */
    public void deleteItem(Item message) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {Integer.toString(message.getID())};
        db.delete(ITEMS_TABLE, ITEMS_KEY_ID + " = ?", args);
        db.close();
    }

    /**
     * Deletes an item given an id
     *
     * @param id the id of the item
     */
    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {Integer.toString(id)};
        db.delete(ITEMS_TABLE, ITEMS_KEY_ID + " = ?", args);
        db.close();
    }

    /**
     * Deletes several items
     *
     * @param ids the ids of the items to delete
     */
    public void deleteItems(List<Integer> ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); ++i) {
            args[i] = Integer.toString(ids.get(i));
        }
        db.delete(ITEMS_TABLE, ITEMS_KEY_ID + " IN (" + createPlaceholders(ids.size()) + ")", args);
        db.close();
    }

    /**
     * Adds an item and updates recipients informations related to it
     *
     * @param item the item to add
     */
    public void addItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createItemValues(item);
        db.replace(ITEMS_TABLE, null, values);
        updateRecipientsWithItem(item, db);
        db.close();
    }

    /**
     * Adds all items given a list, and updates recipients informations related to them
     *
     * @param items the list of items to add
     */
    public void addItems(List<Item> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Item item : items) {
            ContentValues values = createItemValues(item);
            db.replace(ITEMS_TABLE, null, values);
            updateRecipientsWithItem(item, db);
        }
        db.close();
    }


    /**
     * Updates an item and the recipients related to it
     *
     * @param item the item to update
     */
    public void updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createItemValues(item);
        String[] args = {Integer.toString(item.getID())};
        db.update(ITEMS_TABLE, values, ITEMS_KEY_ID + " = ?", args);
        updateRecipientsWithItem(item, db);
        db.close();
    }

    /**
     * Updates several items given a list, as well as the recipients related to them
     *
     * @param items the list of items to update
     */
    public void updateItems(List<Item> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Item item : items) {
            ContentValues values = createItemValues(item);
            String[] args = {Integer.toString(item.getID())};
            db.update(ITEMS_TABLE, values, ITEMS_KEY_ID + " = ?", args);
            updateRecipientsWithItem(item, db);
        }
        db.close();
    }

    /**
     * Returns an item given an id
     *
     * @param id the id of the item to retrieve
     * @return the item, or null
     */
    public Item getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {Integer.toString(id)};
        Cursor cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, ITEMS_KEY_ID + " = ?", args, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            SimpleTextItem item = (SimpleTextItem) createItem(cursor);
            cursor.close();
            db.close();
            return item;
        }
        db.close();
        return null;
    }

    /**
     * Returns a list of items given a list of ids
     *
     * @param ids the ids corresponding to the items
     * @return the items
     */
    public List<Item> getItems(List<Integer> ids) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> items = new ArrayList<>();
        String[] args = new String[ids.size()];
        for (int i=0;i<ids.size();++i){
            args[i]=Integer.toString(ids.get(i));
        }
        Cursor cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, ITEMS_KEY_ID + " IN ("+createPlaceholders(ids.size())+")", args, null, null, ITEMS_KEY_ID + " ASC");
        if (cursor != null) {
            boolean hasNext = cursor.moveToFirst();
            while (hasNext) {
                items.add(createItem(cursor));
                hasNext=cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    /**
     * Returns the list of messages (items) exchanged between the current user and a recipient.
     *
     * @param recipient The contact
     * @return a list of items
     */
    public List<Item> getItemsForContact(Recipient recipient) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> items = new ArrayList<>();
        int currentUserID = app.getCurrentUserID();
        String[] args = {Integer.toString(currentUserID), Integer.toString(recipient.getID()), Integer.toString(currentUserID), Integer.toString(recipient.getID())};
        Cursor cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, "( " + ITEMS_KEY_FROM + " = ? AND " + ITEMS_KEY_TO + " = ? ) OR ( " + ITEMS_KEY_TO + " = ? AND " + ITEMS_KEY_FROM + " = ? ) ", args, null, null, ITEMS_KEY_ID + " ASC");
        boolean hasNext;
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                SimpleTextItem item = (SimpleTextItem) createItem(cursor);
                items.add(item);
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    /**
     * Returns the list of messages (items) exchanged between the current user and a recipient.
     *
     * @param contactID The contact
     * @return a list of items
     */
    public List<Item> getItemsForContact(int contactID) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> items = new ArrayList<>();
        int currentUserID = app.getCurrentUserID();
        String[] args = {Integer.toString(currentUserID), Integer.toString(contactID), Integer.toString(currentUserID), Integer.toString(contactID)};
        Cursor cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, "( " + ITEMS_KEY_FROM + " = ? AND " + ITEMS_KEY_TO + " = ? ) OR ( " + ITEMS_KEY_TO + " = ? AND " + ITEMS_KEY_FROM + " = ? ) ", args, null, null, ITEMS_KEY_ID + " ASC");
        boolean hasNext;
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                SimpleTextItem item = (SimpleTextItem) createItem(cursor);
                items.add(item);
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    /**
     * Returns all items
     *
     * @return the list of Item
     */
    public List<Item> getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ITEMS_TABLE + " ORDER BY " + ITEMS_KEY_ID + " ASC", null);
        boolean hasNext;
        List<Item> items = new ArrayList<>();
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                SimpleTextItem item = (SimpleTextItem) createItem(cursor);
                items.add(item);
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    /**
     * Adds a Recipient to the database
     *
     * @param recipient the recipient to add
     */
    public void addRecipient(Recipient recipient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createRecipientValues(recipient);
        db.replace(RECIPIENTS_TABLE, null, values);
        db.close();
    }

    /**
     * Adds several recipients
     *
     * @param recipients the list of recipients to add.
     */
    public void addRecipients(List<Recipient> recipients) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Recipient recipient : recipients) {
            ContentValues values = createRecipientValues(recipient);
            db.replace(RECIPIENTS_TABLE, null, values);
        }
        db.close();
    }

    /**
     * Updates a recipient
     *
     * @param recipient the recipient to update
     */
    public void updateRecipient(Recipient recipient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createRecipientValues(recipient);
        String[] args = {Integer.toString(recipient.getID())};
        db.update(RECIPIENTS_TABLE, values, RECIPIENTS_KEY_ID + " = ?", args);
        db.close();
    }

    /**
     * Updates all recipients given in the list
     *
     * @param recipients the list of recipients to update
     */
    public void updateRecipients(List<Recipient> recipients) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Recipient recipient : recipients) {
            ContentValues values = createRecipientValues(recipient);
            String[] args = {Integer.toString(recipient.getID())};
            db.update(RECIPIENTS_TABLE, values, RECIPIENTS_KEY_ID + " = ?", args);
        }
        db.close();
    }

    /**
     * Deletes the recipient given in argument
     *
     * @param recipient the recipient to delete
     */
    public void deleteRecipient(Recipient recipient) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {Integer.toString(recipient.getID())};
        db.delete(RECIPIENTS_TABLE, RECIPIENTS_KEY_ID + " = ?", args);
        db.close();
    }

    /**
     * Deletes the recipient corresponding to an id
     *
     * @param id the id
     */
    public void deleteRecipient(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {Integer.toString(id)};
        db.delete(RECIPIENTS_TABLE, RECIPIENTS_KEY_ID + " = ?", args);
        db.close();
    }

    /**
     * Deletes all recipients given as argument.
     *
     * @param ids a list of ids of recipients to delete.
     */
    public void deleteRecipients(List<Integer> ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); ++i) {
            args[i] = Integer.toString(ids.get(i));
        }

        db.delete(RECIPIENTS_TABLE, RECIPIENTS_KEY_ID + " IN ("+createPlaceholders(ids.size())+")", args);
        db.close();
    }

    /**
     * Deletes all recipients
     */
    public void deleteAllRecipients() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(RECIPIENTS_TABLE, null, null);
        db.close();
    }

    /**
     * Returns a recipient corresponding to an id.
     *
     * @param id the id of the recipient
     * @return the recipient
     */
    public Recipient getRecipient(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {Integer.toString(id)};
        Cursor cursor = db.query(RECIPIENTS_TABLE, RECIPIENTS_COLUMN, RECIPIENTS_KEY_ID + " = ?", args, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {

            String name = cursor.getString(1);
            cursor.close();
            db.close();
            //TODO returns only user now
            return new User(id, name);
        }
        db.close();
        return null;
    }

    /**
     * Returns the recipients corresponding to the ids
     *
     * @param ids a list of Integers
     * @return a list of Recipients
     */
    public List<Recipient> getRecipients(List<Integer> ids) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Recipient> recipients = new ArrayList<>();
        String[] args = new String[ids.size()];
        for (int i=0;i<ids.size();++i){
            args[i]=Integer.toString(ids.get(i));
        }
        Cursor cursor = db.query(RECIPIENTS_TABLE, RECIPIENTS_COLUMN, RECIPIENTS_KEY_ID + " IN ("+createPlaceholders(ids.size())+")", args, null, null, RECIPIENTS_KEY_ID + " ASC", null);
        boolean hasNext;
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                recipients.add(createUser(cursor));
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return recipients;
    }

    /**
     * Returns all recipients.
     *
     * @return all recipients as a List
     */
    public List<Recipient> getAllRecipients() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + RECIPIENTS_TABLE + " ORDER BY " + RECIPIENTS_KEY_ID + " ASC", null);
        boolean hasNext;
        List<Recipient> recipients = new ArrayList<>();
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                recipients.add(createUser(cursor));
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return recipients;
    }

    private ContentValues createItemValues(Item item) {
        ContentValues values = new ContentValues();
        values.put(ITEMS_KEY_TYPE,item.getType());
        values.put(ITEMS_KEY_ID, item.getID());
        values.put(ITEMS_KEY_FROM, item.getFrom().getID());
        values.put(ITEMS_KEY_TO, item.getTo().getID());
        values.put(ITEMS_KEY_TIME, item.getDate().getTime());
        values.put(ITEMS_KEY_CONDITION, item.getCondition().toString());
        values.put(ITEMS_KEY_TEXT, ((SimpleTextItem) item).getMessage());
        return values;
    }

    private Item createItem(Cursor cursor) {
        String type = cursor.getString(0);
        int id = cursor.getInt(1);
        User from = (User) getRecipient(cursor.getInt(2));
        Recipient to = getRecipient(cursor.getInt(3));
        Date time = new Date(cursor.getInt(4));
        Condition condition = Condition.fromString(cursor.getString(5));
        String text = cursor.getString(6);
        if (type.equals("SimpleTextItem")){
            return new SimpleTextItem(id, from, to, time, condition, text);
        } else {
            throw new UnsupportedOperationException("Only SimpleTextItem for now");
        }
    }

    private ContentValues createRecipientValues(Recipient recipient) {
        ContentValues values = new ContentValues();
        values.put(RECIPIENTS_KEY_ID, recipient.getID());
        values.put(RECIPIENTS_KEY_NAME, recipient.getName());
        return values;
    }

    private User createUser(Cursor cursor) {
        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        return new User(id, name);
    }

    private void updateRecipientsWithItem(Item item, SQLiteDatabase db) {
        ContentValues valuesFrom = createRecipientValues(item.getFrom());
        ContentValues valuesTo = createRecipientValues(item.getTo());
        db.replace(RECIPIENTS_TABLE, null, valuesFrom);
        db.replace(RECIPIENTS_TABLE, null, valuesTo);
    }

    private String createPlaceholders(int length){
        if (length<1){
            throw new RuntimeException("No placeholders");
        }
        else{
            StringBuilder builder = new StringBuilder(length*2-1);
            builder.append('?');
            for (int i =1 ; i<length;++i){
                builder.append(",?");
            }
            return builder.toString();
        }
    }
}
