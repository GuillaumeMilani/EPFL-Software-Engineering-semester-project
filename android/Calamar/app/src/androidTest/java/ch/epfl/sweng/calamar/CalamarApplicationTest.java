package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public class CalamarApplicationTest extends ApplicationTestCase<CalamarApplication> {

    private final String defaultUsername = "";
    private final int defaultID = -1;
    private final long defaultLastRefresh = 0;

    private final String testUsername = "test";
    private final int testID = 0;
    private final Date testDate = new Date(100);

    private String curUsername = "";
    private int curUserID = 0;

    private CalamarApplication app;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        app = (CalamarApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        getApplication();
    }

    public CalamarApplicationTest() {
        super(CalamarApplication.class);
    }


    @Test
    public void testDefaultValues() {
        app.resetPreferences();
        Log.v("Def ", app.getCurrentUserID() + "");
        assertEquals(defaultID, app.getCurrentUserID());
        assertEquals(defaultUsername, app.getCurrentUserName());
        assertEquals(defaultLastRefresh, app.getLastItemsRefresh());
        assertEquals(defaultLastRefresh, app.getLastUsersRefresh());
        app.resetPreferences();
    }

    @Test
    public void testSetUsername() {
        app.resetPreferences();
        app.setCurrentUserName(testUsername);
        assertEquals(testUsername, app.getCurrentUserName());
        app.resetPreferences();
    }

    @Test
    public void testSetUserID() {
        app.resetPreferences();
        app.setCurrentUserID(testID);
        assertEquals(testID, app.getCurrentUserID());
        app.resetPreferences();
    }

    @Test
    public void testSetLastItemsRefresh() {
        app.resetPreferences();
        app.setLastItemsRefresh(testDate);
        assertEquals(testDate.getTime(), app.getLastItemsRefresh());
        app.resetPreferences();
    }

    @Test
    public void testSetLastUsersRefresh() {
        app.resetPreferences();
        app.setLastUsersRefresh(testDate);
        assertEquals(testDate.getTime(), app.getLastUsersRefresh());
        app.resetPreferences();
    }

    @Test
    public void testResets() {
        app.resetPreferences();
        app.setLastUsersRefresh(testDate);
        app.resetLastUsersRefresh();
        assertEquals(defaultLastRefresh, app.getLastUsersRefresh());
        app.setLastItemsRefresh(testDate);
        app.resetLastItemsRefresh();
        assertEquals(defaultLastRefresh, app.getLastItemsRefresh());
        app.setCurrentUserName(testUsername);
        app.resetUsername();
        assertEquals(defaultUsername, app.getCurrentUserName());
        app.setCurrentUserID(defaultID);
        app.resetUserID();
        assertEquals(defaultID, app.getCurrentUserID());
        app.resetPreferences();
    }

    @After
    public void tearDown() {
        //app.resetPreferences();
    }
}
