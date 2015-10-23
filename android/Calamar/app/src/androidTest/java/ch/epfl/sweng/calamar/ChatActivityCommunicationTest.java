package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.verify;


/**
 * Test the communication of the chat activity with the itemClient.
 */
public class ChatActivityCommunicationTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    private User alice = new User(1,"Alice");
    private User bob = new User(2,"Bob");

    public ChatActivityCommunicationTest() {
        super(ChatActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }


    /**
     * Test that when we click on the send button, a correct item is created.
     * @throws ItemClientException
     */
    @Test
    public void testSendButtonSendCorrectInformation() throws ItemClientException {
        ItemClient client = Mockito.mock(ConstantItemClient.class);
        ItemClientLocator.setItemClient(client);
        getActivity();

        //The argument captor capture the argument that the activity give to the send method.
        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(withId(R.id.chatSendButton)).perform(click());

        verify(client).send(argument.capture());

        assertEquals(argument.getValue().getFrom().getName(), "Alice");

        // We use the same date !
        //SimpleTextItem expected = new SimpleTextItem(1,alice,bob,argument.getValue().getDate(),"Hello Alice !");
        //assertEquals(argument.getValue().getFrom(),expected); // When equals on item is ok.
    }

    /**
     * Test that when we click on the send button, a correct item is created.
     * @throws ItemClientException
     */
    @Test
    public void testRefreshButtonGiveCorrectUser() throws ItemClientException {
        ItemClient client = Mockito.mock(ConstantItemClient.class);
        ItemClientLocator.setItemClient(client);
        getActivity();

        ArgumentCaptor<Recipient> argument = ArgumentCaptor.forClass(Recipient.class);
        ArgumentCaptor<Date> date = ArgumentCaptor.forClass(Date.class);

        onView(withId(R.id.refreshButton)).perform(click());

        verify(client).getAllItems(argument.capture(), date.capture());
        //TODO : Test date == last refresh.
        assertEquals(argument.getValue().getName(), "Alice");
        //assertEquals(argument.getValue(), ChatActivity.actualUser);
    }


}
