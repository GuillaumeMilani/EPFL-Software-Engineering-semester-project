package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        app = CalamarApplication.getInstance();
        dbHandler = app.getDB();
        getApplication();
        dbHandler.deleteAllItems();
        dbHandler.deleteAllRecipients();
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
    public void testDeleteOnEmptyDatabase(){
        dbHandler.deleteItem(0);
        dbHandler.deleteRecipient(0);
        assertEquals(dbHandler.getAllItems().size(), 0);
        assertEquals(dbHandler.getAllRecipients().size(), 0);
    }

    @Test
    public void testAddAndDeleteRecipient() {
        dbHandler.addRecipient(testUser);
        User u = (User) dbHandler.getRecipient(0);
        assertEquals(u, testUser);
        dbHandler.deleteRecipient(testUser.getID());
        u = (User) dbHandler.getRecipient(testUser.getID());
        assertEquals(u, null);
        clearDB();
    }

    @Test
    public void testUpdateRecipient() {
        dbHandler.addRecipient(testUser);
        User u = new User(testUser.getID(), "NotMe");
        dbHandler.updateRecipient(u);
        User userGot = (User) dbHandler.getRecipient(testUser.getID());
        assertEquals(userGot, u);
        clearDB();
    }

    @Test
    public void testUpdateItem() {
        dbHandler.addItem(testItem);
        SimpleTextItem item = new SimpleTextItem(testItem.getID(), testUser, testUser3, new Date(3), "-1");
        dbHandler.updateItem(item);
        assertEquals(dbHandler.getItem(testItem.getID()), item);
        clearDB();
    }

    @Test
    public void testUpdateMultipleRecipients() {
        initDB();
        List<Recipient> toUpdate = new ArrayList<>();
        toUpdate.add(new User(testUser.getID(), "User1"));
        toUpdate.add(new User(testUser2.getID(), "User2"));
        dbHandler.updateRecipients(toUpdate);
        Recipient userGot = dbHandler.getRecipient(testUser.getID());
        assertEquals(userGot, toUpdate.get(0));
        userGot = dbHandler.getRecipient(testUser2.getID());
        assertEquals(userGot, toUpdate.get(1));
        clearDB();
    }

    @Test
    public void testUpdateMultipleItems() {
        initDB();
        List<Item> toUpdate = new ArrayList<>();
        toUpdate.add(new SimpleTextItem(testItem.getID(), testUser3, testUser, new Date(100), "message1"));
        toUpdate.add(new SimpleTextItem(testItem2.getID(), testUser, testUser3, new Date(101), "message2"));
        dbHandler.updateItems(toUpdate);
        SimpleTextItem itemGot = (SimpleTextItem) dbHandler.getItem(testItem.getID());
        assertEquals(itemGot, toUpdate.get(0));
        itemGot = (SimpleTextItem) dbHandler.getItem(testItem2.getID());
        assertEquals(itemGot, toUpdate.get(1));
        clearDB();
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
        clearDB();
    }

    @Test
    public void testGetMultipleRecipients() {
        initDB();
        List<Integer> toGet = new ArrayList<>();
        toGet.add(testUser.getID());
        toGet.add(testUser2.getID());
        List<Recipient> recipientsGot = dbHandler.getRecipients(toGet);
        assertEquals(recipientsGot.size(),2);
        assertEquals(recipientsGot.get(0), testUser);
        assertEquals(recipientsGot.get(1), testUser2);
        clearDB();
    }

    @Test
    public void testAddAndDeleteItem() {
        dbHandler.addItem(testItem);
        SimpleTextItem i = (SimpleTextItem) dbHandler.getItem(testItem.getID());
        assertEquals(i, testItem);
        dbHandler.deleteItem(0);
        i = (SimpleTextItem) dbHandler.getItem(0);
        assertEquals(i, null);
        clearDB();
    }

    @Test
    public void testAddAndDeleteItemSecondMethod() {
        dbHandler.addItem(testItem);
        dbHandler.deleteItem(testItem);
        assertEquals(dbHandler.getItem(testItem.getID()), null);
        clearDB();
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
        clearDB();
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
        clearDB();
    }

    @Test
    public void testAddAndDeleteRecipientSecondMethod() {
        dbHandler.addRecipient(testUser);
        dbHandler.deleteRecipient(testUser);
        assertEquals(dbHandler.getRecipient(testUser.getID()), null);
        clearDB();
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
        clearDB();
    }

    @Test
    public void testGetItemsBetweenTwoRecipients() {
        initDB();
        app.setCurrentUserID(testUser.getID());
        List<Item> contactItems = dbHandler.getItemsForContact(testUser2);
        assertEquals(contactItems.size(), 3);
        SimpleTextItem item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
        app.setCurrentUserID(-1);
        clearDB();
    }

    @Test
    public void testGetItemsBetweenTwoRecipientsSecondMethod() {
        initDB();
        app.setCurrentUserID(testUser.getID());
        List<Item> contactItems = dbHandler.getItemsForContact(testUser2.getID());
        assertEquals(contactItems.size(), 3);
        SimpleTextItem item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item, testItem2);
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item, testItem3);
        item = (SimpleTextItem) contactItems.get(2);
        assertEquals(item, testItem4);
        app.setCurrentUserID(-1);
        clearDB();
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
