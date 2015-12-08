package ch.epfl.sweng.calamar;

import android.content.ComponentCallbacks2;
import android.support.test.InstrumentationRegistry;
import android.test.ApplicationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.User;

@RunWith(JUnit4.class)
public class CalamarApplicationTest extends ApplicationTestCase<CalamarApplication> {

    private final String defaultUsername = "";
    private final int defaultID = -1;
    private final Date defaultLastRefresh = new Date(0);

    private final String testUsername = "test";
    private final int testID = 0;
    private final Date testTime = new Date(100);

    private final Item testItem = new SimpleTextItem(0, new User(0, "Alice"), new User(1, "Bob"), new Date(), "Bla");

    private CalamarApplication app;
    private SQLiteDatabaseHandler dbHandler;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        app = (CalamarApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        getApplication();
        app.resetPreferences();
        dbHandler = app.getDatabaseHandler();
    }

    public CalamarApplicationTest() {
        super(CalamarApplication.class);
    }


    @Test
    public void testDefaultValues() {
        assertEquals(defaultID, app.getCurrentUserID());
        assertEquals(defaultUsername, app.getCurrentUserName());
        assertEquals(defaultLastRefresh, app.getLastItemsRefresh());
        assertEquals(defaultLastRefresh, app.getLastUsersRefresh());
        assertEquals(false, app.getTokenSent());
    }

    @Test
    public void testSetToken() {
        app.setTokenSent(true);
        assertEquals(true, app.getTokenSent());
        app.setTokenSent(false);
        assertEquals(false, app.getTokenSent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetGoogleClientNotSet() {
        app.getGoogleApiClient();
    }

    @Test
    public void testOnTrimMemory() {
        dbHandler.addItem(testItem);
        assertTrue(dbHandler.areOperationsPending());
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND);
        assertFalse(dbHandler.areOperationsPending());
        dbHandler.addItem(testItem);
        assertTrue(dbHandler.areOperationsPending());
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
        assertFalse(dbHandler.areOperationsPending());
        dbHandler.addItem(testItem);
        assertTrue(dbHandler.areOperationsPending());
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
        assertFalse(dbHandler.areOperationsPending());
        dbHandler.addItem(testItem);
        assertTrue(dbHandler.areOperationsPending());
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL);
        assertFalse(dbHandler.areOperationsPending());
        dbHandler.addItem(testItem);
        assertTrue(dbHandler.areOperationsPending());
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW);
        assertTrue(dbHandler.areOperationsPending());
        dbHandler.addItem(testItem);
        assertTrue(dbHandler.areOperationsPending());
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE);
        assertFalse(dbHandler.areOperationsPending());
        dbHandler.addItem(testItem);
        assertTrue(dbHandler.areOperationsPending());
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
        assertFalse(dbHandler.areOperationsPending());
    }

    @Test
    public void testSetUsername() {
        app.setCurrentUserName(testUsername);
        assertEquals(testUsername, app.getCurrentUserName());
    }

    @Test
    public void testSetUserID() {
        app.setCurrentUserID(testID);
        assertEquals(testID, app.getCurrentUserID());
    }

    @Test
    public void testSetLastItemsRefresh() {
        app.setLastItemsRefresh(testTime);
        assertEquals(testTime, app.getLastItemsRefresh());
    }

    @Test
    public void testSetLastUsersRefresh() {
        app.setLastUsersRefresh(testTime);
        assertEquals(testTime, app.getLastUsersRefresh());
    }

    @Test
    public void testResets() {
        app.setLastUsersRefresh(testTime);
        app.resetLastUsersRefresh();
        assertEquals(defaultLastRefresh, app.getLastUsersRefresh());
        app.setLastItemsRefresh(testTime);
        app.resetLastItemsRefresh();
        assertEquals(defaultLastRefresh, app.getLastItemsRefresh());
        app.setCurrentUserName(testUsername);
        app.resetUsername();
        assertEquals(defaultUsername, app.getCurrentUserName());
        app.setCurrentUserID(defaultID);
        app.resetUserID();
        assertEquals(defaultID, app.getCurrentUserID());
    }

    @After
    public void tearDown() {
        app.resetPreferences();
    }
}
