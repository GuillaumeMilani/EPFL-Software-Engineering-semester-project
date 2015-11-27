package ch.epfl.sweng.calamar.item;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.test.ActivityInstrumentationTestCase2;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(JUnit4.class)
public class ItemsDetailsTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    @Rule
    public final ActivityTestRule<ChatActivity> mActivityRule = new ActivityTestRule<>(
            ChatActivity.class);

    public ItemsDetailsTest() {
        super(ChatActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        DatabaseClientLocator.setDatabaseClient(new ConstantDatabaseClient());
        CalamarApplication.getInstance().getDatabaseHandler().deleteAllItems();
    }

    /**
     * Test that the from user is shown in the item view
     * ( Test getView() of Item )
     */
    @Test
    public void testItemDetailsShowsUsers(){
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
    public void testSimpleTextItemDetailsShowCorrectPreview(){
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
    public void testLockedSimpleTextItemDetailsShowCorrectPreview(){
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
    public void testTrueConditionInItemIsCorrectlyDisplayed(){
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("Hello Bob, it's Alice !")).perform(click());

        onView(withText("true")).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test that the false and position condition are correctly displayed
     * (Test getView() of false and position condition)
     */
    @Test
    public void testFalseAndPositionConditionInItemIsCorrectlyDisplayed(){
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("You have to fill the conditions to see the message !")).perform(click());

        onView(withText("Go here !")).check(matches(ViewMatchers.isDisplayed()));
        onView(withText("and")).check(matches(ViewMatchers.isDisplayed()));
        onView(withText("false")).check(matches(ViewMatchers.isDisplayed()));
    }
}
