package ch.epfl.sweng.calamar;


import android.content.Intent;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.item.CreateItemActivity;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.map.GPSProvider;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class CreateItemActivityCommunicationTest extends ActivityInstrumentationTestCase2<CreateItemActivity> {

    private final static String HELLO_ALICE = "Hello Alice !";
    private final static String ALICE = "Alice";

    private static final String RECIPIENT_EXTRA_NAME = "Name";

    private final Recipient BOB = new User(1, "bob");

    public CreateItemActivityCommunicationTest() {
        super(CreateItemActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void testCreateTextItemToPublic() throws DatabaseClientException {
        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        getActivity();

        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.createItemActivity_messageText)).perform(typeText(HELLO_ALICE));
        closeSoftKeyboard();
        onView(withId(R.id.createButton)).perform(click());

        verify(client).send(argument.capture());

        SimpleTextItem expected = new SimpleTextItem(0, argument.getValue().getFrom(), new User(User.PUBLIC_ID, User.PUBLIC_NAME), argument.getValue().getDate(), HELLO_ALICE);
        assertEquals(argument.getValue(), expected);
    }

    @Test
    public void testCreateItemToPrivate() throws DatabaseClientException {
        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        getActivity();

        CalamarApplication.getInstance().getDatabaseHandler().addRecipient(BOB);

        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.createItemActivity_messageText)).perform(typeText(HELLO_ALICE));
        closeSoftKeyboard();

        onView(withId(R.id.privateCheck)).perform(click());
        onView(withId(R.id.contactSpinner)).perform(click());
        onView(withText("bob")).perform(click());

        onView(withId(R.id.createButton)).perform(click());

        verify(client).send(argument.capture());

        SimpleTextItem expected = new SimpleTextItem(0, argument.getValue().getFrom(), BOB, argument.getValue().getDate(), HELLO_ALICE);
        assertEquals(argument.getValue(), expected);
    }


    @Ignore
    //TODO How to get an image ? Should take a photo, launch Activity, find ID of gallery, click on first item in gallery...
    public void testCreateImageItem() throws DatabaseClientException {
        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        getActivity();

        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.selectFileButton)).perform(click());
        onView(withId(R.id.createButton)).perform(click());

        verify(client).send(argument.capture());

        SimpleTextItem expected = new SimpleTextItem(0, argument.getValue().getFrom(), new User(0, "public"), argument.getValue().getDate(), HELLO_ALICE);
        assertEquals(argument.getValue(), expected);
    }

    @Ignore
    public void testPositionIsCorrect() throws DatabaseClientException {
        Location location = new Location("test");
        location.setLongitude(42);
        location.setLatitude(43);

        //TODO : Set mock location (waiting PR #109)

        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        getActivity();

        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.createItemActivity_messageText)).perform(typeText(HELLO_ALICE));
        closeSoftKeyboard();

        onView(withId(R.id.locationCheck)).perform(click());

        onView(withId(R.id.createButton)).perform(click());

        verify(client).send(argument.capture());

        SimpleTextItem expected = new SimpleTextItem(0, argument.getValue().getFrom(), new User(User.PUBLIC_ID, User.PUBLIC_NAME), argument.getValue().getDate(),new PositionCondition(location), HELLO_ALICE);
        assertEquals(argument.getValue(), expected);
    }

    @Test
    public void testIntentAreCorrectlyProcessed(){
        Intent conversation = new Intent();
        conversation.putExtra(RECIPIENT_EXTRA_NAME, "bob");
        setActivityIntent(conversation);

        CalamarApplication.getInstance().getDatabaseHandler().addRecipient(BOB);
        getActivity();


        onView(withText("bob")).check(matches(ViewMatchers.isDisplayed()));

    }
}
