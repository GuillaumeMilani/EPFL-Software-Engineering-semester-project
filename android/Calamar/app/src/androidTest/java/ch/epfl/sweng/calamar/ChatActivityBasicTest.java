package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

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
}
