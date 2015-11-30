package ch.epfl.sweng.calamar;

import android.content.Intent;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import android.widget.Toast;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.client.FaultyDatabaseClient;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.verify;


/**
 * Test the communication of the chat activity with the itemClient.
 */
@RunWith(JUnit4.class)
public class ChatActivityCommunicationTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    private final String ALICE = "Alice";
    private final String MESSAGE = "Hello " + ALICE + " !";

    private Intent conversation;

    public ChatActivityCommunicationTest() {
        super(ChatActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        conversation = new Intent();
        conversation.putExtra(ChatFragment.EXTRA_CORRESPONDENT_NAME, ALICE);
        conversation.putExtra(ChatFragment.EXTRA_CORRESPONDENT_ID, 1);
    }


    /**
     * Test that when we click on the send button, a correct item is created.
     *
     * @throws DatabaseClientException
     */
    @Test
    public void testSendButtonSendCorrectInformation() throws DatabaseClientException {
        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        setActivityIntent(conversation);
        getActivity();

        //The argument captor capture the argument that the activity give to the send method.
        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.messageEdit)).perform(typeText(MESSAGE));
        onView(withId(R.id.chatSendButton)).perform(click());

        verify(client).send(argument.capture());

        assertEquals(argument.getValue().getTo().getName(), ALICE);
        //Test the text of the message
        SimpleTextItem expected = new SimpleTextItem(1, argument.getValue().getFrom(), argument.getValue().getTo(), argument.getValue().getDate(), MESSAGE);
        assertEquals(argument.getValue(), expected);

    }

    /**
     * Test the behaviour with a faulty network when we send an item.
     */
    @Test
    public void testFaultyNetworkItemSend(){
        DatabaseClient client = Mockito.mock(FaultyDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        setActivityIntent(conversation);
        getActivity();

        onView(withId(R.id.messageEdit)).perform(typeText(MESSAGE));

        onView(withId(R.id.chatSendButton)).perform(click());


        onView(withText("Impossible to send the message")).check(matches(ViewMatchers.isDisplayed()));
        onView(withText("OK")).perform(click());
    }

    /**
     * Test the behaviour with a faulty network when we refresh.
     */
    //TODO : Check with Guillaume
    @Ignore
    public void testFaultyNetworkItemRefresh(){
        DatabaseClient client = Mockito.mock(FaultyDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        setActivityIntent(conversation);
        getActivity();

        //Wait that the local refresh end
        SystemClock.sleep(10000);

        onView(withId(R.id.refreshButton)).perform(click());
        onView(withText("Unable to refresh")).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test that the intent are correctly received.
     */
    @Test
    public void testActivityCorrectlyGetIntent() {
        setActivityIntent(conversation);

        getActivity();

        TextView recipient = (TextView) getActivity().findViewById(R.id.recipientLabel);

        assertEquals(ALICE, recipient.getText());
    }
}
