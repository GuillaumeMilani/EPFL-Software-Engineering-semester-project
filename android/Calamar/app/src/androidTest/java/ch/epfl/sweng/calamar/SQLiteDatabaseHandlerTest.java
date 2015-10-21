package ch.epfl.sweng.calamar;

import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


public class SQLiteDatabaseHandlerTest extends ApplicationTestCase<CalamarApplication> {

    private SQLiteDatabaseHandler dbHandler;

    private final User testUser = new User(0,"Me");
    private final User testUser2 = new User(1,"You");
    private final User testUser3 = new User(2,"Him");
    private final Recipient testRecipient = new User(3,"Her");
    private final SimpleTextItem testItem = new SimpleTextItem(0,testUser,testRecipient,new Date(0),"blabla");

    public SQLiteDatabaseHandlerTest(){
        super(CalamarApplication.class);
        System.out.println("APPLICATION : "+getApplication());
        dbHandler=getApplication().getInstance().getDB();
    }


    @Before
    public void testEmptyDatabase(){
        assertTrue(dbHandler.getAllMessages().isEmpty());
        assertTrue(dbHandler.getAllUsers().isEmpty());
    }
    @Test
    public void testAddAndDeleteUser(){
        dbHandler.addUser(testUser);
        User u = dbHandler.getUser(0);
        //TODO could use only one assertEquals if equals is redefined for User and Item
        assertEquals(u.getID(),testUser.getID());
        assertEquals(u.getName(),testUser.getName());
        dbHandler.deleteUser(testUser.getID());
        u = dbHandler.getUser(testUser.getID());
        assertEquals(u, null);
    }

    @Test
    public void testUpdateUser(){
        dbHandler.addUser(testUser);
        User u = new User(testUser.getID(),"NotMe");
        dbHandler.updateUser(u);
        u=dbHandler.getUser(testUser.getID());
        assertEquals(u.getID(), testUser.getID());
        assertEquals(u.getName(),"NotMe");
        dbHandler.deleteUser(testUser.getID());
    }

    @Test
    public void testAddAndDeleteMessage(){
        dbHandler.addMessage(testItem);
        SimpleTextItem i = (SimpleTextItem) dbHandler.getMessage(testItem.getID());
        assertEquals(i.getID(),testItem.getID());
        assertEquals(i.getMessage(),testItem.getMessage());
        assertEquals(i.getFrom(), testItem.getFrom());
        assertEquals(i.getTo(), testItem.getTo());
        assertEquals(i.getDate(),testItem.getDate());
        dbHandler.deleteMessage(0);
        i=(SimpleTextItem) dbHandler.getMessage(0);
        assertEquals(i, null);
    }

    @Test
    public void addThreeUsersAndGetAllUsersAndDeleteAllUsers(){
        List<User> users = new ArrayList<>();
        users.add(testUser);
        users.add(testUser2);
        users.add(testUser3);
        dbHandler.addUsers(users);
        List<User> usersGot = dbHandler.getAllUsers();
        assertEquals(usersGot.size(),3);
        User userGot = usersGot.get(0);
        assertEquals(userGot.getID(),testUser.getID());
        assertEquals(userGot.getName(),testUser.getName());
        userGot=usersGot.get(1);
        assertEquals(userGot.getID(),testUser2.getID());
        assertEquals(userGot.getName(),testUser2.getName());
        userGot=usersGot.get(2);
        assertEquals(userGot.getID(),testUser3.getID());
        assertEquals(userGot.getName(),testUser3.getName());
        dbHandler.deleteAllUsers();
        usersGot=dbHandler.getAllUsers();
        assertTrue(usersGot.isEmpty());
    }
}
