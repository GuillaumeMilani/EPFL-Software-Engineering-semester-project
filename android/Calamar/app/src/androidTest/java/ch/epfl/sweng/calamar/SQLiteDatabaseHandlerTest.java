package ch.epfl.sweng.calamar;

import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SQLiteDatabaseHandlerTest extends ApplicationTestCase<CalamarApplication> {

    private SQLiteDatabaseHandler dbHandler;

    private final User testUser = new User(0,"Me");
    private final User testUser2 = new User(1,"You");
    private final User testUser3 = new User(2,"Him");
    private final Recipient testRecipient = new User(3,"Her");
    private final SimpleTextItem testItem = new SimpleTextItem(0,testUser,testRecipient,new Date(0),"blabla");

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
    }

    @Test
    public void testUpdateUser(){
        dbHandler.addRecipient(testUser);
        User u = new User(testUser.getID(),"NotMe");
        dbHandler.updateRecipient(u);
        u= (User) dbHandler.getRecipient(testUser.getID());
        assertEquals(u, testUser);
        dbHandler.deleteRecipient(testUser.getID());
    }

    @Test
    public void testAddAndDeleteMessage(){
        dbHandler.addItem(testItem);
        SimpleTextItem i = (SimpleTextItem) dbHandler.getItem(testItem.getID());
        assertEquals(i,testItem);
        dbHandler.deleteItem(0);
        i=(SimpleTextItem) dbHandler.getItem(0);
        assertEquals(i, null);
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
    }
}
