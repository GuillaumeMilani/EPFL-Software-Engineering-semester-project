package ch.epfl.sweng.calamar;

import android.content.ContentValues;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.Pair;
import ch.epfl.sweng.calamar.utils.Sorter;


public final class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static CalamarApplication app;

    private static SQLiteDatabaseHandler instance;
    private SQLiteDatabase db;

    private enum Operation {ADD, UPDATE, DELETE}

    private final Map<Integer, Pair<Operation, Item>> pendingItems;
    private final Map<Integer, Pair<Operation, Recipient>> pendingRecipients;

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "CalamarDB";

    private static final int MAX_PLACEHOLDERS_COUNT = 99;
    private final String FULL_PLACEHOLDERS;

    private static final String ITEMS_TABLE = "tb_Items";
    private static final String ITEMS_KEY_TYPE = "type";
    private static final String ITEMS_KEY_ID = "id";
    private static final String ITEMS_KEY_TEXT = "text";
    private static final String ITEMS_KEY_FROM = "from_id";
    private static final String ITEMS_KEY_TO = "to_id";
    private static final String ITEMS_KEY_TIME = "time";
    private static final String ITEMS_KEY_CONDITION = "condition";
    private static final String[] ITEMS_COLUMNS = {ITEMS_KEY_TYPE, ITEMS_KEY_ID, ITEMS_KEY_FROM, ITEMS_KEY_TO, ITEMS_KEY_TIME, ITEMS_KEY_CONDITION, ITEMS_KEY_TEXT};

    private static final String RECIPIENTS_TABLE = "tb_Recipients";
    private static final String RECIPIENTS_KEY_ID = "id";
    private static final String RECIPIENTS_KEY_NAME = "name";
    private static final String[] RECIPIENTS_COLUMN = {RECIPIENTS_KEY_ID, RECIPIENTS_KEY_NAME};

    /**
     * Returns the current and only instance of SQLiteDatabaseHandler
     *
     * @return the DatabaseHandler
     */
    public static SQLiteDatabaseHandler getInstance() {
        if (instance == null) {
            app = CalamarApplication.getInstance();
            SQLiteDatabase.loadLibs(app);
            instance = new SQLiteDatabaseHandler();
        }
        return instance;
    }

    /**
     * Creates a databasehandler for managing stored informations on the user phone.
     */
    private SQLiteDatabaseHandler() {
        super(app, DATABASE_NAME, null, DATABASE_VERSION);
        db = getReadableDatabase(app.getCurrentUser().getPassword());
        this.pendingRecipients = new HashMap<>();
        this.pendingItems = new HashMap<>();
        this.FULL_PLACEHOLDERS = createPlaceholders(MAX_PLACEHOLDERS_COUNT);

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
        //TODO only recreates db at the moment
        db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + RECIPIENTS_TABLE);
        CalamarApplication.getInstance().setLastItemsRefresh(new Date(0));
        CalamarApplication.getInstance().setLastUsersRefresh(new Date(0));
        onCreate(db);
    }

    /**
     * Deletes all items in the database
     */
    public synchronized void deleteAllItems() {
        pendingItems.clear();
        db = getWritableIfNotOpen();
        db.delete(ITEMS_TABLE, null, null);
    }

    /**
     * Deletes an item given itself
     *
     * @param message the item to delete
     */
    public synchronized void deleteItem(Item message) {
        deleteItem(message.getID());
    }

    /**
     * Deletes an item given an id
     *
     * @param id the id of the item
     */
    public synchronized void deleteItem(int id) {
        pendingItems.put(id, new Pair<Operation, Item>(Operation.DELETE, null));
    }

    /**
     * Deletes several items
     *
     * @param ids the ids of the items to delete
     */
    public synchronized void deleteItems(List<Integer> ids) {
        for (Integer i : ids) {
            pendingItems.put(i, new Pair<Operation, Item>(Operation.DELETE, null));
        }
    }

    /**
     * Deletes all items for the given recipient
     *
     * @param recipient the contact to delete the conversation with
     */
    public synchronized void deleteItemsForContact(Recipient recipient) {
        deleteItemsForContact(recipient.getID());
    }

    /**
     * Deletes all items for the given recipient
     *
     * @param id the id of the contact to delete the conversation with
     */
    public synchronized void deleteItemsForContact(int id) {
        List<Item> items = getItemsForContact(id);
        for (Item i : items) {
            pendingItems.put(i.getID(), new Pair<Operation, Item>(Operation.DELETE, null));
        }
    }

    /**
     * Adds an item and updates recipients informations related to it
     *
     * @param item the item to add
     */
    public synchronized void addItem(Item item) {
        pendingItems.put(item.getID(), new Pair<>(Operation.ADD, item));
        addOrUpdateRecipientWithItem(item);
    }

    /**
     * Adds all items given a list, and updates recipients informations related to them
     *
     * @param items the list of items to add
     */
    public synchronized void addItems(List<Item> items) {
        for (Item item : items) {
            pendingItems.put(item.getID(), new Pair<>(Operation.ADD, item));
            addOrUpdateRecipientWithItem(item);
        }
    }


    /**
     * Updates an item and the recipients related to it (Does not add the item if it doesn't already exist)
     *
     * @param item the item to update
     */
    public synchronized void updateItem(Item item) {
        manageItemUpdate(item);
    }

    /**
     * Updates several items given a list, as well as the recipients related to them (Does not add the items if they don't already exist)
     *
     * @param items the list of items to update
     */

    public synchronized void updateItems(List<Item> items) {
        for (Item item : items) {
            manageItemUpdate(item);
        }
    }


    /**
     * Returns an item given an id
     *
     * @param id the id of the item to retrieve
     * @return the item, or null
     */
    public synchronized Item getItem(int id) {
        Pair<Operation, Item> fromPending = pendingItems.get(id);
        if (fromPending != null) {
            if (fromPending.getLeft() == Operation.DELETE) {
                return null;
            } else {
                return fromPending.getRight();
            }
        }
        Item toReturn = null;
        db = getReadableIfNotOpen();
        String[] args = {Integer.toString(id)};
        Cursor cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, ITEMS_KEY_ID + " = ?", args, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                toReturn = createItem(cursor);
            }
            cursor.close();
        }
        return toReturn;
    }

    /**
     * Returns a list of items given a list of ids
     *
     * @param ids the ids corresponding to the items
     * @return the items
     */
    public synchronized List<Item> getItems(List<Integer> ids) {
        List<Integer> databaseIds = new ArrayList<>(ids);
        Set<Item> items = new HashSet<>();
        for (Integer i : ids) {
            Pair<Operation, Item> fromPending = pendingItems.get(i);
            if (fromPending != null) {
                databaseIds.remove(i);
                if (fromPending.getLeft() != Operation.DELETE) {
                    items.add(fromPending.getRight());
                }
            }
        }
        if (items.size() == ids.size()) {
            return Sorter.sortItemList(new ArrayList<>(items));
        }
        db = getReadableIfNotOpen();
        if (databaseIds.size() > MAX_PLACEHOLDERS_COUNT) {
            int count = databaseIds.size();
            while (count > 0) {
                int num = count - MAX_PLACEHOLDERS_COUNT > 0 ? MAX_PLACEHOLDERS_COUNT : count;
                String[] args = new String[num];
                for (int i = 0; i < num; ++i) {
                    args[i] = Integer.toString(databaseIds.get(i + (databaseIds.size() - count)));
                }
                Cursor cursor = null;
                if (num == MAX_PLACEHOLDERS_COUNT) {
                    cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, ITEMS_KEY_ID + " IN (" + FULL_PLACEHOLDERS + ")", args, null, null, ITEMS_KEY_ID);
                } else {
                    cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, ITEMS_KEY_ID + " IN (" + createPlaceholders(num) + ")", args, null, null, ITEMS_KEY_ID);
                }
                if (cursor != null) {
                    boolean hasNext = cursor.moveToFirst();
                    while (hasNext) {
                        items.add(createItem(cursor));
                        hasNext = cursor.moveToNext();
                    }
                    cursor.close();
                }
                count -= num;
            }
        } else {
            String[] args = new String[databaseIds.size()];
            for (int i = 0; i < databaseIds.size(); ++i) {
                args[i] = Integer.toString(databaseIds.get(i));
            }
            Cursor cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, ITEMS_KEY_ID + " IN (" + createPlaceholders(databaseIds.size()) + ")", args, null, null, ITEMS_KEY_ID);
            if (cursor != null) {
                boolean hasNext = cursor.moveToFirst();
                while (hasNext) {
                    items.add(createItem(cursor));
                    hasNext = cursor.moveToNext();
                }
                cursor.close();
            }
        }
        return Sorter.sortItemList(new ArrayList<>(items));
    }

    /**
     * Returns the list of messages (items) exchanged between the current user and a recipient.
     *
     * @param recipient The contact
     * @return a list of items
     */
    public synchronized List<Item> getItemsForContact(Recipient recipient) {
        return getItemsForContact(recipient.getID());
    }

    /**
     * Returns the list of messages (items) exchanged between the current user and a recipient.
     *
     * @param contactID The contact
     * @return a list of items
     */
    public synchronized List<Item> getItemsForContact(int contactID) {
        Set<Integer> mapIds = new HashSet<>();
        Set<Item> items = new HashSet<>();
        int userID = app.getCurrentUserID();
        for (Map.Entry<Integer, Pair<Operation, Item>> e : pendingItems.entrySet()) {
            Pair<Operation, Item> p = e.getValue();
            mapIds.add(e.getKey());
            if (p.getLeft() != Operation.DELETE) {
                Item i = p.getRight();
                int itemFromID = i.getFrom().getID();
                int itemToID = i.getTo().getID();
                if ((itemFromID == userID && itemToID == contactID) || (itemFromID == contactID && itemToID == userID)) {
                    items.add(i);
                }
            }
        }
        db = getReadableIfNotOpen();
        int currentUserID = app.getCurrentUserID();
        String[] args = {Integer.toString(currentUserID), Integer.toString(contactID), Integer.toString(currentUserID), Integer.toString(contactID)};
        Cursor cursor = db.query(ITEMS_TABLE, ITEMS_COLUMNS, "( " + ITEMS_KEY_FROM + " = ? AND " + ITEMS_KEY_TO + " = ? ) OR ( " + ITEMS_KEY_TO + " = ? AND " + ITEMS_KEY_FROM + " = ? ) ", args, null, null, ITEMS_KEY_ID + " ASC");
        boolean hasNext;
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                if (!mapIds.contains(cursor.getInt(1))) {
                    SimpleTextItem item = (SimpleTextItem) createItem(cursor);
                    items.add(item);
                }
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        return Sorter.sortItemList(new ArrayList<>(items));
    }

    /**
     * Returns all items
     *
     * @return the list of Item
     */
    public synchronized List<Item> getAllItems() {
        Set<Integer> mapIds = new HashSet<>();
        Set<Item> items = new HashSet<>();
        for (Map.Entry<Integer, Pair<Operation, Item>> e : pendingItems.entrySet()) {
            Pair<Operation, Item> fromPending = e.getValue();
            mapIds.add(e.getKey());
            if (fromPending.getLeft() != Operation.DELETE) {
                items.add(fromPending.getRight());
            }
        }
        db = getReadableIfNotOpen();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ITEMS_TABLE + " ORDER BY " + ITEMS_KEY_ID, null);
        boolean hasNext;
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                if (!mapIds.contains(cursor.getInt(1))) {
                    SimpleTextItem item = (SimpleTextItem) createItem(cursor);
                    items.add(item);
                }
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        return Sorter.sortItemList(new ArrayList<>(items));
    }

    /**
     * Adds a Recipient to the database
     *
     * @param recipient the recipient to add
     */
    public synchronized void addRecipient(Recipient recipient) {
        pendingRecipients.put(recipient.getID(), new Pair<>(Operation.ADD, recipient));
    }

    /**
     * Adds several recipients
     *
     * @param recipients the list of recipients to add.
     */
    public synchronized void addRecipients(List<Recipient> recipients) {
        for (Recipient r : recipients) {
            pendingRecipients.put(r.getID(), new Pair<>(Operation.ADD, r));
        }
    }

    /**
     * Updates a recipient (Does not add the recipient if it doesn't already exist)
     *
     * @param recipient the recipient to update
     */
    public synchronized void updateRecipient(Recipient recipient) {
        manageRecipientUpdate(recipient);
    }

    /**
     * Updates all recipients given in the list (Does not add the recipients if they don't already exist)
     *
     * @param recipients the list of recipients to update
     */
    public synchronized void updateRecipients(List<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            manageRecipientUpdate(recipient);
        }
    }

    /**
     * Deletes the recipient given in argument
     *
     * @param recipient the recipient to delete
     */
    public synchronized void deleteRecipient(Recipient recipient) {
        deleteRecipient(recipient.getID());
    }

    /**
     * Deletes the recipient corresponding to an id
     *
     * @param id the id
     */
    public synchronized void deleteRecipient(int id) {
        deleteItemsForContact(id);
        pendingRecipients.put(id, new Pair<Operation, Recipient>(Operation.DELETE, null));
    }

    /**
     * Deletes all recipients given as argument.
     *
     * @param ids a list of ids of recipients to delete.
     */
    public synchronized void deleteRecipients(List<Integer> ids) {
        for (Integer i : ids) {
            pendingRecipients.put(i, new Pair<Operation, Recipient>(Operation.DELETE, null));
        }
    }

    /**
     * Deletes all recipients
     */
    public synchronized void deleteAllRecipients() {
        pendingRecipients.clear();
        db = getWritableIfNotOpen();
        db.delete(RECIPIENTS_TABLE, null, null);
    }

    /**
     * Returns a recipient corresponding to an id.
     *
     * @param id the id of the recipient
     * @return the recipient
     */
    public synchronized Recipient getRecipient(int id) {
        Pair<Operation, Recipient> fromPending = pendingRecipients.get(id);
        if (fromPending != null) {
            if (fromPending.getLeft() == Operation.DELETE) {
                return null;
            } else {
                return fromPending.getRight();
            }
        }
        db = getReadableIfNotOpen();
        User toReturn = null;
        String[] args = {Integer.toString(id)};
        Cursor cursor = db.query(RECIPIENTS_TABLE, RECIPIENTS_COLUMN, RECIPIENTS_KEY_ID + " = ?", args, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(1);
                //TODO returns only user now
                toReturn = new User(id, name);
            }
            cursor.close();
        }
        return toReturn;
    }

    /**
     * Returns the recipients corresponding to the ids
     *
     * @param ids a list of Integers
     * @return a list of Recipients
     */
    public synchronized List<Recipient> getRecipients(List<Integer> ids) {
        List<Integer> databaseIds = new ArrayList<>(ids);
        Set<Recipient> recipients = new HashSet<>();
        for (Integer i : ids) {
            Pair<Operation, Recipient> fromPending = pendingRecipients.get(i);
            if (fromPending != null) {
                databaseIds.remove(i);
                if (fromPending.getLeft() != Operation.DELETE) {
                    recipients.add(fromPending.getRight());
                }
            }
        }
        if (recipients.size() == ids.size()) {
            return Sorter.sortRecipientList(new ArrayList<>(recipients));
        }
        db = getReadableIfNotOpen();
        if (databaseIds.size() > MAX_PLACEHOLDERS_COUNT) {
            int count = databaseIds.size();
            while (count > 0) {
                int num = count - MAX_PLACEHOLDERS_COUNT > 0 ? MAX_PLACEHOLDERS_COUNT : count;
                String[] args = new String[num];
                for (int i = 0; i < num; ++i) {
                    args[i] = Integer.toString(databaseIds.get(i+(databaseIds.size()-count)));
                }
                Cursor cursor = null;
                if (num == MAX_PLACEHOLDERS_COUNT) {
                    cursor = db.query(RECIPIENTS_TABLE, RECIPIENTS_COLUMN, RECIPIENTS_KEY_ID + " IN (" + FULL_PLACEHOLDERS + ")", args, null, null, RECIPIENTS_KEY_ID);
                } else {
                    cursor = db.query(RECIPIENTS_TABLE, RECIPIENTS_COLUMN, RECIPIENTS_KEY_ID + " IN (" + createPlaceholders(num) + ")", args, null, null, RECIPIENTS_KEY_ID);
                }
                if (cursor != null) {
                    boolean hasNext = cursor.moveToFirst();
                    while (hasNext) {
                        recipients.add(createUser(cursor));
                        hasNext = cursor.moveToNext();
                    }
                    cursor.close();
                }
                count -= num;
            }
        } else {
            String[] args = new String[databaseIds.size()];
            for (int i = 0; i < databaseIds.size(); ++i) {
                args[i] = Integer.toString(databaseIds.get(i));
            }
            Cursor cursor = db.query(RECIPIENTS_TABLE, RECIPIENTS_COLUMN, RECIPIENTS_KEY_ID + " IN (" + createPlaceholders(databaseIds.size()) + ")", args, null, null, RECIPIENTS_KEY_ID);
            if (cursor != null) {
                boolean hasNext = cursor.moveToFirst();
                while (hasNext) {
                    recipients.add(createUser(cursor));
                    hasNext = cursor.moveToNext();
                }
                cursor.close();
            }
        }
        return Sorter.sortRecipientList(new ArrayList<>(recipients));
    }

    /**
     * Returns all recipients.
     *
     * @return all recipients as a List
     */
    public synchronized List<Recipient> getAllRecipients() {
        Set<Integer> mapIds = new HashSet<>();
        Set<Recipient> recipients = new HashSet<>();
        for (Map.Entry<Integer, Pair<Operation, Recipient>> e : pendingRecipients.entrySet()) {
            Pair<Operation, Recipient> fromPending = e.getValue();
            mapIds.add(e.getKey());
            if (fromPending.getLeft() != Operation.DELETE) {
                recipients.add(fromPending.getRight());
            }
        }
        db = getReadableIfNotOpen();
        Cursor cursor = db.rawQuery("SELECT * FROM " + RECIPIENTS_TABLE + " ORDER BY " + RECIPIENTS_KEY_ID, null);
        boolean hasNext;
        if (cursor != null) {
            hasNext = cursor.moveToFirst();
            while (hasNext) {
                if (!mapIds.contains(cursor.getInt(1))) {
                    Recipient recipient = createUser(cursor);
                    recipients.add(recipient);
                }
                hasNext = cursor.moveToNext();
            }
            cursor.close();
        }
        return Sorter.sortRecipientList(new ArrayList<>(recipients));
    }

    /**
     * Write all pending operations in the database
     */
    public synchronized void applyPendingOperations() {
        if (!(pendingItems.isEmpty() && pendingRecipients.isEmpty())) {
            db = getWritableIfNotOpen();
            db.beginTransaction();
            try {
                if (!pendingItems.isEmpty()) {
                    List<Item> itemsToAdd = new ArrayList<>();
                    List<Item> itemsToUpdate = new ArrayList<>();
                    List<Integer> itemsToDelete = new ArrayList<>();
                    for (Map.Entry<Integer, Pair<Operation, Item>> e : pendingItems.entrySet()) {
                        Pair<Operation, Item> pair = e.getValue();
                        if (pair.getLeft() == Operation.ADD) {
                            itemsToAdd.add(pair.getRight());
                        } else if (pair.getLeft() == Operation.UPDATE) {
                            itemsToUpdate.add(pair.getRight());
                        } else {
                            itemsToDelete.add(e.getKey());
                        }
                    }

                    pendingAddItems(itemsToAdd);
                    pendingUpdateItems(itemsToUpdate);
                    if (!itemsToDelete.isEmpty()) {
                        pendingDeleteItems(itemsToDelete);
                    }
                }
                if (!pendingRecipients.isEmpty()) {
                    List<Recipient> recipientsToAdd = new ArrayList<>();
                    List<Recipient> recipientsToUpdate = new ArrayList<>();
                    List<Integer> recipientsToDelete = new ArrayList<>();
                    for (Map.Entry<Integer, Pair<Operation, Recipient>> e : pendingRecipients.entrySet()) {
                        Pair<Operation, Recipient> pair = e.getValue();
                        if (pair.getLeft() == Operation.ADD) {
                            recipientsToAdd.add(pair.getRight());
                        } else if (pair.getLeft() == Operation.UPDATE) {
                            recipientsToUpdate.add(pair.getRight());
                        } else {
                            recipientsToDelete.add(e.getKey());
                        }
                    }
                    pendingAddRecipients(recipientsToAdd);
                    pendingUpdateRecipients(recipientsToUpdate);
                    if (!recipientsToDelete.isEmpty()) {
                        pendingDeleteRecipients(recipientsToDelete);
                    }
                }
                db.setTransactionSuccessful();
                pendingItems.clear();
                pendingRecipients.clear();
            } finally {
                db.endTransaction();
            }

        }
    }

    /**
     * Closes the database.
     */
    public synchronized void closeDatabase() {
        this.db.close();
    }

    //Helper methods for applyPendingOperations

    private void pendingDeleteItems(List<Integer> ids) {
        if (ids.size() > MAX_PLACEHOLDERS_COUNT) {
            int counter = ids.size();
            while (counter > 0) {
                int num = counter - MAX_PLACEHOLDERS_COUNT > 0 ? MAX_PLACEHOLDERS_COUNT : counter;
                String[] args = new String[num];
                for (int i = 0; i < num; ++i) {
                    args[i] = Integer.toString(ids.get(i + (ids.size() - counter)));
                }
                if (num == MAX_PLACEHOLDERS_COUNT) {
                    db.delete(ITEMS_TABLE, ITEMS_KEY_ID + " IN (" + FULL_PLACEHOLDERS + ")", args);
                } else {
                    db.delete(ITEMS_TABLE, ITEMS_KEY_ID + " IN (" + createPlaceholders(num) + ")", args);
                }
                counter -= num;
            }
        } else {
            String[] args = new String[ids.size()];
            for (int i = 0; i < ids.size(); ++i) {
                args[i] = Integer.toString(ids.get(i));
            }
            db.delete(ITEMS_TABLE, ITEMS_KEY_ID + " IN (" + createPlaceholders(ids.size()) + ")", args);
        }
    }

    private void pendingDeleteRecipients(List<Integer> ids) {
        if (ids.size() > MAX_PLACEHOLDERS_COUNT) {
            int counter = ids.size();
            while (counter > 0) {
                int num = counter - MAX_PLACEHOLDERS_COUNT > 0 ? MAX_PLACEHOLDERS_COUNT : counter;
                String[] args = new String[num];
                for (int i = 0; i < num; ++i) {
                    args[i] = Integer.toString(ids.get(i + (ids.size() - counter)));
                }
                if (num == MAX_PLACEHOLDERS_COUNT) {
                    db.delete(RECIPIENTS_TABLE, RECIPIENTS_KEY_ID + " IN (" + FULL_PLACEHOLDERS + ")", args);
                } else {
                    db.delete(RECIPIENTS_TABLE, RECIPIENTS_KEY_ID + " IN (" + createPlaceholders(num) + ")", args);
                }
                counter -= num;
            }
        } else {
            String[] args = new String[ids.size()];
            for (int i = 0; i < ids.size(); ++i) {
                args[i] = Integer.toString(ids.get(i));
            }
            db.delete(RECIPIENTS_TABLE, RECIPIENTS_KEY_ID + " IN (" + createPlaceholders(ids.size()) + ")", args);
        }
    }

    private void pendingAddItems(List<Item> items) {
        for (Item item : items) {
            ContentValues values = createItemValues(item);
            db.replace(ITEMS_TABLE, null, values);
        }
    }

    private void pendingAddRecipients(List<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            ContentValues values = createRecipientValues(recipient);
            db.replace(RECIPIENTS_TABLE, null, values);
        }
    }

    private void pendingUpdateItems(List<Item> items) {
        for (Item item : items) {
            ContentValues values = createItemValues(item);
            String[] args = {Integer.toString(item.getID())};
            db.update(ITEMS_TABLE, values, ITEMS_KEY_ID + " = ?", args);
        }
    }

    private void pendingUpdateRecipients(List<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            ContentValues values = createRecipientValues(recipient);
            String[] args = {Integer.toString(recipient.getID())};
            db.update(RECIPIENTS_TABLE, values, RECIPIENTS_KEY_ID + " = ?", args);
        }
    }


    private SQLiteDatabase getWritableIfNotOpen() {
        if (!db.isOpen() || db.isReadOnly()) {
            return getWritableDatabase(app.getCurrentUser().getPassword());
        }
        return db;
    }

    private SQLiteDatabase getReadableIfNotOpen() {
        if (!(db.isOpen())) {
            return getReadableDatabase(app.getCurrentUser().getPassword());
        }
        return db;
    }

    private ContentValues createItemValues(Item item) {
        ContentValues values = new ContentValues();
        values.put(ITEMS_KEY_TYPE, item.getType().name());
        values.put(ITEMS_KEY_ID, item.getID());
        values.put(ITEMS_KEY_FROM, item.getFrom().getID());
        values.put(ITEMS_KEY_TO, item.getTo().getID());
        values.put(ITEMS_KEY_TIME, item.getDate().getTime());
        try {
            values.put(ITEMS_KEY_CONDITION, item.getCondition().toJSON().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        values.put(ITEMS_KEY_TEXT, ((SimpleTextItem) item).getMessage());
        return values;
    }

    private Item createItem(Cursor cursor) {
        String type = cursor.getString(0);
        int id = cursor.getInt(1);
        User from = (User) getRecipient(cursor.getInt(2));
        Recipient to = getRecipient(cursor.getInt(3));
        Date time = new Date(cursor.getInt(4));
        Condition condition = null;
        try {
            condition = Condition.fromJSON(new JSONObject(cursor.getString(5)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String text = cursor.getString(6);
        if (type.equals(Item.Type.SIMPLETEXTITEM.name())) {
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

    private void addOrUpdateRecipientWithItem(Item item) {
        pendingRecipients.put(item.getFrom().getID(), new Pair<>(Operation.ADD, (Recipient) item.getFrom()));
        pendingRecipients.put(item.getTo().getID(), new Pair<>(Operation.ADD, item.getTo()));
    }

    private void manageItemUpdate(Item item) {
        Pair<Operation, Item> fromPending = pendingItems.get(item.getID());
        if (fromPending != null) {
            if (fromPending.getLeft() == Operation.UPDATE) {
                pendingItems.put(item.getID(), new Pair<>(Operation.UPDATE, item));
                addOrUpdateRecipientWithItem(item);
            } else if (fromPending.getLeft() == Operation.ADD) {
                pendingItems.put(item.getID(), new Pair<>(Operation.ADD, item));
                addOrUpdateRecipientWithItem(item);
            }
        } else {
            pendingItems.put(item.getID(), new Pair<>(Operation.UPDATE, item));
            addOrUpdateRecipientWithItem(item);
        }
    }

    private void manageRecipientUpdate(Recipient recipient) {
        Pair<Operation, Recipient> fromPending = pendingRecipients.get(recipient.getID());
        if (fromPending != null) {
            if (fromPending.getLeft() == Operation.UPDATE) {
                pendingRecipients.put(recipient.getID(), new Pair<>(Operation.UPDATE, recipient));
            } else if (fromPending.getLeft() == Operation.ADD) {
                pendingRecipients.put(recipient.getID(), new Pair<>(Operation.ADD, recipient));
            }
        } else {
            pendingRecipients.put(recipient.getID(), new Pair<>(Operation.UPDATE, recipient));
        }
    }

    private String createPlaceholders(int length) {
        if (length < 1) {
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder builder = new StringBuilder(length * 2 - 1);
            builder.append('?');
            for (int i = 1; i < length; ++i) {
                builder.append(",?");
            }
            return builder.toString();
        }
    }
}
