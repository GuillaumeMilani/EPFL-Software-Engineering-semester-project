package ch.epfl.sweng.calamar;

import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class SQLiteDatabaseHandlerTest extends ApplicationTestCase<CalamarApplication> {

    private SQLiteDatabaseHandler dbHandler;

    private final User testUser = new User(0,"Me");
    private final User testUser2 = new User(1,"You");
    private final User testUser3 = new User(2,"Him");
    private final Recipient testRecipient = new User(3,"Her");
    private final SimpleTextItem testItem = new SimpleTextItem(0,testUser,testRecipient,new Date(0),"0");
    private final SimpleTextItem testItem2 = new SimpleTextItem(1,testUser2,testUser,new Date(1),"1");
    private final SimpleTextItem testItem3 = new SimpleTextItem(2, testUser, testUser2, new Date(2), "2");
    private final SimpleTextItem testItem4 = new SimpleTextItem(3, testUser, testUser2, new Date(3), "3");

    @Before
    public void setUp(){
        createApplication();
        dbHandler=getApplication().getInstance().getDB();
        dbHandler.deleteAllItems();
        dbHandler.deleteAllRecipients();
    }
    public SQLiteDatabaseHandlerTest() {
        super(CalamarApplication.class);
    }


    @Before
    public void testEmptyDatabase(){
        assertTrue(dbHandler.getAllItems().isEmpty());
        assertTrue(dbHandler.getAllRecipients().isEmpty());
    }
    @Test
    public void testAddAndDeleteUser(){
        dbHandler.addRecipient(testUser);
        User u = (User) dbHandler.getRecipient(0);
        //TODO could use only one assertEquals if equals is redefined for User and Item
        assertEquals(u,testUser);
        dbHandler.deleteRecipient(testUser.getID());
        u = (User) dbHandler.getRecipient(testUser.getID());
        assertEquals(u, null);
        clearDB();
    }

    @Test
    public void testUpdateUser(){
        dbHandler.addRecipient(testUser);
        User u = new User(testUser.getID(),"NotMe");
        dbHandler.updateRecipient(u);
        User userGot= (User) dbHandler.getRecipient(testUser.getID());
        assertEquals(userGot, u);
        clearDB();
    }

    @Test
    public void testAddAndDeleteMessage(){
        dbHandler.addItem(testItem);
        SimpleTextItem i = (SimpleTextItem) dbHandler.getItem(testItem.getID());
        assertEquals(i, testItem);
        dbHandler.deleteItem(0);
        i=(SimpleTextItem) dbHandler.getItem(0);
        assertEquals(i, null);
        clearDB();
    }

    @Test
    public void addThreeUsersAndGetAllUsersAndDeleteAllUsers(){
        List<Recipient> users = new ArrayList<>();
        users.add(testUser);
        users.add(testUser2);
        users.add(testUser3);
        dbHandler.addRecipients(users);
        List<Recipient> usersGot = dbHandler.getAllRecipients();
        assertEquals(usersGot.size(),3);
        User userGot = (User) usersGot.get(0);
        assertEquals(userGot,testUser);
        userGot= (User) usersGot.get(1);
        assertEquals(userGot,testUser2);
        userGot= (User) usersGot.get(2);
        assertEquals(userGot,testUser3);
        dbHandler.deleteAllRecipients();
        usersGot=dbHandler.getAllRecipients();
        assertTrue(usersGot.isEmpty());
        clearDB();
    }

    @Test
    public void testGetMessagesBetweenTwoUsers(){
        initDB();
        getApplication().setCurrentUserID(0);
        List<Item> contactItems = dbHandler.getMessagesForContact(testUser2);
        assertEquals(contactItems.size(), 3);
        SimpleTextItem item = (SimpleTextItem) contactItems.get(0);
        assertEquals(item.getMessage(),testItem2.getMessage());
        assertEquals(item.getID(),testItem2.getID());
        item = (SimpleTextItem) contactItems.get(1);
        assertEquals(item.getMessage(),testItem3.getMessage());
        assertEquals(item.getID(),testItem3.getID());
        item=(SimpleTextItem) contactItems.get(2);
        assertEquals(item.getMessage(),testItem4.getMessage());
        assertEquals(item.getID(),testItem4.getID());
        getApplication().setCurrentUserID(-1);
        clearDB();
    }

    @Ignore
    private void initDB(){
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
    private void clearDB(){
        dbHandler.deleteAllItems();
        dbHandler.deleteAllRecipients();
    }
}
