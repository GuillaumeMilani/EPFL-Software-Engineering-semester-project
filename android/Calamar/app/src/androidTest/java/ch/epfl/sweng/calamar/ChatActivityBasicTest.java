package ch.epfl.sweng.calamar;

import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChatActivityBasicTest {

    @Rule
    public ActivityTestRule<ChatActivity> mActivityRule = new ActivityTestRule<>(
            ChatActivity.class);


    @Before
    public void setUp() throws Exception {
        ItemClientLocator.setQuizClient(new ConstantItemClient());
    }

    @Test
    public void testSendButtonName() {
        onView(withId(R.id.chatSendButton))
                .check(matches(withText("Send")));
    }

    @Test
    public void testRefreshButtonName() {
        onView(withId(R.id.refreshButton))
                .check(matches(withText("Refresh")));
    }

    @Test
      public void testCanWriteInMessageEdit() {
        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(allOf(withId(R.id.messageEdit), withText("Hello Alice !")));
    }

    @Test
    public void testTwoMessageAreDisplayed() {
        onView(withId(R.id.refreshButton)).perform(click());
        ListView list = (ListView)mActivityRule.getActivity().findViewById(R.id.messagesContainer);
        assertEquals(list.getCount(), 2);
    }

    @Test
    public void testMessageEditIsEmptyWhenSend() {
        onView(withId(R.id.messageEdit)).perform(typeText("Hello Alice !"));
        onView(withId(R.id.chatSendButton)).perform(click());
        onView(allOf(withId(R.id.messageEdit), withText("")));
    }
}
