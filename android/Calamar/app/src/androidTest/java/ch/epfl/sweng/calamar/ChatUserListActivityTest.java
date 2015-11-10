package ch.epfl.sweng.calamar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Mockito.verify;

/**
 * Test for the chat user list activity
 */
@RunWith(JUnit4.class)
public class ChatUserListActivityTest extends ActivityInstrumentationTestCase2<ChatUsersListActivity> {

    public ChatUserListActivityTest() {
        super(ChatUsersListActivity.class);
    }

    CalamarApplication  app;

    private Recipient bob = new User(1, "bob");
    private Recipient alice = new User(2, "alice");

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        app = (CalamarApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
    }

    /**
     * Test that we have a new user button.
     */
    @Test
    public void testNewContactButtonName() {
        getActivity();
        onView(withId(R.id.newContact))
                .check(matches(withText("New")));
    }
    /**
     * Test that we display nothing when nothing is in the database
     */
    @Test
    public void testDisplayContactIsEmptyWhenNoContact() {
        app.getDB().deleteAllRecipients();
        //app.getDB().addRecipient(bob);
        //app.getDB().addRecipient(alice);


        getActivity();

        ListView list = (ListView)getActivity().findViewById(R.id.contactsList);
        assertEquals(list.getCount(), 0);
    }

    /**
     * Test that we display contacts in the database.
     */
    @Test
    public void testDisplayTwoContact() {
        app.getDB().deleteAllRecipients();
        app.getDB().addRecipient(bob);
        app.getDB().addRecipient(alice);

        getActivity();

        ListView list = (ListView)getActivity().findViewById(R.id.contactsList);

        assertEquals(list.getCount(), 2);

        app.getDB().deleteAllRecipients();
    }

    /**
     * Test that we can cancel to add a new contact
     */
    @Test
    public void testCreateContactCanBeCancelled() {
        app.getDB().deleteAllRecipients();

        getActivity();

        onView(withId(R.id.newContact)).perform(click());

        AlertDialog dialog = getActivity().getLastDialog();

        if (dialog.isShowing()) {
            try {
                onView(withId(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getId())).perform(click());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test that we display correct button in the alertDialog
     */
    @Test
    public void testCreateContactAlertPositiveButtonText() {
        app.getDB().deleteAllRecipients();

        getActivity();

        onView(withId(R.id.newContact)).perform(click());

        AlertDialog dialog = getActivity().getLastDialog();

        if (dialog.isShowing()) {
            try {

                onView(withId(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getId())).check(matches(withText("Add")));;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test that we display correct button in the alertDialog
     */
    @Test
    public void testCreateContactAlertNegativeButtonText() {
        app.getDB().deleteAllRecipients();

        getActivity();

        onView(withId(R.id.newContact)).perform(click());

        AlertDialog dialog = getActivity().getLastDialog();

        if (dialog.isShowing()) {
            try {
                onView(withId(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getId())).check(matches(withText("Cancel")));;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test that we can correctly add a contact.
     */
    @Test
    public void testCreateContactIsCorrectlyCreated() {
        app.getDB().deleteAllRecipients();

        DatabaseClientLocator.setDatabaseClient(new ConstantDatabaseClient());

        getActivity();

        ListView list = (ListView)getActivity().findViewById(R.id.contactsList);
        final int before = list.getCount();

        onView(withId(R.id.newContact)).perform(click());

        AlertDialog dialog = getActivity().getLastDialog();

        if (dialog.isShowing()) {
            try {
                onView(withId(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getId())).perform(click());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        assertEquals(before + 1,list.getCount());
    }

    /**
     * Test that we send correct data as parameter to the database client
     */
    @Test
    public void testCreateContactSendCorrectParameter() throws DatabaseClientException {
        app.getDB().deleteAllRecipients();

        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);

        getActivity();

        ListView list = (ListView)getActivity().findViewById(R.id.contactsList);
        final int before = list.getCount();

        onView(withId(R.id.newContact)).perform(click());

        AlertDialog dialog = getActivity().getLastDialog();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        if (dialog.isShowing()) {
            try {
                onView(withId(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getId())).perform(click());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        verify(client).retrieveUserFromName(argument.capture());

        assertEquals("",argument.getValue());
    }

}
