package ch.epfl.sweng.calamar;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.verify;


/**
 * Test the communication of the chat activity with the itemClient.
 */
@RunWith(JUnit4.class)
public class ChatActivityCommunicationTest extends ActivityInstrumentationTestCase2<ChatActivity> {


    public ChatActivityCommunicationTest() {
        super(ChatActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }


    /**
     * Test that when we click on the send button, a correct item is created.
     *
     * @throws DatabaseClientException
     */
    @Test
    public void testSendButtonSendCorrectInformation() throws DatabaseClientException {
        DatabaseClient client = Mockito.mock(ConstantItemClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        Intent conversation = new Intent();
        conversation.putExtra(ChatUsersListActivity.EXTRA_CORRESPONDENT_NAME, "Alice");
        conversation.putExtra(ChatUsersListActivity.EXTRA_CORRESPONDENT_ID, 1);
        setActivityIntent(conversation);
        getActivity();

        //The argument captor capture the argument that the activity give to the send method.
        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(withId(R.id.chatSendButton)).perform(click());

        verify(client).send(argument.capture());

        assertEquals(argument.getValue().getTo().getName(), "Alice");
        //Test the text of the message
        SimpleTextItem expected = new SimpleTextItem(1, argument.getValue().getFrom(), argument.getValue().getTo(), argument.getValue().getDate(), "Hello Alice !");
        assertEquals(argument.getValue(), expected);

        //Test the name of the correspondent.
        assertEquals("Alice", argument.getValue().getTo().getName());

    }

    /**
     * Test that the intent are correctly received.
     */
    @Test
    public void testActivityCorrectlyGetIntent() {
        Intent conversation = new Intent();
        conversation.putExtra(ChatUsersListActivity.EXTRA_CORRESPONDENT_NAME, "AliceTest");
        conversation.putExtra(ChatUsersListActivity.EXTRA_CORRESPONDENT_ID, 1);
        setActivityIntent(conversation);

        getActivity();

        TextView recipient = (TextView) getActivity().findViewById(R.id.recipientLabel);

        assertEquals("AliceTest", recipient.getText());
    }
}
