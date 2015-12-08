package ch.epfl.sweng.calamar;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.recipient.User;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Test for the chat activity
 */
@RunWith(AndroidJUnit4.class)
public class ChatActivityBasicTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    private final static String HELLO_ALICE = "Hello Alice !";

    private final User ALICE = new User(1, "Alice");
    private final User BOB = new User(2, "Bob");

    public ChatActivityBasicTest() {
        super(ChatActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        Intent conversation = new Intent();
        conversation.putExtra(ChatFragment.EXTRA_CORRESPONDENT_NAME, ALICE.getName());
        conversation.putExtra(ChatFragment.EXTRA_CORRESPONDENT_ID, 1);

        CalamarApplication.getInstance().resetPreferences();
        CalamarApplication.getInstance().getDatabaseHandler().deleteAllItems();

        setActivityIntent(conversation);
        getActivity();
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
        onView(withId(R.id.messageEdit)).perform(typeText(HELLO_ALICE));
        onView(withId(R.id.messageEdit)).check(matches(withText(HELLO_ALICE)));
    }

    /**
     * Test that the message retrieve from the itemClient are displayed.
     * ( Not more and not less than 2 messages )
     */
    @Test
    public void testTwoMessageAreDisplayed() {
        ListView list = (ListView) getActivity().findViewById(R.id.messagesContainer);
        int before = list.getCount();
        onView(withId(R.id.refreshButton)).perform(click());
        assertEquals(list.getCount(), before + 2);
    }

    /**
     * Test that the message edit field is empty when we send a message.
     */
    @Test
    public void testMessageEditIsEmptyWhenSend() {
        onView(withId(R.id.messageEdit)).perform(typeText(HELLO_ALICE));
        onView(withId(R.id.chatSendButton)).perform(click());
        onView(allOf(withId(R.id.messageEdit), withText("")));
    }

    /**
     * Test that the message is displayed when we send a new one.
     */
    @Test
    public void testMessageIsDisplayedWhenSend() {
        ListView list = (ListView) getActivity().findViewById(R.id.messagesContainer);
        int before = list.getCount();
        onView(withId(R.id.messageEdit)).perform(typeText(HELLO_ALICE));
        onView(withId(R.id.chatSendButton)).perform(click());
        assertEquals(list.getCount(), before + 1);
        onView(withText(HELLO_ALICE)).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test that the message is displayed when we send a new one.
     */
    @Test
    public void testContentOfMessageLockedIsNotDisplayed() {
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("You have to fill the conditions to see the message !")).check(matches(ViewMatchers.isDisplayed()));

        try {
            onView(withText("Hello Alice, it's Bob !")).check(matches(ViewMatchers.isDisplayed()));
            throw new AssertionError("The content of a lock message should not be displayed");
        } catch (NoMatchingViewException e) {
            //Good
        }
    }

    /**
     * Test that item details are shown when we click on a message.
     */
    @Test
    public void testItemDetailsAreShownAndCanBeRemoved() {
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("Hello Bob, it's Alice !")).perform(click());

        // We test the content of the view in ItemsDetailsTests
        //If we can click on OK, the dialog is displayed !
        onView(withText("Item description")).check(matches(ViewMatchers.isDisplayed()));

        onView(withText("OK")).perform(click());
        not(onView(withText("Item description")));
    }

    /**
     * Test that we have a refresh button
     */
    @Test
    public void testCanNotSendEmptyMessages() {
        onView(withId(R.id.chatSendButton)).check(matches(not(isEnabled())));

        onView(withId(R.id.messageEdit)).perform(typeText(HELLO_ALICE));
        onView(withId(R.id.chatSendButton)).check(matches(isEnabled()));
        onView(withId(R.id.chatSendButton)).perform(click());

        onView(withId(R.id.chatSendButton)).check(matches(not(isEnabled())));

        onView(withId(R.id.messageEdit)).perform(typeText(HELLO_ALICE));
        onView(withId(R.id.messageEdit)).perform(clearText());

        onView(withId(R.id.chatSendButton)).check(matches(not(isEnabled())));
    }

    @Test
    public void testClearChat() {
        onView(withId(R.id.refreshButton)).perform(click());
        ListView list = (ListView) getActivity().findViewById(R.id.messagesContainer);
        assertFalse(list.getCount() == 0);
        getActivity().clearChat();
        assertTrue(list.getCount() == 0);
    }

    @Test
    public void testCreateItem() {
        onView(withId(R.id.selectFileButton)).check(doesNotExist());
        onView(withId(R.id.chat_create_item_button)).perform(click());
        onView(withId(R.id.selectFileButton)).check(matches(isDisplayed()));
    }
}
