package ch.epfl.sweng.calamar;

import android.test.ApplicationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

@RunWith(JUnit4.class)
public class SQLiteDatabaseHandlerTest extends ApplicationTestCase<CalamarApplication> {

    private SQLiteDatabaseHandler dbHandler;

    private CalamarApplication app;

    private final User testUser = new User(0, "Me");
    private final User testUser2 = new User(1, "You");
    private final User testUser3 = new User(2, "Him");
    private final Recipient testRecipient = new User(3, "Her");
    private final SimpleTextItem testItem = new SimpleTextItem(0, testUser, testRecipient, new Date(0), "0");
    private final SimpleTextItem testItem2 = new SimpleTextItem(1, testUser2, testUser, new Date(1), "1");
    private final SimpleTextItem testItem3 = new SimpleTextItem(2, testUser, testUser2, new Date(2), "2");
    private final SimpleTextItem testItem4 = new SimpleTextItem(3, testUser, testUser2, new Date(3), "3");

    private final int NUM_ITER = 500;
    private final int MIN_ITER = 100; //For queries reasons => max placeholders=99

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        app = CalamarApplication.getInstance();
        dbHandler = app.getDatabaseHandler();
        getApplication();
        dbHandler.deleteAllItems();
        dbHandler.deleteAllRecipients();
        app.setCurrentUserID(testUser.getID());
        app.setCurrentUserName(testUser.getName());
        app.setLastItemsRefresh(0);
        app.setLastUsersRefresh(0);
    }

    public SQLiteDatabaseHandlerTest() {
        super(CalamarApplication.class);
    }


    @Before
    public void testDatabaseIsEmpty() {
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }

    @Test
    public void testDeleteOnEmptyDatabase() {
        dbHandler.deleteItem(0);
        dbHandler.deleteRecipient(0);
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertTrue(dbHandler.getAllRecipients().isEmpty());
        dbHandler.applyPendingOperations();
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }

    @Test
    public void testAddAndDeleteRecipient() {
        dbHandler.addRecipient(testUser);
        assertEquals(dbHandler.getRecipient(testUser.getID()), testUser);
        dbHandler.deleteRecipient(testUser.getID());
        assertEquals(dbHandler.getRecipient(testUser.getID()), null);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getRecipient(testUser.getID()), null);
    }

    @Test
    public void testUpdateRecipient() {
        dbHandler.addRecipient(testUser);
        User u = new User(testUser.getID(), "NotMe");
        dbHandler.updateRecipient(u);
        assertEquals(dbHandler.getRecipient(testUser.getID()), u);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getRecipient(testUser.getID()), u);
    }

    @Test
    public void testUpdateItem() {
        dbHandler.addItem(testItem);
        SimpleTextItem item = new SimpleTextItem(testItem.getID(), testUser, testUser3, new Date(3), "-1");
        dbHandler.updateItem(item);
        assertEquals(dbHandler.getItem(testItem.getID()), item);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getItem(testItem.getID()), item);
    }

    @Test
    public void testUpdateMultipleRecipients() {
        initDB();
        List<Recipient> toUpdate = new ArrayList<>();
        toUpdate.add(new User(testUser.getID(), "User1"));
        toUpdate.add(new User(testUser2.getID(), "User2"));
        dbHandler.updateRecipients(toUpdate);
        assertEquals(dbHandler.getRecipient(testUser.getID()), toUpdate.get(0));
        assertEquals(dbHandler.getRecipient(testUser2.getID()), toUpdate.get(1));
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getRecipient(testUser.getID()), toUpdate.get(0));
        assertEquals(dbHandler.getRecipient(testUser2.getID()), toUpdate.get(1));
    }

    @Test
    public void testUpdateMultipleItems() {
        initDB();
        List<Item> toUpdate = new ArrayList<>();
        toUpdate.add(new SimpleTextItem(testItem.getID(), testUser3, testUser, new Date(100), "message1"));
        toUpdate.add(new SimpleTextItem(testItem2.getID(), testUser, testUser3, new Date(101), "message2"));
        dbHandler.updateItems(toUpdate);
        assertEquals(dbHandler.getItem(testItem.getID()), toUpdate.get(0));
        assertEquals(dbHandler.getItem(testItem2.getID()), toUpdate.get(1));
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getItem(testItem.getID()), toUpdate.get(0));
        assertEquals(dbHandler.getItem(testItem2.getID()), toUpdate.get(1));
    }

    @Test
    public void testGetMultipleItems() {
        initDB();
        List<Integer> toGet = new ArrayList<>();
        toGet.add(testItem.getID());
        toGet.add(testItem2.getID());
        List<Item> itemsGot = dbHandler.getItems(toGet);
        assertEquals(itemsGot.get(0), testItem);
        assertEquals(itemsGot.get(1), testItem2);
        dbHandler.applyPendingOperations();
        itemsGot = dbHandler.getItems(toGet);
        assertEquals(itemsGot.get(0), testItem);
        assertEquals(itemsGot.get(1), testItem2);
    }

    @Test
    public void testGetMultipleRecipients() {
        initDB();
        List<Integer> toGet = new ArrayList<>();
        toGet.add(testUser.getID());
        toGet.add(testUser2.getID());
        List<Recipient> recipientsGot = dbHandler.getRecipients(toGet);
        assertEquals(recipientsGot.size(), 2);
        assertEquals(recipientsGot.get(0), testUser);
        assertEquals(recipientsGot.get(1), testUser2);
        dbHandler.applyPendingOperations();
        recipientsGot = dbHandler.getRecipients(toGet);
        assertEquals(recipientsGot.size(), 2);
        assertEquals(recipientsGot.get(0), testUser);
        assertEquals(recipientsGot.get(1), testUser2);
    }

    @Test
    public void testAddAndDeleteItem() {
        dbHandler.addItem(testItem);
        assertEquals(dbHandler.getItem(testItem.getID()), testItem);
        dbHandler.deleteItem(testItem.getID());
        assertEquals(dbHandler.getItem(testItem.getID()), null);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getItem(testItem.getID()), null);
    }

    @Test
    public void testAddAndDeleteItemSecondMethod() {
        dbHandler.addItem(testItem);
        dbHandler.deleteItem(testItem);
        assertEquals(dbHandler.getItem(testItem.getID()), null);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getItem(testItem.getID()), null);
    }

    @Test
    public void testDeleteMultipleItems() {
        initDB();
        List<Integer> toDelete = new ArrayList<>();
        toDelete.add(testItem.getID());
        toDelete.add(testItem3.getID());
        dbHandler.deleteItems(toDelete);
        List<Item> itemsGot = dbHandler.getAllItems();
        assertEquals(itemsGot.size(), 2);
        assertEquals(itemsGot.get(0), testItem2);
        assertEquals(itemsGot.get(1), testItem4);
        dbHandler.applyPendingOperations();
        itemsGot = dbHandler.getAllItems();
        assertEquals(itemsGot.size(), 2);
        assertEquals(itemsGot.get(0), testItem2);
        assertEquals(itemsGot.get(1), testItem4);
    }

    @Test
    public void testDeleteMultipleUsers() {
        initDB();
        List<Integer> toDelete = new ArrayList<>();
        toDelete.add(testUser.getID());
        toDelete.add(testUser3.getID());
        dbHandler.deleteRecipients(toDelete);
        List<Recipient> recipientsGot = dbHandler.getAllRecipients();
        assertEquals(recipientsGot.size(), 2);
        assertEquals(recipientsGot.get(0), testUser2);
        assertEquals(recipientsGot.get(1), testRecipient);
        dbHandler.applyPendingOperations();
        recipientsGot = dbHandler.getAllRecipients();
        assertEquals(recipientsGot.size(), 2);
        assertEquals(recipientsGot.get(0), testUser2);
        assertEquals(recipientsGot.get(1), testRecipient);
    }

    @Test
    public void testDeleteUserDeletesItemsOfUser() {
        initDB();
        assertFalse(dbHandler.getItemsForContact(testUser2).isEmpty());
        dbHandler.deleteRecipient(testUser2);
        assertTrue(dbHandler.getItemsForContact(testUser2).isEmpty());
        assertFalse(dbHandler.getAllItems().isEmpty());
        dbHandler.applyPendingOperations();
        assertTrue(dbHandler.getItemsForContact(testUser2).isEmpty());
        assertFalse(dbHandler.getAllItems().isEmpty());
    }

    @Test
    public void testAddAndDeleteRecipientSecondMethod() {
        dbHandler.addRecipient(testUser);
        dbHandler.deleteRecipient(testUser);
        assertEquals(dbHandler.getRecipient(testUser.getID()), null);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getRecipient(testUser.getID()), null);
    }

    @Test
    public void testAddThreeRecipientsAndGetAllOfThemAndDeleteAllRecipients() {
        List<Recipient> users = new ArrayList<>();
        users.add(testUser);
        users.add(testUser2);
        users.add(testUser3);
        dbHandler.addRecipients(users);
        List<Recipient> usersGot = dbHandler.getAllRecipients();
        assertEquals(usersGot.size(), 3);
        User userGot = (User) usersGot.get(0);
        assertEquals(userGot, testUser);
        userGot = (User) usersGot.get(1);
        assertEquals(userGot, testUser2);
        userGot = (User) usersGot.get(2);
        assertEquals(userGot, testUser3);
        dbHandler.deleteAllRecipients();
        usersGot = dbHandler.getAllRecipients();
        assertTrue(usersGot.isEmpty());
        dbHandler.applyPendingOperations();
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }

    @Test
    public void testGetItemsBetweenTwoRecipients() {
        initDB();
        List<Item> contactItems = dbHandler.getItemsForContact(testUser2);
        assertEquals(contactItems.size(), 3);
        SimpleTextItem item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
        dbHandler.applyPendingOperations();
        dbHandler.getItemsForContact(testUser2);
        assertEquals(contactItems.size(), 3);
        item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
    }

    @Test
    public void testGetItemsBetweenTwoRecipientsSecondMethod() {
        initDB();
        List<Item> contactItems = dbHandler.getItemsForContact(testUser2.getID());
        assertEquals(contactItems.size(), 3);
        SimpleTextItem item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
        dbHandler.applyPendingOperations();
        dbHandler.getItemsForContact(testUser2.getID());
        assertEquals(contactItems.size(), 3);
        item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
    }

    @Test
    public void testMultipleRecipientsOperations() {
        assertTrue(NUM_ITER >= MIN_ITER);
        for (int i = 0; i < NUM_ITER; ++i) {
            dbHandler.addRecipient(createDummyUser(i, false));
        }
        for (int i = 0; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, false));
        }
        List<Integer> toGet = new ArrayList<>();
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            toGet.add(i);
            dbHandler.updateRecipient(createDummyUser(i, true));
        }
        List<Recipient> recipientsGot = dbHandler.getRecipients(toGet);
        assertEquals(recipientsGot.size(), NUM_ITER / 2);
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(recipientsGot.get(i), createDummyUser(i, true));
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, true));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, false));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            dbHandler.deleteRecipient(i);
        }
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, true));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getRecipient(i), null);
        }
        dbHandler.deleteAllRecipients();
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }

    @Test
    public void testMultipleItemOperations() {
        assertTrue(NUM_ITER >= MIN_ITER);
        for (int i = 0; i < NUM_ITER; ++i) {
            dbHandler.addItem(createDummyItem(i, false));
        }
        for (int i = 0; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, false));
        }
        List<Integer> toGet = new ArrayList<>();
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            toGet.add(i);
            dbHandler.updateItem(createDummyItem(i, true));
        }
        List<Item> itemsGot = dbHandler.getItems(toGet);
        assertEquals(itemsGot.size(), NUM_ITER / 2);
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, true));
            assertEquals(itemsGot.get(i), createDummyItem(i, true));
        }
        assertEquals(dbHandler.getItems(toGet).size(), NUM_ITER / 2);
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, false));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            dbHandler.deleteItem(i);
        }
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, true));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getItem(i), null);
        }
        dbHandler.deleteAllItems();
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertEquals(dbHandler.getAllRecipients().size(), 3);
        dbHandler.deleteAllRecipients();
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }

    @Test
    public void testGetUpdatedNonexistent() {
        dbHandler.updateRecipient(createDummyUser(10, false));
        dbHandler.updateItem(createDummyItem(10, false));
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertEquals(dbHandler.getAllRecipients().size(), 2);
        assertTrue(dbHandler.getRecipient(10) == null);
        assertTrue(dbHandler.getItem(10) == null);
        List<Integer> toGet = new ArrayList<>();
        toGet.add(10);
        assertTrue(dbHandler.getRecipients(toGet).isEmpty());
        assertTrue(dbHandler.getItems(toGet).isEmpty());
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getAllRecipients().size(), 2);
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertTrue(dbHandler.getRecipient(10) == null);
        assertTrue(dbHandler.getItem(10) == null);
        assertTrue(dbHandler.getRecipients(toGet).isEmpty());
        assertTrue(dbHandler.getItems(toGet).isEmpty());
        dbHandler.addItem(createDummyItem(20, false));
        dbHandler.addRecipient(createDummyUser(20, false));
        dbHandler.applyPendingOperations();
        dbHandler.deleteItem(20);
        dbHandler.deleteRecipient(20);
        dbHandler.updateItem(createDummyItem(20, false));
        dbHandler.updateRecipient(createDummyUser(20, false));
        assertEquals(dbHandler.getAllRecipients().size(), 2);
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertTrue(dbHandler.getRecipient(20) == null);
        assertTrue(dbHandler.getItem(20) == null);
        toGet = new ArrayList<>();
        toGet.add(20);
        assertTrue(dbHandler.getRecipients(toGet).isEmpty());
        assertTrue(dbHandler.getItems(toGet).isEmpty());
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getAllRecipients().size(), 2);
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertTrue(dbHandler.getRecipient(20) == null);
        assertTrue(dbHandler.getItem(20) == null);
        assertTrue(dbHandler.getRecipients(toGet).isEmpty());
        assertTrue(dbHandler.getItems(toGet).isEmpty());
    }

    @Test
    public void testAddAndDeleteRecipientAlwaysApplying() {
        dbHandler.addRecipient(testUser);
        dbHandler.applyPendingOperations();
        User u = (User) dbHandler.getRecipient(0);
        assertEquals(u, testUser);
        dbHandler.deleteRecipient(testUser.getID());
        dbHandler.applyPendingOperations();
        u = (User) dbHandler.getRecipient(testUser.getID());
        assertEquals(u, null);
    }

    @Test
    public void testUpdateRecipientAlwaysApplying() {
        dbHandler.addRecipient(testUser);
        dbHandler.applyPendingOperations();
        User u = new User(testUser.getID(), "NotMe");
        dbHandler.updateRecipient(u);
        dbHandler.applyPendingOperations();
        User userGot = (User) dbHandler.getRecipient(testUser.getID());
        assertEquals(userGot, u);
    }

    @Test
    public void testUpdateItemAlwaysApplying() {
        dbHandler.addItem(testItem);
        dbHandler.applyPendingOperations();
        SimpleTextItem item = new SimpleTextItem(testItem.getID(), testUser, testUser3, new Date(3), "-1");
        dbHandler.updateItem(item);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getItem(testItem.getID()), item);
    }

    @Test
    public void testUpdateMultipleRecipientsAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Recipient> toUpdate = new ArrayList<>();
        toUpdate.add(new User(testUser.getID(), "User1"));
        toUpdate.add(new User(testUser2.getID(), "User2"));
        dbHandler.updateRecipients(toUpdate);
        dbHandler.applyPendingOperations();
        Recipient userGot = dbHandler.getRecipient(testUser.getID());
        assertEquals(userGot, toUpdate.get(0));
        userGot = dbHandler.getRecipient(testUser2.getID());
        assertEquals(userGot, toUpdate.get(1));
    }

    @Test
    public void testUpdateMultipleItemsAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Item> toUpdate = new ArrayList<>();
        toUpdate.add(new SimpleTextItem(testItem.getID(), testUser3, testUser, new Date(100), "message1"));
        toUpdate.add(new SimpleTextItem(testItem2.getID(), testUser, testUser3, new Date(101), "message2"));
        dbHandler.updateItems(toUpdate);
        dbHandler.applyPendingOperations();
        SimpleTextItem itemGot = (SimpleTextItem) dbHandler.getItem(testItem.getID());
        assertEquals(itemGot, toUpdate.get(0));
        itemGot = (SimpleTextItem) dbHandler.getItem(testItem2.getID());
        assertEquals(itemGot, toUpdate.get(1));
    }

    @Test
    public void testGetMultipleItemsAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Integer> toGet = new ArrayList<>();
        toGet.add(testItem.getID());
        toGet.add(testItem2.getID());
        List<Item> itemsGot = dbHandler.getItems(toGet);
        assertEquals(itemsGot.get(0), testItem);
        assertEquals(itemsGot.get(1), testItem2);
    }

    @Test
    public void testGetMultipleRecipientsAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Integer> toGet = new ArrayList<>();
        toGet.add(testUser.getID());
        toGet.add(testUser2.getID());
        List<Recipient> recipientsGot = dbHandler.getRecipients(toGet);
        assertEquals(recipientsGot.size(), 2);
        assertEquals(recipientsGot.get(0), testUser);
        assertEquals(recipientsGot.get(1), testUser2);
    }

    @Test
    public void testAddAndDeleteItemAlwaysApplying() {
        dbHandler.addItem(testItem);
        dbHandler.applyPendingOperations();
        SimpleTextItem i = (SimpleTextItem) dbHandler.getItem(testItem.getID());
        assertEquals(i, testItem);
        dbHandler.deleteItem(testItem.getID());
        dbHandler.applyPendingOperations();
        i = (SimpleTextItem) dbHandler.getItem(testItem.getID());
        assertEquals(i, null);
    }

    @Test
    public void testAddAndDeleteItemSecondMethodAlwaysApplying() {
        dbHandler.addItem(testItem);
        dbHandler.applyPendingOperations();
        dbHandler.deleteItem(testItem);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getItem(testItem.getID()), null);
    }

    @Test
    public void testDeleteMultipleItemsAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Integer> toDelete = new ArrayList<>();
        toDelete.add(testItem.getID());
        toDelete.add(testItem3.getID());
        dbHandler.deleteItems(toDelete);
        dbHandler.applyPendingOperations();
        List<Item> itemsGot = dbHandler.getAllItems();
        assertEquals(itemsGot.size(), 2);
        assertEquals(itemsGot.get(0), testItem2);
        assertEquals(itemsGot.get(1), testItem4);
    }

    @Test
    public void testDeleteMultipleUsersAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Integer> toDelete = new ArrayList<>();
        toDelete.add(testUser.getID());
        toDelete.add(testUser3.getID());
        dbHandler.deleteRecipients(toDelete);
        dbHandler.applyPendingOperations();
        List<Recipient> recipientsGot = dbHandler.getAllRecipients();
        assertEquals(recipientsGot.size(), 2);
        assertEquals(recipientsGot.get(0), testUser2);
        assertEquals(recipientsGot.get(1), testRecipient);
    }

    @Test
    public void testDeleteUserDeletesItemsOfUserAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        assertFalse(dbHandler.getItemsForContact(testUser2).isEmpty());
        dbHandler.deleteRecipient(testUser2);
        dbHandler.applyPendingOperations();
        assertTrue(dbHandler.getItemsForContact(testUser2).isEmpty());
    }

    @Test
    public void testAddAndDeleteRecipientSecondMethodAlwaysApplying() {
        dbHandler.addRecipient(testUser);
        dbHandler.applyPendingOperations();
        dbHandler.deleteRecipient(testUser);
        dbHandler.applyPendingOperations();
        assertEquals(dbHandler.getRecipient(testUser.getID()), null);
    }

    @Test
    public void testAddThreeRecipientsAndGetAllOfThemAndDeleteAllRecipientsAlwaysApplying() {
        List<Recipient> users = new ArrayList<>();
        users.add(testUser);
        users.add(testUser2);
        users.add(testUser3);
        dbHandler.addRecipients(users);
        dbHandler.applyPendingOperations();
        List<Recipient> usersGot = dbHandler.getAllRecipients();
        assertEquals(usersGot.size(), 3);
        User userGot = (User) usersGot.get(0);
        assertEquals(userGot, testUser);
        userGot = (User) usersGot.get(1);
        assertEquals(userGot, testUser2);
        userGot = (User) usersGot.get(2);
        assertEquals(userGot, testUser3);
        dbHandler.deleteAllRecipients();
        dbHandler.applyPendingOperations();
        usersGot = dbHandler.getAllRecipients();
        assertTrue(usersGot.isEmpty());
    }

    @Test
    public void testGetItemsBetweenTwoRecipientsAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Item> contactItems = dbHandler.getItemsForContact(testUser2);
        assertEquals(contactItems.size(), 3);
        SimpleTextItem item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
    }

    @Test
    public void testGetItemsBetweenTwoRecipientsSecondMethodAlwaysApplying() {
        initDB();
        dbHandler.applyPendingOperations();
        List<Item> contactItems = dbHandler.getItemsForContact(testUser2.getID());
        assertEquals(contactItems.size(), 3);
        SimpleTextItem item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
    }

    @Test
    public void testMultipleItemOperationsAlwaysApplying() {
        assertTrue(NUM_ITER >= MIN_ITER);
        for (int i = 0; i < NUM_ITER; ++i) {
            dbHandler.addItem(createDummyItem(i, false));
        }
        dbHandler.applyPendingOperations();
        for (int i = 0; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, false));
        }
        List<Integer> toGet = new ArrayList<>();
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            toGet.add(i);
            dbHandler.updateItem(createDummyItem(i, true));
        }
        dbHandler.applyPendingOperations();
        List<Item> itemsGot = dbHandler.getItems(toGet);
        assertEquals(itemsGot.size(), NUM_ITER / 2);
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, true));
            assertEquals(itemsGot.get(i), createDummyItem(i, true));
        }
        assertEquals(dbHandler.getItems(toGet).size(), NUM_ITER / 2);
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, false));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            dbHandler.deleteItem(i);
        }
        dbHandler.applyPendingOperations();
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(dbHandler.getItem(i), createDummyItem(i, true));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getItem(i), null);
        }
        dbHandler.deleteAllItems();
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertEquals(dbHandler.getAllRecipients().size(), 3);
        dbHandler.deleteAllRecipients();
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }


    @Test
    public void testMultipleRecipientOperationAlwaysApplying() {
        assertTrue(NUM_ITER >= MIN_ITER);
        for (int i = 0; i < NUM_ITER; ++i) {
            dbHandler.addRecipient(createDummyUser(i, false));
        }
        dbHandler.applyPendingOperations();
        for (int i = 0; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, false));
        }
        List<Integer> toGet = new ArrayList<>();
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            toGet.add(i);
            dbHandler.updateRecipient(createDummyUser(i, true));
        }
        dbHandler.applyPendingOperations();
        List<Recipient> recipientsGot = dbHandler.getRecipients(toGet);
        assertEquals(recipientsGot.size(), NUM_ITER / 2);
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(recipientsGot.get(i), createDummyUser(i, true));
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, true));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, false));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            dbHandler.deleteRecipient(i);
        }
        dbHandler.applyPendingOperations();
        for (int i = 0; i < NUM_ITER / 2; ++i) {
            assertEquals(dbHandler.getRecipient(i), createDummyUser(i, true));
        }
        for (int i = NUM_ITER / 2; i < NUM_ITER; ++i) {
            assertEquals(dbHandler.getRecipient(i), null);
        }
        dbHandler.deleteAllRecipients();
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }

    @Test
    public void testLastUpdateTimeAlwaysApplying() {
        checkLastTime(0);
        Item item = new SimpleTextItem(1, new User(1, ""), new User(2, ""), new Date(1000), "");
        dbHandler.addItem(item);
        checkLastTime(0);
        dbHandler.applyPendingOperations();
        checkLastTime(1000);
        Item item2 = new SimpleTextItem(2, new User(2, ""), new User(3, ""), new Date(0), "");
        dbHandler.addItem(item2);
        checkLastTime(1000);
        dbHandler.applyPendingOperations();
        checkLastTime(1000);
        Item item3 = new SimpleTextItem(3, new User(3, ""), new User(1, ""), new Date(1001), "");
        dbHandler.updateItem(item3);
        checkLastTime(1000);
        dbHandler.applyPendingOperations();
        checkLastTime(1001);
    }

    @Test
    public void testLastUpdateTime() {
        checkLastTime(0);
        Item item = new SimpleTextItem(1, new User(1, ""), new User(2, ""), new Date(1000), "");
        dbHandler.addItem(item);
        checkLastTime(0);
        Item item2 = new SimpleTextItem(2, new User(2, ""), new User(3, ""), new Date(0), "");
        dbHandler.addItem(item2);
        checkLastTime(0);
        Item item3 = new SimpleTextItem(3, new User(3, ""), new User(1, ""), new Date(1001), "");
        dbHandler.updateItem(item3);
        checkLastTime(0);
        dbHandler.applyPendingOperations();
        checkLastTime(1001);
    }

    @Ignore
    private void checkLastTime(long time) {
        assertEquals(dbHandler.getLastUpdateTime(), time);
    }

    @Ignore
    private SimpleTextItem createDummyItem(int i, boolean update) {
        if (update) {
            return new SimpleTextItem(i, new User(1, ""), new User(2, ""), new Date(1), "bla");
        } else {
            return new SimpleTextItem(i, new User(0, ""), new User(1, ""), new Date(0), "");
        }
    }

    @Ignore
    private User createDummyUser(int i, boolean update) {
        if (update) {
            return new User(i, "hey");
        } else {
            return new User(i, "");
        }
    }

    @Test
    public void testDeleteEverything() {
        initDB();
        assertFalse(dbHandler.getAllItems().isEmpty());
        dbHandler.deleteAllItems();
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertFalse(dbHandler.getAllRecipients().isEmpty());
        dbHandler.deleteAllRecipients();
        assertTrue(dbHandler.getAllRecipients().isEmpty());
        clearDB();
    }

    @Override
    @After
    public void tearDown() {
        clearDB();
        app.getDatabaseHandler().closeDatabase();
        app.resetPreferences();
        dbHandler.resetLastUpdateTime();
    }

    @Ignore
    private void initDB() {
        dbHandler.addRecipient(testUser);
        dbHandler.addRecipient(testUser2);
        dbHandler.addRecipient(testUser3);
        dbHandler.addRecipient(testRecipient);
        dbHandler.addItem(testItem);
        dbHandler.addItem(testItem2);
        dbHandler.addItem(testItem3);
        dbHandler.addItem(testItem4);
    }

    @Ignore
    private void clearDB() {
        dbHandler.deleteAllItems();
        dbHandler.deleteAllRecipients();
    }
}
