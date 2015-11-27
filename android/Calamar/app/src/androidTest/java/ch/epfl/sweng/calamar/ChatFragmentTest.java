package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import ch.epfl.sweng.calamar.chat.ChatUsersListActivity;
import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.verify;

/**
 * Created by Quentin Jaquier, sciper 235825 on 27.11.2015.
 */
@RunWith(JUnit4.class)
public class ChatFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public ChatFragmentTest() {
        super(MainActivity.class);
    }

    private CalamarApplication app;

    private final Recipient bob = new User(1, "bob");
    private final Recipient alice = new User(2, "alice");

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        app = (CalamarApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    /**
     * Test that we have a new user button.
     */
    @Test
    public void testNewContactButtonName() {
        getActivity();
        onView(withText("Chat")).perform(click());

        onView(withId(R.id.newContact))
                .check(matches(withText("New")));
    }

    /**
     * Test that we display nothing when nothing is in the database
     */
    @Test
    public void testDisplayContactIsEmptyWhenNoContact() {
        app.getDatabaseHandler().deleteAllRecipients();
        getActivity();
        onView(withText("Chat")).perform(click());

        ListView list = (ListView) getActivity().findViewById(R.id.contactsList);
        assertEquals(list.getCount(), 0);
    }

    /**
     * Test that we display contacts in the database.
     */
    @Test
    public void testDisplayTwoContact() {
        app.getDatabaseHandler().deleteAllRecipients();
        app.getDatabaseHandler().addRecipient(bob);
        app.getDatabaseHandler().addRecipient(alice);

        getActivity();
        onView(withText("Chat")).perform(click());

        ListView list = (ListView) getActivity().findViewById(R.id.contactsList);

        assertEquals(list.getCount(), 2);

        app.getDatabaseHandler().deleteAllRecipients();
    }

    /**
     * Test that we can cancel to add a new contact.
     */
    @Test
    public void testCreateContactCanBeCancelled() {
        app.getDatabaseHandler().deleteAllRecipients();

        getActivity();
        onView(withText("Chat")).perform(click());

        onView(withId(R.id.newContact)).perform(click());

        onView(withText("Cancel")).perform(click());
    }

    /**
     * Test that we can correctly add a contact.
     */
    @Test
    public void testCreateContactIsCorrectlyCreated() {
        app.getDatabaseHandler().deleteAllRecipients();

        DatabaseClientLocator.setDatabaseClient(new ConstantDatabaseClient());

        getActivity();
        onView(withText("Chat")).perform(click());

        ListView list = (ListView) getActivity().findViewById(R.id.contactsList);
        final int before = list.getCount();

        onView(withId(R.id.newContact)).perform(click());

        onView(withText("Add")).perform(click());

        assertEquals(before + 1, list.getCount());
    }

    /**
     * Test that we send correct data as parameter to the database client
     */
    @Test
    public void testCreateContactSendCorrectParameter() throws DatabaseClientException {
        app.getDatabaseHandler().deleteAllRecipients();

        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);

        getActivity();
        onView(withText("Chat")).perform(click());

        onView(withId(R.id.newContact)).perform(click());

        onView(withHint("Mail")).perform(typeText("calamar@gmail.com"));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        onView(withText("Add")).perform(click());

        verify(client).findUserByName(argument.capture());

        assertEquals("calamar@gmail.com", argument.getValue());
    }

}

