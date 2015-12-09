package ch.epfl.sweng.calamar.item;

import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.recipient.User;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(JUnit4.class)
public class ItemsDetailsTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    private final User ALICE = new User(1, "Alice");
    private final User BOB = new User(2, "Bob");

    private Intent conversation;

    public ItemsDetailsTest() {
        super(ChatActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        conversation = new Intent();
        conversation.putExtra(ChatFragment.EXTRA_CORRESPONDENT_NAME, ALICE.getName());
        conversation.putExtra(ChatFragment.EXTRA_CORRESPONDENT_ID, 1);

        setActivityIntent(conversation);
        getActivity();
        CalamarApplication.getInstance().resetPreferences();
        CalamarApplication.getInstance().getDatabaseHandler().deleteAllItems();

        DatabaseClientLocator.setDatabaseClient(new ConstantDatabaseClient());
        CalamarApplication.getInstance().getDatabaseHandler().deleteAllItems();
        CalamarApplication.getInstance().setCurrentUserID(BOB.getID());
        CalamarApplication.getInstance().setCurrentUserName(BOB.getName());

    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        CalamarApplication.getInstance().getDatabaseHandler().deleteAllItems();
        CalamarApplication.getInstance().resetPreferences();
    }

    /**
     * Test that the from user is shown in the item view
     * ( Test getView() of Item )
     */
    @Test
    public void testItemDetailsShowsUsers() {
        SystemClock.sleep(1);
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("Hello Bob, it's Alice !")).perform(click());

        onView(withText("From :")).check(matches(withId(R.id.ItemDetailsUserFromTitle)));
        onView(withText("Alice")).check(matches(withId(R.id.ItemDetailsUserFrom)));

        onView(withText("To :")).check(matches(withId(R.id.ItemDetailsUserToTitle)));
        onView(withText("Bob")).check(matches(withId(R.id.ItemDetailsUserTo)));
    }

    /**
     * Test that the preview of the Simple text item is correct
     * ( Test getPreview() of SimpleTextItem )
     */
    @Test
    public void testSimpleTextItemDetailsShowCorrectPreview() {
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("Hello Bob, it's Alice !")).perform(click());

        onView(withText("Preview")).check(matches(withId(R.id.ItemDetailsPreviewTitle)));

        onView(withText("Hello Bob, it's Alice !")).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test that the preview of a locked Simple text item is correct
     * ( Test getPreview() of SimpleTextItem )
     */
    @Test
    public void testLockedSimpleTextItemDetailsShowCorrectPreview() {
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("You have to fill the conditions to see the message !")).perform(click());

        //We want to see if the details is the same as the preview
        onView(withText("You have to fill the conditions to see the message !")).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test that the true Condition is correctly displayed
     * (Test getView() of true condition )
     */
    @Test
    public void testTrueConditionInItemIsCorrectlyDisplayed() {
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("Hello Bob, it's Alice !")).perform(click());

        onView(withText("true")).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test that the false and position condition are correctly displayed
     * (Test getView() of false and position condition)
     */
    @Test
    public void testFalseAndPositionConditionInItemIsCorrectlyDisplayed() {
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("You have to fill the conditions to see the message !")).perform(click());

        onView(withText("Go here !")).check(matches(ViewMatchers.isDisplayed()));
        onView(withText("and")).check(matches(ViewMatchers.isDisplayed()));
        onView(withText("false")).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testItemLocked() {
        Item item1 = new SimpleTextItem(0, ALICE, BOB, new Date(), Condition.falseCondition(), "bla");
        assertTrue(item1.isLocked());
        Item item2 = new SimpleTextItem(0, ALICE, BOB, new Date(), Condition.trueCondition(), "bla");
        assertFalse(item2.isLocked());
    }

    @Test
    public void testGetLocation() {
        Item item1 = new SimpleTextItem(0, ALICE, BOB, new Date(), new PositionCondition(new Location("test")), "bla");
        assertNotNull(item1.getLocation());
    }
}
