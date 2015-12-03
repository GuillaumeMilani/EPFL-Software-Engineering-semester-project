package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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
 *
 * One can run these tests on a real device.
 *
 * We ignore these tests on an emulator for many reason :
 * - The gps always shows a popup asking to change settings to have a better precision.
 * Unsuccessful tries have been made to remove these.
 * - The account creation popup always shows on a new emulator.
 * It is really hard to set a google account on the emulator on jenkins.
 * - I did not manage to found a way to directly test android.support.v4.app.Fragment.
 * As I understood this kind of fragment can not be tested the same way as others fragment.
 */
@Ignore
@RunWith(JUnit4.class)
public class ChatFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public ChatFragmentTest() {
        super(MainActivity.class);
    }

    private CalamarApplication app;

    private final Recipient bob = new User(1, "bob");
    private final Recipient alice = new User(2, "alice");

    private final String CHAT_FRAGMENT_NAME = "Chat";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        app = (CalamarApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        app.getDatabaseHandler().deleteAllRecipients();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @After
    public void tearDown(){
        app.getDatabaseHandler().deleteAllRecipients();
    }

    /**
     * Test that we have a new user button.
     */
    @Test
    public void testNewContactButtonName() {
        getActivity();
        onView(withText(CHAT_FRAGMENT_NAME)).perform(click());

        onView(withId(R.id.newContact))
                .check(matches(withText("New")));
    }

    /**
     * Test that we display nothing when nothing is in the database
     */
    @Test
    public void testDisplayContactIsEmptyWhenNoContact() {
        getActivity();
        onView(withText(CHAT_FRAGMENT_NAME)).perform(click());

        ListView list = (ListView) getActivity().findViewById(R.id.contactsList);
        assertEquals(list.getCount(), 0);
    }

    /**
     * Test that we display contacts in the database.
     */
    @Test
    public void testDisplayTwoContact() {
        app.getDatabaseHandler().addRecipient(bob);
        app.getDatabaseHandler().addRecipient(alice);

        getActivity();
        onView(withText(CHAT_FRAGMENT_NAME)).perform(click());

        ListView list = (ListView) getActivity().findViewById(R.id.contactsList);

        assertEquals(list.getCount(), 2);
    }

    /**
     * Test that we can cancel to add a new contact.
     */
    @Test
    public void testCreateContactCanBeCancelled() {
        getActivity();
        onView(withText(CHAT_FRAGMENT_NAME)).perform(click());

        onView(withId(R.id.newContact)).perform(click());

        onView(withText("Cancel")).perform(click());
    }

    /**
     * Test that we send correct data as parameter to the database client
     */
    @Test
    public void testCreateContactSendCorrectParameter() throws DatabaseClientException {
        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);

        getActivity();
        onView(withText(CHAT_FRAGMENT_NAME)).perform(click());

        onView(withId(R.id.newContact)).perform(click());

        onView(withHint("Mail")).perform(typeText("calamar@gmail.com"));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        onView(withText("Add")).perform(click());

        verify(client).findUserByName(argument.capture());

        assertEquals("calamar@gmail.com", argument.getValue());
    }

    /**
     * Test that we can correctly add a contact.
     */
    @Test
    public void testCreateContactIsCorrectlyCreated() {
        getActivity();
        DatabaseClient client = new ConstantDatabaseClient();
        DatabaseClientLocator.setDatabaseClient(client);

        onView(withText(CHAT_FRAGMENT_NAME)).perform(click());

        ListView list = (ListView) getActivity().findViewById(R.id.contactsList);
        final int before = list.getCount();

        onView(withId(R.id.newContact)).perform(click());

        onView(withText("Add")).perform(click());

        assertEquals(before + 1, list.getCount());
    }

}

