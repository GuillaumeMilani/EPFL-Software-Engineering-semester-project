package ch.epfl.sweng.calamar;

import android.test.ApplicationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public class CalamarApplicationTest extends ApplicationTestCase<CalamarApplication> {

    private final String defaultUsername="";
    private final int defaultID=-1;
    private final long defaultLastRefresh=0;

    private final String testUsername="test";
    private final int testID=0;
    private final Date testDate=new Date(100);

    private String curUsername="";
    private int curUserID=0;

    private CalamarApplication app;

    @Before
    public void setUp() {
        createApplication();
        app=getApplication().getInstance();
    }

    public CalamarApplicationTest() {
        super(CalamarApplication.class);
    }

    @Test
    public void testDefaultValues(){
        app.resetPreferences();
        assertEquals(defaultID, app.getCurrentUserID());
        assertEquals(defaultUsername, app.getCurrentUserName());
        assertEquals(defaultLastRefresh, app.getLastItemsRefresh());
        assertEquals(defaultLastRefresh, app.getLastUsersRefresh());
    }

    @Test
    public void testSetUsername(){
        app.resetPreferences();
        app.setCurrentUserName(testUsername);
        assertEquals(testUsername,app.getCurrentUserName());
    }

    @Test
    public void testSetUserID(){
        app.resetPreferences();
        app.setCurrentUserID(testID);
        assertEquals(testID,app.getCurrentUserID());
    }

    @Test
    public void testSetLastItemsRefresh(){
        app.resetPreferences();
        app.setLastItemsRefresh(testDate);
        assertEquals(testDate.getTime(),app.getLastItemsRefresh());
    }

    @Test
    public void testSetLastUsersRefresh(){
        app.resetPreferences();
        app.setLastUsersRefresh(testDate);
        assertEquals(testDate.getTime(),app.getLastUsersRefresh());
    }

    @Test
    public void testResets(){
        app.resetPreferences();
        app.setLastUsersRefresh(testDate);
        app.resetLastUsersRefresh();
        assertEquals(defaultLastRefresh, app.getLastUsersRefresh());
        app.setLastItemsRefresh(testDate);
        app.resetLastItemsRefresh();
        assertEquals(defaultLastRefresh,app.getLastItemsRefresh());
        app.setCurrentUserName(testUsername);
        app.resetUsername();
        assertEquals(defaultUsername, app.getCurrentUserName());
        app.setCurrentUserID(defaultID);
        app.resetUserID();
        assertEquals(defaultID,app.getCurrentUserID());
    }

    @After
    public void tearDown(){
        app.resetPreferences();
    }
}
