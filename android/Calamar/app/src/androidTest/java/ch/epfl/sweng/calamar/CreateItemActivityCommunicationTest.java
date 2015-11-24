package ch.epfl.sweng.calamar;


import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

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
import ch.epfl.sweng.calamar.item.CreateItemActivity;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.User;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class CreateItemActivityCommunicationTest extends ActivityInstrumentationTestCase2<CreateItemActivity> {

    private final static String HELLO_ALICE = "Hello Alice !";
    private final static String ALICE = "Alice";

    public CreateItemActivityCommunicationTest() {
        super(CreateItemActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void testCreateTextItem() throws DatabaseClientException {
        DatabaseClient client = Mockito.mock(ConstantDatabaseClient.class);
        DatabaseClientLocator.setDatabaseClient(client);
        getActivity();

        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        onView(withId(R.id.createItemActivity_messageText)).perform(typeText(HELLO_ALICE));
        closeSoftKeyboard();
        onView(withId(R.id.createButton)).perform(click());

        verify(client).send(argument.capture());

        SimpleTextItem expected = new SimpleTextItem(0, argument.getValue().getFrom(), new User(0, "public"), argument.getValue().getDate(), HELLO_ALICE);
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
}
