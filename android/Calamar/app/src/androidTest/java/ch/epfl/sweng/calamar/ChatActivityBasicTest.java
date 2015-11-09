package ch.epfl.sweng.calamar;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Test for the chat activity
 */
@RunWith(AndroidJUnit4.class)
public class ChatActivityBasicTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    @Rule
    public final ActivityTestRule<ChatActivity> mActivityRule = new ActivityTestRule<>(
            ChatActivity.class);

    public ChatActivityBasicTest() {
        super(ChatActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ItemClientLocator.setItemClient(new ConstantItemClient());
    }


    /**
     * Test that we have a send button.
     */
    @Test
    public void testSendButtonName() {
        onView(withId(R.id.chatSendButton))
                .check(matches(withText("Send")));
    }

    /**
     * Test that we have a refresh button
     */
    @Test
    public void testRefreshButtonName() {
        onView(withId(R.id.refreshButton))
                .check(matches(withText("Refresh")));
    }

    /**
     * Test that we can write on the message edit field.
     */
    @Test
      public void testCanWriteInMessageEdit() {
        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(allOf(withId(R.id.messageEdit), withText("Hello Alice !")));
    }

    /**
     * Test that the message retrieve from the itemClient are displayed.
     */
    @Test
    public void testTwoMessageAreDisplayed() {
        ListView list = (ListView)mActivityRule.getActivity().findViewById(R.id.messagesContainer);
        int before = list.getCount();
        onView(withId(R.id.refreshButton)).perform(click());
        assertEquals(list.getCount(), before + 2);
    }

    /**
     * Test that the message edit field is empty when we send a message.
     */
    @Test
    public void testMessageEditIsEmptyWhenSend() {
        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(withId(R.id.chatSendButton)).perform(click());
        onView(allOf(withId(R.id.messageEdit), withText("")));
    }

    /**
     * Test that the message is displayed when we send a new one.
     */
    @Test
    public void testMessageIsDisplayedWhenSend() {
        ListView list = (ListView)mActivityRule.getActivity().findViewById(R.id.messagesContainer);
        int before = list.getCount();
        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(withId(R.id.chatSendButton)).perform(click());
        assertEquals(list.getCount(), before + 1);
    }

}
