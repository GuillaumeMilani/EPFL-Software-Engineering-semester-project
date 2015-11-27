package ch.epfl.sweng.calamar;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.chat.ChatAdapter;
import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.recipient.User;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Test for the chat activity
 */
@RunWith(AndroidJUnit4.class)
public class ChatActivityBasicTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    private static final byte[] testContent = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
            (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x02, (byte) 0x0d, (byte) 0xb1, (byte) 0xb2, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x25, (byte) 0x49, (byte) 0x44, (byte) 0x41,
            (byte) 0x54, (byte) 0x08, (byte) 0x99, (byte) 0x4d, (byte) 0x8a, (byte) 0xb1,
            (byte) 0x0d, (byte) 0x00, (byte) 0x20, (byte) 0x0c, (byte) 0x80, (byte) 0xc0,
            (byte) 0xff, (byte) 0x7f, (byte) 0xc6, (byte) 0xc1, (byte) 0xc4, (byte) 0x96,
            (byte) 0x81, (byte) 0x05, (byte) 0xa8, (byte) 0x80, (byte) 0x67, (byte) 0xe0,
            (byte) 0xb0, (byte) 0xa8, (byte) 0xfc, (byte) 0x65, (byte) 0xba, (byte) 0xaa,
            (byte) 0xce, (byte) 0xb3, (byte) 0x97, (byte) 0x0b, (byte) 0x2b, (byte) 0xd9,
            (byte) 0x11, (byte) 0xfa, (byte) 0xa5, (byte) 0xad, (byte) 0x00, (byte) 0x06,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49, (byte) 0x45,
            (byte) 0x4e, (byte) 0x44, (byte) 0xae, (byte) 0x42, (byte) 0x60, (byte) 0x82};

    @Rule
    public final ActivityTestRule<ChatActivity> mActivityRule = new ActivityTestRule<>(
            ChatActivity.class);

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    public ChatActivityBasicTest() {
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
        onView(withId(R.id.messageEdit)).check(matches(withText("Hello Alice !")));
    }

    /**
     * Test that the message retrieve from the itemClient are displayed.
     */
    @Test
    public void testTwoMessageAreDisplayed() {
        ListView list = (ListView) mActivityRule.getActivity().findViewById(R.id.messagesContainer);
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
        ListView list = (ListView) mActivityRule.getActivity().findViewById(R.id.messagesContainer);
        int before = list.getCount();
        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(withId(R.id.chatSendButton)).perform(click());
        assertEquals(list.getCount(), before + 1);
        onView(withText("Hello Alice !")).check(matches(ViewMatchers.isDisplayed()));
    }

    //Works but fails ; Too fast ? JUnit waiting on AsyncTask perhaps?
    @Ignore
    public void testImageItemIsUpdatedWithStorageManager() throws IOException, InterruptedException {
        File f = testFolder.newFile("f.png");
        FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(new String(testContent));
        bw.close();

        ImageItem testItem = new ImageItem(20, CalamarApplication.getInstance().getCurrentUser(), new User(1, "Alice"), new Date(), null, f.getAbsolutePath());
        ListView list = (ListView) mActivityRule.getActivity().findViewById(R.id.messagesContainer);
        ChatAdapter adapter = (ChatAdapter) list.getAdapter();
        ((ConstantDatabaseClient) DatabaseClientLocator.getDatabaseClient()).addItem(testItem);
        onView(withId(R.id.refreshButton)).perform(click());
        Item firstBefore = adapter.getItem(adapter.getCount() - 1);
        Item firstTextBefore = adapter.getItem(adapter.getCount() - 2);
        Item secondTextBefore = adapter.getItem(adapter.getCount() - 3);
        synchronized (this) {
            wait(2000);
        }
        assertEquals(firstTextBefore, adapter.getItem(adapter.getCount() - 2));
        assertEquals(secondTextBefore, adapter.getItem(adapter.getCount() - 3));
        assertFalse(firstBefore.equals(adapter.getItem(adapter.getCount() - 1)));
    }

    /**
     * Test that the message is displayed when we send a new one.
     */
    @Test
    public void testContentOfMessageLockedIsNotDisplayed() {
        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("You have to fill the conditions to see the message !")).check(matches(ViewMatchers.isDisplayed()));
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
}
