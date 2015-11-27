package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.Before;
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
 * Test for the chat user list activity
 */
@RunWith(JUnit4.class)
public class ChatUserListActivityTest extends ActivityInstrumentationTestCase2<ChatUsersListActivity> {

    public ChatUserListActivityTest() {
        super(ChatUsersListActivity.class);
    }

    CalamarApplication app;

    private final Recipient bob = new User(1, "bob");
    private final Recipient alice = new User(2, "alice");

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        app = (CalamarApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
    }













}
